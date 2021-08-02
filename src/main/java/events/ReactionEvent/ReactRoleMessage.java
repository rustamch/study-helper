package events.ReactionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.Document;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.server.Server;

import exceptions.InvalidDocumentException;
import model.Bot;
import persistence.DBReader;
import persistence.Writable;

public class ReactRoleMessage extends Writable {
    private static String COLLECTION_NAME = "reaction_role_msgs";
    private static DBReader reader = new DBReader(COLLECTION_NAME);
    private long msgId;
    private Map<String,String> reactRoles;
    
    public ReactRoleMessage(long id) {
        this.msgId = id;
        this.reactRoles = new HashMap<>();
    }

    public static void checkAndRemoveRole(long messageID, Emoji emoji, Server server, long userID) {
        long serverID = server.getId();
        String emojiId = getEmojiId(emoji);
        getRoleID(messageID, emojiId, serverID).ifPresent(roleID -> 
            server.getRoleById(roleID).ifPresent(role -> 
                Bot.API.getUserById(userID).thenAccept(user ->
                    role.removeUser(user))));
    }

    public static void checkAndAddRole(long messageID, Emoji emoji, Server server, long userID) {
        long serverID = server.getId();
        String emojiID = getEmojiId(emoji);
        getRoleID(messageID, emojiID, serverID).ifPresent(roleID -> 
            server.getRoleById(roleID).ifPresent(role -> 
                Bot.API.getUserById(userID).thenAccept(user ->
                    role.addUser(user))));
    }


    public static Optional<Long> getRoleID(long messageId, String emojiId, long serverId) {
        try {
            Document reactionRoleMsg = reader.loadObject(messageId);
            Long roleID = reactionRoleMsg.getLong(emojiId);
            if (roleID != null) {
                return Optional.of(roleID);
            } else {
                return Optional.empty();
            }
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
    }
    
    // public static Optional<Document> loadReactRoleMsgDoc(long messageId, long severId) {
    //     try {
    //         Document reactionRoleMsg = reader.loadObject(messageId);
    //         reactionRoleMsg.
    //         if (roleId != null) {
    //             return Optional.of(roleId);
    //         } else {
    //             return Optional.empty();
    //         }
    //     } catch (InvalidDocumentException e) {
    //         return Optional.empty();
    //     }
    // }

    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put(ACCESS_KEY, msgId);
        for (Map.Entry<String, String> entry : reactRoles.entrySet()) {
            retDoc.put(entry.getKey(), entry.getValue());
        }
        return retDoc;
    }


    public static String getEmojiId(Emoji userReaction) {
        AtomicReference<String> emojiID = new AtomicReference<>();
        userReaction.asCustomEmoji().ifPresentOrElse(customEmoji -> emojiID.set(customEmoji.getIdAsString()),
                () -> userReaction.asKnownCustomEmoji().ifPresentOrElse(knownCustomEmoji ->
                    emojiID.set(knownCustomEmoji.getIdAsString()),
                        () -> userReaction.asUnicodeEmoji().ifPresent(unicodeEmoji ->
                            emojiID.set(unicodeEmoji))));
        return emojiID.get();
    }

    public Object trackMsg(long id) {
        return null;
    }

}
