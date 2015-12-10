package com.castronu.easymarketsurvey;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;


/**
 * Created by castronu on 09/12/15.
 */
public class App {




    static BlockingQueue<EmailResult> emailToWrite = new LinkedBlockingQueue<EmailResult>(10) {
    };

    static String query = "Corsi+di+ballo";

    static String fileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
    static File outputFile = new File(query+fileName);

    public static void main(String[] args) {


        //   List<Integer> pages = Arrays.asList(10);
        List<Integer> pages = Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        pool.execute(new DataConsumer());

        for (Integer page : pages) {
            pool.execute(new MyTask(page));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    static class MyTask implements Runnable {

        private int page;

        public MyTask(int page) {
            this.page = page;
        }

        @Override
        public void run() {
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            Set<String> googleResultsUrl = new HashSet<String>();
            Document doc;
            try {
                Document linksDoc = null;
                linksDoc = Jsoup.connect("http://www.google.com/search?q=" + query + "&start=" + page).userAgent("Moilla").get();
                Elements titless = linksDoc.select("h3.r > a");

                //Get all the url results for the first page
                for (Element e : titless) {
                    String resultUrl = e.attr("href").replace("/url?q=", "").split("/")[2];
                    googleResultsUrl.add(resultUrl);
                }


                // Here I need another executor, one for each link!

                ExecutorService pool = Executors.newFixedThreadPool(10);


                for (String result : googleResultsUrl) {
                    pool.execute(new OneLinkAnalyzer(result, page));
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    static class OneLinkAnalyzer implements Runnable {

        private String url;
        private int pageIndex;

        OneLinkAnalyzer(String url, int pageIndex) {
            this.url = url;
            this.pageIndex = pageIndex;
        }


        public void run() {
            EmailResult emailsEmailResult = EmailScraper.scrapeEmailForUrl(pageIndex, url);
            if (emailsEmailResult != null) {
                if (!emailsEmailResult.getEmails().isEmpty())
                    emailToWrite.add(emailsEmailResult);
            }
        }
    }


    static class DataConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    EmailResult emailResult = emailToWrite.take();
                    System.out.println(emailResult);


                    StringBuilder lineWithCommaSeparatedEmails = new StringBuilder();
                    Set<String> emails = emailResult.getEmails();

                    for (String email : emails) {
                        lineWithCommaSeparatedEmails.append(email);
                        lineWithCommaSeparatedEmails.append(",");
                    }

                    FileUtils.writeStringToFile(outputFile, lineWithCommaSeparatedEmails.toString(), true);
                    FileUtils.writeStringToFile(outputFile, System.getProperty("line.separator"), true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}


