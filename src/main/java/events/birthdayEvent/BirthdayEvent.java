package events.birthdayEvent;

import exception.IllegalDateException;
import exception.InvalidDateFormatException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import persistence.JSONReader;
import persistence.JSONWriter;

import java.io.FileNotFoundException;
import java.util.Date;

/**
 * Represents a handler for !bday commands
 */
public class BirthdayEvent extends ListenerAdapter {
    private BirthdayLog bdayLog;
    private JSONReader reader = new JSONReader(BirthdayLog.BDAYLOG_LOCATION);
    private JSONWriter writer = new JSONWriter(BirthdayLog.BDAYLOG_LOCATION);

    public BirthdayEvent() {
        reader.loadObject();
        bdayLog = new BirthdayLog(reader.getBDayLog());
    }

    /**
     * Analyzes the given event to set birthday or lookup a member's birthday
     * @param event message event that started with message "!bday"
     */
    public void handleBdayActions(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String[] msg = event.getMessage().getContentRaw().split(" ");
        if (msg.length < 3) {
            return;
        }
        String name = event.getAuthor().getName();
        if (msg[1].equalsIgnoreCase("setbday")) {
            setBDay(channel, msg[2], name);
        } else if (msg[1].equalsIgnoreCase("lookup")) {
            lookupBDay(channel, msg[2]);
        }
    }

    /**
     * Finds the birthday of member mentioned in the message, prints to channel birthday message if found, print
     * "birthday not found" otherwise
     * @param channel channel to which message should be sent
     * @param name name of the member whose birthday is being looked up
     */
    private void lookupBDay(MessageChannel channel, String name) {
        Date date = bdayLog.getDateByName(name);
        if (date != null) {
            sendBDayMsg(channel, name, dateToStr(date));
        } else {
            channel.sendMessage("Birthday not found :(").queue();
        }
    }

    /**
     * Sets the bday of the member that sent the message according to the message if given date format is correct and
     * date is valid. Sends error message to the channel otherwise
     * @param channel channel to which message should be sent
     * @param date string representation of a date
     * @param name name of the member
     */
    private void setBDay(MessageChannel channel, String date, String name) {
        try {
            recordBDay(name, date);
            sendBDayMsg(channel, name, getMemberBday(name));
        } catch (InvalidDateFormatException e) {
            channel.sendMessage("Sorry, birthday format illegal. Not recorded.").queue();
        } catch (IllegalDateException e) {
            channel.sendMessage("O.o Wha").queue();
        }
    }

    /**
     * Sends a birthday message
     * @param channel channel to which message should be sent
     * @param name name of the member
     * @param date string representation of a date
     */
    private void sendBDayMsg(MessageChannel channel, String name, String date) {
        channel.sendMessage(name + "'s birthday is " + date + "!").queue();
    }

    /**
     * Records the birthday and save to JSON
     * @param name name of the member
     * @param date string representation of birthday
     * @throws InvalidDateFormatException when given date format is unrecognized
     * @throws IllegalDateException when given date has illegal year/month/day values
     */
    public void recordBDay(String name, String date) throws InvalidDateFormatException, IllegalDateException {
        reader.loadObject();
        bdayLog = new BirthdayLog(reader.getBDayLog());
        Date bday = getDateFromStr(date);
        bdayLog.addMemberBirthday(name, bday);
        try {
            writer.saveObject(bdayLog);
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Produce a date object specified by the given string
     * @param inpt string representation of a date
     * @return a Date object
     * @throws InvalidDateFormatException when given date format is unrecognized
     * @throws IllegalDateException when given date has illegal year/month/day values
     */
    public static Date getDateFromStr(String inpt) throws InvalidDateFormatException, IllegalDateException {
        String[] lst = inpt.split("[.\\/]");
        if (lst.length != 3) {
            throw new InvalidDateFormatException();
        }
        if (Integer.parseInt(lst[0]) < 1900 || Integer.parseInt(lst[1]) > 12 || Integer.parseInt(lst[2]) > 31) {
            throw new IllegalDateException();
        }
        return new Date(Integer.parseInt(lst[0]) - 1900,
                Integer.parseInt(lst[1]) - 1, Integer.parseInt(lst[2]));
    }

    public String getMemberBday(String nickname) {
        return dateToStr(bdayLog.getDateByName(nickname));
    }

    public static String dateToStr(Date d) {
        StringBuilder b = new StringBuilder();
        b.append(d.getYear() + 1900);
        b.append("/");
        b.append(d.getMonth() + 1);
        b.append("/");
        b.append(d.getDate());
        return b.toString();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String rawMsg = msg.getContentRaw();
        if (rawMsg.length() > 5 && rawMsg.substring(0, 5).equalsIgnoreCase("!bday")) {
            handleBdayActions(event);
        }
    }
}
