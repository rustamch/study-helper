package events;

import exceptions.InvalidDocumentException;
import org.bson.Document;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;

import java.util.Optional;

public class ServerConfig implements Writable {
    private static String COLLECTION_NAME = "server_configs";
    private static String STUDY_ROLE_KEY = "study_role_key";
    private static DBReader reader = new DBReader(COLLECTION_NAME);
    private static DBWriter writer = new DBWriter(COLLECTION_NAME);
    private long serverId;
    private long studyRoleId;


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
            }
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
        return  Optional.empty();
    }

    /**
     * Checks if the given role is the study role for the given server.
     * @param role A role that needs to be checked
     * @param server A server on which StudyRole needs to be retrieved
     * @return true if the role is StudyRole, false otherwise
     */
    public static boolean isStudyRole(Role role, Server server) {
        if (getStudyRoleForServer(server).isPresent()) {
            return role.equals(getStudyRoleForServer(server).get());
        } else {
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
        retDoc.put(STUDY_ROLE_KEY,studyRoleId);
        return retDoc;
    }
}
