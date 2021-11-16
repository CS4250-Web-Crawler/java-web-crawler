import java.util.ArrayList;

public class Page {
  // mainly used to identify the page
  private String pageUrl;  
  // used for the page rank formula
  private double currPageRank;
  private double newPageRank;  
  private int numOfOutlinks;
  // the inlink pages contain a link that points to the current page
  private ArrayList<Page> inlinkPages = new ArrayList<>();  

  public Page(int numOfPages, String pageUrl) {
      this.pageUrl = pageUrl;
      this.currPageRank = 1.0/numOfPages;
      this.newPageRank = 1.0/numOfPages;
      this.numOfOutlinks = 0;
      inlinkPages = new ArrayList<>();      
  }

  public String getPageUrl() {
    return pageUrl;
  }

  public void addInlinkPage(Page newInlinkPage) {
    this.inlinkPages.add(newInlinkPage);
  }
  
  public ArrayList<Page> getInlinkPages() {
    return this.inlinkPages;
  }

  public void setPageRank(double calcPageRank) {
      currPageRank = newPageRank;
      newPageRank = calcPageRank;
  }

  public double getPageRank() {
    return this.currPageRank;
  }

  public double displayPageRank() {
    return this.newPageRank;
  }

  public void setNumOfOutlinks(int numOfOutlinks) {
    this.numOfOutlinks = numOfOutlinks;
  }

  public int getNumOfOutlinks() {
    return this.numOfOutlinks;
  }
}