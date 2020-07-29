import net.dv8tion.jda.api.entities.MessageChannel;

import static java.lang.Integer.parseInt;

public class Game {

    public static boolean isRunning = false;
    private int width;
    private int height;
    private int bombs;

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
}
