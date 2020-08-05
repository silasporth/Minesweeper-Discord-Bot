package minesweeperdiscordbot;

import minesweeperdiscordbot.Game.Difficulty;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class CommandListener extends ListenerAdapter {

    HashMap<User, Game> games = new HashMap<>();

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
                case "play":
                case "mark":
                case "dig":
                    if (!games.containsKey(event.getAuthor()))
                        games.put(event.getAuthor(), new Game());
                    games.get(event.getAuthor()).run(event.getChannel(), messageUsable);
                    break;

                case "end":
                    if (games.containsKey(event.getAuthor())) {
                        games.get(event.getAuthor()).run(event.getChannel(), messageUsable);
                        games.remove(event.getAuthor());
                    } else {
                        event.getChannel().sendMessage("There is currently no Game running belonging to you!").queue();
                    }
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
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getMember()).getUser().isBot())
            return;

        boolean reactionCommand = false;

        if (!games.get(event.getMember().getUser()).isRunning) {
            games.get(event.getMember().getUser()).run(event.getChannel(), new String[]{event.getReactionEmote().getEmoji()});
            games.get(event.getMember().getUser()).isPermitted = true;
            reactionCommand = true;
        } else {
        }

//        Choose Difficulty via Reactions while no Game is running
        if (event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor().equals(event.getJDA().getSelfUser()) && reactionCommand) {
            event.getReaction().removeReaction(Objects.requireNonNull(event.getUser())).queue();
        }
    }
}
