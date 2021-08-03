package persistence;
import org.bson.Document;


/**
 * Represents a class that can be saved to BSON files
 */
public interface Writable {
    public final static String ACCESS_KEY = "readKey";

    /**
     * Produces a BSON document that stores some information about the users
     * @return a BSON document
     */
    public Document toDoc();
}
