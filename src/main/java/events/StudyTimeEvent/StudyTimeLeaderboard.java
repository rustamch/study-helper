package events.StudyTimeEvent;

import java.awt.Color;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import model.Bot;
import model.DailyTask;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.Writable;

/**
 * Represents a studytime leaderboard that stores ids of all users and their
 * respective studyTime. Outside classes can iterate through user ids in the
 * leaderboard and get studytime of the given user by calling getUserTime.
 */
public class StudyTimeLeaderboard implements DailyTask {
    private static final String COLLECTION_NAME = "study_times";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private Map<String, Long> timesMap;

    /**
     * Either returns a leaderboard that was previously saved to the database or
     * returns a new leaderboard if it is first time a leaderboard is requested
     * 
     * @return a leaderboard that contains user ids and the time they studied
     */
    public static StudyTimeLeaderboard loadTimeLeaderboard() {
        FindIterable<Document> docs = reader.loadAllDocuments()
                .sort(new BasicDBObject(StudyTimeRecord.STUDY_TIME_KEY, -1))
                .limit(StudyTimeEvent.NUMBER_OF_USERS_ON_LEADERBOARD);
        Map<String, Long> timesMap = new LinkedHashMap<>();
        for (Document doc : docs) {
            String userId = doc.getString(Writable.ACCESS_KEY);
            Long time = doc.getLong(StudyTimeRecord.STUDY_TIME_KEY);
            timesMap.put(userId, time);
        }
        StudyTimeLeaderboard leaderboard = new StudyTimeLeaderboard(timesMap);
        return leaderboard;
    }

    public EmbedBuilder getLeaderboardEmbed(Server msgServer) {
        EmbedBuilder leaderboard = new EmbedBuilder();
        leaderboard.setTitle("\uD83D\uDCD8 Grind Leaderboard");
        leaderboard.setColor(new Color(0x9CD08F));
        int place = 1;
        for (Map.Entry<String, Long> entry : timesMap.entrySet()) {
            final int currPlace = place;
            msgServer.getMemberById(entry.getKey()).ifPresent(user -> {
                String name = user.getDisplayName(msgServer);
                long minutes  = entry.getValue() / 60;
                leaderboard.addField(currPlace + ". " + name, name + " has studied for " + minutes / 60 + " hour(s) " + minutes % 60 + " minute(s) so far.", false);
            });
            place++;
        }
        return leaderboard;
    }

    /**
     * Constructs a new StudyTimeLeaderBoard
     * 
     * @param timesMap a map that contains id of the users and the amount of time
     *                 they studied
     */
    public StudyTimeLeaderboard(Map<String, Long> timesMap) {
        this.timesMap = timesMap;
    }

    /**
     * Constructs a new StudyTimeLeaderboard
     */
    public StudyTimeLeaderboard() {
        this.timesMap = new HashMap<>();
    }

    /**
     * Returns amount of time user with provided id has studied
     * 
     * @param memberID id of the user
     * @return the amount of time the user with the given id has studied
     */
    public Long getUserTime(String memberID) {
        return timesMap.get(memberID);
    }

    @Override
    public void execute() {
        if (needsToBeReset()) {
            resetLeaderboard();
        }
    }

    /**
     * Deletes all the StudyTimeRecords from the database and consequently
     * resets the leaderboard.
     */
    private void resetLeaderboard() {
        Server msgServer = Bot.API.getServersByName("Studium Praetorium").iterator().next();    // TODO: Change this after each server is associated with a different database
        TextChannel botSpam = msgServer.getTextChannelsByName("bot-spam").get(0);               // TODO: Change this after each server has a config file.
        botSpam.sendMessage("Resetting leaderboard...");
        botSpam.sendMessage(getLeaderboardEmbed(msgServer));
        writer.removeDocuments(new Document());
    }

    /**
     * Returns whether leaderboard needs to be reset or not.
     * @return a boolean value that indicates whether leaderboard needs to be reset or not 
     */
    private boolean needsToBeReset() {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek() == DayOfWeek.MONDAY;
    }

}
