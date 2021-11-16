import java.util.*;

public class PageRank {
  public static void main(String[] args) {

    int numberOfPages = 4;    
    // populate by looping through the links
    Map<String, Page> pages = new HashMap<>();
    // populate using crawler
    Map<String, Integer> pageUrlWithNumOfOutlinks = new HashMap<>();    
    // populate using crawler
    Map<String, HashSet<String>> pageUrlWithOutlinks = new HashMap<>();

    // assume we get this info from crawler
    pageUrlWithNumOfOutlinks.put("a", 1);
    pageUrlWithNumOfOutlinks.put("b", 2);
    pageUrlWithNumOfOutlinks.put("c", 3);
    pageUrlWithNumOfOutlinks.put("d", 2);
    pageUrlWithOutlinks.put("a", new HashSet<>(Arrays.asList("b")));
    pageUrlWithOutlinks.put("b", new HashSet<>(Arrays.asList("a", "d")));
    pageUrlWithOutlinks.put("c", new HashSet<>(Arrays.asList("a", "b", "d")));
    pageUrlWithOutlinks.put("d", new HashSet<>(Arrays.asList("a", "c")));

    // create all the page objects according to the pages we crawled 
    for (Map.Entry<String, HashSet<String>> entry : pageUrlWithOutlinks.entrySet()) {      
      String pageUrl = entry.getKey();      
      Page newPage = new Page(numberOfPages, pageUrl);      
      pages.put(pageUrl, newPage);
    }

    for (Map.Entry<String, Integer> entry : pageUrlWithNumOfOutlinks.entrySet()) {
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

    // calculate page rank based on the relationship graph
    for (Map.Entry<String, Page> entry : pages.entrySet()) {
      Page page = entry.getValue();
      double newPageRank = page.getPageRank();
      ArrayList<Page> inlinkPages = page.getInlinkPages();
      for (int i = 0; i < inlinkPages.size(); i++) {
        Page inlinkPage = inlinkPages.get(i);
        double inlinkPageRank = inlinkPage.getPageRank();
        double inlinkNumOfOutlinks = inlinkPage.getNumOfOutlinks();
        newPageRank += (inlinkPageRank / inlinkNumOfOutlinks);
      }
      page.setPageRank(newPageRank);
    }        
  }
}