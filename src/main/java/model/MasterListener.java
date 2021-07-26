import net.dv8tion.jda.api.hooks.ListenerAdapter;
import events.BirthdayEvent.BirthdayEvent;
import events.ReminderEvent.ReminderFeature;
import events.SimpleEvents.DoraListener;
import events.StudyTimeEvent.StudyTimeEvent;
import events.TodoEvent.TodoEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MasterListener extends ListenerAdapter{
    private BirthdayEvent bdayEvent;
    private TodoEvent todoEvent;
    private ReminderFeature reminderEvent;
    private StudyTimeEvent studyEvent;
    private DoraListener oOEvent;

    public MasterListener() {
        bdayEvent = new BirthdayEvent();
        todoEvent = new TodoEvent();
        reminderEvent = new ReminderFeature();
        studyEvent = new StudyTimeEvent();
        oOEvent = new DoraListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String rawMsg = msg.getContentRaw();
        if (rawMsg.length() > 5 && rawMsg.substring(0, 5).equalsIgnoreCase("!bday")) {
            handleBdayActions (event);
        }
    }
}