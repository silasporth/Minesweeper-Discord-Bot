import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    private String lastMessageID;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.startsWith("!")) {
            String[] messageUsable = message.split("!");
            switch (messageUsable[1].toLowerCase()) {
                case "info": {
                    event.getChannel().sendMessage("Work in Progress...").queue();
                    break;
                }
                case "play": {
                    if (!Game.isRunning)
                        event.getChannel().sendMessage("At which difficulty you wanna play? (Easy/Normal/Hard)").queue();
                    break;
                }
                default:
                    event.getChannel().sendMessage("Unknown Command").queue();
                    break;
            }
        }

        if (event.getMessage().getAuthor().isBot() && message.contains("difficulty")) {
            event.getMessage().addReaction("U+1F1EA").queue();
            event.getMessage().addReaction("U+1F1F3").queue();
            event.getMessage().addReaction("U+1F1ED").queue();
        }

        if (event.getMessage().getAuthor().isBot()) {
            lastMessageID = event.getMessageId();
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot())
            return;

        String difficulty = "";
        boolean reactionCommand = true;

        if (!Game.isRunning) {
            switch (event.getReactionEmote().toString()) {
                case "RE:U+1f1ea":
                    difficulty = "easy";
                    break;
                case "RE:U+1f1f3":
                    difficulty = "normal";
                    break;
                case "RE:U+1f1ed":
                    difficulty = "hard";
                    break;
                default:
                    reactionCommand = false;
                    break;
            }

            if (event.getMessageId().equals(lastMessageID)) {
                if (reactionCommand) {
                    new Game(event.getChannel(), difficulty);
                }
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }
}
