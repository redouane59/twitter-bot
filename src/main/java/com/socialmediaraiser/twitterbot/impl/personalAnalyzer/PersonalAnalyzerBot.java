package com.socialmediaraiser.twitterbot.impl.personalAnalyzer;
import com.socialmediaraiser.twitter.TwitterClient;
import com.socialmediaraiser.twitter.dto.user.IUser;
import com.socialmediaraiser.twitter.helpers.ConverterHelper;
import com.socialmediaraiser.twitterbot.AbstractIOHelper;
import com.socialmediaraiser.twitterbot.GoogleSheetHelper;
import com.socialmediaraiser.twitterbot.impl.User;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


@Getter
@Setter
@CustomLog
public class PersonalAnalyzerBot {

  private       String            userName;
  private       AbstractIOHelper  ioHelper;
  private       TwitterClient     twitterClient = new TwitterClient();
  private final Date              iniDate       = ConverterHelper.dayBeforeNow(30);
  private       DataArchiveHelper dataArchiveHelper;
  private       ApiSearchHelper   apiSearchHelper;

  public PersonalAnalyzerBot(String userName, String archiveFileName) {
    this.userName          = userName;
    this.ioHelper          = new GoogleSheetHelper(userName);
    this.dataArchiveHelper = new DataArchiveHelper(userName, archiveFileName, iniDate);
    this.apiSearchHelper   = new ApiSearchHelper(userName);
  }

  public void launch(boolean includeFollowers, boolean includeFollowings, boolean onyFollowBackFollowers)
  throws InterruptedException {
    String      userId       = this.twitterClient.getUserFromUserName(userName).getId();
    Map<String, UserStats>  interactions = this.getNbInteractions(); // @todo to change
    List<IUser> followings   = this.twitterClient.getFollowingUsers(userId);
    List<IUser> followers = this.twitterClient.getFollowerUsers(userId);
    Set<IUser>  allUsers  = HashSet.ofAll(followings).addAll(followers); // @todo duplicate

    List<User> usersToWrite = new ArrayList<>();
    int        nbUsersToAdd = 50;
    for (IUser iUser : allUsers) {
      if (hasToAddUser(iUser, followings, followers, includeFollowings, includeFollowers, onyFollowBackFollowers)) {
        User user = new User(iUser);
     /*   user.setNbRepliesReceived(interactions.get(iUser.getId()).getNbRepliesReceived());
        user.setNbRepliesGiven(interactions.get(iUser.getId()).getNbRepliesGiven());
        user.setNbRetweetsReceived(interactions.get(iUser.getId()).getNbRetweetsReceived());
        user.setNbLikesGiven(interactions.get(iUser.getId()).getNbLikesGiven());
        user.setNbRetweetsGiven(interactions.get(iUser.getId()).getNbRetweetsGiven()); */
        usersToWrite.add(user);
        if (usersToWrite.size() == nbUsersToAdd) {
          this.ioHelper.addNewFollowerLineSimple(usersToWrite);
          usersToWrite = new ArrayList<>();
          LOGGER.info("adding " + nbUsersToAdd + " users ...");
          TimeUnit.MILLISECONDS.sleep(500);
        }
      }
    }
    this.ioHelper.addNewFollowerLineSimple(usersToWrite);
    LOGGER.info("finish with success");
  }

  private boolean hasToAddUser(IUser user, List<IUser> followings, List<IUser> followers,
                               boolean showFollowings, boolean showFollowers, boolean onyFollowBackUsers) {
    // case 0 : only follow back users
    if (onyFollowBackUsers && followings.contains(user) && !followers.contains(user)) {
      return false;
    }

    // case 1 : show all the people i'm following and all the users following me
    if (!showFollowers && !showFollowings) {
      return true;
    }
    // case 2 : show all the people I'm following who are following me back
    else if (showFollowers && showFollowings && onyFollowBackUsers) {
      return (followings.contains(user) && followers.contains(user));
    }
    // case 3 : show all the people i'm following or all the people who are following me
    else {
      return ((followings.contains(user) && showFollowings) || followers.contains(user) && showFollowers);
    }
  }

