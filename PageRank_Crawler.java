import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.*;

public class PageRank_v3 {

    public static final int MAX_CRAWL_COUNT = 20;
    public static final String CPP_PRIMARY_SEED = "https://www.CPP.edu";
    // key = visited url, value = number of outlinks (csv)
    public static HashMap<String, Integer> linkCollection;
    // keeps the count of visited urls (limit)
    public static int visitedLinksCount;

    public static HashMap<String, HashSet<String>> pageUrlWithOutlinks = new HashMap<>();

    public PageRank_v3() {
        linkCollection = new HashMap<>();
        visitedLinksCount = 0;
    }

    public void crawl(String url) throws IOException {
        //linkCollection  -> this keeps the url and the number outlinks that url has

        // validate duplicates and crawling limit                                                   //todo: need the html to specify what is written on page regarding LA Lakers
        if (!linkCollection.containsKey(url) && visitedLinksCount < MAX_CRAWL_COUNT) {
//            System.out.println("CURRENT URL: " + url + " and CURRENT COUNT: " + visitedLinksCount);

            try {
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                visitedLinksCount++;

                // get outlinks
                Elements linksOnPage = document.select("a[href]");
                // mark current url as visited and initialize the num of outlinks with 0
                linkCollection.put(url, 0);
                int outlinksCount = 0;
                String plainUrl = "";

//                htmlContents[visitedLinksCount++] = remove(htmlString.toString());       --removes javascript and css


                ArrayList<String> arrlist = new ArrayList<>();

                // for each outlink, increment count and crawl it
                for (Element page : linksOnPage) {
                    if (page.attr("abs:href").contains("cpp.edu")) {

                        plainUrl = page.attr("abs:href");

                        if (!arrlist.contains(plainUrl)) {
                            arrlist.add(plainUrl);
                            outlinksCount++;
                        }

                        crawl(plainUrl);
                    }
                }

                // update the url with the correct outlinks count
                linkCollection.put(url, outlinksCount);

//                for(int i = 0; i < arrlist.size(); i++){
//                    System.out.println("Array List stuff: " + arrlist.get(i));
//                }

                pageUrlWithOutlinks.put(url, new HashSet<>(arrlist));

//
//                System.out.println("Size of array: " + arrlist.size());
//                System.out.println("Number of outLinks: " + outlinksCount);
//                System.out.println("Visited Links: " + visitedLinksCount);


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

        PageRank_v3 englishCrawler = new PageRank_v3();

        //crawl ESPN sites
        englishCrawler.crawl(CPP_PRIMARY_SEED);

        // populate by looping through the links
        Map<String, Page> pages = new HashMap<>();

//            System.out.println();
//            System.out.println("URl and URL ADDRESS ");
//            for (Map.Entry<String, HashSet<String>> entry : pageUrlWithOutlinks.entrySet()) {
//                System.out.println(entry.getKey() + " = " + entry.getValue());
//            }
//
//            System.out.println();
//            System.out.println("URl and number of outlinks ");
//            for (Map.Entry<String, Integer> entry : linkCollection.entrySet()) {
//                System.out.println(entry.getKey() + " = " + entry.getValue());
//            }

        // assume we get this info from crawler
//        pageUrlWithNumOfOutlinks.put("a", 1);
//        pageUrlWithNumOfOutlinks.put("b", 2);
//        pageUrlWithNumOfOutlinks.put("c", 3);
//        pageUrlWithNumOfOutlinks.put("d", 2);

        //main url page -> list url
//        pageUrlWithOutlinks.put("a", new HashSet<>(Arrays.asList("b")));
////        pageUrlWithOutlinks.put("b", new HashSet<>(Arrays.asList("a", "d")));
////        pageUrlWithOutlinks.put("c", new HashSet<>(Arrays.asList("a", "b", "d")));
//        pageUrlWithOutlinks.put("d", new HashSet<>(Arrays.asList("a", "c")));
//
        // create all the page objects according to the pages we crawled
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
                if (pageOneUrl == pageTwoUrl) continue;
                // iterate through all the outlinks and compare
                for (String outlink : entryTwo.getValue()) {
                    // if there is a link match, then add it into the corresponding page object inlinkPages field
                    if (entryOne.getKey() == outlink) {
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
        for (int i = 0; i < 2; i++) {
            // calculate the page rank but DO NOT update the page rank until we finish the current iteration
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                Page page = entry.getValue();
                double newPageRank = 0;
                System.out.println("PageRank before calc for link " + page.getPageUrl() + " is " + newPageRank);
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

                // insert converge code here

                page.updatePageRank();
            }
        }
    }
}
