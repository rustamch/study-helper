package events.StudyTimeEvent;
import java.time.Duration;
import java.time.Instant;

import org.bson.Document;
import persistence.Writable;

public class StudyTimeSession extends Writable{

    private Instant start;
    private String userID;


    /**
     * Constructs a new StudyTimeSession
     * @param userID the String that contains id of the user to whom this study seesion belongs too
     */
    public StudyTimeSession(String userID) {
        this.start = Instant.now();
        this.userID = userID;
    }
    
    /**
     * TODO
     */
    @Override
    public Document toDoc() {
        Document doc = new Document();
        doc.put(Writable.ACCESS_KEY, userID);
        doc.put("epoch", start.getEpochSecond());
        doc.put("nanos", start.getNano());
        return doc;
    }

    /**
     * Returns the time elapsed between the start of this session and the current time
     * @return Time elapsed 
     */
    public long finishSession() {
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        return timeElapsed;
    }
    
}
