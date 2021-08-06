package events.BirthdayEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import model.Bot;
import model.DailyTask;

import org.javacord.api.entity.message.embed.EmbedBuilder;


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
        Set<String> ids;
        if (today.getDayOfMonth() == 1) {
            ids = BirthdayRecord.findAllMembersWithBdayOnGivenMonth(today.getMonthValue());
            msgEachCommonServer(ids);
        }
        ids = BirthdayRecord.findMembersWithBdayOnGivenDay(today);
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

    public void msgEachCommonServer(Set<String> memberIDs) {
        Map<String, EmbedBuilder> msgMap = new HashMap<>();
        memberIDs.forEach(id -> {
            Bot.API.getUserById(id).thenAccept(user -> {
                user.getMutualServers().forEach(server -> {
                    EmbedBuilder builder = msgMap.getOrDefault(server.getIdAsString(), new EmbedBuilder());
                    builder.addField(user.getDisplayName(server) + ": ", BirthdayRecord.getDateById(user.getIdAsString()).toString());
                });
            });
        });
    }   
}
