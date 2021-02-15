package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static minesweeperdiscordbot.Game.getKeyByValue;

public class Grid {

    public static int[][] createBombGrid(int width, int height, int bombs) { /*This function builds the array which contains all information where the bombs are placed*/
        int[][] bombGrid = new int[height][width];
        int randomY;
        int randomX;
        int i = 0;
        do { /*Randomly places all bombs*/
            randomY = new Random().nextInt(height);
            randomX = new Random().nextInt(width);
            if (bombGrid[randomY][randomX] != 1) {
                bombGrid[randomY][randomX] = 1;
                i++;
            }
        } while (i < bombs);

//        count bombs for each Tile and save as 10 - 18 in array
//        Special Thanks to https://github.com/sebschmitt for creating this Algorithm
        for (int j = 0; j < bombGrid.length; j++) { /*This algorithm checks for each tile how many bombs it has as neighbors*/
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

    public static void exposeEmptySquares(int[][] bombGrid, int[][] currentGrid, int x, int y) { /*Auto-Uncover of tiles with no bombs as neighbours*/
        ArrayList<int[]> emptyTiles1 = new ArrayList<>();
        ArrayList<int[]> emptyTiles2;
        int[] firstXY = {x, y};
        emptyTiles1.add(firstXY);
        while (!emptyTiles1.isEmpty()) { /*Automatically adds neighboring tiles with no neighboring bombs to the list which is worked through*/
            emptyTiles2 = new ArrayList<>(emptyTiles1);
            for (int[] coords : emptyTiles2) {
                x = coords[0];
                y = coords[1];
                currentGrid[y][x] = 1;
                emptyTiles1.remove(coords);
                for (int j = (y == 0 ? y : y - 1); j <= (y == bombGrid.length - 1 ? y : y + 1); j++) {
                    for (int i = (x == 0 ? x : x - 1); i <= (x == bombGrid[j].length - 1 ? x : x + 1); i++) {
                        if (bombGrid[j][i] == 10 && currentGrid[j][i] == 0) {
                            emptyTiles1.add(new int[]{i, j});
                        } else {
                            currentGrid[j][i] = 1;
                        }
                    }
                }
            }
        }
    }

    public static boolean exposeSquare(int[][] bombGrid, int[][] currentGrid, int x, int y) { /*Uncover all neighbouring tiles and if an bomb gets uncovered returns true at the end*/
        boolean loose = false;
        ArrayList<int[]> coords = new ArrayList<>();
        for (int j = (y == 0 ? y : y - 1); j <= (y == currentGrid.length - 1 ? y : y + 1); j++) {
            for (int i = (x == 0 ? x : x - 1); i <= (x == currentGrid[j].length - 1 ? x : x + 1); i++) {
                if (currentGrid[j][i] == 0) /*Uncover the tile*/
                    currentGrid[j][i] = 1;
                if (bombGrid[j][i] == 10) /*If tile has no bomb neighbour add to list*/
                    coords.add(new int[]{i, j});
                if (bombGrid[j][i] == 1 && currentGrid[j][i] != 2) {
                    loose = true;
                }
            }
        }
        for (int[] k : coords) /*Empty all tiles around it*/
            if (currentGrid[k[1]][k[0]] != 1)
                exposeEmptySquares(bombGrid, currentGrid, k[0], k[1]);
        return loose;
    }

    public static void setValueAtPos(int[][] grid, int x, int y, int value) {
        grid[y][x] = value;
    }

    public static String getEmojiByPos(int[][] bombGrid, int[][] currentGrid, int x, int y) { /*Returns the correct emoji for the position by its importance*/
        HashMap<String, Integer> currentGridEmojis = new HashMap<>();
        HashMap<String, Integer> bombGridEmojis = new HashMap<>();

        currentGridEmojis.put("\uD83D\uDFE9", 0);
        currentGridEmojis.put("\uD83D\uDFE6", 1);
        currentGridEmojis.put("\uD83D\uDEA9", 2);
        currentGridEmojis.put("❌", 3);
        bombGridEmojis.put("\uD83D\uDCA3", 1);
        int hex = 0x0031;
        for (int i = 11; i < 19; i++) {
            bombGridEmojis.put(((char) (hex + i - 11) + "\u20e3"), i);
        }

        int bombGridValue = bombGrid[y][x];
        int currentGridValue = currentGrid[y][x];

        if (currentGridValue == 0 || currentGridValue == 2 || currentGridValue == 3) { /*Tile Covered or Flag*/
            return getKeyByValue(currentGridEmojis, currentGridValue);
        } else if (bombGridValue == 1 || (11 <= bombGridValue && bombGridValue <= 18)) { /*Bomb or Bombs nearby*/
            return getKeyByValue(bombGridEmojis, bombGridValue);
        } else { /*Nothing*/
            return getKeyByValue(currentGridEmojis, currentGridValue);
        }
    }

    public static void sendGrid(MessageChannel channel, StringBuilder message) { /*This is function is needed due to Discord's limitation of used emojis*/
        long count = message.chars().filter(ch -> ch == '\n').count();
        int linesAmount;
        if (count < 10) /*Switches between two modes of how many lines are printed per message*/
            linesAmount = 3;
        else linesAmount = 6;
        int tempCount = 0;
        StringBuilder messageToSend = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            messageToSend.append(message.substring(i, i + 1));
            if (message.substring(i, i + 1).equals("\n"))
                tempCount++;
            if (tempCount != 0 && tempCount % linesAmount == 0) {
                channel.sendMessage(messageToSend).queue();
                messageToSend.setLength(0);
                tempCount = 0;
            }
        }
        channel.sendMessage(messageToSend).queue();
    }
}
