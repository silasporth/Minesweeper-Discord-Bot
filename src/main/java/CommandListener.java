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
                /*TODO: Add Minesweeper to Commands*/
                case "info": {
                    event.getChannel().sendMessage("Work in Progress...").queue();
                    break;
                }
                case "play": {
                    if (!Game.isRunning)
                        event.getChannel().sendMessage(Game.Difficulty.difficultyQuestion).queue();
                    break;
                }
                /*Just for testing purposes*/
                case "stop":
                    Game.isRunning = false;
                    break;

                default:
                    event.getChannel().sendMessage("Unknown Command").queue();
                    break;
            }
        }

        if (event.getMessage().getAuthor().isBot() && message.equals(Game.Difficulty.difficultyQuestion)) {
            for (Game.Difficulty difficulty : Game.Difficulty.values())
                event.getMessage().addReaction(difficulty.getEmoji()).queue();
        }

        if (event.getMessage().getAuthor().isBot()) {
            lastMessageID = event.getMessageId();
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot())
            return;

//        Choose Difficulty via Reactions while no Game is running
        if (!Game.isRunning) {
            Game.Difficulty difficulty = Game.Difficulty.difficultyByEmoji(event.getReactionEmote().toString().substring(3));
            if (event.getMessageId().equals(lastMessageID)) {
                if (!Game.isRunning) {
                    new Game(event.getChannel(), difficulty);
                }
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }
}
