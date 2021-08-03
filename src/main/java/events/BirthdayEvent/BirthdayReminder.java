package events.BirthdayEvent;
import java.time.LocalDate;
import java.util.Set;
import model.Bot;
import model.DailyTask;


public class BirthdayReminder implements DailyTask {

    @Override
    public void execute() {
        checkBirthdays();
    }

    /**
     * Checks if the users have a birthday today.
     */
    private void checkBirthdays() {
        LocalDate today = LocalDate.now();
        Set<String> ids = BirthdayRecord.findMembersWithBdayOnGivenDay(today);
        if (ids.size() > 0) {
            congratulateUsers(ids);
        }
    }

    /**
     * Sends a congradulations message to the users with a birthday today.
     * @param memberIDs ids of the users with a birthday today.
     */
    public void congratulateUsers(Set<String> memberIDs) {
        memberIDs.forEach(id -> {
            Bot.API.getUserById(id).thenAccept(user -> {
                user.getMutualServers().forEach(server -> {
                    server.getTextChannelsByName("general").forEach(channel -> {
                        channel.sendMessage(user.getMentionTag() + " has a birthday today!");
                    });
                });
            });
        });
    }
}