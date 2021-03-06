package com.binaryfork.onmap.model.twitter;

import android.text.Spannable;
import android.text.SpannableString;

import com.binaryfork.onmap.model.ApiSource;
import com.binaryfork.onmap.model.Media;
import com.twitter.sdk.android.core.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TweetMedia implements Media {

    private Tweet tweet;

    public TweetMedia(Tweet tweet) {
        this.tweet = tweet;
    }

    @Override public String getPhotoUrl() {
        if (tweet.entities.media == null)
            return null;
        return tweet.entities.media.get(0).mediaUrl;
    }

    @Override public String getThumbnail() {
        if (tweet.entities.media == null)
            return null;
        return tweet.entities.media.get(0).mediaUrl;
    }

    @Override public String getVideoUrl() {
        return null;
    }

    @Override public String getTitle() {
        return tweet.user.name;
    }

    @Override public String getUserpic() {
        return tweet.user.profileImageUrl;
    }

    @Override public String getSiteUrl() {
        return tweet.entities.media.get(0).url;
    }

    @Override public double getLatitude() {
        if (tweet.coordinates == null)
            return 0;
        return tweet.coordinates.getLatitude();
    }

    @Override public double getLongitude() {
        if (tweet.coordinates == null)
            return 0;
        return tweet.coordinates.getLongitude();
    }

    @Override public boolean isVideo() {
        return false;
    }

    @Override public long getCreatedDate() {
        String tweetFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(tweetFormat, Locale.ENGLISH);
        sf.setLenient(true);
        long seconds = 0;
        try {
            seconds = sf.parse(tweet.createdAt).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return seconds;
    }

    @Override public Spannable getComments() {
        return new SpannableString(tweet.text);
    }

    @Override public String getAdderss() {
        return null;
    }

    @Override public ApiSource getApiSource() {
        return ApiSource.TWITTER;
    }
}
