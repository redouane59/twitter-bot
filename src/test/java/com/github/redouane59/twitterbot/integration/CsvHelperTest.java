package com.github.redouane59.twitterbot.integration;

import com.github.redouane59.twitterbot.io.CsvHelper;
import org.junit.jupiter.api.BeforeAll;

public class CsvHelperTest {

  private CsvHelper csvHelper = new CsvHelper();

  @BeforeAll
  static void init() {
  }
  /*

  @Test
  public void testAddNewLine(){
    CustomerUser user1 = new CustomerUser();
    user1.setId("12345");
    user1.setName("Red1");
    user1.setDescription("First description");
    user1.setFollowersCount(1);
    user1.setFollowingCount(2);
    user1.setTweetCount(3);
    user1.getUserStats().setNbRepliesReceived(4);
    user1.getUserStats().setNbRetweetsReceived(5);
    user1.getUserStats().setNbRepliesGiven(6);
    user1.getUserStats().setNbRetweetsGiven(7);
    user1.getUserStats().setNbLikesGiven(8);
    CustomerUser user2 = new CustomerUser();
    user2.setId("00001");
    user2.setName("Red2");
    user2.setDescription("2nd description");
    csvHelper.addUserLine(List.of(user1, user2).asJava());
  }


   */
}
