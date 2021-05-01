package birthdays;

import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import persistence.JSONReader;
import persistence.JSONWriter;

import java.io.FileNotFoundException;
import java.util.Date;


public class BirthdayManager {
    private BirthdayLog bdayLog;
    private JSONReader reader = new JSONReader(BirthdayLog.BDAYLOG_LOCATION);
    private JSONWriter writer = new JSONWriter(BirthdayLog.BDAYLOG_LOCATION);

    public BirthdayManager() {
        reader.loadObject();
        bdayLog = new BirthdayLog(reader.getBDayLog());
    }

    public void handleBdayActions(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String[] msg = event.getMessage().getContentRaw().split(" ");
        if (msg.length < 3) {
            return;
        }
        String name = event.getAuthor().getName();
        if (msg[1].equalsIgnoreCase("setbday")) {
            try {
                recordBDay(name, msg[2]);
                sendBDayMsg(channel, name, getMemberBday(name));
            } catch (InvalidDateFormatException e) {
                channel.sendMessage("Sorry, birthday format illegal. Not recorded.").queue();
            } catch (IllegalDateException e) {
                channel.sendMessage("O.o Wha").queue();
            }
        } else if (msg[1].equalsIgnoreCase("lookup")) {
            Date date = bdayLog.getDateByName(msg[2]);
            if (date != null) {
                sendBDayMsg(channel, name, dateToStr(date));
            } else {
                channel.sendMessage("Birthday not found :(").queue();
            }
        }
    }

    private void sendBDayMsg(MessageChannel channel, String name, String date) {
        channel.sendMessage(name + "'s birthday is " + date + "!").queue();
    }

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
}
