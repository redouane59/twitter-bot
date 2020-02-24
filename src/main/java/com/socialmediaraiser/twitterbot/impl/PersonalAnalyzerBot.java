package com.socialmediaraiser.twitterbot.impl;

import com.socialmediaraiser.twitter.TwitterClient;
import com.socialmediaraiser.twitter.dto.user.IUser;
import com.socialmediaraiser.twitter.helpers.ConverterHelper;
import com.socialmediaraiser.twitter.dto.tweet.ITweet;
import com.socialmediaraiser.twitter.dto.tweet.TweetDataDTO;
import com.socialmediaraiser.twitterbot.AbstractIOHelper;
import com.socialmediaraiser.twitterbot.GoogleSheetHelper;
import com.socialmediaraiser.twitterbot.PersonalAnalyzerLauncher;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
@Setter
public class PersonalAnalyzerBot {

    private String userName;
    private static final Logger LOGGER = Logger.getLogger(PersonalAnalyzerLauncher.class.getName());
    private AbstractIOHelper ioHelper;
    private TwitterClient twitterClient = new TwitterClient();
    private final Date initRepliesDate = ConverterHelper.dayBeforeNow(30);
    private final Date initRetweetsDate = ConverterHelper.dayBeforeNow(180);

    public PersonalAnalyzerBot(String userName){
        this.userName = userName;
        this.ioHelper = new GoogleSheetHelper(userName);
    }

    public void launch(boolean showFollowers, boolean showFollowings) throws IOException, InterruptedException {
        UserInteractions interactions = this.getNbInterractions();
        String userId = this.twitterClient.getUserFromUserName(userName).getId();
        List<IUser> followings = this.twitterClient.getFollowingUsers(userId);
        List<IUser> followers = this.twitterClient.getFollowerUsers(userId);
        Set<IUser> allUsers = new HashSet<>() {
            {
                addAll(followings);
                addAll(followers);
            } };
        for(IUser iUser : allUsers){
            if(hasToAddUser(iUser, followings, followers, showFollowings, showFollowers)){
                User user = new User(iUser);
                user.setNbRepliesFrom(interactions.get(iUser.getId()).getNbRepliesFrom());
                user.setNbRepliesTo(interactions.get(iUser.getId()).getNbRepliesTo());
                user.setNbRetweets(interactions.get(iUser.getId()).getNbRetweets());
                this.ioHelper.addNewFollowerLineSimple(user);
                TimeUnit.MILLISECONDS.sleep(600); // @todo manage the limit better with Less calls but bigger objects
                LOGGER.info("adding " + iUser.getName() + "...");
            }
        }
        LOGGER.info("finish with success");
    }

    private boolean hasToAddUser(IUser user, List<IUser> followings, List<IUser> followers, boolean showFollowings, boolean showFollowers){
        // case 1 : show all the people i'm following and all the users following me
        if(!showFollowers && !showFollowings){
            return true;
        }
        // case 2 : show all the people I'm following who are following me back
        else if(showFollowers && showFollowings){
            return (followings.contains(user) && followers.contains(user));
        }
        // case 3 : show all the people i'm following or all the people who are following me
        else{
            return ((followings.contains(user) && showFollowings) || followers.contains(user) && showFollowers);
        }
    }

