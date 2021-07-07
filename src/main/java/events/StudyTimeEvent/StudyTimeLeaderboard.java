package events.StudyTimeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import exceptions.InvalidDocumentException;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.Writable;

/**
 * Represents a studytime leaderboard that stores ids of all users and their
 * respective studyTime. Outside classes can iterate through user ids in the
 * leaderboard and get studytime of the given user by calling getUserTime.
 */
public class StudyTimeLeaderboard extends Writable implements Iterable<String> {

    private static final String COLLECTION_NAME = "study_times";
    private Map<String, Long> timesMap;


    /**
     * Either returns a leaderboard that was previously saved to the database or 
     * returns a new leaderboard if it is first time a leaderboard is requested
     * @return a leaderboard that contains user ids and the time they studied
     */
    public static StudyTimeLeaderboard loadTimeLeaderboard() {
        DBReader reader = new DBReader(COLLECTION_NAME, "times_leaderboard");
        try {
            Document doc = reader.loadObject();
            Document userTimes = (Document) doc.get("times");
            Set<String> userIDs = userTimes.keySet();
            Map<String,Long> timesMap = new HashMap<>();
            for (String userID : userIDs) {
                timesMap.put(userID, userTimes.getLong(userID));
            }
            StudyTimeLeaderboard leaderboard = new StudyTimeLeaderboard(timesMap);
            return leaderboard;
        } catch (InvalidDocumentException e) {
            return new StudyTimeLeaderboard();
        }
    }

    /**
     * Sorts the given map, and returns iterator of the keyset
     */
    @Override
    public Iterator<String> iterator() {
        sortMap();
        return timesMap.keySet().iterator();
    }

    @Override
    /**
     * Returns a BSON document that contains information about 
     * this StudyTimeLeaderboard
     */
    public Document toDoc() {
        Document result = new Document();
        result.put(ACCESS_KEY, "times_leaderboard");
        Document times = new Document();
        for (Map.Entry<String, Long> entry : timesMap.entrySet()) {
            times.put(entry.getKey(), entry.getValue());
        }
        result.put("times", times);
        return result;
    }

    /**
     * Constructs a nee StudyTimeLeaderBoard
     * @param timesMap a map that contains id of the users and the amount of time they studied
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
     * Adds the amount of time given to the user that holds id provided,
     * after that saves the object to the database
     * @param memberID String - id of a user
     * @param timeElapsed  Long - amount of time added to the user
     */
    public void addUserTime(String memberID, long timeElapsed) {
        long curr = 0;
        if (timesMap.get(memberID) != null) {
            curr += timesMap.get(memberID);
        }
        this.timesMap.put(memberID, timeElapsed / 1000 / 60  + curr);
        DBWriter writer = new DBWriter(COLLECTION_NAME, "times_leaderboard");
        writer.saveObject(this);
    }

    /**
     * Returns amount of time user with provided id has studied 
     * @param memberID id of the user
     * @return the amount of time the user with the given id has studied
     */
    public Long getUserTime(String memberID) {
        return timesMap.get(memberID);
    }

    /**
     * Sorts the map of based on the values of the map.
     */
    private void sortMap() {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(timesMap.entrySet());
        entries.sort(new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> lhs, Map.Entry<String, Long> rhs) {
                return rhs.getValue().compareTo(lhs.getValue());
            }
        });
        timesMap = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : entries) {
            timesMap.put(entry.getKey(), entry.getValue());
        }
    }

}
