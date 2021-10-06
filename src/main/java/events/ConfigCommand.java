package events;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

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

    /**
     * Handles the different subcommands that users can provide.
     *
     * @param event   a MessageCreate event that contains all information about user and the message that made the request
     * @param content a String array that contains all the words in the message.
     */
    private void handleSubCommand(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        if (subCommand != null) {
            switch (subCommand) {
                case ("studyrole"):
                    handleStudyRoleCommands(event, content);
                    break;
                case ("help"):
                    sendHelpMessage(event);
                    break;
                case ("records-channel"):
                    setRecordsChannel(event, content);
                    break;
                default:
                    event.getChannel().sendMessage("Please enter a valid subcommand!" +
                            "Please use !config help to learn more.");
                    break;
            }
        } else {
            event.getChannel().sendMessage("Please enter a valid subcommand!" +
                    "Please use !config help to learn more.");
        }
    }

    private void setRecordsChannel(MessageCreateEvent event, String[] content) {
        if (content[1] != null) {
            try {
                long textChannelId = Long.parseLong(content[1]);
                event.getServer().ifPresent(server -> {
                    server.getTextChannelById(textChannelId).ifPresentOrElse(textChannel -> {
                        ServerConfig.setRecordsChannel(server.getId(), textChannelId);
                        event.getChannel().sendMessage("Successfully set the study-records channel to be `" +
                                        textChannel.getName() + "`!");
                    }, () -> event.getChannel()
                            .sendMessage("Please provide valid ID for the textchannel!"));
                });
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Please provide valid ID for the textchannel!");
            }
        }
    }

    private void sendHelpMessage(MessageCreateEvent event) {
        EmbedBuilder about = new EmbedBuilder();
        about.setTitle("Config command:");
        about.setDescription("A command intended to customize the bot for your use cases!");
        about.addField("___StudyRole commands___", "**Set the StudyRole to be the role you provide!**: !config studyrole set <roleId>" +
                "**Automatically configure StudyRole!**: !config studyrole auto-config", false);
        about.setColor(new Color(0x9CD08F));
        event.getChannel().sendMessage(about);
    }

    private void handleStudyRoleCommands(MessageCreateEvent event, String[] content) {
        String studySubCommand = content[1];
        event.getServer().ifPresent(server -> {
            switch (studySubCommand) {
                case ("set"):
                    setStudyRole(event, content, server);
                    break;
                case ("auto-config"):
                    createStudyRole(event, server);
                    break;
            }
        });
    }

    private void createStudyRole(MessageCreateEvent event, Server server) {
        server.createRoleBuilder().setName("studying").create().thenAccept(role -> {
            ServerConfig.setStudyRoleForServer(server.getId(), role.getId());
            server.getChannelCategories().forEach(channelCategory ->
                    channelCategory.createUpdater()
                            .addPermissionOverwrite(role, Permissions.fromBitmask(0, 66560))
                            .update());
            server.getTextChannels().forEach(textChannel ->
                    textChannel.createUpdater()
                            .addPermissionOverwrite(role, Permissions.fromBitmask(0, 66560))
                            .update());
            server.getVoiceChannels().forEach(voiceChannel ->
                    voiceChannel.createUpdater()
                            .addPermissionOverwrite(role, Permissions.fromBitmask(0, 1049600))
                            .update());
            event.getChannel().sendMessage("Successfully configured study-role!");
        });
    }

    private void setStudyRole(MessageCreateEvent event, String[] content, Server server) {
        if (content[2] != null) {
            try {
                long roleId = Long.parseLong(content[2]);
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
        } else {
            event.getChannel().sendMessage("Please provide the ID of the role that you want to configure as " +
                    "StudyRole!");
        }
    }
}

