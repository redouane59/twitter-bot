package com.socialMediaRaiser.twitter;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FollowProperties {

    private static String fileName = "config.properties";
    @Setter @Getter
    private static Map<String, String> propertiesMap = new HashMap<>();

    public static String TWEET_NAME = "customer.tweetName";
    public static String MIN_NB_FOLLOWERS = "target.minNbFollowers";
    public static String MAX_NB_FOLLOWERS = "target.maxNbFollowers";
    public static String MIN_NB_FOLLOWINGS = "target.minNbFollowings";
    public static String MAX_NB_FOLLOWINGS = "target.maxNbFollowings";
    public static String MIN_RATIO = "target.minRatio";
    public static String MAX_RATIO = "target.maxRatio";
    public static String LANGUAGE = "target.language";
    public static String MAX_DAYS_SINCE_LAST_TWEET = "target.maxDaysSinceLastTweet";
    public static String DESCRIPTION = "target.description";
    public static String LOCATION = "target.location";
    public static String MIN_PERCENT_MATCH = "target.minimumPercentMatch";
    public static String TERMS = "target.terms";
    public static String NB_BASE_FOLLOWERS = "target.nbBaseFollowers";
    public static String INFLUENCER_MIN_NB_FOLLOWERS = "influencer.minNbFollowers";
    public static String INFLUENCER_MIN_RATIO = "influencer.minRatio";
    public static String IO_SHEET_IT = "io.sheet.id";
    public static String IO_SHEET_TABNAME = "io.sheet.tabName";
    public static String IO_SHEET_RESULT_COLUMN = "io.sheet.resultColumn";
    public static String IO_SHEET_FOLLOW_DATE_INDEX = "io.sheet.followDateIndex";

    public static void init() {

        try (OutputStream output = new FileOutputStream("src/main/resources/"+fileName)) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty(TWEET_NAME, "RedouaneBali");
            prop.setProperty(MIN_NB_FOLLOWERS, "1");
            prop.setProperty(MAX_NB_FOLLOWERS, "10000");
            prop.setProperty(MIN_NB_FOLLOWINGS, "1");
            prop.setProperty(MAX_NB_FOLLOWINGS, "10000");
            prop.setProperty(MIN_RATIO, "0.5");
            prop.setProperty(MAX_RATIO, "2");
            prop.setProperty(LANGUAGE, "fr");
            prop.setProperty(MAX_DAYS_SINCE_LAST_TWEET, "15");
            prop.setProperty(DESCRIPTION, "decathlon,dev,java,tech,data,software,network,digital,prog");
            prop.setProperty(LOCATION, "");
            prop.setProperty(MIN_PERCENT_MATCH, "80");
            prop.setProperty(TERMS, "");
            prop.setProperty(NB_BASE_FOLLOWERS, "20");
            prop.setProperty(INFLUENCER_MIN_NB_FOLLOWERS, "20");
            prop.setProperty(INFLUENCER_MIN_RATIO, "0.5");
            prop.setProperty(IO_SHEET_IT, "1rpTWqHvBFaxdHcbnHmry2quQTKhPVJ-dA2n_wep0hrs");
            prop.setProperty(IO_SHEET_TABNAME, "V2");
            prop.setProperty(IO_SHEET_RESULT_COLUMN, "L");
            prop.setProperty(IO_SHEET_FOLLOW_DATE_INDEX, "10");

            // save properties to project root folder
            prop.store(output, null);

            // @todo degueu
            if(propertiesMap.size()==0){
                for (final String name: prop.stringPropertyNames())
                    propertiesMap.put(name, prop.getProperty(name));
            }
            System.out.println(propertiesMap);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String getStringProperty(String propertyName){
        if(propertiesMap.containsKey(propertyName)) {
            return propertiesMap.get(propertyName);
        } else{
            System.err.println(propertyName + " property not found");
            return "";
        }
    }

    public static int getIntProperty(String propertyName){
        if(propertiesMap.containsKey(propertyName)){
            return Integer.valueOf(propertiesMap.get(propertyName));
        } else{
            System.err.println(propertyName + " property not found");
            return -1;
        }
    }

    public static Long getLongProperty(String propertyName){
        if(propertiesMap.containsKey(propertyName)) {
            return Long.valueOf(propertiesMap.get(propertyName));
        }else{
            System.err.println(propertyName + " property not found");
            return 0L;
        }
    }

    public static Float getFloatProperty(String propertyName){
        if(propertiesMap.containsKey(propertyName)) {
            return Float.valueOf(propertiesMap.get(propertyName));
        }else{
            System.err.println(propertyName + " property not found");
            return (float)0;
        }
    }

    public static String[] getStringArrayProperty(String propertyName){
        if(propertiesMap.containsKey(propertyName)) {
            return propertiesMap.get(propertyName).split(",");
        }else{
            System.err.println(propertyName + " property not found");
            return new String[]{};
        }
    }
}