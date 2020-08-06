package minesweeperdiscordbot;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Game {

    public static final String positionQuestion = "What do you wanna do? Which position do you choose? (!ms mark/dig <emoji1> <emoji2>)";
    public static final String win = "\uD83C\uDF89 CONGRATULATIONS! You won! \uD83C\uDF89";
    public static final String loss = "You lost!";
    public static final HashMap<String, Integer> action = new HashMap<>();
    private final HashMap<String, Integer> emojiX = new HashMap<>();
    private final HashMap<String, Integer> emojiY = new HashMap<>();
    private final User user;
    public boolean isRunning = false;
    public boolean isPermitted = false;
    private MessageChannel channel;
    private int width;
    private int height;
    private int bombs;
    private int flagsAvailable;
    //    Bomb = 1; 10,11,12,... = Number of Bombs in radius
    private int[][] bombGrid;
    //    Covered = 0; Uncovered = 1; Flag = 2
    private int[][] currentGrid;
    private boolean isFirstTime = true;

    public Game(User user) {
        this.user = user;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /*Sets everything up for a new game*/
    private void newGame(MessageChannel channel, Difficulty difficulty) {
        this.channel = channel;
        this.width = difficulty.getWidth();
        this.height = difficulty.getHeight();
        this.bombs = difficulty.getBombs();
        this.flagsAvailable = difficulty.getBombs();
        isRunning = true;
        buildHashmaps();
        currentGrid = new int[height][width];
        bombGrid = new int[height][width];
        StringBuilder message = buildMessage(width, height);
        channel.sendMessage("Player: " + user.getName() + " \uD83D\uDEA9 " + flagsAvailable).queue();
        Grid.sendGrid(channel, message);
        choosePosition();
    }

    /*The function which runs everything*/
    public void run(MessageChannel channel, String[] input) {
        if (input.length == 1) { /*Gets emoji as input and then starts a new game with the corresponding difficulty*/
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
                case "flag":
                case "dig":
                    if (isPermitted) { /*The commands only do something if they are unlocked*/
                        int currentAction = action.get(input[1]);
                        int x, y;
                        String emoji1;
                        String emoji2;
                        if (input.length == 3) { /*Checks if the emojis have a space between each other*/
                            emoji1 = input[2].substring(0, 2);
                            emoji2 = input[2].substring(2, 4);
                        } else {
                            emoji1 = input[2];
                            emoji2 = input[3];
                        }
                        try {
                            if (emojiX.containsKey(emoji1)) { /*It doesnt matter which emoji of what direction is first written*/
                                x = emojiX.get(emoji1);
                                y = emojiY.get(emoji2);
                            } else {
                                x = emojiX.get(emoji2);
                                y = emojiY.get(emoji1);
                            }

                            if (isFirstTime && currentAction == 1) { /*It is not possible to loose at the first move*/
                                do {
                                    bombGrid = Grid.createBombGrid(width, height, bombs);
                                } while (bombGrid[y][x] != 10);
                                isFirstTime = false;
                            }

                            boolean loose = false;

                            if (currentAction == 1) { /*Dig*/
                                if (currentGrid[y][x] == 0) { /*Covered*/
                                    if (bombGrid[y][x] == 10) { /*Auto-Uncover of tiles with no bombs as neighbours*/
                                        Grid.exposeEmptySquares(bombGrid, currentGrid, x, y);
                                    } else if (bombGrid[y][x] == 1) { /*Hit bomb and lost*/
                                        currentGrid[y][x] = 1;
                                        loose = true;
                                    } else { /*Standard Dig Move*/
                                        Grid.setValueAtPos(currentGrid, x, y, currentAction);
                                    }
                                } else if (currentGrid[y][x] == 1) { /*Uncover all neighbouring tiles and if an bomb gets uncovered -> lost*/
                                    loose = Grid.exposeSquare(bombGrid, currentGrid, x, y);
                                }
                            } else if (currentAction == 2) { /*Flag*/
                                if (currentGrid[y][x] == 0) { /*Placing Flag on Covered Tile*/
                                    flagsAvailable--;
                                    Grid.setValueAtPos(currentGrid, x, y, currentAction);
                                } else if (currentGrid[y][x] == 2) { /*Removing Flag*/
                                    currentGrid[y][x] = 0;
                                    flagsAvailable++;
                                }
                            }
                            if (flagsAvailable > 0) { /*Prints Game*/
                                StringBuilder message = buildMessage(width, height);
                                channel.sendMessage("Player: " + user.getName() + " \uD83D\uDEA9 " + flagsAvailable).queue();
                                Grid.sendGrid(channel, message);
                                if (!loose) { /*Lets the player choose his next Tile*/
                                    choosePosition();
                                } else { /*Lost the Game*/
                                    channel.sendMessage(loss).queue();
                                    isRunning = false;
                                }
                            } else { /*Checks if the game is won only if all flags are placed*/
                                checkWin();
                            }
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

    private void checkWin() {
        boolean broke = false;
        for (int i = 0; i < currentGrid.length; i++) { /*Checks if all bombs are flagged*/
            for (int j = 0; j < currentGrid[i].length; j++) {
                if (bombGrid[i][j] == 1 && currentGrid[i][j] != 2) {
                    broke = true;
                    break;
                }
            }
            if (broke) break;
        }
        if (!broke) { /*Runs only if truly all bombs are flagged.*/
            channel.sendMessage(win).queue();
            isRunning = false;
            for (int i = 0; i < currentGrid.length; i++) { /*Sets all remaining covered tiles uncovered*/
                for (int i1 = 0; i1 < currentGrid[i].length; i1++) {
                    if (currentGrid[i][i1] == 0)
                        currentGrid[i][i1] = 1;
                }
            } /*Prints the Game one last time*/
            StringBuilder message = buildMessage(width, height);
            channel.sendMessage("Player: " + user.getName() + " \uD83D\uDEA9 " + flagsAvailable).queue();
            Grid.sendGrid(channel, message);
        }
    }

    private StringBuilder buildMessage(int width, int height) { /*This function is used to build the game as a message*/
        StringBuilder message = new StringBuilder();
        for (int i = -1; i < width; i++) { /*X-Axis*/
            message.append(getKeyByValue(emojiX, i));
        }
        for (int j = 0; j < height; j++) {
            message.append("\n");
            message.append(getKeyByValue(emojiY, j)); /*Y-Axis*/
            for (int k = 0; k < width; k++)
                message.append(Grid.getEmojiByPos(bombGrid, currentGrid, k, j)); /*Gets the right emoji for the message*/
        }
        return message;
    }

    public void choosePosition() {
        channel.sendMessage(positionQuestion).queue();
    }

    private void buildHashmaps() { /*This builds the Hashmaps so every other function can use them*/
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

    public enum Difficulty { /*Everything needed for the difficulties*/
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
