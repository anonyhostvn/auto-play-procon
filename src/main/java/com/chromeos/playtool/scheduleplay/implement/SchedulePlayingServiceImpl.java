package com.chromeos.playtool.scheduleplay.implement;

import com.chromeos.playtool.repositories.PlayerStagingRepository;
import com.chromeos.playtool.scheduleplay.ISchedulePlayingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SchedulePlayingServiceImpl implements ISchedulePlayingService {

    private final PlayerStagingRepository playerStagingRepository;

    private List<CompletableFuture<?>> listWaiting;

    public SchedulePlayingServiceImpl(PlayerStagingRepository playerStagingRepository) {
        this.playerStagingRepository = playerStagingRepository;
    }

    @Override
    public void startASchedulePlaying() {

    }

    @Override
    public void stopAllSchedulePlaying() {

    }
}
