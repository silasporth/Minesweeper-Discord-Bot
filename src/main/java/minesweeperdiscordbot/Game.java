package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.*;

import static minesweeperdiscordbot.CommandListener.COMMAND_NAME_GRID;
import static minesweeperdiscordbot.CommandListener.COMMAND_NAME_PLAY;

public class Game {

    public static final String WIN = "\uD83C\uDF89 CONGRATULATIONS! You won! \uD83C\uDF89";
    public static final String LOSS = "\uD83D\uDCA3\uD83D\uDCA3\uD83D\uDCA3 You lost! \uD83D\uDCA3\uD83D\uDCA3\uD83D\uDCA3";
    public static final String BUTTON_ID_FLAG = "buttonFlag";
    public static final String BUTTON_ID_DIG = "buttonDig";
    public static final String BUTTON_ID_YES = "buttonYes";
    public static final String BUTTON_ID_NO = "buttonNo";
    public static final String SELECTION_ID_DIFFICULTY = "selectionDifficulty";
    public static final String SELECTION_ID_POSITION_X = "selectionPositionX";
    public static final String SELECTION_ID_POSITION_Y = "selectionPositionY";
    public static final HashMap<String, Integer> currentGridEmojis = new HashMap<>();
    public static final HashMap<String, Integer> bombGridEmojis = new HashMap<>();
    private final ArrayList<String> emojiX = new ArrayList<>();
    private final ArrayList<String> emojiY = new ArrayList<>();
    private final User user;
    private boolean usedPlayCommand = false;
    private boolean running = false;
    private boolean firstDig = true;
    private boolean wonOrLost = false;
    private String selectedPositionX = "";
    private String selectedPositionY = "";
    private String action = "";
    private InteractionHook hook;
    private Difficulty gameDifficulty;
    private int flagsAvailable;
    private int[][] bombGrid;    //    Bomb = 1; 10,11,12,... = Number of Bombs in radius
    private int[][] currentGrid;     //    Covered = 0; Uncovered = 1; Flag = 2; Wrong placed = 3
    private long lastInteractionMessageID;
    private long lastTextChannelID;


