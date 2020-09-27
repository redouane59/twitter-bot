package com.github.redouane59.twitterbot.impl;

import com.github.redouane59.twitter.helpers.ConverterHelper;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class PersonalAnalyzerBot30days extends AbstractPersonalAnalyzerBot {

  private final LocalDateTime initDate = ConverterHelper.dayBeforeNow(7).truncatedTo(ChronoUnit.DAYS).plusHours(1);

  public PersonalAnalyzerBot30days(final String userName, TwitterCredentials twitterCredentials) {
    super(userName, twitterCredentials);
  }

  @Override
  protected Map<String, UserInteraction> countLikesGiven() {
    // @todo impossible ot to have 7 days likes
    return this.getApiSearchHelper().countGivenLikesOnStatuses();
  }

  @Override
  protected Map<String, UserInteraction> countRepliesGiven() {
    Map<String, UserInteraction> currentWeekTweets = this.getApiSearchHelper().countRepliesGiven(true);
    Map<String, UserInteraction> monthlyTweets     = this.getApiSearchHelper().countRepliesGiven(false);
    return currentWeekTweets.merge(monthlyTweets, UserInteraction::merge);
  }

  @Override
  protected Map<String, UserInteraction> countQuotesGiven() {
    Map<String, UserInteraction> currentWeekTweets = this.getApiSearchHelper().countQuotesGiven(true);
    Map<String, UserInteraction> monthlyTweets     = this.getApiSearchHelper().countQuotesGiven(false);
    return currentWeekTweets.merge(monthlyTweets, UserInteraction::merge);
  }

  @Override
  protected Map<String, UserInteraction> countRetweetsGiven() {
    Map<String, UserInteraction> currentWeekTweets = this.getApiSearchHelper().countRecentRetweetsGiven(true);
    Map<String, UserInteraction> monthlyTweets     = this.getApiSearchHelper().countRecentRetweetsGiven(false);
    return currentWeekTweets.merge(monthlyTweets, UserInteraction::merge);
  }

  @Override
  protected Map<String, TweetInteraction> countLikesReceived() {
    LOGGER.error("not implemented");
    return HashMap.empty();
  }

  @Override
  protected Map<String, TweetInteraction> countRepliesReceived() {
    Map<String, TweetInteraction> currentWeekTweets = getApiSearchHelper().countRepliesReceived(true);
    Map<String, TweetInteraction> monthlyTweets     = getApiSearchHelper().countRepliesReceived(false);
    return currentWeekTweets.merge(monthlyTweets, TweetInteraction::merge);
  }

  @Override
  protected Map<String, TweetInteraction> countQuotesReceived() {
    Map<String, TweetInteraction> currentWeekTweets = getApiSearchHelper().countQuotesReceived(true);
    Map<String, TweetInteraction> monthlyTweets     = getApiSearchHelper().countQuotesReceived(false);
    return currentWeekTweets.merge(monthlyTweets, TweetInteraction::merge);
  }

  @Override
  protected Map<String, TweetInteraction> countRetweetsReceived() {
    Map<String, TweetInteraction> currentWeekTweets = this.getApiSearchHelper().countRecentRetweetsReceived(true);
    Map<String, TweetInteraction> monthlyTweets     = this.getApiSearchHelper().countRecentRetweetsReceived(false);
    return currentWeekTweets.merge(monthlyTweets, TweetInteraction::merge);
  }

}
