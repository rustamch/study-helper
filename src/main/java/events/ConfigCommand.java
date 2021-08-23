package events;

import org.javacord.api.event.message.MessageCreateEvent;

public class ConfigCommand implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        switch (subCommand) {
            case ("set"):
                if (event.getMessageAuthor().isServerAdmin()) {
                    event.getServer().ifPresent(server -> {
                        try {
                            long roleId = Long.parseLong(content[1]);
                            server.getRoleById(roleId).ifPresentOrElse(role -> {
                                long serverId = server.getId();
                                ServerConfig.setStudyRoleForServer(serverId, roleId);
                                event.getChannel().sendMessage("Successfully set the study role to be " +
                                        role.getName() + "role !");
                            }, () -> event.getChannel()
                                    .sendMessage("It looks like id you provided isn't associated" +
                                            " with any role on this server!"));
                        } catch (NumberFormatException e) {
                            event.getChannel()
                                    .sendMessage("It looks like you have put a word instead of role id!" +
                                            "Try sending !config set roleId");
                        }
                    });
                }
                break;
        }
    }
}
