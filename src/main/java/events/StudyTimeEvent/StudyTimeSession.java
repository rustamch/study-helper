package events.StudyTimeEvent;
import java.time.Duration;
import java.time.Instant;
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
public class StudyTimeSession extends Writable{

    private static final String COLLECTION_NAME = "study_times";
    private Instant start;
    private String memberID;

    /**
     * Loads a study sesssion of a user with a given ID from the database
     * @param memberID id of the member whose study session needs to be returned
     * @return a StudyTimeSession which contatains the time when user started studying
     * @throws InvalidDocumentException thrown to signify that a study session for given user doesn't exist
     */
    public static StudyTimeSession getStudySession(String memberID) throws InvalidDocumentException {
        DBReader reader = new DBReader(COLLECTION_NAME, memberID);
        Document doc = reader.loadObject();
        long epoch = doc.getLong("epoch");
        long nanos = doc.getInteger("nanos");
        StudyTimeSession session = new StudyTimeSession(memberID, epoch, nanos);
        return session;
    }

    @Override
    /**
     * 
     * @return a document that contains information about current session 
     */
    public Document toDoc() {
        Document doc = new Document();
        doc.put(Writable.ACCESS_KEY, memberID);
        doc.put("epoch", start.getEpochSecond());
        doc.put("nanos", start.getNano());
        return doc;
    }


    /**
     * Constructs a new StudyTimeSession and saves it to database 
     * @param memberID the String that contains id of the user to whom this study seesion belongs too
     */
    public StudyTimeSession(String memberID) {
        this.start = Instant.now();
        this.memberID = memberID;
    }

    /**
     * Constructs a new StudySession
     * @param memberID id of the member to whom this session belongs to
     * @param epoch an Epoch that points to a specific time
     */
    public StudyTimeSession(String memberID, long epoch, long nanos) {
        this.start = Instant.ofEpochSecond(epoch,nanos);
        this.memberID = memberID;
    }

    /**
     * Saves this session to the database
     */
    public void trackSession() {
        DBWriter writer = new DBWriter(COLLECTION_NAME, memberID);
        writer.saveObject(this, SaveOption.DEFAULT);
    }
    
    /**
     * Returns the time elapsed between the start of this session and the current time
     * @return time elapsed since between start of the session and the current moment
     */
    public long finishSession() {
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        return timeElapsed;
    }
    
}
