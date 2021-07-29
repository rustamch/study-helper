package events.TodoEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import exceptions.IllegalDateException;
import exceptions.MissingElementException;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoEvent implements BotMessageEvent {
    private TodoManager manager;

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        String rawMsg = event.getMessageContent();
        MessageAuthor author = event.getMessageAuthor();
        User user = author.asUser().get();
        manager = new TodoManager(user);
        if (content.length != 0) {
            if (content[0].equalsIgnoreCase("add")) {
                addTodo(event, rawMsg, content);
            } else if (content[0].equalsIgnoreCase("check")) {
                messageTodoList(event.getChannel());
            } else if (content[0].equalsIgnoreCase("rm") && content.length > 2) {
                if (content[1].equalsIgnoreCase("all")) {
                    manager.clearTodo();
                    event.getChannel().sendMessage("Your todo list is cleared!");
                } else {
                    removeTodo(event, content);
                }
            } else if (content[0].equalsIgnoreCase("done") && content.length > 2) {
                if (content[1].equalsIgnoreCase("all")) {
                    manager.clearTodo();
                    event.getChannel().sendMessage("List completed, hooray!");
                } else {
                    setComplete(event, content);
                }
            } else if (content[0].equalsIgnoreCase("post")) {
                TextChannel textChannel = event.getServer().get().getTextChannelsByName("todos").get(0);
                messageTodoList(textChannel);
            }
        }
    }

    private void setComplete(MessageCreateEvent event, String[] msgLst) {
        StringBuilder builder = new StringBuilder();
        int numCompleted = 0;
        try {
            for (int i = 2; i < msgLst.length; i++) {
                manager.setTodoAsComplete(Integer.parseInt(msgLst[i]) - 1 - numCompleted);
                builder.append(Integer.parseInt(msgLst[i])).append(" ");
                numCompleted++;
            }
        } catch (NumberFormatException e) {

        } finally {
            if (manager.listIsCleared()) {
                event.getChannel().sendMessage("List completed, hooray!");
            } else {
                builder.append("completed!");
                event.getChannel().sendMessage(builder.toString());
            }
        }
    }

    private void messageTodoList(TextChannel channel) {
        channel.sendMessage(manager.getTodoMessage());
    }

    private void addTodo(MessageCreateEvent event, String rawMsg, String[] msgLst) {
        try {
            if (msgLst.length == 3) {
                setTodayGoal(msgLst[2]);
            } else {
                addLongTodo(rawMsg);
            }
            messageTodoList(event.getChannel());
        } catch (MissingElementException | IllegalDateException e) {
            event.getChannel().sendMessage(e.getMessage());
        }
    }

    private void removeTodo(MessageCreateEvent event, String[] msgLst) {
        StringBuilder builder = new StringBuilder();
        int numRemoved = 0;
        try {
            for (int i = 2; i < msgLst.length; i++) {
                manager.removeTodoByNumber(Integer.parseInt(msgLst[i]) - 1 - numRemoved);
                builder.append(Integer.parseInt(msgLst[i])).append(" ");
                numRemoved++;
            }
        } catch (NumberFormatException e) {

        } finally {
            builder.append("removed!");
            event.getChannel().sendMessage(builder.toString());
        }
    }

    private void addLongTodo(String rawMsg) throws MissingElementException, IllegalDateException {
        String course = findCourse(rawMsg);
        String description = findDescription(rawMsg);
        LocalDate due = findDue(rawMsg);
        manager.addTodo(course, description, due);
    }

    private void setTodayGoal(String rawMsg) throws MissingElementException, IllegalDateException {
        LocalDate due = LocalDate.now();
        String description = findDescription(rawMsg);
        manager.addTodo(null, description, due);
    }

    private LocalDate findDue(String rawMsg) throws MissingElementException {
        String[] lst = rawMsg.split("due ");
        if (lst.length > 1) {
            String[] date = lst[1].split("[\\/\\.-]");
            if (date.length != 2 && date.length != 3) {
                throw new MissingElementException("Date incomplete!");
            }
            if (date.length == 2) {
                int year = LocalDate.now().getYear();
                int month = Integer.parseInt(date[0]);
                int day = Integer.parseInt(date[1]);
                return LocalDate.of(year, month, day);
            } else {
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                return LocalDate.of(year, month, day);
            }
        }
        return LocalDate.now();
    }

    private String findDescription(String rawMsg) throws MissingElementException {
        try {
            return rawMsg.split("[\"“”]")[1];
        } catch (IndexOutOfBoundsException e) {
            throw new MissingElementException("Missing description!");
        }
    }

    private String findCourse(String rawMsg) {
        Pattern p = Pattern.compile("[QWERTYUIOPASDFGHJKLZXCVBNM]{4}\\s\\d\\d\\d");
        Matcher m = p.matcher(rawMsg);
        if (m.find()) {
            return m.group();
        }
        return null;
    }
}
