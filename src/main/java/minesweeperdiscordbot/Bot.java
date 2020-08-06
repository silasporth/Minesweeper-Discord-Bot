package minesweeperdiscordbot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Bot {
    public static void main(String[] args) throws LoginException, IOException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(new String(Files.readAllBytes(Paths.get("token.txt"))));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("!minesweeper info"));
        builder.addEventListeners(new CommandListener());
        builder.build();
    }
}
