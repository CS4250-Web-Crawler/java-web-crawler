import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Crawler {
    public static void main(String[] args) throws IOException{
        String seed = "https://www.wikipedia.org/";
        int startDepth = 1;
        int maxDepth = 5;
        String desiredLang = "en";
        crawl(seed, startDepth, maxDepth, new ArrayList<String>(), desiredLang); 
            
    }
        
    
    private static void crawl(String url, int startDepth, int maxDepth, ArrayList<String> linkCollection, String desiredLang) throws IOException{
        if(startDepth <= maxDepth) {
            verify(url, desiredLang, linkCollection);
        
        }
    }
    
     private static int verify(String url, String desiredLang,  ArrayList<String> linkCollection) throws IOException{
        int score = 0;
        try {
            Connection connection = Jsoup.connect(url);
            
            Document doc = connection.get();
        
        
            if(connection.response().statusCode() == 200){  //verify connection
                score++;
            
            }
            
            Elements element = doc.select("html");
            
            if(element.attr("lang") == desiredLang) { //verify desired language
                score++;
                
            }
            
            if(linkCollection.contains(url) == false) { //verify duplicates
                score++;
            }
            
            return score;
        }
        catch(IOException e) {
            return (Integer) null;
        }
    }
    
    
}
