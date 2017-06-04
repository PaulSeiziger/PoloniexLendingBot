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

            System.out.println(settingsHelper.getCurrencies());

//            NonceReminder reminder = new NonceReminder(Paths.get("nonce.txt"));
//
//            Properties botProperties = loadBotProperties();
//            Properties currenciesProperties = loadProperties("currencies.properties");
//            Properties strategyProperties = loadProperties("strategy.properties");
//
//            PoloniexBot bot = new PoloniexBot(botProperties, currenciesProperties, strategyProperties, reminder);
//
//            Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));
//
//            bot.start();
//
//            Scanner scanner = new Scanner(System.in);
//
//            while (scanner.hasNextLine()){
//                String line = scanner.nextLine();
//
//                if (line.equals("exit")){
//                    bot.stop();
//                    break;
//                }
//            }
        }catch (Exception ex){
            log.fatal(ex.getMessage(),ex);
        }

    }

    private static Properties loadBotProperties(){
        final String POLONIEX_KEY = "poloniex.bot.key";
        final String POLONIEX_SECRET = "poloniex.bot.secret";

        Properties properties = loadProperties("bot.properties");
        String key = System.getenv("POLONIEX_KEY");
        String secret = System.getenv("POLONIEX_SECRET");

        if (!properties.containsKey(POLONIEX_KEY)){
            if (key == null){
                throw new IllegalArgumentException("Can't find everonment variable POLONIEX_KEY");
            }
            properties.put(POLONIEX_KEY, key);
        }

        if (!properties.containsKey(POLONIEX_SECRET)){
            if (secret == null){
                throw new IllegalArgumentException("Can't find everonment variable POLONIEX_SECRET");
            }
            properties.put(POLONIEX_SECRET, secret);
        }

        return properties;
    }

    private static Properties loadProperties(String fileName) {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream input = loader.getResourceAsStream(fileName)){

            if(input != null){
                prop.load(input);
            }

        }catch (IOException ex){
            log.error(ex.getMessage(), ex);
        }

        return prop;
    }
}
