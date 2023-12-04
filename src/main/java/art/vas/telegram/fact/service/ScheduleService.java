package art.vas.telegram.fact.service;

import art.vas.telegram.fact.FactBot;
import art.vas.telegram.fact.command.HolidayCommand;
import art.vas.telegram.fact.command.WeatherPhotoCommand;
import art.vas.telegram.fact.command.common.Commando;
import art.vas.telegram.fact.model.Users;
import art.vas.telegram.fact.model.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("schedule.enable")
public class ScheduleService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final WeatherPhotoCommand weatherPhotoCommand;
    private final HolidayCommand holidayCommand;
    private final UserRepo userRepo;
    private final FactBot factBot;
    private final List<ScheduledFuture<?>> schedules = new LinkedList<>();

    @Scheduled(cron = "1 1 1 * * *")
    public void take() {
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Samara"));
        log.info("now " + now);
        for (ScheduledFuture<?> schedule : schedules) {
            schedule.cancel(true);
        }
        schedules.clear();

        for (Users user : userRepo.findAll()) {
            Optional.ofNullable(user.getCronWeather())
                    .map(n -> getScheduledFuture(now, user, n, weatherPhotoCommand))
                    .ifPresent(schedules::add);
            Optional.ofNullable(user.getCronToday())
                    .map(n -> getScheduledFuture(now, user, n, holidayCommand))
                    .ifPresent(schedules::add);
        }
    }

    private ScheduledFuture<?> getScheduledFuture(LocalTime now, Users user, LocalTime cron, Commando<?> commando) {
        Duration duration = Duration.between(now, cron);

        return scheduler.schedule(() -> {
            try {
                commando.execute(commando.answer(user.getChatId()), factBot);
            } catch (Exception e) {
                log.error("Smth went wrong at schedule", e);
            }
        }, Math.abs(duration.getSeconds()), TimeUnit.SECONDS);
    }
}
