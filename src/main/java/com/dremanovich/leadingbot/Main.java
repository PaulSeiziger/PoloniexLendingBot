package com.dremanovich.leadingbot;

import com.dremanovich.leadingbot.bot.PoloniexBot;
import com.dremanovich.leadingbot.api.NonceReminder;
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
    public static void main(String[] args) {

        NonceReminder reminder = new NonceReminder(Paths.get("nonce.txt"));

        PoloniexBot bot = new PoloniexBot(loadBotProperties(), reminder);

        Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));

        bot.start(loadProperties("currencies.properties"));

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()){
            String line = scanner.nextLine();

            if (line.equals("exit")){
                break;
            }
        }
    }

    private static Properties loadBotProperties(){
        final String poloniexKey = "poloniex.bot.key";
        final String poloniexSecret = "poloniex.bot.secret";

        Properties properties = loadProperties("bot.properties");
        String key = System.getenv("POLONIEX_KEY");
        String secret = System.getenv("POLONIEX_SECRET");

        if (!properties.containsKey(poloniexKey)){
            if (key == null){
                throw new IllegalArgumentException("Can't find everonment variable POLONIEX_KEY");
            }
            properties.put(poloniexKey, key);
        }

        if (!properties.containsKey(poloniexSecret)){
            if (secret == null){
                throw new IllegalArgumentException("Can't find everonment variable POLONIEX_SECRET");
            }
            properties.put(poloniexSecret, secret);
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
            ex.printStackTrace();
        }

        return prop;
    }
}
