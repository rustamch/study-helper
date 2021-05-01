import com.sun.media.sound.InvalidFormatException;
import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import model.ReminderManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReminderFeature extends ListenerAdapter {

    public ReminderFeature() {}

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        String messageStringRaw = message.getContentRaw();

        if (messageStringRaw.matches("!reminder .*")) {
            ReminderManager reminderManager = new ReminderManager();
            try {
                reminderManager.addReminder(event);
                System.out.println("Reminder was added");
            } catch (DuplicateReminderException e) {
                System.out.println("Duplicated reminder.");
            } catch (InvalidReminderFormatException e) {
                System.out.println("Invalid reminder format.");
            }
        }
    }
}