  private Map<String, UserStats> getNbInteractions() {
    Stream<Tuple2<String, UserInteraction>> givenInteractions = this.getGivenInteractions().toStream();
    var result = givenInteractions
        .peek(ui -> LOGGER.info("analyzing : " + ui._1()))
        .groupBy(Tuple2::_1)
        .map(ui -> buildTurpleFromUserInteractions(ui._1(), ui._2()));
    return result; // @todo KO : is not Map<String, UserStats> but Seq<Object> instead !
  }
  private Tuple2<String, UserStats> buildTurpleFromUserInteractions(String userId,
                                                                    Stream<Tuple2<String, UserInteraction>> userInteractions){
    return Tuple.of(userId,
                    userInteractions.foldLeft(new UserStats(),
                                              (userStats, userInteraction) ->
                                                  userStats.addRepliesGiven(userInteraction._2().getAnswersIds().size())
                                                          .addRetweetsGiven(userInteraction._2().getRetweetsIds().size())
                                                          .addLikesGiven(userInteraction._2().getLikesIds().size())));
  }
   /* Map<String, TweetInteraction> receivedInteractions = this.getReceivedInteractions();
    receivedInteractions.forEach((s, tweetInteraction) -> LOGGER.info(
        s + "-> RT : " + tweetInteraction.getRetweeterIds().size() + " | A : " + tweetInteraction.getAnswererIds().size()) );

    return this.mapsToUserInteractions(givenInteractions, receivedInteractions); */

  //@todo  apiSearchHelper.countRecentRepliesGiven(userInteractions,
  //                                         dataArchiveHelper.filterTweetsByRetweet(false).get(0).getCreatedAt()); // @todo test 2nd arg
  //  return userInteractions;

  private Map<String, UserStats> mapsToUserInteractions(Map<String, UserInteraction> userInteractionMap, Map<String,
      TweetInteraction> tweetInteractionMap){
    // adding all users

    // adding data
   /* var result = userInteractionMap
                           .groupBy(Tuple2::_1)
                           .map(ui -> this.buildTurpleFromUserInteractions(ui._2())); */

    return null;
  }


  private Tuple2<String, UserStats> buildTurple(String userId, Set<TweetInteraction> tweetInteractions){
    return Tuple.of(userId,
                    tweetInteractions.foldLeft(new UserStats(),
                                               (userstat, tweetInteraction) ->
                                                   userstat.addRepliesReceived(tweetInteraction.getAnswererIds().length())
                                                           .addRetweetsReceived(tweetInteraction.getRetweeterIds().length())));

  }



  private Map<String, TweetInteraction> getReceivedInteractions() {
    Map<String, TweetInteraction> map1 = dataArchiveHelper.countRetweetsReceived();
    Map<String, TweetInteraction> map2 = apiSearchHelper.countRepliesReceived(true);
    Map<String, TweetInteraction> map3 = apiSearchHelper.countRepliesReceived(false);
    return map1.merge(map2,TweetInteraction::merge).merge(map3,TweetInteraction::merge);
  }

  private Map<String, UserInteraction> getGivenInteractions(){
    return dataArchiveHelper.countRetweetsGiven()
                            .merge(dataArchiveHelper.countRepliesGiven(), UserInteraction::merge)
                            .merge(apiSearchHelper.countGivenLikesOnStatuses(),UserInteraction::merge);
  }

  @SneakyThrows
  public void unfollow(String[] toUnfollow, String[] whiteList) {

    int nbUnfollows = 0;
    for (String unfollowName : toUnfollow) {
      unfollowName.replace(" ", "");
      if (!Arrays.asList(whiteList).contains(unfollowName)) {
        this.getTwitterClient().unfollowByName(unfollowName);
        nbUnfollows++;
        TimeUnit.MILLISECONDS.sleep(500);
        System.out.println(unfollowName + " unfollowed");
      }
    }
    LOGGER.info(nbUnfollows + " users unfollowed with success !");
  }
}
