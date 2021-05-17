package persistence;


import org.json.JSONObject;

public abstract class Writable {
    public final static String ACCESS_KEY = "readKey";

    public abstract JSONObject toJSON();
}
