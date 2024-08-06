package com.likelion.runtale.domain.running.service;

import com.likelion.runtale.common.exception.BadRequestException;
import com.likelion.runtale.common.exception.NotFoundException;
import com.likelion.runtale.common.response.ErrorMessage;
import com.likelion.runtale.domain.running.dto.RunningRequest;
import com.likelion.runtale.domain.running.dto.RunningResponse;
import com.likelion.runtale.domain.running.dto.RunningStatsResponse;
import com.likelion.runtale.domain.running.entity.Location;
import com.likelion.runtale.domain.running.entity.Running;
import com.likelion.runtale.domain.running.entity.RunningStatus;
import com.likelion.runtale.domain.running.repository.RunningRepository;
import com.likelion.runtale.domain.scenario.entity.Scenario;
import com.likelion.runtale.domain.scenario.entity.ScenarioStep;
import com.likelion.runtale.domain.scenario.repository.ScenarioRepository;
import com.likelion.runtale.domain.user.entity.User;
import com.likelion.runtale.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RunningService {

    private final RunningRepository runningRepository;
    private final UserRepository userRepository;
    private final ScenarioRepository scenarioRepository;
    private static final int TTL_MINUTES = 30;

    public RunningResponse saveRunning(Long userId, RunningRequest runningRequest) {
        Running running = getOrCreateRunning(runningRequest);
        User user = findUserById(userId);
        String audioUrl;

        if (running.getId() == null) {
            if (runningRequest.getScenarioId() != 0) {
                Scenario scenario = findScenarioById(runningRequest.getScenarioId());
                running.setScenario(scenario);
            } else {
                running.setScenario(null); // 시나리오 ID가 0이면 시나리오 없이 설정
            }
            running.setUser(user);
        }

        updateRunningWithRequest(running, runningRequest);
        user.addOrUpdateRunning(running);
        runningRepository.save(running);

        audioUrl = checkAndReturnAudioUrl(running);
        return new RunningResponse(running, audioUrl);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_EXIST));
    }
    private Scenario findScenarioById(Long scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.SCENARIO_NOT_FOUND));
    }
    private Running getOrCreateRunning(RunningRequest runningRequest) {
        if (runningRequest.getId() != null) {
            return runningRepository.findById(runningRequest.getId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.RUNNING_NOT_FOUND));
        }
        return new Running();
    }

    private void updateRunningWithRequest(Running running, RunningRequest runningRequest) {
        running.setEndTime(runningRequest.getEndTime());
        running.setDistance(runningRequest.getDistance());
        running.setPace(runningRequest.getPace());
        running.setStatus(runningRequest.getEndTime() == null ? RunningStatus.IN_PROGRESS : RunningStatus.COMPLETED);
        if(running.getTargetDistance() == null && running.getTargetPace() == null) {
            running.setTargetPace(runningRequest.getTargetPace());
            running.setTargetDistance(runningRequest.getTargetDistance());
        }
        running.setModifiedAt(LocalDateTime.now());

        if (runningRequest.getLatitude() != null && runningRequest.getLongitude() != null) {
            Location location = new Location();
            location.setLatitude(runningRequest.getLatitude());
            location.setLongitude(runningRequest.getLongitude());
            running.getLocations().add(location);
        }
    }
    private String checkAndReturnAudioUrl(Running running) {
        double currentDistance = running.getDistance();
        int stepNumber;

        Scenario scenario = running.getScenario();

        if (scenario == null) return null;

        stepNumber = determineStepNumber(currentDistance);

        if (running.getMilestones().contains(stepNumber)) return null; // 이미 해당 구간을 처리한 경우

        running.getMilestones().add(stepNumber);

        ScenarioStep scenarioStep = scenario.getSteps().stream()
                .filter(step -> step.getStepNumber() == stepNumber)
                .findFirst()
                .orElse(null);

        return scenarioStep != null ? scenarioStep.getAudioUrl() : null;
    }
    private int determineStepNumber(double distance) {
        // 거리 기준으로 단계 결정 로직
        if (distance >= 3.0) {
            return 4;
        } else if (distance >= 2.0) {
            return 3;
        } else if (distance >= 1.0) {
            return 2;
        } else {
            return 1; // 아직 달성한 단계가 없을 때
        }
    }

    public List<Running> getRunningsByUserId(Long userId) {
        return runningRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Running getRunningById(Long id) {
        return runningRepository.findById(id).orElseThrow(() -> new BadRequestException(ErrorMessage.RUNNING_NOT_FOUND));
    }



    public void deleteRunning(Long id) {
        Running running = runningRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorMessage.RUNNING_NOT_FOUND));

        User user = running.getUser();
        if (user != null) {
            // User의 runnings 리스트에서 Running 객체 제거
            user.getRunnings().remove(running);
        }
        runningRepository.delete(running);
    }

    // TTL이 지난 러닝 세션 삭제하는 스케줄러
    @Scheduled(fixedRate = 180  * 1000) // 3분마다 실행
    public void deleteExpiredRunningSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<Running> allRunnings = runningRepository.findAll();
        allRunnings.stream()
                .filter(running -> (running.getStatus() == RunningStatus.IN_PROGRESS && running.getLastModifiedDate().plusMinutes(TTL_MINUTES).isBefore(now))
                        || running.getDistance() == 0 || running.getPace() == null)
                .forEach(running -> {
                    User user = running.getUser();
                    if (user != null) {
                        user.getRunnings().remove(running); // User의 runnings 리스트에서 Running 객체 제거
                    }
                    runningRepository.delete(running);
                });
    }

    public RunningStatsResponse getRunningStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Running> runnings = runningRepository.findByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .filter(running -> running.getStatus() == RunningStatus.COMPLETED && running.getDistance() != 0 && running.getPace() != null) // Add null check for pace
                .collect(Collectors.toList());

        int totalRunningCount = runnings.size();
        double totalDistance = runnings.stream().mapToDouble(Running::getDistance).sum();
        double totalPace = runnings.stream().mapToDouble(Running::getPace).sum();
        int targetPaceAchievedCount = (int) runnings.stream().filter(running -> running.getPace() <= running.getTargetPace()).count();
        int targetDistanceAchievedCount = (int) runnings.stream().filter(running -> running.getDistance() >= running.getTargetDistance()).count();
        double averagePace = calculateAveragePace(totalPace, totalRunningCount);

        return new RunningStatsResponse(
                runnings,
                targetPaceAchievedCount,
                targetDistanceAchievedCount,
                totalDistance,
                totalRunningCount,
                averagePace
        );
    }
    private double calculateAveragePace(double totalPace, int totalRunningCount) {
        return totalRunningCount > 0 ? totalPace / totalRunningCount : 0;
    }

//    public ScenarioStep checkScenarioStep(Long runningId, double distance) {
//        Running running = runningRepository.findById(runningId)
//                .orElseThrow(() -> new NotFoundException(ErrorMessage.RUNNING_NOT_FOUND));
//
//        Scenario scenario = running.getScenario();
//        if (scenario == null) {
//            // 시나리오가 없는 경우의 로직 처리 (예: 그냥 null 반환)
//            return null;
//        }
//
//        // 거리 기준으로 시나리오 단계 체크
//        int stepNumber = determineStepNumber(distance);
//        return scenario.getSteps().stream()
//                .filter(step -> step.getStepNumber() == stepNumber)
//                .findFirst()
//                .orElseThrow(() -> new NotFoundException(ErrorMessage.SCENARIO_STEP_NOT_FOUND));
//    }
}
