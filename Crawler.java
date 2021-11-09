import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import com.github.pemistahl.lingua.api.*;
import static com.github.pemistahl.lingua.api.Language.*;

public class Crawler {
    public static final String EN = "ENGLISH";
    public static final String ES = "SPANISH";
    public static final String FR = "FRENCH";
    public static final int MAX_CRAWL_COUNT = 500;
    public static final String EN_PRIMARY_SEED = "https://www.cpp.edu/";
    public static final String FR_PRIMARY_SEED = "https://www.lepoint.fr/24h-info/monde/";
    public static final String ES_PRIMARY_SEED = "https://www.univision.com";


    final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(ENGLISH, FRENCH, CHINESE, SPANISH).build();

    // key = visited url, value = number of outlinks (csv)
    public static HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls (limit)
    public int visitedLinksCount;
    // holds the html content for each visited url (file)
    public static String[] htmlContents;

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

                //String that holds all the html content of the url
                String htmlString = document.html();

                // get first 100 characters from page and use it to detect language
                String bodyStr = document.select("html").text();
                String partialStr = bodyStr.substring(0, Math.min(bodyStr.length(), 100));
                Language detectedLanguage = detector.detectLanguageOf(partialStr);

                System.out.println("The language is " + detectedLanguage.toString());

                // validate html lang and in desired domain
                if (detectedLanguage.toString().equals(lang)) {
                    // html might no have a head containing the lang attribute, need to change the validation method
                    System.out.println("Language Validated for URL:" + url);

                    // if html content in desired lang, call remove method to remove images, CSS, and JavaScript, and add to array
                    htmlContents[visitedLinksCount++] = remove(htmlString.toString());
                    // get outlinks
                    Elements linksOnPage = document.select("a[href]");
                    // mark current url as visited and initialize the num of outlinks with 0
                    linkCollection.put(url, 0);
                    int outlinksCount = 0;
                    // for each outlink, increment count and crawl it
                    for (Element page : linksOnPage) {
                        //domain restriction
                        if(page.attr("abs:href").contains("cpp.edu")) {
                            outlinksCount++;
                            String plainUrl = page.attr("abs:href");
                            // might not do anything if the first step of validation isn't passed
                            crawl(plainUrl, lang);
                        }
                    }

                    // update the url with the correct outlinks count
                    linkCollection.put(url, outlinksCount);
                } else {
                    System.out.println(url + "is not " + lang + ", it is " + detectedLanguage.toString());
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

    // method that removes unwanted images, CSS, and JavaScript
    public String remove(String html) {
        Safelist sl = Safelist.relaxed();
        //remove style, script, and img tags
        sl.removeTags("style", "script", "img");

        //return the cleaned HTML
        return Jsoup.clean(html, sl);
    }

    // iterate and write all html content to a text file in repository folder
    public static void download(String[] content, String lang) {
        //create file based on lang
        File file = new File("src/repository/" + lang + ".txt");

        //write content of array to a .txt file in repository folder
        try {
            FileWriter writer = new FileWriter(file, true);

            // write all content collected from content array
            for (String s : content) {
                writer.write(s);
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("cannot write to file");
        }
    }

    public static void writeToCSV(HashMap<String, Integer> linkCollection, String lang) throws IOException{
        // create csv file
        File csvFile = new File("report.csv");
        FileWriter output = new FileWriter(csvFile, true);

        output.write( "\nLanguage , " + lang + "\n");
        //iterate through hashmap elements (visited url and number of outlinks) and write to csv file
        for (HashMap.Entry<String, Integer> entry: linkCollection.entrySet()) {
            output.write(entry.getKey() + " , " + entry.getValue() + "\n");
        }
        output.close();
    }

    public static void main(String[] args) throws IOException{
        Crawler englishCrawler = new Crawler();
        englishCrawler.crawl(EN_PRIMARY_SEED, EN);
        download(htmlContents, EN);
        writeToCSV(linkCollection, EN);

        Crawler frenchCrawler = new Crawler();
        frenchCrawler.crawl(FR_PRIMARY_SEED, FR);
        download(htmlContents, FR);
        writeToCSV(linkCollection, FR);

        Crawler spanishCrawler = new Crawler();
        spanishCrawler.crawl(ES_PRIMARY_SEED, ES);
        download(htmlContents, ES);
        writeToCSV(linkCollection, ES);
    }
}