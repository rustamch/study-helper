package events.StudyTimeEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.client.FindIterable;
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
    public static final String STUDY_TIME_KEY = "study_time";
    public static final String START_TIME_KEY = "start_time";
    public static final String END_TIME_KEY = "end_time";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private long startTime;
    private long endTime;
    private long studyTime;
    private String memberID;

    /**
     * Loads a study sesssion of a user with a given ID from the database
     * @param memberID id of the member whose study session needs to be returned
     * @return a StudyTimeSession which contatains the time when user started studying
     */
    public static StudyTimeRecord getStudySession(String memberID) {
        try {
            Document doc = reader.loadObject(memberID);
            long startTime = doc.getLong(START_TIME_KEY);
            long endTime = doc.getLong(END_TIME_KEY);
            long studyTime = doc.getLong(STUDY_TIME_KEY);
            return new StudyTimeRecord(memberID, startTime, endTime, studyTime);
        } catch (InvalidDocumentException e) {
            return new StudyTimeRecord(memberID);
        }
    }

   public static List<StudyTimeRecord> getDueStudySessions() {
        List<StudyTimeRecord> records = new ArrayList<>();
        long currEpoch = Instant.now().getEpochSecond();
        Bson filter = Filters.and(Filters.lte(END_TIME_KEY,currEpoch),
                Filters.not(Filters.eq(END_TIME_KEY,-1)));
        reader.loadDocumentsWithFilter(filter).forEach((Consumer<? super Document>) doc -> {
            long startTime = doc.getLong(START_TIME_KEY);
            long endTime = doc.getLong(END_TIME_KEY);
            long studyTime = doc.getLong(STUDY_TIME_KEY);
            String memberID = doc.getString(ACCESS_KEY);
            StudyTimeRecord rec = new StudyTimeRecord(memberID,startTime,endTime,studyTime);
            records.add(rec);
        });
        return  records;
   }

    @Override
    public Document toDoc() {
        Document doc = new Document();
        doc.put(ACCESS_KEY, memberID);
        doc.put(START_TIME_KEY, startTime);
        doc.put(END_TIME_KEY,endTime);
        doc.put(STUDY_TIME_KEY, studyTime);
        return doc;
    }


    /**
     * Constructs a new StudyTimeSession and saves it to database 
     * @param memberID the String that contains id of the user to whom this study seesion belongs too
     */
    public StudyTimeRecord(String memberID) {
        this.memberID = memberID;
        this.studyTime = 0;
        this.endTime = -1;
        this.startTime = -1;
    }

    /**
     * Constructs a new StudySession
     * @param memberID id of the member to whom this session belongs to.
     * @param startTime an epoch that points to the start of the current study session.
     * @param endTime an epoch that points to the end of the current study session.
     */
    public StudyTimeRecord(String memberID, long startTime, long endTime, long studyTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberID = memberID;
        this.studyTime = studyTime;
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
            this.studyTime = this.studyTime + timeElapsed;
            this.startTime = -1;
            this.endTime = -1;
            return timeElapsed;
        } else {
            throw new IllegalStateException();
        }
    }

    public static void subtractStudyTime(String idAsString, long time) throws InvalidDocumentException {
        StudyTimeRecord session = getStudySession(idAsString);
        session.studyTime -= time;
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
