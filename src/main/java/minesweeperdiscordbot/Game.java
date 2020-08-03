package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Game {

    public static boolean isRunning = false;
    //    Nothing = 0, Bomb = 1
    private int[][] bombGrid;
    //    Covered = 0, Uncovered = 1, Flag = 2
    private final int[][] currentGrid;

    private final HashMap<String, Integer> emojiX = new HashMap<>();
    private final HashMap<String, Integer> emojiY = new HashMap<>();

    public Game(MessageChannel channel, Difficulty difficulty) {
        isRunning = true;
        buildHashmaps();
        channel.sendMessage(difficulty.getWidth() + " " + difficulty.getHeight() + " " + difficulty.getBombs()).queue();
        currentGrid = new int[difficulty.getHeight()][difficulty.getWidth()];
        StringBuilder message = buildMessage(difficulty.getWidth(), difficulty.getHeight());
        sendMessage(channel, message);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
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
                message.append(Grid.getEmojiByPos(currentGrid, k, j));
        }
        return message;
    }

    private void sendMessage(MessageChannel channel, StringBuilder message) {
        int count = 0;
        StringBuilder messageToSend = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            messageToSend.append(message.substring(i, i + 1));
            if (message.substring(i, i + 1).equals("\n"))
                count++;
            if (count != 0 && count % 3 == 0) {
                channel.sendMessage(messageToSend).queue();
                messageToSend.setLength(0);
                count = 0;
            }
        }
        channel.sendMessage(messageToSend).queue();
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
