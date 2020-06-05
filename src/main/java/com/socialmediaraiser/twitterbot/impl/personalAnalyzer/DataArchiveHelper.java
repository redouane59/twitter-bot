package com.socialmediaraiser.twitterbot.impl.personalAnalyzer;

import com.socialmediaraiser.twitter.dto.tweet.ITweet;
import com.socialmediaraiser.twitter.dto.tweet.TweetDTOv1;
import com.socialmediaraiser.twitter.dto.tweet.TweetType;
import io.vavr.collection.Stream;
import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.util.*;
import io.vavr.collection.Map;

@CustomLog
public class DataArchiveHelper extends AbstractSearchHelper {

    private List<TweetDTOv1> tweets = new ArrayList<>();

    public DataArchiveHelper(String userName, String archiveFileName, Date initDate) {
        super(userName);
        File file = new File(getClass().getClassLoader().getResource(archiveFileName).getFile());
        List<TweetDTOv1> allTweets = null;
        try {
            allTweets = this.getTwitterClient().readTwitterDataFile(file);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        for(TweetDTOv1 tweet : allTweets){
            Date tweetDate = tweet.getCreatedAt();
            if(tweetDate!=null && tweetDate.compareTo(initDate)>0){
                this.tweets.add(tweet);
            }
        }
    }

    public void countRepliesGiven(UserInteractions userInteractions){
        LOGGER.info("\ncounting replies from user (archive)...");
        Set<String> answeredByUserTweets = new HashSet<>();
        int repliesGiven = 0;
        for(TweetDTOv1 tweet : tweets){ // @todo check for retweets + exclude own answers
            // checking the reply I gave to other users
            String inReplyUserId = tweet.getInReplyToUserId();
            if(inReplyUserId!=null && this.isUserInList(tweet.getInReplyToUserId())) {
                repliesGiven++;
                ITweet initialTweet = this.getTwitterClient().getInitialTweet(tweet, true);
                if(!this.getUserId().equals(initialTweet.getAuthorId()) && !answeredByUserTweets.contains(initialTweet.getId())) {
                    System.out.print(".");
                    UserInteractions.UserInteraction userInteraction = userInteractions.get(inReplyUserId);
                    userInteraction.incrementNbRepliesGiven();
                    answeredByUserTweets.add(initialTweet.getId());
                }
            }
        }
        LOGGER.info(repliesGiven + " replies given found, " + answeredByUserTweets.size() + " replies given saved");
    }

    public void countRetweetsReceived(UserInteractions userInteractions){
        LOGGER.info("\ncounting retweets received (archive)...");
        int rtCount = 0;
        for(TweetDTOv1 tweet : this.filterTweetsByRetweet(false)){
            if(tweet.getRetweetCount()>0 && !tweet.getText().startsWith(("@"))){
                this.countRetweetsOfTweet(tweet, userInteractions);
                rtCount++;
            }
        }
        LOGGER.info(rtCount + " retweets received found");
    }

    private void countRetweetsOfTweet(TweetDTOv1 tweet, UserInteractions userInteractions){
        List<String> retweeterIds = this.getTwitterClient().getRetweetersId(tweet.getId());
        LOGGER.info("counting " + retweeterIds.size() + " retweeters of tweet " + tweet.getId());
        for(String retweeterId : retweeterIds){
            if(this.isUserInList(retweeterId)){
                UserInteractions.UserInteraction userInteraction = userInteractions.get(retweeterId);
                userInteraction.incrementNbRetweetsReceived();
            }
        }
    }

    // new

    public Map<String, TweetInteraction> countRetweetsReceived(){

        LOGGER.info("\ncounting retweets received (archive)...");

        return Stream.ofAll(filterTweetsByRetweet(false))
                     .filter(tweet -> tweet.getRetweetCount()>0 && !tweet.getText().startsWith(("@")))
                     .toMap(TweetDTOv1::getId,
                            this::countRetweetsOfTweet);
    }

    private TweetInteraction countRetweetsOfTweet(TweetDTOv1 tweet){
        TweetInteraction result = new TweetInteraction();
        List<String> retweeterIds = this.getTwitterClient().getRetweetersId(tweet.getId());
        LOGGER.info("counting " + retweeterIds.size() + " retweeters of tweet " + tweet.getId());
        for(String retweeterId : retweeterIds){
            if(this.isUserInList(retweeterId)){
                result.getRetweeterIds().add(retweeterId);
            }
        }
        return result;
    }
    // end new
    public void countRetweetsGiven(UserInteractions userInteractions){
        LOGGER.info("\ncounting retweets given (archive)...");
        List<TweetDTOv1> retweets = this.filterTweetsByRetweet(true);
        int rtCount = 0;
        for(TweetDTOv1 tweet : retweets){
            ITweet fullTweet = this.getTwitterClient().getTweet(tweet.getId());
            String retweetedTweetId = fullTweet.getInReplyToStatusId(TweetType.RETWEETED);
            if(retweetedTweetId!=null){
                ITweet retweetedTweet = this.getTwitterClient().getTweet(retweetedTweetId); // @todo check null
                String userId = retweetedTweet.getAuthorId(); // because info missing in data archive
                if(this.isUserInList(userId)){
                    UserInteractions.UserInteraction userInteraction = userInteractions.get(userId);
                    userInteraction.incrementNbRetweetsGiven();
                    rtCount++;
                }
            }
        }
        LOGGER.info(rtCount + " retweets given found");

    }

    // @todo add mentions argument
    public List<TweetDTOv1> filterTweetsByRetweet(boolean onlyRetweets){
        List<TweetDTOv1> result = new ArrayList<>();
        for(TweetDTOv1 tweet : this.tweets){
            if(tweet.getText().startsWith("RT @") == onlyRetweets){
                result.add(tweet);
            }
        }
        return result;
    }
}
