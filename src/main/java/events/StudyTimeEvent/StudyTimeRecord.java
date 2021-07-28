package events.StudyTimeEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.bson.Document;

import exceptions.InvalidDocumentException;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;


/**
 * Represents an abstraction for a time instance that is mapped to 
 * a user ID.
 */
public class StudyTimeRecord extends Writable  {
    public static final String ACCESS_KEY = Writable.ACCESS_KEY;
    public static final String COLLECTION_NAME = "study_times";
    public static final String STUDY_TIME_KEY = "study_time";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private Instant start;
    private long studyTime;
    private String memberID;

    /**
     * Loads a study sesssion of a user with a given ID from the database
     * @param memberID id of the member whose study session needs to be returned
     * @return a StudyTimeSession which contatains the time when user started studying
     * @throws InvalidDocumentException thrown to signify that a study session for given user doesn't exist
     */
    public static StudyTimeRecord getStudySession(String memberID) throws InvalidDocumentException {
        Document doc = reader.loadObject(memberID);
        long epoch = doc.getLong("epoch");
        long studyTime = doc.getLong(STUDY_TIME_KEY);
        StudyTimeRecord session = new StudyTimeRecord(memberID, epoch,studyTime);
        return session;
    }

    @Override
    /**
     * 
     * @return a document that contains information about current session 
     */
    public Document toDoc() {
        Document doc = new Document();
        doc.put(ACCESS_KEY, memberID);
        doc.put("epoch", start.getEpochSecond());
        doc.put("study_time", studyTime);
        return doc;
    }


    /**
     * Constructs a new StudyTimeSession and saves it to database 
     * @param memberID the String that contains id of the user to whom this study seesion belongs too
     */
    public StudyTimeRecord(String memberID) {
        this.start = Instant.now();
        this.memberID = memberID;
        this.studyTime = 0;
    }

    /**
     * Constructs a new StudySession
     * @param memberID id of the member to whom this session belongs to
     * @param epoch an Epoch that points to a specific time
     */
    public StudyTimeRecord(String memberID, long epoch, long studyTime) {
        this.start = Instant.ofEpochSecond(epoch);
        this.memberID = memberID;
        this.studyTime = studyTime;
    }

    /**
     * Saves this session to the database
     */
    public void trackSession() {
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    /**
     * Returns the time elapsed between the start of this session and the current time
     * @return time elapsed since between start of the session and the current moment
     */
    public long finishSession() {
        Instant finish = Instant.now();
        long timeElapsed =  start.until(finish, ChronoUnit.SECONDS);
        this.studyTime += timeElapsed;
        writer.saveObject(this, SaveOption.DEFAULT);
        return timeElapsed;
    }

    public static void subtractStudyTime(String idAsString, long time) throws InvalidDocumentException {
        StudyTimeRecord session = getStudySession(idAsString);
        session.studyTime -= time;
        writer.saveObject(session, SaveOption.DEFAULT);
    }   
}
