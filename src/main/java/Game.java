import net.dv8tion.jda.api.entities.MessageChannel;

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

//        TODO: count bombs for each panel and save as 10 - 18 in array

        return bombGrid;
    }
}
