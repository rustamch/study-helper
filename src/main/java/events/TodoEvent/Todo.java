package events.TodoEvent;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import persistence.Writable;

import java.time.LocalDate;

public class Todo implements Writable, Comparable<Todo> {
    private String course;
    private String description;
    private LocalDate dueDate;
    private boolean incomplete;

    public boolean isIncomplete() {
        return incomplete;
    }

    public Todo(String course, String descript, LocalDate due) {
        this.course = course;
        description = descript;
        dueDate = due;
        incomplete = true;
    }

    public void setComplete() {
        incomplete = false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!incomplete) {
            builder.append("~~");
        }
        if (course != null) {
            builder.append(course + " ");
        }
        builder.append("**" + description + "** -- due ");
        if (dueDate.isEqual(LocalDate.now())) {
            builder.append("TODAY");
        } else if (LocalDate.now().isBefore(dueDate) && dueDate.compareTo(LocalDate.now().plusWeeks(1)) < 0) {
            builder.append(dueDate.getDayOfWeek());
        } else {
            builder.append(dueDate);
        }
        if (!incomplete) {
            builder.append("~~");
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (course == null) {
            json.put("course", "null");
        } else {
            json.put("course", course);
        }
        json.put("description", description);
        json.put("dueDate", dueDate.toString());
        json.put("incomplete", incomplete);
        return json;
    }

    @Override
    public int compareTo(@NotNull Todo o) {
        if (this.incomplete == o.incomplete) {
            if (this.dueDate.isBefore(o.dueDate)) {
                return -1;
            } else {
                return 1;
            }
        } else if (this.incomplete) {
            return -1;
        } else {
            return 1;
        }
    }
}
