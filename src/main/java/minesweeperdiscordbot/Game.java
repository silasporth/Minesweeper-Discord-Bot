package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Game {

    public static final String positionQuestion = "What do you wanna do? Which position do you choose? (!ms mark/dig <emoji1> <emoji2>)";
    public static final HashMap<String, Integer> action = new HashMap<>();
    private final HashMap<String, Integer> emojiX = new HashMap<>();
    private final HashMap<String, Integer> emojiY = new HashMap<>();
    public boolean isRunning = false;
    public boolean isPermitted = false;
    private MessageChannel channel;
    private int width;
    private int height;
    //    Bomb = 1; 10,11,12,... = Number of Bombs in radius
    private int[][] bombGrid;
    //    Covered = 0; Uncovered = 1; Flag = 2
    private int[][] currentGrid;

    public Game() {
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void newGame(MessageChannel channel, Difficulty difficulty) {
        this.channel = channel;
        this.width = difficulty.getWidth();
        this.height = difficulty.getHeight();
        isRunning = true;
        buildHashmaps();
        bombGrid = Grid.createBombGrid(width, height, difficulty.getBombs());
        currentGrid = new int[width][height];
        StringBuilder message = buildMessage(width, height);
        Grid.sendGrid(channel, message);
        choosePosition();
    }

    public void run(MessageChannel channel, String[] input) {
        if (input.length == 1) {
            newGame(channel, Difficulty.difficultyByEmoji(input[0]));
        } else {
            switch (input[1].toLowerCase()) {
                case "play":
                    channel.sendMessage(Difficulty.difficultyQuestion).queue();
                    break;
                case "end":
                    channel.sendMessage("Ended Game.").queue();
                    isRunning = false;
                    break;
                case "mark":
                case "dig":
                    if (isPermitted) {
                        int currentAction = action.get(input[1]);
                        int x, y;
                        String emoji1;
                        String emoji2;
                        if (input.length == 3) {
                            emoji1 = input[2].substring(0, 2);
                            emoji2 = input[2].substring(2, 4);
                        } else {
                            emoji1 = input[2];
                            emoji2 = input[3];
                        }
                        try {
                            if (emojiX.containsKey(emoji1)) {
                                x = emojiX.get(emoji1);
                                y = emojiY.get(emoji2);
                            } else {
                                x = emojiX.get(emoji2);
                                y = emojiY.get(emoji1);
                            }
                            if (currentGrid[y][x] == 2) {
                                currentGrid[y][x] = 0;
                            } else {
                                Grid.setValueAtPos(currentGrid, x, y, currentAction);
                            }
                            StringBuilder message = buildMessage(width, height);
                            Grid.sendGrid(channel, message);
                            choosePosition();
                        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                            channel.sendMessage("Please input one emoji per direction!").queue();
                        }
                    } else {
                        channel.sendMessage("You're currently not permitted to use this command!").queue();
                    }
                    break;
            }
        }
    }

    private StringBuilder buildMessage(int width, int height) {
        StringBuilder message = new StringBuilder();
        for (int i = -1; i < width; i++) {
            message.append(getKeyByValue(emojiX, i));
        }
        for (int j = 0; j < height; j++) {
            message.append("\n");
            message.append(getKeyByValue(emojiY, j));
            for (int k = 0; k < width; k++)
                message.append(Grid.getEmojiByPos(bombGrid, currentGrid, k, j));
        }
        return message;
    }

    public void choosePosition() {
        channel.sendMessage(positionQuestion).queue();
    }

    private void buildHashmaps() {
        String emojiXString = "\uD83D\uDC36\uD83D\uDC31\uD83D\uDC2D\uD83D\uDC39\uD83D\uDC30\uD83E\uDD8A\uD83D\uDC3B\uD83D\uDC3C\uD83D\uDC28\uD83D\uDC2F\uD83E\uDD81\uD83D\uDC2E\uD83D\uDC37\uD83D\uDC38\uD83D\uDC35\uD83D\uDC14\uD83D\uDC27\uD83D\uDC26\uD83D\uDC24\uD83E\uDD86\uD83E\uDD85\uD83E\uDD89\uD83E\uDD87\uD83D\uDC3A\uD83D\uDC17\uD83D\uDC34\uD83D\uDC1D\uD83D\uDC1B\uD83E\uDD8B\uD83D\uDC0C";
        emojiX.put("⬛", -1);
        int count = 0;
        for (int i = 0; i < emojiXString.length(); i += 2)
            emojiX.put(emojiXString.substring(i, i + 2), count++);

        count = 0;
        String emojiYString = "\uD83C\uDF4E\uD83C\uDF50\uD83C\uDF4A\uD83C\uDF4B\uD83C\uDF4C\uD83C\uDF49\uD83C\uDF47\uD83C\uDF53\uD83C\uDF48\uD83C\uDF52\uD83C\uDF51\uD83E\uDD6D\uD83C\uDF4D\uD83E\uDD65\uD83E\uDD5D\uD83C\uDF6A";
        for (int i = 0; i < emojiYString.length(); i += 2)
            emojiY.put(emojiYString.substring(i, i + 2), count++);

        action.put("dig", 1);
        action.put("mark", 2);
    }

    public enum Difficulty {
        EASY(8, 8, 10, "\uD83C\uDDEA"),
        NORMAL(16, 16, 40, "\uD83C\uDDF3"),
        HARD(30, 16, 99, "\uD83C\uDDED");

        public static final String difficultyQuestion = "At which difficulty you wanna play? (Easy/Normal/Hard)";
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

        public static Difficulty difficultyByEmoji(String emoji) {
            for (Difficulty difficulty : values())
                if (emoji.equals(difficulty.getEmoji()))
                    return difficulty;
            throw new IllegalArgumentException("Emoji not supported");
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