    public Game(User user) {
        this.user = user;

        final String[] emojiXArray = {"\u2B1B", "\uD83D\uDC0C", "\uD83D\uDC14", "\uD83D\uDC17", "\uD83D\uDC1B", "\uD83D\uDC1D", "\uD83D\uDC24", "\uD83D\uDC26", "\uD83D\uDC27", "\uD83D\uDC28", "\uD83D\uDC2D", "\uD83D\uDC2E", "\uD83D\uDC2F", "\uD83D\uDC30", "\uD83D\uDC31", "\uD83D\uDC34", "\uD83D\uDC35", "\uD83D\uDC36", "\uD83D\uDC37", "\uD83D\uDC38", "\uD83D\uDC39", "\uD83D\uDC3A", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83E\uDD81", "\uD83E\uDD85", "\uD83E\uDD86", "\uD83E\uDD87", "\uD83E\uDD89", "\uD83E\uDD8A", "\uD83E\uDD8B"};
        emojiX.addAll(Arrays.asList(emojiXArray));

        final String[] emojiYArray = {"\uD83C\uDF47", "\uD83C\uDF48", "\uD83C\uDF49", "\uD83C\uDF4A", "\uD83C\uDF4B", "\uD83C\uDF4C", "\uD83C\uDF4D", "\uD83C\uDF4E", "\uD83C\uDF50", "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF53", "\uD83C\uDF6A", "\uD83E\uDD5D", "\uD83E\uDD65", "\uD83E\uDD6D"};
        emojiY.addAll(Arrays.asList(emojiYArray));

        currentGridEmojis.put("\uD83D\uDFE9", 0);
        currentGridEmojis.put("\uD83D\uDFE6", 1);
        currentGridEmojis.put("\uD83D\uDEA9", 2);
        currentGridEmojis.put("❌", 3);
        bombGridEmojis.put("\uD83D\uDCA3", 1);
        int hex = 0x0031;
        for (int i = 11; i < 19; i++) {
            bombGridEmojis.put(((char) (hex + i - 11) + "\u20e3"), i);
        }
    }

    /*Sets everything up for a new game*/
    public void newGame(Difficulty difficulty, InteractionHook hook) {
        this.gameDifficulty = difficulty;
        this.flagsAvailable = difficulty.getBombs();
        this.hook = hook;
        running = true;
        currentGrid = new int[gameDifficulty.getHeight()][gameDifficulty.getWidth()];
        bombGrid = new int[gameDifficulty.getHeight()][gameDifficulty.getWidth()];
        GridFunctions.sendGrid(hook, user.getName(), flagsAvailable, buildMessage());
        choosePosition();
    }

    /*The function which runs everything*/
    public void run(InteractionHook hook, String input) {
        this.hook = hook;
        switch (input) {
            case COMMAND_NAME_PLAY -> {
                if (running) {
                    hook.sendMessage("Your game is already running! (Use `/ms grid`)").queue();
                    return;
                }
                usedPlayCommand = true;
                hook.sendMessage(Difficulty.difficultyQuestion)
                        .addActionRow(SelectionMenu.create(SELECTION_ID_DIFFICULTY)
                                .addOptions(Difficulty.getSelectionOptions())
                                .build()).queue(message -> {
                            lastInteractionMessageID = message.getIdLong();
                            lastTextChannelID = message.getTextChannel().getIdLong();
                        });
            }
            case COMMAND_NAME_GRID -> {
                GridFunctions.sendGrid(hook, user.getName(), flagsAvailable, buildMessage());
                choosePosition();
            }
            case BUTTON_ID_FLAG, BUTTON_ID_DIG -> {
                action = input;

                Collection<SelectOption> selectOptionsX = new ArrayList<>();
                for (int i = 1; i <= gameDifficulty.getWidth(); i++)
                    selectOptionsX.add(SelectOption.of("" + i, "" + i)
                            .withEmoji(Emoji.fromUnicode(emojiX.get(i))));
                Collection<SelectOption> selectOptionsY = new ArrayList<>();
                for (int i = 0; i < gameDifficulty.getHeight(); i++)
                    selectOptionsY.add(SelectOption.of("" + (i + 1), "" + (i + 1))
                            .withEmoji(Emoji.fromUnicode(emojiY.get(i))));

                hook.sendMessage("Where do you want to " + (input.equals(BUTTON_ID_FLAG) ? "place a flag?" : "dig?") + " (Width, Height)")
                        .addActionRow(SelectionMenu.create(SELECTION_ID_POSITION_X)
                                .addOptions(selectOptionsX).build())
                        .addActionRow(SelectionMenu.create(SELECTION_ID_POSITION_Y)
                                .addOptions(selectOptionsY).build()).queue(message -> {
                            lastInteractionMessageID = message.getIdLong();
                            lastTextChannelID = message.getTextChannel().getIdLong();
                        });
            }
            case SELECTION_ID_POSITION_X, SELECTION_ID_POSITION_Y -> {
                int x = emojiX.indexOf(selectedPositionX) - 1;
                int y = emojiY.indexOf(selectedPositionY);

                boolean loss = false;
                switch (action) {
                    case BUTTON_ID_FLAG -> {
                        hook.sendMessage("\uD83D\uDEA9 Placing a flag at " + selectedPositionX + " " + selectedPositionY + " \uD83D\uDEA9").queue();
                        if (currentGrid[y][x] == 0) { /*Placing Flag on Covered Tile*/
                            if (flagsAvailable > 0) {
                                flagsAvailable--;
                                currentGrid[y][x] = 2;
                            } else hook.sendMessage("You have no flags left!").queue();
                        } else if (currentGrid[y][x] == 2) { /*Removing Flag*/
                            currentGrid[y][x] = 0;
                            flagsAvailable++;
                        }
                    }
                    case BUTTON_ID_DIG -> {
                        if (firstDig) {
                            do bombGrid = GridFunctions.createBombGrid(gameDifficulty.getWidth(),
                                    gameDifficulty.getHeight(), gameDifficulty.getBombs());
                            while (bombGrid[y][x] != 10);
                        }
                        if (firstDig || currentGrid[y][x] != 2) { /*Uncover all neighbouring tiles and if a bomb gets uncovered -> game is lost*/
                            loss = GridFunctions.exposeSquare(bombGrid, currentGrid, x, y);
                            firstDig = false;
                        }

                        hook.sendMessage("\u26CF Digging at " + selectedPositionX + " " + selectedPositionY + " \u26CF").queue();
                        if (currentGrid[y][x] == 0) { /*Covered*/
                            if (bombGrid[y][x] == 1) { /*Hit bomb and lost*/
                                currentGrid[y][x] = 1;
                                loss = true;
                            } else currentGrid[y][x] = 1; /*Standard Dig Move*/
                        }
                    }
                }

                selectedPositionX = "";
                selectedPositionY = "";

                if (checkWin()) {
                    wonOrLost = true;
                    playAgain(WIN);
                    return;
                }

                if (loss) for (int i = 0; i < currentGrid.length; i++)
                    for (int j = 0; j < currentGrid[i].length; j++) {
                        if (currentGrid[i][j] == 2 && bombGrid[i][j] != 1) currentGrid[i][j] = 3;
                        if (currentGrid[i][j] == 0) currentGrid[i][j] = 1;
                    }
                GridFunctions.sendGrid(hook, user.getName(), flagsAvailable, buildMessage());
                if (loss) {
                    wonOrLost = true;
                    playAgain(LOSS);
                    return;
                }

                if (flagsAvailable == 0)
                    hook.sendMessage("*Tip: One or more flags are misplaced!*").queue();
                choosePosition();
            }
        }
    }

    private StringBuilder buildMessage() { /*This function is used to build the game as a message*/
        StringBuilder message = new StringBuilder();
        /*X-Axis*/
        for (int i = 0; i <= gameDifficulty.getWidth(); i++) message.append(emojiX.get(i));
        for (int j = 0; j < gameDifficulty.getHeight(); j++) {
            message.append("\n");
            message.append(emojiY.get(j)); /*Y-Axis*/
            for (int k = 0; k < gameDifficulty.getWidth(); k++)
                message.append(GridFunctions.getEmojiByPosition(bombGrid, currentGrid, k, j)); /*Gets the right emoji for the message*/
        }
        return message;
    }

    private boolean checkWin() {
        for (int i = 0; i < currentGrid.length; i++) /*Checks if all bombs are isolated*/
            for (int j = 0; j < currentGrid[i].length; j++)
                if ((bombGrid[i][j] != 1) && (currentGrid[i][j] == 0)) return false;

        GridFunctions.sendGrid(hook, user.getName(), flagsAvailable, buildMessage());
        running = false;
        return true;
    }

    private void choosePosition() {
        hook.sendMessage("What do you want to do?")
                .addActionRows(
                        ActionRow.of(Button.success(BUTTON_ID_DIG, "\u26CF️ Dig")),
                        ActionRow.of(Button.success(BUTTON_ID_FLAG, "\uD83D\uDEA9️ Flag"))
                ).queue(message -> {
                    lastInteractionMessageID = message.getIdLong();
                    lastTextChannelID = message.getTextChannel().getIdLong();
                });
    }

    private void playAgain(String result) {
        hook.sendMessage(result).queue();
        hook.sendMessage("Do you wanna play again?").addActionRows(
                ActionRow.of(Button.success(BUTTON_ID_YES, "✔️")),
                ActionRow.of(Button.danger(BUTTON_ID_NO, "❌"))
        ).queue(message -> {
            lastInteractionMessageID = message.getIdLong();
            lastTextChannelID = message.getTextChannel().getIdLong();
        });
    }

    public boolean usedPlayCommand() {
        return usedPlayCommand;
    }

    public boolean wonOrLost() {
        return !wonOrLost;
    }

    public String getSelectedPositionX() {
        return selectedPositionX;
    }

    public void setSelectedPositionX(String selectedPositionX) {
        this.selectedPositionX = selectedPositionX;
    }

    public String getSelectedPositionY() {
        return selectedPositionY;
    }

    public void setSelectedPositionY(String selectedPositionY) {
        this.selectedPositionY = selectedPositionY;
    }

    public String action() {
        return action;
    }

    public long getLastInteractionMessageID() {
        return lastInteractionMessageID;
    }

    public long getLastTextChannelID() {
        return lastTextChannelID;
    }

    public enum Difficulty { /*Everything needed for the difficulties*/
        EASY(8, 8, 10, "\uD83C\uDDEA"),
        NORMAL(16, 16, 40, "\uD83C\uDDF3"),
        HARD(25, 16, 82, "\uD83C\uDDED");

        public static final String difficultyQuestion = "At which difficulty you wanna play?";
        private final int width;
        private final int height;
        private final int bombs;
        private final String emoji;

        Difficulty(int width, int height, int bombs, String emoji) {
            this.width = width;
            this.height = height;
            this.bombs = bombs;
            this.emoji = emoji;
        }

        public static Difficulty difficultyByName(String name) {
            for (Difficulty difficulty : values())
                if (difficulty.name().equals(name)) return difficulty;
            throw new IllegalArgumentException("Difficulty not existing");
        }

        public static Collection<SelectOption> getSelectionOptions() {
            List<SelectOption> list = new ArrayList<>();
            for (Difficulty difficulty : Difficulty.values())
                list.add(SelectOption.of(difficulty.name(), difficulty.name())
                        .withDescription(String.format("Size: %dx%d", difficulty.getWidth(), difficulty.getHeight()))
                        .withEmoji(Emoji.fromUnicode(difficulty.getEmoji())));
            return list;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getBombs() {
            return bombs;
        }

        public String getEmoji() {
            return emoji;
        }
    }
}
