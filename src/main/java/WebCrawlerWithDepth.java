import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class WebCrawlerWithDepth {

    public HashSet<String> getLinks() {
        return links;
    }

    private final HashSet<String> links;

    public WebCrawlerWithDepth() {
        links = new HashSet<>();
    }

    public void getPageLinks(String URL, int MAX_DEPTH){
        getLinksFromPage(URL,0,MAX_DEPTH);
    }
    
    private void getLinksFromPage(String URL, int depth,int MAX_DEPTH) {
        if (!links.contains(URL) && (depth < MAX_DEPTH)) {
            try {
                links.add(URL);
                System.out.println(URL);
                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                depth++;
                for (Element page : linksOnPage) {
                    getLinksFromPage(page.attr("abs:href"), depth, MAX_DEPTH);
                }
            } catch (IOException e) {
                //System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }
}