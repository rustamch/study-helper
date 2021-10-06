package events.BirthdayEvent;

import model.Bot;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class BirthdayReminder {

    /**
     * Checks if the users have a birthday today.
     */
    private static void checkBirthdays() {
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
     *
     * @param memberIDs ids of the users with a birthday today.
     */
    public static void congratulateUsers(Set<String> memberIDs) {
        memberIDs.forEach(id ->
                Bot.API.getUserById(id).thenAccept(user ->
                        user.getMutualServers().forEach(server ->
                                server.getTextChannelsByName("general").forEach(channel ->
                                        channel.sendMessage(user.getMentionTag() + " has a birthday today!")))));
    }

    public static void msgEachCommonServer(Set<String> memberIDs) {
        Map<Server, EmbedBuilder> msgMap = new HashMap<>();
        memberIDs.forEach(id -> Bot.API.getUserById(id).thenAccept(user ->
                user.getMutualServers().forEach(server -> {
            EmbedBuilder builder = msgMap.getOrDefault(server, new EmbedBuilder().setTitle(LocalDate.now().getMonth() + " Birthdays").setColor(Color.CYAN));
            builder.addField(Objects.requireNonNull(BirthdayRecord.getDateById(user.getIdAsString())).toString(), " - " + user.getMentionTag());
            msgMap.put(server, builder);
        })));
        msgMap.forEach((key, value) -> key.getTextChannelsByName("general").get(0).sendMessage(value));
    }

    public static void dailyExecutorSchedule() {
        checkBirthdays();
    }

    private void monthlyBDayUpdate() {
        Set<String> ids = BirthdayRecord.findMembersWithBdayOnGivenMonth(LocalDate.now().getMonthValue());
        String title = LocalDate.now().getMonth() + " Birthdays";
        if (!ids.isEmpty()) {
            Map<Server, EmbedBuilder> embeds = new HashMap<>();
            ids.forEach(id ->
                    Bot.API.getUserById(id).thenAccept(user ->
                            user.getMutualServers().forEach(server -> {
                                EmbedBuilder builder = new EmbedBuilder().setTitle(title);
                                embeds.putIfAbsent(server, builder);
                            })));
        }
    }
}
