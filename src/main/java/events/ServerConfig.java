package events;

import exceptions.InvalidDocumentException;
import org.bson.Document;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;

import java.util.Optional;

public class ServerConfig implements Writable {
    private static final String COLLECTION_NAME = "server_configs";
    private static final String STUDY_ROLE_KEY = "study_role_key";
    private static final String RECORDS_CHANNEL_KEY = "records_channel_key";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private final long serverId;
    private long studyRoleId;
    private long recordsChannelId;



    public ServerConfig(long serverId) {
        this.serverId = serverId;
    }

    /**
     * Returns an optional that may contain the studyRole for the given server.
     * @param server a server on which "StudyRole" needs to be retrieved.
     * @return a "StudyRole" that signifies that user is studying.
     */
    public static Optional<Role> getStudyRoleForServer(Server server) {
        try {
            long serverId = server.getId();
            Document readDoc = reader.loadObject(serverId);
            long roleId = readDoc.getLong(STUDY_ROLE_KEY);
            if (server.getRoleById(roleId).isPresent()){
                return Optional.of(server.getRoleById(roleId).get());
            } else {
                return Optional.empty();
            }
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<TextChannel> getTextChannelForServer(Server server) {
        try {
            long serverId = server.getId();
            Document readDoc = reader.loadObject(serverId);
            long recordsChannelId = readDoc.getLong(RECORDS_CHANNEL_KEY);
            if (server.getTextChannelById(recordsChannelId).isPresent()){
                return Optional.of(server.getTextChannelById(recordsChannelId).get());
            } else {
                return Optional.empty();
            }
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
    }

    public static boolean isStudyRole(long roleId, long serverId) {
        try {
            Document readDoc = reader.loadObject(serverId);
            long studyRoleId = readDoc.getLong(STUDY_ROLE_KEY);
            return studyRoleId == roleId;
        } catch (InvalidDocumentException e) {
            return false;
        }
    }

    public static void setStudyRoleForServer(long serverId, long roleId) {
        try {
            Document readDoc = reader.loadObject(serverId);
            readDoc.put(STUDY_ROLE_KEY,roleId);
            writer.saveDocument(readDoc);
        } catch (InvalidDocumentException e) {
            ServerConfig serverConfig = new ServerConfig(serverId);
            serverConfig.setStudyRoleId(roleId);
            serverConfig.save();
        }
    }

    public static void setRecordsChannel(long serverId, long textChannelId) {
        try {
            Document readDoc = reader.loadObject(serverId);
            readDoc.put(RECORDS_CHANNEL_KEY,textChannelId);
            writer.saveDocument(readDoc);
        } catch (InvalidDocumentException e) {
            ServerConfig serverConfig = new ServerConfig(serverId);
            serverConfig.setRecordsChannelId(textChannelId);
            serverConfig.save();
        }
    }

    private void setRecordsChannelId(long textChannelId) {
        this.recordsChannelId = textChannelId;
    }

    private void save() {
        writer.saveObject(this, SaveOption.DEFAULT);
    }


    public void setStudyRoleId(long studyRoleId) {
        this.studyRoleId = studyRoleId;
    }

    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put(ACCESS_KEY,serverId);
        retDoc.put(RECORDS_CHANNEL_KEY,recordsChannelId);
        retDoc.put(STUDY_ROLE_KEY,studyRoleId);
        return retDoc;
    }
}
