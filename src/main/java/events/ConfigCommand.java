package events;

import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class ConfigCommand implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (event.getMessageAuthor().isServerAdmin()) {
            if (content[0] != null) {
                handleSubCommand(event, content);
            } else {
                event.getChannel().sendMessage("Please enter a valid subcommand!" +
                        "Please use !config help to learn more.");
            }
        } else {
            event.getChannel().sendMessage("Only server admins can use config commands!");
        }
    }

    private void handleSubCommand(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        switch (subCommand) {
            case ("studyrole"):
                handleStudyRoleCommands(event, content);
                break;
        }
    }

    private void handleStudyRoleCommands(MessageCreateEvent event, String[] content) {
        String studySubCommand = content[1];
            event.getServer().ifPresent(server -> {
                switch (studySubCommand) {
                    case ("set"):
                        setStudyRole(event,content,server);
                        break;
                    case ("auto-config"):
                        createStudyRole(event,content,server);
                       break;
                }
            });
    }

    private void createStudyRole(MessageCreateEvent event, String[] content, Server server) {
        server.createRoleBuilder().setName("studying").create().thenAccept(role -> {
            ServerConfig.setStudyRoleForServer(server.getId(), role.getId());
            server.getChannelCategories().forEach(channelCategory -> {
                channelCategory
                        .createUpdater()
                        .addPermissionOverwrite(role, Permissions.fromBitmask(0,66560))
                        .update();
            });
        });
    }

    private void setStudyRole(MessageCreateEvent event, String[] content, Server server) {
        try {
            long roleId = Long.parseLong(content[1]);
            server.getRoleById(roleId).ifPresentOrElse(role -> {
                long serverId = server.getId();
                ServerConfig.setStudyRoleForServer(serverId, roleId);
                event.getChannel().sendMessage("Successfully set the study role to be " +
                        role.getName() + " role !");
            }, () -> event.getChannel()
                    .sendMessage("It looks like id you provided isn't associated" +
                            " with any role on this server!"));
        } catch (NumberFormatException e) {
            event.getChannel()
                    .sendMessage("It looks like you have put a word instead of role id!" +
                            "Try sending !config set roleId");
        }
    }

}

