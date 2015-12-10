package com.castronu.easymarketsurvey;

import java.util.List;
import java.util.Set;

/**
 * Created by castronu on 10/12/15.
 */
public class EmailResult {

    private Set<String> emails;
    private String url;
    private int googlePageIndex;

    public EmailResult(Set<String> emails, String url, int googlePageIndex) {
        this.emails = emails;
        this.url = url;
        this.googlePageIndex = googlePageIndex;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public String getUrl() {
        return url;
    }

    public int getGooglePageIndex() {
        return googlePageIndex;
    }

    @Override
    public String toString() {
        return "EmailResult{" +
                "emails=" + emails +
                ", url='" + url + '\'' +
                ", googlePageIndex=" + googlePageIndex +
                '}';
    }
}
