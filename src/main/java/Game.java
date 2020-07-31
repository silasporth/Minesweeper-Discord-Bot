import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Random;

public class Game {

    public static boolean isRunning = false;
    //    Nothing = 0, Bomb = 1
    private int[][] bombGrid;
    //    Covered = 0, Uncovered = 1, Flag = 2
    private int[][] currentGrid;
    public Game(MessageChannel channel, Difficulty difficulty) {
        isRunning = true;
        channel.sendMessage(difficulty.getWidth() + " " + difficulty.getHeight() + " " + difficulty.getBombs()).queue();
    }

    private int[][] createBombGrid(int width, int height, int bombs) {
        int[][] bombGrid = new int[height][width];
        int randomY;
        int randomX;
        int i = 0;
        do {
            randomY = new Random().nextInt(height);
            randomX = new Random().nextInt(width);
            if (bombGrid[randomY][randomX] != 1) {
                bombGrid[randomY][randomX] = 1;
                i++;
            }
        } while (i < bombs);

//        count bombs for each panel and save as 10 - 18 in array
//        Special Thanks to https://github.com/sebschmitt for this Algorithm
        for (int j = 0; j < bombGrid.length; j++) {
            for (int k = 0; k < bombGrid[j].length; k++) {
                if (bombGrid[j][k] != 0) continue;
                int bombNumber = 10;
                /*Inline If checks if its at the edge so there will be no error*/
                for (int l = (k == 0 ? k : k - 1); l <= (k == bombGrid[j].length - 1 ? k : k + 1); l++) {
                    for (int zettt = (j > 0 ? j - 1 : j); zettt <= (j == bombGrid.length - 1 ? j : j + 1); zettt++) {
                        if (bombGrid[zettt][l] == 1) {
                            bombNumber++;
                        }
                    }
                }
                bombGrid[j][k] = bombNumber;
            }
        }
        return bombGrid;
    }

    public enum Difficulty {
        EASY(8, 8, 10, "U+1f1ea"),
        NORMAL(16, 16, 40, "U+1f1f3"),
        HARD(16, 30, 99, "U+1f1ed");

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
