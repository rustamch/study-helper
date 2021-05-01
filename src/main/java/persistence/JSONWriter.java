package persistence;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Represents a class that can be saved to JSON files
 */

public class JSONWriter {
    private String fileLocation;

    public JSONWriter(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public void saveObject(Writable writable) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(fileLocation));
        writer.print(writable.toJSON().toString());
        writer.close();
    }
}
