
public class page {
	String pageName;
	int numberOfInlinks = 0;
	int pageScore = 0;
	
	public page (int numberOfPages, String pageURL) {
		pageScore = 1/numberOfPages;
		pageName = pageURL;
	}
	
	
	
	public void setInlinkCount(int inlinkCount) {
		numberOfInlinks = inlinkCount;
	}
	
	public int getInlinkCount(int inlinkCount) {
		return numberOfInlinks;
	}
	
	
}
