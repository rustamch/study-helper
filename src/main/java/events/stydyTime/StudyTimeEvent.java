package events.stydyTime;

import java.time.Instant;
import java.time.Duration;
import java.util.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import persistence.JSONReader;
import persistence.JSONWriter;
import persistence.Writable;
import model.Bot;
import sun.awt.image.ImageWatched;

import javax.jws.soap.SOAPBinding;

public class StudyTimeEvent extends ListenerAdapter {
  public static final String STUDY_CHANNEL = "silent study";
  private static final String COLLECTION_NAME = "times";
  private static final String DOC_NAME = "study_times";
  TextChannel textChannel;
  String memberID;

  @Override
  public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
    if (event.getChannelLeft().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
      endAndRecord(event);
    } else if (event.getChannelJoined().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
      trackStartTime(event);
    }
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    String rawMsg = event.getMessage().getContentRaw();
    String[] msgLst = rawMsg.split(" ");
    if (msgLst[0].equalsIgnoreCase("!leaderboard")) {
      JSONReader reader = new JSONReader(COLLECTION_NAME, DOC_NAME);
      EmbedBuilder about = new EmbedBuilder();
      about.setTitle("\uD83D\uDCD8 Grind Leaderboard");
      about.setColor(0x9CD08F);
      JSONObject jsonObject = reader.getStoredTimes();
      JSONObject times = jsonObject.getJSONObject("times");
      Set<String> timeKeys = times.keySet();
      Map<String, Long> map = new LinkedHashMap<>();
      for (String keys : timeKeys) {
        map.put(keys, times.getLong(keys));
        List<Long> timeList = new ArrayList<>(map.values());
        timeList.sort(Comparator.reverseOrder());
      }
      int i = 1;
      for (Map.Entry<String, Long> entry : map.entrySet()) {
        if (i > 10) {
          break;
        }
        String key = entry.getKey();
        Long val = entry.getValue();
        User user = Bot.jda.retrieveUserById(key).complete();
        String name = user.getName();
        about.addField(i + ". " + name, name + " has studied for " + val + " minutes so far.", false);
        i++;
      }
      event.getChannel().sendMessage(about.build()).queue();
      about.clear();
    }
  }

  @Override
  public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    if (event.getChannelJoined().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
      trackStartTime(event);
    }
  }

  @Override
  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    if (event.getChannelLeft().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
      endAndRecord(event);
    }
  }

  private void trackStartTime(GenericGuildVoiceEvent event) {
    Member m = event.getMember();
    memberID = m.getId();
    Instant start = Instant.now();
    JSONWriter jsonWriter = new JSONWriter(COLLECTION_NAME, memberID);
    JSONObject obj = new JSONObject();
    obj.put(Writable.ACCESS_KEY, m.getId());
    obj.put("epoch", start.getEpochSecond());
    obj.put("nanos", start.getNano());
    jsonWriter.saveString(obj.toString());
    textChannel = event.getGuild().getTextChannelsByName("study-records", true).get(0);
    textChannel.sendMessage(m.getEffectiveName() + " has started studying!").queue();
  }


  private void endAndRecord(GenericGuildVoiceEvent event) {
    Member m = event.getMember();
    memberID = m.getId();
    JSONReader jsonReader = new JSONReader(COLLECTION_NAME, memberID);
    JSONObject jsonObject = jsonReader.getStudySeesion();
    Instant start = Instant.ofEpochSecond(jsonObject.getLong("epoch"), jsonObject.getLong("nanos"));
    Instant finish = Instant.now();
    long timeElapsed = Duration.between(start, finish).toMillis();
    sendTimeElapsedMessage(timeElapsed);
    storeElapsedTime(memberID, timeElapsed);
  }

  private void storeElapsedTime(String memberID, long timeElapsed) {
    JSONReader reader = new JSONReader(COLLECTION_NAME, DOC_NAME);
    JSONWriter writer = new JSONWriter(COLLECTION_NAME, DOC_NAME);
    JSONObject jobj = reader.getStoredTimes();
    jobj.put(Writable.ACCESS_KEY, "study_times");
    long timeAcc = timeElapsed / 1000 / 60;
    if (jobj.has("times")) {
      JSONObject times = jobj.getJSONObject("times");
      if (times.has(memberID)) {
        timeAcc += times.getLong(memberID);
        times.remove(memberID);

      }
    } else {
      JSONObject times = new JSONObject();
      jobj.put("times", times);
    }
    JSONObject times = jobj.getJSONObject("times");
    times.put(memberID, timeAcc);
    jobj.put("times", times);
    writer.saveString(jobj.toString());
  }

  private void sendTimeElapsedMessage(long timeElapsed) {
    if (timeElapsed / 1000 > 3600)
      textChannel.sendMessage("<@" + memberID + ">" + " has studied for **" + timeElapsed / 1000 / 60 / 60 + "** hours" +
              " and " + timeElapsed / 1000 / 60 % 60 + " minutes!").queue();
    else if (timeElapsed / 1000 > 60)
      textChannel.sendMessage("<@" + memberID + ">" + " has studied for **" + timeElapsed / 1000 / 60 + "** minutes!").queue();
    else
      textChannel.sendMessage("<@" + memberID + ">" + " has studied for **" + timeElapsed / 1000 + "** seconds!").queue();
  }
}
