package minesweeperdiscordbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import static minesweeperdiscordbot.Game.*;

public class CommandListener extends ListenerAdapter {

    public static final HashMap<User, Game> games = new HashMap<>();
    public static final String COMMAND_NAME_INFO = "info";
    public static final String COMMAND_NAME_PLAY = "play";
    public static final String COMMAND_NAME_END = "end";
    public static final String COMMAND_NAME_GRID = "grid";


    private final String NOT_EXISTING = "You have no game running!";
    private final EmbedBuilder embed = new EmbedBuilder();

    public CommandListener() {
        embed.setTitle("Minesweeper Discord Bot");
        embed.setDescription("You can play Minesweeper at three difficulties with this bot!");
        embed.addField("General Command", "/ms <control>", false);
        embed.addField("Controls", "play, end, flag, dig", false);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        CommandListUpdateAction commands = event.getJDA().updateCommands();
        commands.addCommands(new CommandData("ms", "Info")
                .addSubcommands(new SubcommandData(COMMAND_NAME_INFO, "Info"))
                .addSubcommands(new SubcommandData(COMMAND_NAME_PLAY, "Start a game"))
                .addSubcommands(new SubcommandData(COMMAND_NAME_END, "Stop the game"))
                .addSubcommands(new SubcommandData(COMMAND_NAME_GRID, "See the grid again"))).queue();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        super.onSlashCommand(event);
        if (event.getName().equals("ms")) {
            event.deferReply().queue();
            User issuer = event.getUser();

            if (!games.containsKey(issuer) && !Objects.requireNonNull(event.getSubcommandName()).equals(COMMAND_NAME_INFO) && !Objects.requireNonNull(event.getSubcommandName()).equals(COMMAND_NAME_PLAY)) {
                event.getHook().sendMessage(NOT_EXISTING).queue();
                return;
            }

            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case COMMAND_NAME_INFO -> event.getHook().sendMessageEmbeds(embed.build()).queue();
                case COMMAND_NAME_PLAY -> {
                    games.put(issuer, new Game(issuer));
                    games.get(issuer).run(event.getHook(), event.getSubcommandName());
                }
                case COMMAND_NAME_END -> {
                    event.getHook().sendMessage("Ended Game.").queue();
                    games.remove(issuer);
                }
                case COMMAND_NAME_GRID -> games.get(issuer).run(event.getHook(), event.getSubcommandName());
            }
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        super.onButtonClick(event);
        event.deferReply().queue();
        User issuer = event.getUser();
        if (!games.containsKey(issuer)) {
            event.getHook().sendMessage(NOT_EXISTING).queue();
            return;
        }

        Game game = games.get(issuer);
        String id = "";
        try {
            id = Objects.requireNonNull(Objects.requireNonNull(event.getButton()).getId());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            Objects.requireNonNull(event.getJDA().getTextChannelById(game.getLastTextChannelID()))
                    .deleteMessageById(game.getLastInteractionMessageID()).queue();
        } catch (NullPointerException e) {
            System.out.println("Can't delete Message in Text Channel " + game.getLastTextChannelID());
        }
        switch (id) {
            case BUTTON_ID_FLAG, BUTTON_ID_DIG -> game.run(event.getHook(), id);
            case BUTTON_ID_YES -> {
                if (game.wonOrLost()) {
                    event.getHook().sendMessage("This is not your decision " + event.getUser().getName() + "!").queue();
                    return;
                }
                games.replace(issuer, new Game(issuer));
                games.get(issuer).run(event.getHook(), COMMAND_NAME_PLAY);
            }
            case BUTTON_ID_NO -> {
                if (game.wonOrLost()) {
                    event.getHook().sendMessage("This is not your decision " + event.getUser().getName() + "!").queue();
                    return;
                }
                games.remove(event.getUser());
                event.getHook().sendMessage("Thanks for Playing!").queue();
            }
        }
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        super.onSelectionMenu(event);
        event.deferReply().queue();
        if (!games.containsKey(event.getUser())) {
            event.getHook().sendMessage(NOT_EXISTING).queue();
            return;
        }

        Game game = games.get(event.getUser());
        String id = "";
        try {
            id = Objects.requireNonNull(Objects.requireNonNull(event.getSelectionMenu()).getId());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        switch (id) {
            case SELECTION_ID_DIFFICULTY -> {
                if (!game.usedPlayCommand()) {
                    event.getHook().sendMessage("This is not your decision " + event.getUser().getName() + "!").queue();
                    return;
                }
                game.newGame(Difficulty.difficultyByName(event.getInteraction()
                        .getValues().get(0)), event.getHook());
                try {
                    Objects.requireNonNull(event.getJDA().getTextChannelById(game.getLastTextChannelID()))
                            .deleteMessageById(game.getLastInteractionMessageID()).queue();
                } catch (NullPointerException e) {
                    System.out.println("Can't delete Message in Text Channel " + game.getLastTextChannelID());
                }
            }
            case SELECTION_ID_POSITION_X, SELECTION_ID_POSITION_Y -> {
                if (game.action().isEmpty()) {
                    event.getHook().sendMessage("This is not your decision " + event.getUser().getName() + "!").queue();
                    return;
                }

                if (id.equals(SELECTION_ID_POSITION_X))
                    game.setSelectedPositionX(event.getInteraction().getSelectedOptions().get(0).getEmoji().getName());
                else
                    game.setSelectedPositionY(event.getInteraction().getSelectedOptions().get(0).getEmoji().getName());
                event.getHook().sendMessage("Processing...").queue(message -> message.delete().queue());
                if (!game.getSelectedPositionX().isEmpty() && !game.getSelectedPositionY().isEmpty()) {
                    try {
                        Objects.requireNonNull(event.getJDA().getTextChannelById(game.getLastTextChannelID()))
                                .deleteMessageById(game.getLastInteractionMessageID()).queue();
                    } catch (NullPointerException e) {
                        System.out.println("Can't delete Message in Text Channel " + game.getLastTextChannelID());
                    }
                    game.run(event.getHook(), id);
                }
            }
        }
    }
}
