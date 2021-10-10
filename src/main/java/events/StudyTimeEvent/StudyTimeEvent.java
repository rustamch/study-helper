package events.StudyTimeEvent;

import model.Bot;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import exceptions.InvalidDocumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a handler for !studytime commands
 */
public class StudyTimeEvent implements BotMessageEvent {
  public static final String STUDY_CHANNEL = "silent study";
  public static final int NUMBER_OF_USERS_ON_LEADERBOARD = 5;

  /**
   * Constructs and sends a message that tells user for how long has he studied
   * already.
   *
   * @param event a JDA event
   */
  private void msgStudyTimeForUser(String userId, MessageCreateEvent event) {
    long studytimeMin = StudyTimeRecord.getUserStudytime(userId) / 60;
    Bot.API.getCachedUserById(userId).ifPresent(user -> {
      if (studytimeMin > 0) {
        event.getChannel().sendMessage(user.getMentionTag() + " has studied for " +
                studytimeMin / 60 + " hour(s) " + studytimeMin % 60 + " minute(s)");
      } else {
        event.getChannel().sendMessage(user.getMentionTag() + " has not studied yet.");
      }
    });
  }

  private void msgWeeklyStudyTimeForUser(String userId, MessageCreateEvent event) {
    long studytimeMin = StudyTimeRecord.getUserWeeklyStudytime(userId) / 60;
    Bot.API.getCachedUserById(userId).ifPresent(user -> {
      if (studytimeMin > 0) {
        event.getChannel().sendMessage(user.getMentionTag() + " has studied for " +
                studytimeMin / 60 + " hour(s) " + studytimeMin % 60 + " minute(s)");
      } else {
        event.getChannel().sendMessage(user.getMentionTag() + " has not studied yet.");
      }
    });
  }

  @Override
  public void invoke(MessageCreateEvent event, String[] content) {
    String command = content[0];
    switch (command) {
      case "check":
        if (content.length > 1) {
          if (content[1].equals("weekly")) {
            msgWeeklyStudyTimeForUser(event.getMessageAuthor().getIdAsString(),event);
          } else {
            Pattern p = Pattern.compile("\\d{18}");
            Matcher matcher = p.matcher(content[1]);
            if (matcher.find()) {
              String userId = matcher.group(0);
              msgStudyTimeForUser(userId, event);
            }
          }
        } else {
          msgStudyTimeForUser(event.getMessageAuthor().getIdAsString(),event);
        }
        break;
      case "leaderboard":
        processesLeaderboardComm(event, content);
        break;
      case "sub":
        if (content.length > 1) {
          try {
            long time = Math.abs(Long.parseLong(content[1]));
            StudyTimeRecord.subtractStudyTime(event.getMessageAuthor().getIdAsString(), time * 60);
            event.getChannel().sendMessage("Successfully subtracted " + time + " minute(s)!");
          } catch (InvalidDocumentException e) {
            event.getChannel().sendMessage("You haven't studied yet >:(");
          }
        } else {
          event.getChannel().sendMessage("You need to specify how much you want to subtract!");
        }
        break;
    }
  }

  private void processesLeaderboardComm(MessageCreateEvent event, String[] content) {
    if (content.length > 1) {
      if (content[1].equals("reset")) {
        StudyTimeLeaderboard.loadGlobalLeaderboard(event.getMessageAuthor().getIdAsString()).resetLeaderboard();
      } else if (content[1].equals("weekly")) {
        StudyTimeLeaderboard studyTimeLeaderboard = StudyTimeLeaderboard.loadWeeklyLeaderboard();
        event.getServer().ifPresent(server -> {
          EmbedBuilder eb = studyTimeLeaderboard.getLeaderboardEmbed(server);
          event.getChannel().sendMessage(eb);
        });
      }
    } else {
      StudyTimeLeaderboard studyTimeLeaderboard = StudyTimeLeaderboard.loadGlobalLeaderboard(event.getMessageAuthor().getIdAsString());
      event.getServer().ifPresent(server -> {
        EmbedBuilder eb = studyTimeLeaderboard.getLeaderboardEmbed(server);
        event.getChannel().sendMessage(eb);
      });
    }
  }
}
