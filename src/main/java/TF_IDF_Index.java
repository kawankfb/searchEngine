import Exceptions.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class TF_IDF_Index {

    private HashMap<Integer,Document> docStore=new HashMap<>();
    private HashSet<String> distinctTokens=new HashSet<>();

    private void addDocument(String URL,String body){
        Document document=new Document(URL,body);
        docStore.put(document.getId(),document);
        Statement statement= null;
        try {
            statement = Main.getConnection().createStatement();
            StringBuilder queryBuilder=new StringBuilder("insert into documents (url, body) values ('");
            queryBuilder.append(URL);
            queryBuilder.append("','");
            queryBuilder.append(body);
            queryBuilder.append("');");
            statement.execute(queryBuilder.toString());


            int result=0;
            ArrayList<String> tokens= getDocumentTokens(body);
            for (String token : tokens) {
                statement = Main.getConnection().createStatement();
                ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM df WHERE token=\""+token+"\";");
                while (myResults.next()) {
                    result=myResults.getInt(1);
                }
                if (result==0){
                    statement = Main.getConnection().createStatement();
                     queryBuilder=new StringBuilder("insert into df (token, count) values ('");
                    queryBuilder.append(token);
                    queryBuilder.append("','");
                    queryBuilder.append(1);
                    queryBuilder.append("');");
                    statement.execute(queryBuilder.toString());
                }
                else if (result==1){
                    long tokenCount=1;
                    statement = Main.getConnection().createStatement();
                     myResults = statement.executeQuery("SELECT count FROM df WHERE token=\""+token+"\";");
                    while (myResults.next()) {
                        tokenCount=myResults.getLong(1);
                    }
                    statement = Main.getConnection().createStatement();
                    queryBuilder=new StringBuilder("UPDATE df SET count=");
                    queryBuilder.append(tokenCount+1);
                    queryBuilder.append(" WHERE token=\"");
                    queryBuilder.append(token);
                    queryBuilder.append("\";");

                    statement.execute(queryBuilder.toString());
                }
            }
            BigramIndex bigramIndex=new BigramIndex();
            for (String token : tokens) {
                long max= getTokenCountInJsonBody(token,body);
                for (long i = 0; i <max ; i++) {
                    bigramIndex.add(token);
                }
            }
            bigramIndex.insertBigramIndexToDB();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Document> getQuery(String query){
        ArrayList<Document> results=new ArrayList<>();

        return results;
    }
    private double calculateIDF(long docFrequency,long docCount){
        if (docFrequency==0)
            return 0;
        return Math.log10((double)docCount/docFrequency);
    }
    private int getDocumentCount(){
        int result=-1;
        Statement statement= null;
        try {
            statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM documents");
            while (myResults.next()) {
                result=myResults.getInt(1);
            }
        }
        catch (Exception e){

        }
        return result;
    }
    public double getTFIDFScore(String token,int docID){
        long allDocumentsCount=getDocumentCount();
        long tokenCount=getTokenCountInDocument(token,docID);
        long documentFrequency=getDocumentFrequencyOfToken(token);
        double invertDocumentFrequency= calculateIDF(documentFrequency,allDocumentsCount);
        return tokenCount*invertDocumentFrequency;
    }
    private long getTokenCountInDocument(String token,int docId){
        long result=0;
        Statement statement= null;
        try {
            statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT body FROM documents WHERE id="+docId+';');
            if (myResults.next()) {
                String jsonString=myResults.getString(1);
                JSONParser parser =new JSONParser();
                JSONObject jsonObject=(JSONObject) parser.parse(jsonString);
                JSONArray jsonArray= (JSONArray) jsonObject.get("bag_of_words");
                Iterator iterator= jsonArray.iterator();
                while (iterator.hasNext()){
                    JSONObject temp= (JSONObject) iterator.next();
                    if (token.equals(temp.get("token"))) {
                        result = (long) temp.get("count");
                        break;
                    }
                    }
            }
        }
        catch (Exception e){

        }
        return result;
    }
    private ArrayList<String> getDocumentTokens(String jsonString){
        ArrayList<String> results=new ArrayList<String>();
        Statement statement= null;

                JSONParser parser =new JSONParser();
        JSONObject jsonObject= null;
        try {
            jsonObject = (JSONObject) parser.parse(jsonString);
            JSONArray jsonArray= (JSONArray) jsonObject.get("bag_of_words");
            Iterator iterator= jsonArray.iterator();
            while (iterator.hasNext()){
                JSONObject temp= (JSONObject) iterator.next();
                results.add((String) temp.get("token"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return results;
    }
    private long getTokenCountInJsonBody(String token,String jsonString){
        long result=0;
        try {
            JSONParser parser =new JSONParser();
            JSONObject jsonObject=(JSONObject) parser.parse(jsonString);
            JSONArray jsonArray= (JSONArray) jsonObject.get("bag_of_words");
            Iterator iterator= jsonArray.iterator();
            while (iterator.hasNext()){
                JSONObject temp= (JSONObject) iterator.next();
                if (token.equals(temp.get("token"))) {
                    result = (long) temp.get("count");
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private long getDocumentFrequencyOfToken(String token){
        long result=0;
        try{
        Statement statement = Main.getConnection().createStatement();
        ResultSet myResults = statement.executeQuery("SELECT COUNT(*) FROM df WHERE token=\""+token+"\";");
        while (myResults.next()) {
            result=myResults.getInt(1);
        }if (result==1) {
            long tokenCount = 1;
            statement = Main.getConnection().createStatement();
            myResults = statement.executeQuery("SELECT count FROM df WHERE token=\"" + token + "\";");
            while (myResults.next()) {
                tokenCount = myResults.getLong(1);
                return tokenCount;
            }
        }}catch (Exception e){

        }
        return result;
    }
    public ArrayList<DocumentLink> rankDocumentsByTFIDF(String query){
        ArrayList<DocumentLink> results=new ArrayList<DocumentLink>();
        ArrayList<String> tokens=Tokenizer.tokenize(CharacterNormalizer.normalizeString(query));
        try{
            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT id,url FROM documents;");
            while (myResults.next()) {
                int id=myResults.getInt(1);
                String url=myResults.getString(2);
                double sum=0;
                for (String token : tokens) {
                    sum=sum+getTFIDFScore(token,id);
                }
                results.add(new DocumentLink(sum,url));
            }
            }catch (Exception e){
                e.printStackTrace();
        }
        results.sort(DocumentLink::compareTo);
        return results;
    }
    public ArrayList<String> getIndexedURLs() {
        ArrayList<String> result = new ArrayList<>();
        try {
            Statement statement = Main.getConnection().createStatement();
            ResultSet myResults = statement.executeQuery("SELECT url FROM documents;");
            while (myResults.next()) {
                String url = myResults.getString(1);
                result.add(url);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


        return result;
    }

    public void addAllDocuments(List<String> urls){
        ArrayList<String> indexedURLs=getIndexedURLs();
        int count=0;
        for (String url : urls) {
            if (count++>13)
                break;
            if (indexedURLs.contains(url))
                continue;
            URLHandler temp=new URLHandler(url);
            addDocument(temp.getURL(), temp.parseURL().getBody());
        }
    }
    public void addDocumentByURL(String url) throws AppException{
        ArrayList<String> indexedURLs=getIndexedURLs();
            if (indexedURLs.contains(url))
throw new UrlAlreadyExistsException();
        URLHandler temp=new URLHandler(url);
            addDocument(temp.getURL(), temp.parseURL().getBody());

    }

    public void updateDocument(String url) throws AppException {
        if (!getIndexedURLs().contains(url))
        throw new UrlNotExistsException();
            deleteDocument(url);
        addDocumentByURL(url);
    }

    public void deleteDocument(String url) throws DeleteUnsuccessfulException, UrlAlreadyDeletedException {
        if (!getIndexedURLs().contains(url))
            throw new UrlAlreadyDeletedException();
            Statement statement= null;
            try {
                statement = Main.getConnection().createStatement();

                StringBuilder queryBuilder=new StringBuilder("DELETE FROM documents WHERE url=\""+url+"\";");
                statement.execute(queryBuilder.toString());
                if (getIndexedURLs().contains(url))
                getIndexedURLs().remove(url);
            } catch (SQLException throwables) {
                throw new DeleteUnsuccessfulException();
            }


        }

}
