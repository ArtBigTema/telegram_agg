package art.vas.telegram.fact.command;

import art.vas.telegram.fact.model.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static art.vas.telegram.fact.model.Users.getTimeIfCorrect;

@Component
public class ScheduleTodayCommand extends ScheduleCommand {
    @Autowired
    public ScheduleTodayCommand(UserRepo userRepo) {
        super(userRepo, (user, time) -> user.setCronToday(getTimeIfCorrect(time)));
    }

    @Override
    public String getCommandLine() {
        return "/scheduleToday";
    }

    @Override
    protected String getSubject() {
        return "празднике";
    }
}
