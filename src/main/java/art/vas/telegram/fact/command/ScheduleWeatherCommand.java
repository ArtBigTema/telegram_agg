package art.vas.telegram.fact.command;

import art.vas.telegram.fact.model.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static art.vas.telegram.fact.model.Users.getTimeIfCorrect;

@Component
public class ScheduleWeatherCommand extends ScheduleCommand {

    @Autowired
    public ScheduleWeatherCommand(UserRepo userRepo) {
        super(userRepo, (user, time) -> user.setCronWeather(getTimeIfCorrect(time)));
    }

    @Override
    public String getCommandLine() {
        return "/scheduleWeather";
    }

    @Override
    protected String getSubject() {
        return "погоде";
    }

}
