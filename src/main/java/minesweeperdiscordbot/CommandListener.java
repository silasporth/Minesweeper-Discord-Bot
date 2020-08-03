package minesweeperdiscordbot;

import minesweeperdiscordbot.Game.Difficulty;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    private String lastMessageID;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay();
        if (message.toLowerCase().startsWith("!minesweeper") || message.toLowerCase().startsWith("!ms")) {
            String[] messageUsable = message.split(" ");
            switch (messageUsable[1].toLowerCase()) {
                case "info": {
                    event.getChannel().sendMessage("Work in Progress...").queue();
                    break;
                }
                case "play": {
                    if (!Game.isRunning)
                        event.getChannel().sendMessage(Difficulty.difficultyQuestion).queue();
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

        if (event.getMessage().getAuthor().isBot() && message.equals(Difficulty.difficultyQuestion)) {
            for (Difficulty difficulty : Difficulty.values())
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
        if (!Game.isRunning && (event.getReactionEmote().getEmoji().equals(Difficulty.EASY.getEmoji()) || event.getReactionEmote().getEmoji().equals(Difficulty.NORMAL.getEmoji()) || event.getReactionEmote().getEmoji().equals(Difficulty.HARD.getEmoji()))) {
            Difficulty difficulty = Difficulty.difficultyByEmoji(event.getReactionEmote().getEmoji());
            if (event.getMessageId().equals(lastMessageID)) {
                if (!Game.isRunning) {
                    new Game(event.getChannel(), difficulty);
                }
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }
}
