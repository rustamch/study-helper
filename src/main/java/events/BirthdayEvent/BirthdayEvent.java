package events.BirthdayEvent;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import persistence.DBReader;
import persistence.DBWriter;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;

/**
 * Represents a handler for !bday commands
 */
public class BirthdayEvent extends ListenerAdapter {
    private DBReader reader = new DBReader(BirthdayLog.BDAYLOG_LOCATION,BirthdayLog.SAVE_VAL);
    private DBWriter writer = new DBWriter(BirthdayLog.BDAYLOG_LOCATION,BirthdayLog.SAVE_VAL);

    /**
     * Analyzes the given event to set birthday or lookup a member's birthday
     * @param event message event that started with message "!bday"
     */
    public void handleBdayActions(BirthdayLog bdayLog, MessageReceivedEvent event) {
        String[] msg = event.getMessage().getContentRaw().split(" ");
        if (msg.length < 3) {
            return;
        }
        String id = event.getAuthor().getId();
        if (msg[1].equalsIgnoreCase("setbday")) {
            setBDay(bdayLog, event, msg[2], id);
        } else if (msg[1].equalsIgnoreCase("check")) {
            lookupBDay(bdayLog, event, msg[2]);
        }
    }

    /**
     * Finds the birthday of member mentioned in the message, prints to channel birthday message if found, print
     * "birthday not found" otherwise
     * @param event message event
     * @param name name or atMention of the member whose birthday is being looked up
     */
    private void lookupBDay(BirthdayLog bdayLog, MessageReceivedEvent event, String name) {
        Pattern p = Pattern.compile("\\d{18}");
        Matcher matcher = p.matcher(name);
        String id;
        if (matcher.find()) {
            id = matcher.group(0);
        } else {
            id = event.getGuild().getMembersByEffectiveName(name, true).get(0).getId();
        }
        Date date = bdayLog.getDateById(id);
        if (date != null) {
            sendBDayMsg(event, id, dateToStr(date));
        } else {
            event.getChannel().sendMessage("Birthday not found :(").queue();
        }
    }

    /**
     * Sets the bday of the member that sent the message according to the message if given date format is correct and
     * date is valid. Sends error message to the channel otherwise
     * @param event message event
     * @param date string representation of a date
     * @param id name of the member
     */
    private void setBDay(BirthdayLog bdayLog, MessageReceivedEvent event, String date, String id) {
        try {
            recordBDay(bdayLog, id, date);
            sendBDayMsg(event, id, getMemberBday(bdayLog, id));
        } catch (InvalidDateFormatException e) {
            event.getChannel().sendMessage("Sorry, birthday format illegal. Not recorded.").queue();
        } catch (IllegalDateException e) {
            event.getChannel().sendMessage("O.o Wha").queue();
        }
    }

    /**
     * Sends a birthday message
     * @param event message event
     * @param id name of the member
     * @param date string representation of a date
     */
    private void sendBDayMsg(MessageReceivedEvent event, String id, String date) {
        event.getChannel().sendMessage( event.getGuild().getMemberById(id).getEffectiveName() + "'s birthday is " + date + "!").queue();
    }

    /**
     * Records the birthday and save to DataBase
     * @param name name of the member
     * @param date string representation of birthday
     * @throws InvalidDateFormatException when given date format is unrecognized
     * @throws IllegalDateException when given date has illegal year/month/day values
     */
    public void recordBDay(BirthdayLog bdayLog, String name, String date) throws InvalidDateFormatException, IllegalDateException {
        bdayLog = new BirthdayLog(reader.getBDayLog());
        Date bday = getDateFromStr(date);
        bdayLog.addMemberBirthday(name, bday);
        writer.saveObject(bdayLog);
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

    public String getMemberBday(BirthdayLog bdayLog, String id) {
        return dateToStr(bdayLog.getDateById(id));
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
        BirthdayLog bdayLog = new BirthdayLog(reader.getBDayLog());
        if (rawMsg.length() > 5 && rawMsg.substring(0, 5).equalsIgnoreCase("!bday")) {
            handleBdayActions(bdayLog, event);
        }
    }
}
