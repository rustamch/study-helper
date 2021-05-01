package persistence;

import org.json.JSONObject;

public interface Writable {
    JSONObject toJSON();
}
