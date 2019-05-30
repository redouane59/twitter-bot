package com.socialMediaRaiser.twitter;

import com.socialMediaRaiser.twitter.helpers.GoogleSheetHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleSheetHelperTest {

    private GoogleSheetHelper googleSheetHelper = new GoogleSheetHelper();

    @BeforeAll
    public static void init(){
        FollowProperties.init();
    }

    @Test
    public void testGetPreviouslyFollowedIdsAll(){
        List<Long> result = googleSheetHelper.getPreviouslyFollowedIds();
        assertTrue(result.size()>400);
    }

    @Test
    public void testGetPreviouslyFollowedIdsByDate(){
        Date date = new Date();
        date.setDate(18);
        date.setMonth(4);
        List<Long> result = googleSheetHelper.getPreviouslyFollowedIds(true, true, date);
        assertTrue(result.size()>250);
    }

}