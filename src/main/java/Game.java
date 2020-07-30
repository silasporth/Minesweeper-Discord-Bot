import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class Game {

    public static boolean isRunning = false;
    private int width;
    private int height;
    private int bombs;
    //    Nothing = 0, Bomb = 1
    private int[][] bombGrid;
    //    Covered = 0, Uncovered = 1, Flag = 2
    private int[][] currentGrid;

    public Game(MessageChannel channel, String difficulty) {
        isRunning = true;
        setDifficulty(difficulty);
        channel.sendMessage(width + " " + height + " " + bombs).queue();
    }

    private void setDifficulty(String difficulty) {
        String[][] difficulties = {{"easy", "8", "8", "10"}, {"normal", "16", "16", "40"}, {"hard", "16", "30", "99"}};
        for (String[] strings : difficulties) {
            if (difficulty.equals(strings[0])) {
                width = parseInt(strings[1]);
                height = parseInt(strings[2]);
                bombs = parseInt(strings[3]);
            }
        }
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
        int bombNumber = 10;
        for (int j = 0; j < bombGrid.length; j++) {
            for (int k = 0; k < bombGrid[j].length; k++) {
                if (bombGrid[j][k] == 0) {
//                    Reihe 1
                    if (j == 0) {
//                        Position 1
                        if (k == 0) {
                            for (int l : Arrays.copyOfRange(bombGrid[j], k, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k, k + 2))
                                if (l == 1) bombNumber++;
//                        Letzte Position
                        } else if (k == bombGrid[j].length - 1) {
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 1))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k - 1, k + 1))
                                if (l == 1) bombNumber++;
//                        Alle Positionen dazwischen
                        } else {
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                        }
                    }
//                    Letzte Reihe
                    else if (j == bombGrid.length - 1) {
//                        Position 1
                        if (k == 0) {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k, k + 2))
                                if (l == 1) bombNumber++;
//                        Letzte Position
                        } else if (k == bombGrid[j].length - 1) {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k - 1, k + 1))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 1))
                                if (l == 1) bombNumber++;
//                        Alle Positionen dazwischen
                        } else {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                        }
                    }
//                    Alle Reihen dazwischen
                    else {
//                        Position 1
                        if (k == 0) {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k, k + 2))
                                if (l == 1) bombNumber++;
//                        Letzte Position
                        } else if (k == bombGrid[j].length - 1) {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k - 1, k + 1))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 1))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k - 1, k + 1))
                                if (l == 1) bombNumber++;
//                        Alle Positionen dazwischen
                        } else {
                            for (int l : Arrays.copyOfRange(bombGrid[j - 1], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                            for (int l : Arrays.copyOfRange(bombGrid[j + 1], k - 1, k + 2))
                                if (l == 1) bombNumber++;
                        }
                    }
                    bombGrid[j][k] = bombNumber;
                }
                bombNumber = 10;
            }
        }
        return bombGrid;
    }
}
