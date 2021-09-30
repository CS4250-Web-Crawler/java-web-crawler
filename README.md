# Java Web Crawler 

### Basic Requirements
1. Crawl the web with one seed URL 
2. Minimum of 500 pages with no duplicates 
3. For each URL 
  3.1 Downlaod page content (HTML tags excluding images and JavaScript/CSS)
4. Create a folder called *repository* with all the downloaded content from all the URLs.
5. Create a CSV file called *report* with all the URLs and their respective number of outlinks 

### Code 
For step 1 and step 2
  - Create global variables to hold the seed url, unique URLs (Use HashMap where the key is the URL and value is the number of outlinks), the count of unique URLs, and an array of strings where each string holds the html content (check if )
  - Create connection to jsoup.

For step 3 
  - Check if the HashMap contains the current URL. If false then proceed, else skip.
  - Retrieve HTML content (if connection was succesful), 
    - Check if the content is in the desired language. If it is not, then skip.
    - Convert document into a string and add to the array of strings 
  - Use jsoup connection and read url.
    - Insert URL and the respective outlinks into HashMap and Increment count of unique URLs.
  - Get all URLS in the page (if any)
    - For each URL
      - do above (recursive method)   
      
For step 4
  - When the crawler stops at URL count == 500
  - Create a file containing all the strings with the html content from each URL. 
  - Create a folder and store file into the folder.

For step 5
  - Use the HashMap containing the URL and Number of Outlinks to create the CSV file. 

