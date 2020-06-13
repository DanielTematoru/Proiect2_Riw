package Proiect_02;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Site {


    public String getTitle(Document doc) 
    {
        return doc.title();
    }
    
    public String getRobots(Document doc)
    {
        Element robots = doc.selectFirst("meta[name=robots]");
        String robotsString = "";
        if (robots != null) {
            robotsString = robots.attr("content");
        }
        return robotsString;
    }
    
    public String getDescription(Document doc)
    {
        Element description = doc.selectFirst("meta[name=description]");
        String descriptionString = "";
        if (description != null) {
            descriptionString = description.attr("content");
        }
        return descriptionString;
    }

}
