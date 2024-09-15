package com.likelion.runtale.domain.tier.service;

import com.likelion.runtale.common.exception.NotFoundException;
import com.likelion.runtale.common.response.ErrorMessage;
import com.likelion.runtale.domain.tier.dto.UserTierInfo;
import com.likelion.runtale.domain.tier.entity.Tier;
import com.likelion.runtale.domain.tier.repository.TierRepository;
import com.likelion.runtale.domain.user.entity.User;
import com.likelion.runtale.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TierService {

    private final TierRepository tierRepository;
    private final UserRepository userRepository;

    @Transactional
    public void assignTiersToUsers(int year, int month) {
        List<User> users = userRepository.findAll();

        // 러닝 기록이 있는 사용자와 없는 사용자로 분리
        List<User> usersWithRuns = users.stream()
                .filter(user -> !user.getRunnings().isEmpty())
                .toList();

        List<User> usersWithoutRuns = users.stream()
                .filter(user -> user.getRunnings().isEmpty())
                .collect(Collectors.toList());

        // 러닝 기록이 있는 사용자 점수 계산
        usersWithRuns.forEach(user -> {
            double score = calculateUserScore(user, year, month);
            user.setScore(score);
        });

        // 점수 기준으로 사용자 정렬
        List<User> sortedUsersWithRuns = usersWithRuns.stream()
                .sorted((u1, u2) -> Double.compare(u2.getScore(), u1.getScore()))
                .toList();

        // 상대적 순위에 따라 티어 할당
        int totalUsersWithRuns = sortedUsersWithRuns.size();
        for (int i = 0; i < totalUsersWithRuns; i++) {
            User user = sortedUsersWithRuns.get(i);
            double percentile = (double) (i + 1) / totalUsersWithRuns * 100;
            assignTierBasedOnPercentile(user, percentile);
            userRepository.save(user);
        }

        // 러닝 기록이 없는 사용자에게 디폴트 티어 할당
        Tier defaultTier = findTierByName("돌멩이");
        usersWithoutRuns.forEach(user -> {
            user.setTier(defaultTier);
            user.setScore(0);
            user.setPercentile(0);
            user.setProgress(0);
            userRepository.save(user);
        });
    }

    @Transactional
    public void assignDefaultTierToUser(User user) {
        // 러닝 기록이 없는 경우 디폴트 티어 할당
        Tier defaultTier = findTierByName("돌멩이");
        user.setTier(defaultTier);
        user.setScore(0);
        user.setPercentile(0);
        user.setProgress(0);
        userRepository.save(user);
    }

    private void assignTierBasedOnPercentile(User user, double percentile) {
        user.setPercentile(percentile);
        if (percentile > 80) {
            user.setTier(findTierByName("달팽이"));
            user.setProgress(percentile - 80);
        } else if (percentile > 60) {
            user.setTier(findTierByName("거북이"));
            user.setProgress(percentile - 60);
        } else if (percentile > 40) {
            user.setTier(findTierByName("토끼"));
            user.setProgress(percentile - 40);
        } else if (percentile > 20) {
            user.setTier(findTierByName("말"));
            user.setProgress(percentile - 20);
        } else {
            user.setTier(findTierByName("치타"));
            user.setProgress(percentile);
        }
    }

    @Transactional
    public void resetTiers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            user.setTier(null);
            user.setScore(0);
            user.setPercentile(0);
            userRepository.save(user);
        });
    }

    private double calculateUserScore(User user, int year, int month) {
        double runningDaysWeight = 1.0;
        double totalDistanceWeight = 0.00333;
        double averagePaceWeight = 0.2;

        int runningDays = user.getRunningDays(year, month);
        double totalDistance = user.getTotalDistance(year, month);
        double averagePace = user.getAveragePace(year, month);

        // averagePace가 0이 되지 않도록 처리
        if (averagePace == 0) {
            averagePace = 1;
        }

        return (runningDays * runningDaysWeight) +
                (totalDistance * totalDistanceWeight) +
                ((1 / averagePace) * averagePaceWeight);
    }

    private Tier findTierByName(String name) {
        return tierRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.TIER_NOT_FOUND));
    }

    @Scheduled(cron = "0 0 0 1 * ?") // 매달 1일 자정에 실행
    @Transactional
    public void resetTiersMonthly() {
        resetTiers();
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6시간마다 실행
    @Transactional
    public void updateTiersPeriodically() {
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        assignTiersToUsers(year, month);
    }

    @Transactional(readOnly = true)
    public List<UserTierInfo> getAllUsersWithTiers() {
        return userRepository.findAll().stream()
                .map(user -> new UserTierInfo(
                        user.getNickname(),
                        user.getTier().getName(),
                        user.getPercentile(),
                        user.getTier().getImageUrl()
                ))
                .collect(Collectors.toList());
    }
}

//test

