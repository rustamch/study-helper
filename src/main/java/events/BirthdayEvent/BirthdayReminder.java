package events.BirthdayEvent;
import java.awt.Color;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import model.Bot;
import model.DailyTask;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;


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

    public static void msgEachCommonServer(Set<String> memberIDs) {
        Map<Server, EmbedBuilder> msgMap = new HashMap<>();
        memberIDs.forEach(id -> {
            Bot.API.getUserById(id).thenAccept(user -> {
                user.getMutualServers().forEach(server -> {
                    EmbedBuilder builder = msgMap.getOrDefault(server, new EmbedBuilder().setTitle(LocalDate.now().getMonth() + " Birthdays").setColor(Color.CYAN));
                    builder.addInlineField("\n", user.getMentionTag() + " : " + BirthdayRecord.getDateById(user.getIdAsString()).toString());
                    msgMap.put(server, builder);
                });
            });
        });
        msgMap.entrySet().forEach(entry -> {
            entry.getKey().getTextChannelsByName("general").get(0).sendMessage(entry.getValue());
        });
    }
}
