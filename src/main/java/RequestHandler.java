import Exceptions.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mysql.cj.xdevapi.JsonArray;
import io.javalin.http.Context;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RequestHandler {
    TF_IDF_Index index;
    BigramIndex bigramIndex;
    private Cache<String,ArrayList<DocumentLink>> searchCache= CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build();

    private Cache<String,ArrayList<String>> spellCorrectionCache= CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build();


    public RequestHandler(TF_IDF_Index index) {
        this.index = index;
        bigramIndex=new BigramIndex();
    }
    public void search(Context ctx,String query,String newQuery){
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(searchQuery(query,newQuery).toJSONString());
    }

    public JSONObject searchQuery(String query,String newQuery){
        boolean queryCorrected=!query.equals(newQuery);
        ArrayList<DocumentLink> results;
        results=searchCache.getIfPresent(query);

        if (results==null){
            results = index.rankDocumentsByTFIDF(newQuery);
            searchCache.put(query,results);
        }

        results.removeIf(result -> result.getTf_idf_score() < 0.0000000001);
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        for (int i = 0; i < results.size() && i<10; i++) {
            JSONObject temp=new JSONObject();
            temp.put("url",results.get(i).getURL());
            temp.put("score",results.get(i).getTf_idf_score());
            temp.put("rank",i+1);
            jsonArray.add(temp);
        }
        jsonObject.put("results",jsonArray);
        if (queryCorrected) {
            jsonObject.put("results for ",newQuery);
            jsonObject.put("you searched for",query);
        } else {
            jsonObject.put("results for ",query);

        }
        return jsonObject;
    }

    public void normalSearch(Context ctx){
        String query=ctx.pathParam("query");
        String newQuery=topQuerySuggestionsAsArrayList(query).get(0);
        search(ctx,query,newQuery);
    }
    public void exactSearch(Context ctx){
        String query=ctx.pathParam("query");
        search(ctx,query,query);
    }

    public void getTopQuerySuggestions(Context ctx) {
        String query=ctx.pathParam("query");
        ArrayList<String> results=topQuerySuggestionsAsArrayList(query);
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        for (int i = 0; i < results.size() && i<10; i++) {
            JSONObject temp=new JSONObject();
            temp.put("query",results.get(i));
            temp.put("rank",i+1);
            jsonArray.add(temp);
        }
        jsonObject.put("results",jsonArray);
        jsonObject.put("query",query);
        ctx.status(200);
        if (results.isEmpty())
            ctx.status(404);
        ctx.contentType("application/json");
        ctx.result(jsonObject.toJSONString());
    }

    public ArrayList<String> topQuerySuggestionsAsArrayList(String query){

        ArrayList<String> topQuerySuggestions;
        topQuerySuggestions=spellCorrectionCache.getIfPresent(query);
        if (topQuerySuggestions==null) {
            topQuerySuggestions = (ArrayList<String>) bigramIndex.getTopQuerySuggestions(query, 10);
            spellCorrectionCache.put(query,topQuerySuggestions);
        }
        return topQuerySuggestions;
    }

    public void addDocument(Context ctx) throws AppException {
        String url=ctx.formParam("url");
        urlValidator(url);
        if (!url.startsWith("http://") && !url.startsWith("https://")){
            throw new BadUrlException();
        }
        if (index.getIndexedURLs().contains(url))
            throw new UrlAlreadyExistsException();
        index.addDocumentByURL(url);
        if (index.getIndexedURLs().contains(url)){
            ctx.status(201);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("message","Document was added to our collection");
            jsonObject.put("url",url);
            ctx.result(jsonObject.toJSONString());
        }
        else {
            ctx.status(400);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("error","something went wrong");
            jsonObject.put("url",url);
            ctx.result(jsonObject.toJSONString());
        }
    }

    public void homePage(Context context) throws IOException {
    StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>");
        stringBuilder.append("<html lang=\"fa-IR\">");
        stringBuilder.append("<meta charset=\"utf-8\"");
        stringBuilder.append('\n');
        stringBuilder.append("<head>");
        stringBuilder.append('\n');
        stringBuilder.append("</head>");
        stringBuilder.append('\n');
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("<form action=\"/result\" accept-charset=\"utf-8\" method=\"post\">\n" +
                "        <input type=\"text\" placeholder=\"search\" name=\"query\" >\n" +
                "        <br><br>\n" +
                "        <input type=\"submit\" name=\"submit\">\n" +
                "    </form>");
                stringBuilder.append("<br>");
        stringBuilder.append("<form action=\"/exact_result\" accept-charset=\"utf-8\" method=\"post\">\n" +
                "        <input type=\"text\" placeholder=\"exact search\" name=\"query\" >\n" +
                "        <br><br>\n" +
                "        <input type=\"submit\" name=\"submit\">\n" +
                "    </form>");
        stringBuilder.append("<br>");
        stringBuilder.append("<form action=\"/spell_result\" accept-charset=\"utf-8\" method=\"post\">\n" +
                "        <input type=\"text\" accept-charset=\"utf-8\" placeholder=\"تصحیح خطا\" name=\"query\" >\n" +
                "        <br><br>\n" +
                "        <input type=\"submit\"  name=\"submit\">\n" +
                "    </form>");
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("</body>");
        stringBuilder.append('\n');
        stringBuilder.append("</html>");
        context.contentType("text/html");
        context.result(stringBuilder.toString());
    }

    public void showSearchResult(Context context) {
        String query=context.formParam("query");
        String newQuery=topQuerySuggestionsAsArrayList(query).get(0);
        JSONObject jsonObject= searchQuery(query,newQuery);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("<html>");
        stringBuilder.append('\n');
        stringBuilder.append("<meta charset=\"utf-8\"");
        stringBuilder.append('\n');
        stringBuilder.append("<head>");
        stringBuilder.append('\n');
        stringBuilder.append("</head>");
        stringBuilder.append('\n');
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("search result");
        stringBuilder.append("<br>");

        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("</body>");
        stringBuilder.append('\n');
        stringBuilder.append("</html>");
        context.contentType("text/html");
        context.result(stringBuilder.toString());
    }

    public void showExactSearchResult(Context context) {
        String query=context.formParam("query");
        JSONObject jsonObject=searchQuery(query,query);
        JSONArray jsonArray= (JSONArray) jsonObject.get("results");
        DocumentLink[] documentLinks=new DocumentLink[jsonArray.size()];
        for (int i = 0; i < documentLinks.length; i++) {
            JSONObject temp=(JSONObject) jsonArray.get(i);
            documentLinks[i]=new DocumentLink(Double.parseDouble(temp.get("score").toString()),temp.get("url").toString());
        }

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("<html>");
        stringBuilder.append('\n');
        stringBuilder.append("<meta charset=\"utf-8\"");
        stringBuilder.append('\n');
        stringBuilder.append("<head>");
        stringBuilder.append('\n');
        stringBuilder.append("</head>");
        stringBuilder.append('\n');
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("exact search result");
        stringBuilder.append("<br>");
        stringBuilder.append(documentLinks[0].getURL() +" score : "+documentLinks[0].getTf_idf_score());
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("</body>");
        stringBuilder.append('\n');
        stringBuilder.append("</html>");
        context.contentType("text/html");
        context.result(stringBuilder.toString());
    }

    public void showSpellCorrectionResult(Context context) {
        String query=context.formParam("query");
        StringBuilder stringBuilder=new StringBuilder();
        ArrayList<String> suggestions=topQuerySuggestionsAsArrayList(query);
        stringBuilder.append("<html>");
        stringBuilder.append('\n');
        stringBuilder.append("<meta charset=\"utf-8\"");
        stringBuilder.append('\n');
        stringBuilder.append("<head>");
        stringBuilder.append('\n');
        stringBuilder.append("</head>");
        stringBuilder.append('\n');
        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("spell result");
        stringBuilder.append("<br>");
        for (String suggestion : suggestions) {
            stringBuilder.append("<h1 accept-charset=\"utf-8\">");
            stringBuilder.append(" "+suggestion+" ");
            stringBuilder.append("</h1>");
            stringBuilder.append("<br>");
        }

        stringBuilder.append("<body>");
        stringBuilder.append('\n');
        stringBuilder.append("</body>");
        stringBuilder.append('\n');
        stringBuilder.append("</html>");
        context.contentType("text/html");
        context.result(stringBuilder.toString());
    }

    public void deleteUrl(Context context) throws AppException {
    String url=context.formParam("url");
        urlValidator(url);
    index.deleteDocument(url);
    }

    public void updateDocument(Context context) throws AppException {
        String url=context.formParam("url");
        urlValidator(url);
        index.updateDocument(url);
    }
    public static void urlValidator(String url) throws BadUrlException {
        // Get an UrlValidator using default schemes
        UrlValidator defaultValidator = new UrlValidator();
         if (!defaultValidator.isValid(url)){
             throw new BadUrlException();
         }
    }
}
