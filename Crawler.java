package web_crawler2;

import java.io.File;
import java.io.FileWriter;
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

    // key = visited url, value = number of outlinks (csv)
    public HashMap<String, Integer> linkCollection;
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

                Elements html = document.select("html");                                

                // validate html lang
                if (html.attr("lang").equals(lang)) {
                    // html might no have a head containing the lang attribute, need to change the validation method
                    System.out.println("Language Validated for URL:" + url);
                    // if html content in desired lang, add to array
                    htmlContents[visitedLinksCount++] = html.toString();
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
            } catch (IOException e) {
                System.out.println("Error for " + url + ": " + e.getMessage());
            }
        }
    }
    
    // iterate and write all html content to a text file in respository folder 
    public static void download(String[] content, String lang) {
    	int arraySize = content.length;
    	
    	//create a repository folder to hold all lang html files
    	try {
    		File folder = new File("repository");
        	folder.mkdir();
        	//create file based on lang
        	File file = new File(folder, lang + ".txt");
			file.createNewFile();
		} catch (IOException e) {
			System.out.println("file cannot be created");
		}
    	
    	//write content of array to a .txt file in repository folder 
		try {
			FileWriter writer= new FileWriter("/Users/alondra/eclipse-workspace/web_crawler2/repository/" +lang + ".txt");
			
			// write all content collected from content array
			for(int index =0; index < arraySize; index++) {
				writer.write(content[index]);
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("cannot write to file");
		}
    }// end download
    
    
     public static void main(String[] args) throws IOException{
        Crawler englishCrawler = new Crawler();
        englishCrawler.crawl(PRIMARY_SEED, ENGLISH);
        System.out.println(englishCrawler.linkCollection);
        System.out.println(englishCrawler.htmlContents[0]); 
        download(htmlContents, ENGLISH);    
    }
}