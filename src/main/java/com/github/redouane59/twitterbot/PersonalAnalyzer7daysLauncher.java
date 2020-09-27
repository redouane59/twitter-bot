package com.github.redouane59.twitterbot;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.redouane59.twitterbot.impl.PersonalAnalyzerBot7days;
import com.github.redouane59.twitterbot.impl.RankedUser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonalAnalyzer7daysLauncher {

  public static void main(String[] args) throws IOException {

    TwitterCredentials twitterCredentials = TwitterClient.OBJECT_MAPPER
        .readValue(new File("C:/Users/Perso/Documents/GitHub/twitter-credentials.json"), TwitterCredentials.class);

    if (args.length < 1) {
      LOGGER.error("missing arguments");
    } else {
      String                   userName = "MrLdM";//args[0];
      PersonalAnalyzerBot7days bot      = new PersonalAnalyzerBot7days(userName, twitterCredentials);
      List<RankedUser>         result   = bot.launch().subList(0, 10);

      for (RankedUser ru : result) {
        System.out.println(ru);
      }
    }
  }


}
