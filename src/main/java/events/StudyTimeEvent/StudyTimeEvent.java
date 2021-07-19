package events.StudyTimeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import exceptions.InvalidDocumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a handler for !studytime commands
 */
public class StudyTimeEvent extends ListenerAdapter {
  public static final String STUDY_CHANNEL = ".*study.*";
  public static final int NUMBER_OF_USERS_ON_LEADERBOARD = 5;
  TextChannel textChannel;


  @Override
  public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
    if (event.getChannelLeft().getName().matches(STUDY_CHANNEL)) {
      try {
        endAndRecord(event);
      } catch (InvalidDocumentException e) {
        textChannel.sendMessage("Sigh... something went wrong.");
      }
    } else if (event.getChannelJoined().getName().matches(STUDY_CHANNEL)) {
      trackStartTime(event);
    }
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    String rawMsg = event.getMessage().getContentRaw();
    String[] msgLst = rawMsg.split(" ");
    if (msgLst[0].equalsIgnoreCase("!leaderboard")) {
      EmbedBuilder leaderBoard;
      try {
        leaderBoard = createLeaderBoard(event);
        event.getChannel().sendMessage(leaderBoard.build()).queue();
        leaderBoard.clear();
      } catch (InvalidDocumentException e) {
        event.getChannel().sendMessage("Something went wrong!");
      }
    } else if (msgLst[0].equalsIgnoreCase("!studytime")) {
      if (msgLst[1].equalsIgnoreCase("check")) {
        msgStudyTimeForUser(event);
      } else if (msgLst[1].equalsIgnoreCase("sub")) { // IT'S BACK!!
        StudyTimeLeaderboard studyLeaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
        
        if (studyLeaderboard.getUserTime(event.getAuthor().getId()) != null) {
          long time = Math.abs(Long.parseLong(msgLst[2]) * 60 * 1000);
          storeElapsedTime(event.getAuthor().getId(), -time);
          event.getChannel().sendMessage("Successfully subtracted " + Long.toString(time / 60000) + " minute(s)!").queue();
        } else {
          event.getChannel().sendMessage("You haven't studied yet >:(").queue();
        }
      }
    }
  }
  

  
  @Override
  public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    if (event.getChannelJoined().getName().matches(STUDY_CHANNEL)) {
      trackStartTime(event);
    }
  }

  @Override
  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    if (event.getChannelLeft().getName().matches(STUDY_CHANNEL)) {
      try {
        endAndRecord(event);
      } catch (InvalidDocumentException e) {
        textChannel.sendMessage("Sigh... something went wrong.");
      }
    }
  }

  /**
   * Constructs and sends a message that tells user for how long has he studied already.
   * @param event a JDA event
   */
  private void msgStudyTimeForUser(@NotNull MessageReceivedEvent event) {
    StudyTimeLeaderboard studyTimeLeaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
    Long time = studyTimeLeaderboard.getUserTime(event.getAuthor().getId());
    if (time > 0) {
      event.getChannel().sendMessage(event.getAuthor().getAsMention() + " has studied for "
              + time + " minutes").queue();
    } else {
      event.getChannel().sendMessage(event.getAuthor().getAsMention() + " you have not studied yet.").queue();
    }
  }

  /**
   * Creates a visual representation of the current top-3 places on the leaderboard
   * @param event a JDA event
   * @return EmbedBuilder that 
   * @throws InvalidDocumentException
   */
  @NotNull
  private EmbedBuilder createLeaderBoard(@NotNull MessageReceivedEvent event) throws InvalidDocumentException  {
    EmbedBuilder about = new EmbedBuilder();
    about.setTitle("\uD83D\uDCD8 Grind Leaderboard");
    about.setColor(0x9CD08F);
    int i = 1;
    StudyTimeLeaderboard leaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
    for (String memberID : leaderboard) {
      if (i > NUMBER_OF_USERS_ON_LEADERBOARD) {
        break;
      }
      Long val = leaderboard.getUserTime(memberID);
      String name = event.getGuild().retrieveMemberById(memberID).complete().getEffectiveName();
      about.addField(i + ". " + name, name + " has studied for " + val + " minutes so far.", false);
      i++;
    }
    return about;
  }
  /**
   * Creates a new a study session and saves it to the database
   * @param event a JDA event that contains information about the server, user etc.
   */
  private void trackStartTime(GenericGuildVoiceEvent event) {
    Member m = event.getMember();
    StudyTimeSession session = new StudyTimeSession(m.getId());
    session.trackSession();
    textChannel = event.getGuild().getTextChannelsByName("study-records", true).get(0);
    event.getMember().getUser().openPrivateChannel().queue((channel) ->
    {
        channel.sendMessage("Have a productive study session!").queue();
    });
  }

  /**
   * Calculates amount of time the given user has studied and saves it to the database
   * @param event a JDA event that contains information about the server, user etc.
   * @throws InvalidDocumentException thrown if the original study session cannot be obtained
   */
  private void endAndRecord(GenericGuildVoiceEvent event) throws InvalidDocumentException {
    Member m = event.getMember();
    StudyTimeSession session = StudyTimeSession.getStudySession(m.getId());
    long timeElapsed = session.finishSession();
    sendTimeElapsedMessage(event,timeElapsed);
    storeElapsedTime(m.getId(), timeElapsed);
  }

  /**
   * Stores studytime of the given user to the database
   * @param memberID id of the user who just finished his study session
   * @param timeElapsed a number of miliseconds the given user has studied
   */
  private void storeElapsedTime(String memberID, long timeElapsed) {
    StudyTimeLeaderboard leaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
    leaderboard.addUserTime(memberID,timeElapsed);
  }

  /**
   * Sends a message that tells for how long the given user has studied.
   * @param timeElapsed amount of time in miliseconds.
   */
  private void sendTimeElapsedMessage(GenericGuildVoiceEvent event, long timeElapsed) {
    String msg;
    if (timeElapsed / 1000 > 3600)
      msg = "You just studied for **" + timeElapsed / 1000 / 60 / 60 + "** hours" +
              " and " + timeElapsed / 1000 / 60 % 60 + " minutes!";
    else if (timeElapsed / 1000 > 60)
      msg = "You just studied for **" + timeElapsed / 1000 / 60 + "** minutes!";
    else
      msg = "You just studied for **" + timeElapsed / 1000 + "** seconds!";
    event.getMember().getUser().openPrivateChannel().queue((channel) ->
      {
          channel.sendMessage(msg).queue();
      });
  }

}
