package com.castronu.easymarketsurvey;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by castronu on 09/12/15.
 */
public class EmailScraper {


    public static EmailResult scrapeEmailForUrl(int page, String url) {

        if (url.contains("google")||
                url.contains("yellow.local.ch")||
                url.contains("www.klubschule.ch")||
                url.contains("www.scuola-club.ch")
                ) {
            //System.out.println("Blacklisted");
            return null;
        }

        Set<String> emailsForUrl = new HashSet<String>();

        Document doc;
        try{
            Document linksDoc = null;
            String baseUrl = "http://"+url;
            linksDoc = Jsoup.connect(baseUrl).userAgent("Mozilla").get();
            String html = linksDoc.html();
            getEmail(emailsForUrl, html);
            Set<String> linksInUrl = searchLinksInUrl(emailsForUrl, baseUrl);
            for (String innerLink : linksInUrl) {
                    searchLinksInUrl(emailsForUrl, baseUrl+"/"+innerLink);
            }
            //System.out.println("For url "+ url +" I've found these emails: "+emailsForUrl);
            return new EmailResult(emailsForUrl, url, page);
        }
        catch (IOException e) {
            //Doing Nonthing...
            //e.printStackTrace();
        }

        return null;
    }

    private static Set<String> searchLinksInUrl(Set<String> emailsForUrl, String url) {
        Set<String> linkResults = new HashSet<String>();
        Document doc;
            Document linksDoc = null;
            try {
                linksDoc = Jsoup.connect(url).userAgent("Mozilla").get();
                String html = linksDoc.html();

                getEmail(emailsForUrl, html);

                Elements titless = linksDoc.select("a");

                //Get all the url results for the first page
                for(Element e: titless){
                    //   System.out.println(e.attr("href"));
                    linkResults.add(e.attr("href"));
                }
            } catch (IOException   e ) {
                //Do nothing...
            }
        return linkResults;
    }


    private static void getEmail(Set<String> emailsForUrl, String webPage) {
        String emailPattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern p = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(webPage);
        Set<String> emails = new HashSet<String>();
        while(matcher.find()) {
            emailsForUrl.add(matcher.group());
        }

    }
}
