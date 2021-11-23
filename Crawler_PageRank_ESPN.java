import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.*;

public class Crawler_PageRank_ESPN {

    public static final int MAX_CRAWL_COUNT = 20;
    public static final String CPP_PRIMARY_SEED = "https://www.espn.com/nba";
    // key = visited url, value = number of outlinks (csv)
    public static HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls (limit)
    public static int visitedLinksCount;

    public static HashMap<String, HashSet<String>> pageUrlWithOutlinks = new HashMap<>();

    public Crawler_PageRank_ESPN() {
        linkCollection = new HashMap<>();
        visitedLinksCount = 0;
    }

    public void crawl(String url) throws IOException {

        // validate duplicates and crawling limit
        if (!linkCollection.containsKey(url) && visitedLinksCount < MAX_CRAWL_COUNT) {

            try {
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                //Crawl pages that only mention LA Lakers
                String htmlString = document.html();
                htmlString = remove(htmlString);
                if (htmlString.contains("Lakers") || htmlString.contains("LAKERS")) {

                    visitedLinksCount++;

                    // get outlinks
                    Elements linksOnPage = document.select("a[href]");
                    // mark current url as visited and initialize the num of outlinks with 0
                    linkCollection.put(url, 0);
                    int outlinksCount = 0;
                    String plainUrl = "";       // outlink absolute url

                    HashSet<String> hashSet = new HashSet<>();

                    // for each outlink, increment count and crawl it
                    for (Element page : linksOnPage) {
                        if (page.attr("abs:href").contains("www.espn.com/nba")) {

                            plainUrl = page.attr("abs:href");
                            hashSet.add(plainUrl);
                            outlinksCount++;
                            crawl(plainUrl);
                        }
                    }

                    // update the url with the correct outlinks count
                    linkCollection.put(url, outlinksCount);
                    pageUrlWithOutlinks.put(url, hashSet);
                }
            } catch(MalformedURLException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch(IOException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            } catch(IllegalArgumentException e){
                System.out.println("Error for " + url + ":" + e.getMessage());
            }

        }

    }


    public String remove(String html) {
        Safelist sl = Safelist.relaxed();
        sl.removeTags("style", "script", "img", "a");
        return Jsoup.clean(html, sl);
    }

    public static void main(String[] args) throws IOException {
        boolean noConvergence = true;
        Crawler_PageRank_ESPN englishCrawler = new Crawler_PageRank_ESPN();

        //crawl CPP sites
        englishCrawler.crawl(CPP_PRIMARY_SEED);
        for (Map.Entry<String, HashSet<String>> entry : pageUrlWithOutlinks.entrySet()) {
            HashSet<String> currentHashSet = entry.getValue();
            HashSet<String> updatedHashSet = new HashSet<>();
            String currentUrl = entry.getKey();
            for (String ele : currentHashSet) {
                if (linkCollection.containsKey(ele)) {
                    updatedHashSet.add(ele);
                }
            }
            pageUrlWithOutlinks.put(currentUrl, updatedHashSet);
            linkCollection.put(currentUrl, updatedHashSet.size());
        }

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
//        for (Map.Entry<String, Page> entry : pages.entrySet()) {
//            Page page = entry.getValue();
//            System.out.print("link " + page.getPageUrl() + " <-- ");
//            for (int i = 0; i < page.getInlinkPages().size(); i++) {
//                System.out.print(page.getInlinkPages().get(i).getPageUrl() + " ");
//            }
//            System.out.print("   with a page rank of " + page.getPageRank());
//            System.out.println(" and " + page.getNumOfOutlinks() + " outlinks");
//        }

        HashMap<String, Double> finalPageRanks = new HashMap<>();

        System.out.println("\n");
        // calculate page rank based on the relationship graph
        while (noConvergence) {
            // calculate the page rank but DO NOT update the page rank until we finish the current iteration
            noConvergence = false;
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                Page page = entry.getValue();
                double newPageRank = 0;
//                System.out.println("PageRank before calc for link " + page.getPageUrl() + " is " + page.getPageRank());
//                System.out.print("Calculation: ");
                ArrayList<Page> inlinkPages = page.getInlinkPages();
                for (int j = 0; j < inlinkPages.size(); j++) {
                    Page inlinkPage = inlinkPages.get(j);
                    double inlinkPageRank = inlinkPage.getPageRank();
                    double inlinkNumOfOutlinks = inlinkPage.getNumOfOutlinks();
//                    System.out.print("(" + inlinkPageRank + " / " + inlinkNumOfOutlinks + ") + ");
                    newPageRank += (inlinkPageRank / inlinkNumOfOutlinks);
                }
                page.setPageRank(new BigDecimal(newPageRank).setScale(4, RoundingMode.HALF_UP).doubleValue());
//                System.out.println("\nPageRank after calc for link " + page.getPageUrl() + " is " + newPageRank + "\n");

                //Store url and updated page ranks into a new hashmap so we can sort it at the end
                finalPageRanks.put(page.getPageUrl(), newPageRank);

            }


            // test convergence and update the page rank so that we use the new page rank for the next iteration
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                Page page = entry.getValue();

        /*Set noConvergence flag if the difference from current to new page rank is too high
	      and if the last page rank isn't the same as the new pagerank */
                if (Math.abs(page.getPageRank() - page.getNewPageRank()) > 0.001) {
                    noConvergence = true;
                }

                page.updatePageRank();
            }
        }

        //Print out top 100 most important pages
        Set<Map.Entry<String, Double>> set = finalPageRanks.entrySet();
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
                set);
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        FileWriter output = new FileWriter("100MostImportantPages_ESPN.txt");
        int index = 1;
        for (Map.Entry<String, Double> entry : list) {
            if (index > 100) {
                break;
            }
            output.write(index + ". " + entry.getKey() + " = " + entry.getValue() + '\n');
            index++;
        }

        double totalSum = 0.0;
        for (Map.Entry<String, Page> entry : pages.entrySet()) {
            Page page = entry.getValue();
            totalSum += page.getPageRank();
        }

        output.write("\n");
        output.write("The total page rank sum is: " + totalSum);

        output.close();
    }
 }
