package minesweeperdiscordbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Bot {
    public static void main(String[] args) throws LoginException, IOException {
        JDABuilder builder = JDABuilder.createDefault(new String(Files.readAllBytes(Paths.get("token.txt"))));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("/ms info"));
        builder.addEventListeners(new CommandListener());
        builder.build();

        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            if (reader.readLine().contains("clear")) {
                CommandListener.games.clear();
                System.out.println("Successful\n");
            }
//          TODO: Some Terminal Commands
        }
    }
}
