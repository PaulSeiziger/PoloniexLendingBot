package com.dremanovich.leadingbot;

import com.dremanovich.leadingbot.bot.PoloniexBot;
import com.dremanovich.leadingbot.api.NonceReminder;
import com.dremanovich.leadingbot.helpers.SettingsHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

/**
 * You must provide environment variables POLONIEX_KEY and POLONIEX_SECRET
 * File nonce.txt store counter for Poloniex API
 */
public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            System.out.println("\"Poloniex lending bot\" greatings you!");
            System.out.println("Type \"exit\" for quit from application.");
            System.out.println("\r\nShow detail information in logs.");

            SettingsHelper settingsHelper = new SettingsHelper("settings.json");

            NonceReminder reminder = new NonceReminder(Paths.get("nonce.txt"));

            PoloniexBot bot = new PoloniexBot(settingsHelper, reminder);

            Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));

            bot.start();

            Scanner scanner = new Scanner(System.in);

            while (scanner.hasNextLine()){
                String line = scanner.nextLine();

                if (line.equals("exit")){
                    bot.stop();
                    break;
                }
            }
        }catch (Exception ex){
            log.fatal(ex.getMessage(),ex);
        }

    }
}
