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
    private static final int TTL_MINUTES = 15;

    public RunningResponse saveRunning(Long userId, RunningRequest runningRequest) {
        Running running = getOrCreateRunning(runningRequest);
        User user = findUserById(userId);

        if (running.getId() == null) {
            Scenario scenario = findScenarioById(runningRequest.getScenarioId());
            running.setUser(user);
            running.setScenario(scenario);
        }

        updateRunningWithRequest(running, runningRequest);
        user.addOrUpdateRunning(running);
        runningRepository.save(running);

        return new RunningResponse(running);
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
                .filter(running -> running.getStatus() == RunningStatus.IN_PROGRESS && running.getLastModifiedDate().plusMinutes(TTL_MINUTES).isBefore(now))
                .forEach(running -> {
                    User user = running.getUser();
                    if (user != null) {
                        user.getRunnings().remove(running); // User의 runnings 리스트에서 Running 객체 제거
                    }
                    runningRepository.delete(running);
                });
    }

    public List<Running> getRunningsByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return runningRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public RunningStatsResponse getRunningStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Running> runnings = runningRepository.findByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .filter(running -> running.getStatus() == RunningStatus.COMPLETED)
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
    public List<Running> getRunningsByUserIdAndMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return runningRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

}
