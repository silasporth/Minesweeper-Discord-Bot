package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;
import java.util.Random;

import static minesweeperdiscordbot.Game.getKeyByValue;

public class Grid {

    public static int[][] createBombGrid(int width, int height, int bombs) {
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
//        Special Thanks to https://github.com/sebschmitt for creating this Algorithm
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

    public static void setValueAtPos(int[][] grid, int x, int y, int value) {
        grid[y][x] = value;
    }

    private static int getValueAtPos(int[][] grid, int x, int y) {
        return grid[y][x];
    }

    public static String getEmojiByPos(int[][] bombGrid, int[][] currentGrid, int x, int y) {
        HashMap<String, Integer> currentGridEmojis = new HashMap<>();
        HashMap<String, Integer> bombGridEmojis = new HashMap<>();

        currentGridEmojis.put("\uD83D\uDFE9", 0);
        currentGridEmojis.put("\uD83D\uDFE6", 1);
        currentGridEmojis.put("\uD83D\uDEA9", 2);
        bombGridEmojis.put("\uD83D\uDCA3", 1);
        int hex = 0x0031;
        for (int i = 11; i < 19; i++) {
            bombGridEmojis.put(((char) (hex + i - 11) + "\u20e3"), i);
        }

        int bombGridValue = getValueAtPos(bombGrid, x, y);
        int currentGridValue = getValueAtPos(currentGrid, x, y);

        if (currentGridValue == 0 /*Panel Covered*/) {
            return getKeyByValue(currentGridEmojis, currentGridValue);
        } else if (currentGridValue == 2 /*Flag*/) {
            return getKeyByValue(currentGridEmojis, currentGridValue);
        } else if (bombGridValue == 1 /*Bomb*/) {
            return getKeyByValue(bombGridEmojis, bombGridValue);
        } else if (11 <= bombGridValue && bombGridValue <= 18 /*Bombs nearby*/) {
            return getKeyByValue(bombGridEmojis, bombGridValue);
        } else /*Nothing*/ {
            return getKeyByValue(currentGridEmojis, currentGridValue);
        }
    }

    public static void sendGrid(MessageChannel channel, StringBuilder message) {
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
}
