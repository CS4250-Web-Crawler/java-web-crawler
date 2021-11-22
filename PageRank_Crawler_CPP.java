import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.*;

public class PageRank {

    public static final int MAX_CRAWL_COUNT = 5;
    public static final String CPP_PRIMARY_SEED = "https://www.CPP.edu";
    // key = visited url, value = number of outlinks (csv)
    public static HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls (limit)
    public static int visitedLinksCount;

    public static HashMap<String, HashSet<String>> pageUrlWithOutlinks = new HashMap<>();

    public PageRank() {
        linkCollection = new HashMap<>();
        visitedLinksCount = 0;
    }

    public void crawl(String url) throws IOException {

        // validate duplicates and crawling limit
        if (!linkCollection.containsKey(url) && visitedLinksCount < MAX_CRAWL_COUNT) {

            try {
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                visitedLinksCount++;

                // get outlinks
                Elements linksOnPage = document.select("a[href]");
                // mark current url as visited and initialize the num of outlinks with 0
                linkCollection.put(url, 0);
                int outlinksCount = 0;
                String plainUrl = "";       // outlink absolute url
                String newPlainUrl = "";    // outlink without https
                String updatedUrl ="";      // seed without https
                HashSet<String> hashSet = new HashSet<>();

                updatedUrl = url.replace(" ","");       //removes empty space at the end of urls

                // for each outlink, increment count and crawl it
                for (Element page : linksOnPage) {
                    if (page.attr("abs:href").contains("www.cpp.edu")) {

                        plainUrl = page.attr("abs:href");
//                        newPlainUrl = plainUrl.replace("http://","").replace("https://","");
                        hashSet.add(plainUrl);
                        outlinksCount++;

                        crawl(plainUrl);
                    }
                }

                // update the url with the correct outlinks count
                linkCollection.put(url, outlinksCount);

                pageUrlWithOutlinks.put(updatedUrl, hashSet);

            } catch(MalformedURLException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch(IllegalArgumentException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            }
        }

    }

    public static void main(String[] args) throws IOException {
        boolean noConvergence = true;
        PageRank englishCrawler = new PageRank();

        //crawl CPP sites
        englishCrawler.crawl(CPP_PRIMARY_SEED);

        // populate by looping through the links
        Map<String, Page> pages = new HashMap<>();

//         create all the page objects according to the pages we crawled
        for (Map.Entry<String, HashSet<String>> entry : pageUrlWithOutlinks.entrySet()) {
            String pageUrl = entry.getKey();
            Page newPage = new Page(visitedLinksCount, pageUrl);
            pages.put(pageUrl, newPage);
        }

        for (Map.Entry<String, Integer> entry : linkCollection.entrySet()) {
            String pageUrl = entry.getKey();
            int numOfOutlinks = entry.getValue();
            Page page = pages.get(pageUrl);
            page.setNumOfOutlinks(numOfOutlinks);
        }

        // build the relationship graph by comparing links and populating the page.inlinkPages field
        for (Map.Entry<String, HashSet<String>> entryOne : pageUrlWithOutlinks.entrySet()) {
            for (Map.Entry<String, HashSet<String>> entryTwo : pageUrlWithOutlinks.entrySet()) {
                String pageOneUrl = entryOne.getKey();
                String pageTwoUrl = entryTwo.getKey();
                // skip if we are looking at the same entry
                if (pageOneUrl.equals(pageTwoUrl)) continue;
                // iterate through all the outlinks and compare
                for (String outlink : entryTwo.getValue()) {
                    // if there is a link match, then add it into the corresponding page object inlinkPages field
                    if (entryOne.getKey().equals(outlink)) {
                        // get the page object from the pages hashmap
                        Page pageOne = pages.get(pageOneUrl);
                        Page pageTwo = pages.get(pageTwoUrl);
                        // since page two contains a link that points to page one, then page A has page B as an inlink
                        pageOne.addInlinkPage(pageTwo);
                    }
                }
            }
        }

        // display the graph
        for (Map.Entry<String, Page> entry : pages.entrySet()) {
            Page page = entry.getValue();
            System.out.print("link " + page.getPageUrl() + " <-- ");
            for (int i = 0; i < page.getInlinkPages().size(); i++) {
                System.out.print(page.getInlinkPages().get(i).getPageUrl() + " ");
            }
            System.out.print("   with a page rank of " + page.getPageRank());
            System.out.println(" and " + page.getNumOfOutlinks() + " outlinks");
        }

        
        System.out.println("\n");
        // calculate page rank based on the relationship graph
        // boolean noConvergence = true;
        while (noConvergence) {
            // calculate the page rank but DO NOT update the page rank until we finish the current iteration
            noConvergence = false;
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                Page page = entry.getValue();
                double newPageRank = 0;
                System.out.println("PageRank before calc for link " + page.getPageUrl() + " is " + page.getPageRank());
                System.out.print("Calculation: ");
                ArrayList<Page> inlinkPages = page.getInlinkPages();
                for (int j = 0; j < inlinkPages.size(); j++) {
                    Page inlinkPage = inlinkPages.get(j);
                    double inlinkPageRank = inlinkPage.getPageRank();
                    double inlinkNumOfOutlinks = inlinkPage.getNumOfOutlinks();
                    System.out.print("(" + inlinkPageRank + " / " + inlinkNumOfOutlinks + ") + ");
                    newPageRank += (inlinkPageRank / inlinkNumOfOutlinks);
                }
                page.setPageRank(new BigDecimal(newPageRank).setScale(4, RoundingMode.HALF_UP).doubleValue());
                System.out.println("\nPageRank after calc for link " + page.getPageUrl() + " is " + newPageRank + "\n");
            }

            // test convergence and update the page rank so that we use the new page rank for the next iteration
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                Page page = entry.getValue();

        /*Set noConvergence flag if the difference from current to new page rank is too high
	      and if the last page rank isn't the same as the new pagerank */
                if (Math.abs(page.getPageRank() - page.getNewPageRank()) > 0.001) {
                    noConvergence = true;
                }
                // insert converge code here

                page.updatePageRank();
            }
        }
    }
}
