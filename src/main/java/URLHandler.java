import com.mysql.cj.xdevapi.JsonArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class URLHandler {
    public String getURL() {
        return URL;
    }

    private String URL;

    public URLHandler(String URL) {
        this.URL = URL;
    }
    public Document parseURL(){
        return new Document(URL,getText());
    }

    private String getURLBody(){
        String str=DownloadWebPage(URL);
        org.jsoup.nodes.Document body = Jsoup.parse(str);
        ArrayList<String> texts=new ArrayList<>();
        for (int i = 0; i <body.select("p").size() ; i++) {

            Element link = body.select("p").get(i);
            if (!link.hasText())
                break;
            else
                texts.add(link.text());
        }
        for (int i = 0; i <body.select("ol").size() ; i++) {
            Element olLink=body.select("ol").get(i);
            for (int j = 0; j <olLink.select("li").size() ; j++) {
                Element link = olLink.select("li").get(j);
                if (!link.hasText())
                    break;
                else
                    texts.add(link.text());
            }

        }
        StringBuilder builder=new StringBuilder();
        for (String text : texts) {
            builder.append(text);
            builder.append(' ');
        }
        return builder.toString();

    }

    private String getText(){
        org.jsoup.nodes.Document doc = Jsoup.parse(DownloadWebPage(URL));
        String bodyOfURL=doc.text();
        ArrayList<String> tokens= Tokenizer.tokenize(CharacterNormalizer.normalizeString(bodyOfURL));
        Map<String,Integer> tableOfTokens=new HashMap<>();
        for (String token : tokens) {
            tableOfTokens.putIfAbsent(token,0);
            tableOfTokens.put(token,tableOfTokens.get(token)+1);
        }
        //convert into JSON array
        JSONObject JO=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        for (String s : tableOfTokens.keySet()) {
            JSONObject temp=new JSONObject();
            temp.put("count",tableOfTokens.get(s));
            temp.put("token",s);
            jsonArray.add(temp);

        }
        JO.put("bag_of_words",jsonArray);
        String jsonString= JO.toJSONString();
        System.out.println(jsonString);
        return jsonString;
        /*StringBuilder builder=new StringBuilder();
        for (Element element : doc.getAllElements()) {
            if(element.nodeName().equals("p") && element.childNodes().size()==0){
                System.out.println(element.text());
                builder.append(element.text());
            }
            else if(element.childNodes().size()>0 && element.childNode(0).nodeName().equals("#text")){
                System.out.println(element.text());
                builder.append(element.text());
            }
        }
        return builder.toString();

         */
    }

    private String DownloadWebPage(String webpage)
    {
        try {
            java.net.URL url = new URL(webpage);
            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder builder=new StringBuilder();
            String line;
            while ((line = readr.readLine()) != null) {
                builder.append(line);
                builder.append(' ');
            }

            readr.close();
            return builder.toString();
        }
        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        }
        catch (IOException ie) {
            System.out.println("IOException raised");
        }
        return "";
    }
}
