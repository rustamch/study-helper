package events.StudyTimeEvent;

import javax.swing.text.Document;

import persistence.Writable;

public class StudyTimeSession extends Writable{


    public Document toDoc() {
        return new org.bson.Document();
    }
    
}
