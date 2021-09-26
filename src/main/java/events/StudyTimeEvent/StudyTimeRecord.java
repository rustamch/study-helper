package events.StudyTimeEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.client.model.Filters;
import org.bson.Document;

import exceptions.InvalidDocumentException;
import org.bson.conversions.Bson;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;


/**
 * Represents an abstraction for a time instance that is mapped to 
 * a user ID.
 */
public class StudyTimeRecord implements Writable  {
    public static final String COLLECTION_NAME = "study_times";
    public static final String GLOBAL_STUDY_TIME_KEY = "global_study_time";
    public static final String WEEKLY_STUDY_TIME_KEY = "weekly_study_time";
    public static final String START_TIME_KEY = "start_time";
    public static final String END_TIME_KEY = "end_time";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private final String memberID;
    private long startTime;
    private long endTime;
    private long globalStudyTime;
    private long weeklyStudyTime;

    /**
     * Loads a study session of a user with a given ID from the database
     * @param memberID id of the member whose study session needs to be returned
     * @return a StudyTimeSession which contains the time when user started studying
     */
    public static StudyTimeRecord getStudySession(String memberID) {
        try {
            Document doc = reader.loadObject(memberID);
            long startTime = doc.getLong(START_TIME_KEY);
            long endTime = doc.getLong(END_TIME_KEY);
            long globalStudyTime = doc.getLong(GLOBAL_STUDY_TIME_KEY);
            long weeklyStudyTime = doc.getLong(WEEKLY_STUDY_TIME_KEY);
            return new StudyTimeRecord(memberID, startTime, endTime, globalStudyTime, weeklyStudyTime);
        } catch (InvalidDocumentException e) {
            return new StudyTimeRecord(memberID);
        }
    }

   public static List<StudyTimeRecord> getDueStudySessions() {
        List<StudyTimeRecord> records = new ArrayList<>();
        long currEpoch = Instant.now().getEpochSecond();
        Bson filter = Filters.lte(END_TIME_KEY,currEpoch);
        reader.loadDocumentsWithFilter(filter).forEach((Consumer<? super Document>) doc -> {
            long endTime = doc.getLong(END_TIME_KEY);
            if (endTime != -1) {
                long startTime = doc.getLong(START_TIME_KEY);
                long globalStudyTime = doc.getLong(GLOBAL_STUDY_TIME_KEY);
                long weeklyStudyTime = doc.getLong(WEEKLY_STUDY_TIME_KEY);
                String memberID = doc.getString(ACCESS_KEY);
                StudyTimeRecord rec = new StudyTimeRecord(memberID, startTime, endTime, globalStudyTime, weeklyStudyTime);
                records.add(rec);
            }
        });
        return  records;
   }

    public static long getUserStudytime(String userId) {
        try {
            Document doc = reader.loadObject(userId);
            return doc.getLong(GLOBAL_STUDY_TIME_KEY);
        } catch (InvalidDocumentException e) {
            return 0;
        }
    }

    @Override
    public Document toDoc() {
        Document doc = new Document();
        doc.put(ACCESS_KEY, memberID);
        doc.put(START_TIME_KEY, startTime);
        doc.put(END_TIME_KEY,endTime);
        doc.put(GLOBAL_STUDY_TIME_KEY, globalStudyTime);
        doc.put(WEEKLY_STUDY_TIME_KEY, weeklyStudyTime);
        return doc;
    }


    /**
     * Constructs a new StudyTimeSession and saves it to database 
     * @param memberID the String that contains id of the user to whom this study seesion belongs too
     */
    public StudyTimeRecord(String memberID) {
        this.memberID = memberID;
        this.globalStudyTime = 0;
        this.weeklyStudyTime = 0;
        this.endTime = -1;
        this.startTime = -1;
    }

    /**
     * Constructs a new StudySession
     * @param memberID id of the member to whom this session belongs to.
     * @param startTime an epoch that points to the start of the current study session.
     * @param endTime an epoch that points to the end of the current study session.
     */
    private StudyTimeRecord(String memberID, long startTime, long endTime, long globalStudyTime, long weeklyStudyTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberID = memberID;
        this.globalStudyTime = globalStudyTime;
        this.weeklyStudyTime = weeklyStudyTime;
    }

    public String getMemberId() {
        return this.memberID;
    }

    /**
     * Saves this session to the database
     */
    public void trackSession() {
        this.startTime = Instant.now().getEpochSecond();
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    /**
     * Returns the time elapsed between the start of this session and the current time
     * @return time elapsed since between start of the session and the current moment
     */
    public long finishSession() {
        if (startTime != -1) {
            long timeElapsed = Instant.now().getEpochSecond() - this.startTime;
            this.globalStudyTime = this.globalStudyTime + timeElapsed;
            this.weeklyStudyTime = this.weeklyStudyTime + timeElapsed;
            this.startTime = -1;
            this.endTime = -1;
            return timeElapsed;
        } else {
            throw new IllegalStateException();
        }
    }

    public static void subtractStudyTime(String idAsString, long time) throws InvalidDocumentException {
        StudyTimeRecord session = getStudySession(idAsString);
        session.globalStudyTime -= time;
        session.weeklyStudyTime -= time;
        writer.saveObject(session, SaveOption.DEFAULT);
    }

    public void setEndTime(Long endEpoch) {
        this.endTime = endEpoch;
    }

    public boolean inProgress() {
        return startTime != -1;
    }
    public void save() {
        writer.saveObject(this, SaveOption.DEFAULT);
    }
}
