package minesweeperdiscordbot;

import minesweeperdiscordbot.Game.Difficulty;
import net.dv8tion.jda.api.EmbedBuilder;
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
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Minesweeper Discord Bot");
                    embed.setDescription("You can play Minesweeper at three difficulties with this bot.");
                    embed.addField("General Command", "!minesweeper/!ms <control>", false);
                    embed.addField("Controls", "play, end, flag, dig", false);
                    event.getChannel().sendMessage(embed.build()).queue();
                    break;
                }
                case "play":
                case "flag":
                case "dig":
                    if (!games.containsKey(event.getAuthor()))
                        games.put(event.getAuthor(), new Game(event.getAuthor()));
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

        if (event.getMessage().getAuthor().isBot()) {
            if (message.equals(Difficulty.difficultyQuestion)) {
                for (Difficulty difficulty : Difficulty.values())
                    event.getMessage().addReaction(difficulty.getEmoji()).queue();
            } else if (message.equals(Game.win) || message.equals(Game.loss)) {
                event.getChannel().sendMessage("Do you wanna play again?").queue(message1 -> {
                    message1.addReaction("\uD83C\uDDFE").queue();
                    message1.addReaction("\uD83C\uDDF3").queue();
                });
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getMember()).getUser().isBot())
            return;

        boolean reactionCommand = false;
        String emoji = event.getReactionEmote().getEmoji();

        if (!games.get(event.getMember().getUser()).isRunning) {
            if (emoji.equals("\uD83C\uDDF3")) { /*N-Emoji*/
                games.remove(event.getMember().getUser());
                event.getChannel().sendMessage("Thanks for Playing!").queue();
            } else if (emoji.equals("\uD83C\uDDFE")) { /*Y-Emoji*/
                games.remove(event.getMember().getUser());
                games.put(event.getMember().getUser(), new Game(event.getMember().getUser()));
                games.get(event.getMember().getUser()).run(event.getChannel(), new String[]{"!ms", "play"});
            } else { /*Every other Emoji*/
                games.get(event.getMember().getUser()).run(event.getChannel(), new String[]{emoji});
                games.get(event.getMember().getUser()).isPermitted = true;
            }
            reactionCommand = true;
        }

        if (event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor().equals(event.getJDA().getSelfUser()) && reactionCommand) { /*Auto-Remove of user-added reactions*/
            event.getReaction().removeReaction(Objects.requireNonNull(event.getUser())).queue();
        }
    }
}
