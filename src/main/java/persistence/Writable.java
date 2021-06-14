package persistence;
import org.bson.Document;

public abstract class Writable {
    public final static String ACCESS_KEY = "readKey";

    public abstract Document toDoc();
}
