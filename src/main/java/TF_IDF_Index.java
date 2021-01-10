import javax.print.Doc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class TF_IDF_Index {

    private HashMap<Integer,Document> docStore=new HashMap<>();
    private HashSet<String> distinctTokens=new HashSet<>();

    public void addDocument(String URL,String body){
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

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Document> getQuery(String query){
        ArrayList<Document> results=new ArrayList<>();

        return results;
    }
}
