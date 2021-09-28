import java.io.IOException;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    public static final String ENGLISH = "en";
    public static final String SPANISH = "es";
    public static final String CHINESE = "zh";
    public static final String FRENCH = "fr";
    public static final int MAX_CRAWL_COUNT = 5;
    public static final String PRIMARY_SEED = "https://www.wikipedia.org/";

    // key = visited url, value = number of outlinks
    public HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls
    public int visitedLinksCount;
    // holds the html content for each visited url
    public String[] htmlContents;

    public Crawler() {
        linkCollection = new HashMap<>();
        visitedLinksCount = 0;
        htmlContents = new String[MAX_CRAWL_COUNT]; 
    }
   
    public void crawl(String url, String lang) throws IOException {
        if (!linkCollection.containsKey(url) && visitedLinksCount < MAX_CRAWL_COUNT) {
            System.out.println("CURRENT URL: " + url + " and CURRENT COUNT: " + visitedLinksCount);

            try {
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                Elements html = document.select("html");
                htmlContents[visitedLinksCount] = html.toString();

                Elements linksOnPage = document.select("a[href]");

                if (html.attr("lang").equals(lang)) {
                    int outlinksCount = 0;
            
                    for (Element page : linksOnPage) {
                        outlinksCount++;
                        String plainUrl = page.attr("abs:href");
                        visitedLinksCount++;
                        linkCollection.put(url, outlinksCount);
                        crawl(plainUrl, lang);
                    }

                    linkCollection.put(url, outlinksCount);
                }
            } catch (IOException e) {
                System.out.println("Error for " + url + ": " + e.getMessage());
            }
        }
    }
     public static void main(String[] args) throws IOException{
        System.out.println("SOMETHING");
        Crawler test = new Crawler();
        test.crawl(PRIMARY_SEED, ENGLISH);
        System.out.println(test.linkCollection);
    }
}
