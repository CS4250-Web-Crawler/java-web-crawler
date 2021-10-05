import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

public class Crawler {
    public static final String ENGLISH = "en";
    public static final String SPANISH = "es";
    public static final String CHINESE = "zh";
    public static final String FRENCH = "fr";
    public static final int MAX_CRAWL_COUNT = 10;
    public static final String PRIMARY_SEED = "https://www.wikipedia.org/";

    // key = visited url, value = number of outlinks (csv)
    public static HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls (limit)
    public int visitedLinksCount;
    // holds the html content for each visited url (file)
    public String[] htmlContents;

    public Crawler() {
        linkCollection = new HashMap<>();
        visitedLinksCount = 0;
        htmlContents = new String[MAX_CRAWL_COUNT];
    }

    public void crawl(String url, String lang) throws IOException {
        // validate duplicates and crawling limit
        if (!linkCollection.containsKey(url) && visitedLinksCount < MAX_CRAWL_COUNT) {
            System.out.println("CURRENT URL: " + url + " and CURRENT COUNT: " + visitedLinksCount);

            try {
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                Elements htmlElement = document.select("html");
                //String that holds all the html content of the url
                String htmlString = document.html();

                    // validate html lang
                    if (htmlElement.attr("lang").equals(lang)) {
                        // html might no have a head containing the lang attribute, need to change the validation method
                        System.out.println("Language Validated for URL:" + url);

                        // if html content in desired lang, call remove method to remove images, CSS, and JavaScript, and then add to array
                        htmlContents[visitedLinksCount++] = remove(htmlString.toString());
                        // get outlinks
                        Elements linksOnPage = document.select("a[href]");
                        // mark current url as visited and initialize the num of outlinks with 0
                        linkCollection.put(url, 0);

                        int outlinksCount = 0;
                        // for each outlink, increment count and crawl it
                        for (Element page : linksOnPage) {
                            outlinksCount++;
                            String plainUrl = page.attr("abs:href");
                            // might not do anything if the first step of validation isn't passed
                            crawl(plainUrl, lang);
                        }

                        // update the url with the correct outlinks count
                        linkCollection.put(url, outlinksCount);
                    }

            } catch(MalformedURLException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch(IllegalArgumentException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            }
        }
    }

    //method that removes unwanted images, CSS, and JavaScript
    public String remove(String html) {
        Safelist wl = Safelist.relaxed();
        //remove style, script, and img tags
        wl.removeTags("style", "script", "img");

        //store the cleaned HTML in a new string
        String cleanHTML = Jsoup.clean(html, wl);
        return cleanHTML;
    }

    public static void main(String[] args) throws IOException{
        Crawler englishCrawler = new Crawler();
        englishCrawler.crawl(PRIMARY_SEED, ENGLISH);
        System.out.println(englishCrawler.linkCollection);
        System.out.println(englishCrawler.htmlContents[0]);

        // create csv file
         File csvFile = new File("report.csv");
        PrintWriter output = new PrintWriter(csvFile);

        //iterate through hashmap elements (visited url and number of outlinks) and write to csv file
        for (HashMap.Entry<String, Integer> entry: linkCollection.entrySet()) {
            output.write(entry.getKey() + " , " + entry.getValue() + "\n");
        }
        output.close();
    }
}