    private UserInteractions getNbInterractions() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("tweet-history.json").getFile());
        List<TweetDataDTO> tweets = this.removeRTsFromTweetList(twitterClient.readTwitterDataFile(file));
        UserInteractions userInteractions = new UserInteractions();
        this.countRetweets(userInteractions, tweets, initRetweetsDate);
        this.countRepliesTo(userInteractions, tweets, initRepliesDate);
        this.countRecentRepliesFrom(userInteractions, true); // D-7 -> D0
        this.countRecentRepliesFrom(userInteractions, false); // D-30 -> D-7
        return userInteractions;
    }

    private List<TweetDataDTO> removeRTsFromTweetList(List<TweetDataDTO> tweetList){
        List<TweetDataDTO> result = new ArrayList<>();
        for(TweetDataDTO tweet : tweetList){
            if(!tweet.getTweet().getText().startsWith("RT @")){
                result.add(tweet);
            }
        }
        return result;
    }

    public void countRecentRepliesFrom(UserInteractions userInteractions, boolean currentWeek) {
        Date toDate;
        Date fromDate;
        if(currentWeek){
            toDate = DateUtils.truncate(ConverterHelper.minutesBeforeNow(60), Calendar.HOUR);
            fromDate = DateUtils.ceiling(DateUtils.addDays(toDate, -7),Calendar.HOUR);
        } else{
            toDate = DateUtils.truncate(ConverterHelper.dayBeforeNow(7),Calendar.DAY_OF_MONTH);
            fromDate = DateUtils.ceiling(DateUtils.addDays(toDate, -23), Calendar.DAY_OF_MONTH);
        }

        List<ITweet> tweetWithReplies;
        String query = "to:"+userName+" has:mentions";
        if(currentWeek){
            tweetWithReplies= this.twitterClient.searchForTweetsWithin7days(query, fromDate, toDate);
        } else{
            tweetWithReplies= this.twitterClient.searchForTweetsWithin30days(query, fromDate, toDate);
        }
        for(ITweet tweet : tweetWithReplies){
            UserInteractions.UserInteraction userInteraction = userInteractions.get(tweet.getAuthorId());
            userInteraction.incrementNbRepliesFrom();
        }
    }

    private void countRepliesTo(UserInteractions userInteractions, List<TweetDataDTO> tweets, Date initDate){
        Date tweetDate;
        for(TweetDataDTO tweetDataDTO : tweets){
            ITweet tweet = tweetDataDTO.getTweet();
            // checking the reply I gave to other users
            String inReplyUserId = tweet.getInReplyToUserId();
            tweetDate = tweet.getCreatedAt();
            if(inReplyUserId!=null && tweetDate!=null && tweetDate.compareTo(initDate)>0) {
                    UserInteractions.UserInteraction userInteraction = userInteractions.get(inReplyUserId);
                    userInteraction.incrementNbRepliesTo();
            }
        }
    }

    private void countRetweets(UserInteractions userInteractions, List<TweetDataDTO> tweets, Date initDate){
        Date tweetDate;
        for(TweetDataDTO tweetDataDTO : tweets){
            ITweet tweet = tweetDataDTO.getTweet();
            tweetDate = tweet.getCreatedAt();
            if(tweetDate!=null && tweetDate.compareTo(initDate)>0){
                if(tweetDataDTO.getTweet().getRetweetCount()>0 && !tweetDataDTO.getTweet().getText().startsWith(("@"))){
                    this.countRetweetsOfTweet(tweetDataDTO, userInteractions);
                }
            }
        }
    }

    private void countRetweetsOfTweet(TweetDataDTO tweetDataDTO, UserInteractions userInteractions){
        List<String> retweeterIds = this.twitterClient.getRetweetersId(tweetDataDTO.getTweet().getId());
        for(String retweeterId : retweeterIds){
            UserInteractions.UserInteraction userInteraction = userInteractions.get(retweeterId);
            userInteraction.incrementNbRetweets();
        }
    }

    @SneakyThrows
    public void unfollow(String[] toUnfollow){
        //String[] toUnfollow = {"amiinedz78","josko3s","RiynK","Faridb59","9Madriida","SarahLdyHnz","_Niniiiiiiiii","ptitcafebrioche","CamilleKyrie2","WhiteKumaaa","clandestins_","sanaaells","sudiste213","El_P0w","Nadiaa_trore01","FaisEnUnAutre","bahweles","mrslindachibani","Dow_Jones2","da_wood7","hb_213","riihaaabk","arbitkt","Qatar_Happiness","EdouardBeyeme","fatimezzzahrae","mel79117","NassDurkio93","Mlle_batata","MarvinLakeers","jal___","Txrzan__","Coline_prchnt","Rv_Mathoux","94kg__","CaptainBooz7","dzzz_35","NassiBrown","graaldeuf","samyra75","tantinekimberly","ALGERIENNEDP","NB_Rp59","alalgerienneee","shhhut_","R0LK","OrLiliane","sslipknoot","BLLVCKSCARAIBES","BillionBix","Abdel_hz93","sheikas4","bulane20","PoetryLifeTimes","vivbrillant","IsYVEVO","sarabndry","Inaaya_93","fromMarsToPluto","ccldiaz","tiefadaouquoi","Le_FeuFoullet","InitialsDD_","Mio_Karaa","MystHumain24046","HappyYoSmile","IamYankeeBoy","risMOrisMO","plutonnique","PpaulBasse","Channel_sah","MorganeDns","Meliza_Elvz","baydou","na2s____","apresmarxavril","sameclc","ZacNasra","marine77290","NikiLaarson","elyyssa__1","wild4James","ia_sa66","otracopadevino","NamoryCoulibal2","IamNormanZ","MoussaXXVIIV","marinaa_77","Madison_gmty","ines86_","Vacensii","CarlaBZH","kriskonan","Mhra_57","grrmymy","LopezLibongo","kelsy_","vinsmooke_","TRK03BSN","henenetlb","Mlle_Ninoush","YacineMahfoufi","Sfrediin","MedMierda","amneziia91","SoxnaSi_","yasminemost","KToTheeN","ccbyeesh","IamMad__","Mchaumier","DatBoyKayLo","pmontp19","m2ldu952","moufiane1","Busoo_Joop","GazouilleurFou","missssjuriste","Ndoyamy","byminaaaa","Happy_Gnanchou","_vivelapolska_","Adam_Amrane","lunaameelo","kabylee21369","braulio_jcz","linamrni","HerBijiKurdi","sbnh___","Fuetardo_243","FranckCyril_","YenigunSevim","BoHamzouz17","MelissaPabisz","flanaa94","PRAISEDA8IGHT","AmiraDreams","lindamez2","dturotte","Ablaze191","jdereg13","Nicolas_WISSER","TheDrzy","thehaankee","libertashio_","bonbouaz","ziasiam","iya_mouhamed","jujuulagarce","fiaso78","demi_sword","JurgenTgt","Djzr__","21Draxlaaaa","pactu10","adriencuchet","Amandinee_prr","Dabebi_","MaryDedeur","nicco_tbo","WechHamzHamz","WhoTFYouR","_OncleDom","_izzb__","francksman","pluto299","africaanwhite"};
        int nbUnfollows = 0;
        for(String unfollowName : toUnfollow){
            boolean result = this.getTwitterClient().unfollowByName(unfollowName);
            if(result){
                nbUnfollows++;

            } else{
                LOGGER.severe(unfollowName + " not unfollowed !!");
            }
            TimeUnit.MILLISECONDS.sleep(500);
        }

        LOGGER.info(nbUnfollows + " users unfollowed with success !");
    }
}