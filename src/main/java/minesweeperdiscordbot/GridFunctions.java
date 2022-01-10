package minesweeperdiscordbot;

import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static minesweeperdiscordbot.Game.bombGridEmojis;
import static minesweeperdiscordbot.Game.currentGridEmojis;

public class GridFunctions {

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * This function builds the array which contains all information where the bombs are placed
     *
     * @param width  of the grid
     * @param height of the grid
     * @param bombs  contained in the grid
     * @return new Integer 2D-Array
     */
    public static int[][] createBombGrid(int width, int height, int bombs) {
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

//      count bombs for each Tile and save as 10 - 18 in array
//      Special Thanks to https://github.com/sebschmitt for creating this Algorithm
        for (int j = 0; j < bombGrid.length; j++) /*This algorithm checks for each tile how many bombs it has as neighbors*/
            for (int k = 0; k < bombGrid[j].length; k++) {
                if (bombGrid[j][k] != 0) continue;
                int bombNumber = 10;
                /*Inline If checks if it's at the edge so there will be no error*/
                for (int l = (k == 0 ? k : k - 1); l <= (k == bombGrid[j].length - 1 ? k : k + 1); l++)
                    for (int m = (j > 0 ? j - 1 : j); m <= (j == bombGrid.length - 1 ? j : j + 1); m++)
                        if (bombGrid[m][l] == 1) bombNumber++;
                bombGrid[j][k] = bombNumber;
            }
        return bombGrid;
    }

    public static boolean exposeSquare(int[][] bombGrid, int[][] currentGrid, int x, int y) {
        int flags = 0;
        for (int i = (y == 0 ? y : y - 1); i <= (y == currentGrid.length - 1 ? y : y + 1); i++)
            for (int j = (x == 0 ? x : x - 1); j <= (x == currentGrid[i].length - 1 ? x : x + 1); j++)
                if (currentGrid[i][j] == 2) flags++;
        if (bombGrid[y][x] - 10 != flags) return false;

        for (int i = (y == 0 ? y : y - 1); i <= (y == currentGrid.length - 1 ? y : y + 1); i++)
            for (int j = (x == 0 ? x : x - 1); j <= (x == currentGrid[i].length - 1 ? x : x + 1); j++) {
                if (currentGrid[i][j] != 0) continue;
                if (bombGrid[i][j] == 1) return true;
                currentGrid[i][j] = 1;
                if (bombGrid[i][j] == 10) exposeSquare(bombGrid, currentGrid, j, i);
            }
        return false;
    }

    /**
     * Returns the correct emoji for the position by its importance
     *
     * @param bombGrid    Integer 2D-Array containing information about the bombs
     * @param currentGrid Integer 2D-Array containing information what the user can see
     * @param x           x-coordinate
     * @param y           y-coordinate
     * @return a Emoji
     */
    public static String getEmojiByPosition(int[][] bombGrid, int[][] currentGrid, int x, int y) {
        int bombGridValue = bombGrid[y][x];
        int currentGridValue = currentGrid[y][x];

        if ((currentGridValue == 0) || (currentGridValue == 2) || (currentGridValue == 3)) /*Tile Covered or Flag*/
            return getKeyByValue(currentGridEmojis, currentGridValue);
        else if ((bombGridValue == 1) || ((11 <= bombGridValue) && (bombGridValue <= 18))) /*Bomb or Bombs nearby*/
            return getKeyByValue(bombGridEmojis, bombGridValue);
        return getKeyByValue(currentGridEmojis, currentGridValue); /*Nothing*/
    }

    /**
     * This is function is needed due to Discord's limitation of used emojis
     *
     * @param hook    The position where the command was issued
     * @param message a message to process
     */
    public static void sendGrid(InteractionHook hook, String name, int flagsAvailable, StringBuilder message) {
        hook.sendMessage("Player: " + name + " \uD83D\uDEA9 " + flagsAvailable).complete();
        long count = message.chars().filter(ch -> ch == '\n').count();
        int tempCount = 0;
        StringBuilder messageToSend = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            messageToSend.append(message.substring(i, i + 1));
            if (message.substring(i, i + 1).equals("\n")) tempCount++;
            if (tempCount != 0 && ((tempCount % (count < 10 ? 3 : 6) == 0) || (i + 1 == message.length()))) {
                hook.getInteraction().getMessageChannel().sendMessage(messageToSend.toString()).complete();
                messageToSend.setLength(0);
                tempCount = 0;
            }
        }
    }
}
