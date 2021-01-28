import Exceptions.AppException;
import io.javalin.Javalin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final String baseURL="https://www.digikala.com/mag/";

    public static void setConnection(Connection connection) {
        Main.connection = connection;
    }

    private static Connection connection=null;
    public static void main(String[] args) {
        ArrayList<String> links= getLinks();

        String url = "jdbc:mysql://localhost:3306/tf_idf";
        String username = "root";
        String password = "";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            setConnection(connection);
            System.out.println("Database connected!");
            /*
            Statement statement=connection.createStatement();
            ResultSet myResults=statement.executeQuery("SELECT * FROM documents");
             while (myResults.next()){
                System.out.print("url = "+myResults.getString("url"));
                System.out.println(" ->  body = "+myResults.getString("body"));
            }*/

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        TF_IDF_Index index=new TF_IDF_Index();
       index.addAllDocuments(links);
//        while (true){
//            System.out.println("please Enter your Query");
//            Scanner scanner=new Scanner(System.in);
//            String query=scanner.nextLine();
//            ArrayList<DocumentLink> rankedDocumentLinks=index.rankDocumentsByTFIDF(query);
//            for (DocumentLink rankedDocumentLink : rankedDocumentLinks) {
//                System.out.println(rankedDocumentLink.getURL()+" : "+rankedDocumentLink.getTf_idf_score());
//            }
//        }

        Javalin app=Javalin.create().start(7000);
        RequestHandler requestHandler=new RequestHandler(index);
        app.get("/search/:query",requestHandler::normalSearch);
        app.get("/",requestHandler::homePage);
        app.get("/exact_search/:query",requestHandler::exactSearch);
        app.get("/spellchecker/:query",requestHandler::getTopQuerySuggestions);
        app.post("/document",requestHandler::addDocument);
        app.exception(AppException.class,(e, ctx) -> {
            ctx.contentType("application/json");
            ctx.status(e.getHttpCode());
            ctx.result(e.getMessage());
        });
    }


    public static ArrayList<String> getLinks(){
        ArrayList<String> links=new ArrayList<>();// Contains the desired links
        if (Files.exists(Path.of("saved_links.txt")))
        {
            FileInputStream fis= null;
            try {
                fis = new FileInputStream("saved_links.txt");
                Scanner scanner=new Scanner(fis);
                while (scanner.hasNextLine())
                    links.add(scanner.nextLine());
                return links;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        WebCrawlerWithDepth crawler= new WebCrawlerWithDepth();
        crawler.getPageLinks(Main.baseURL,2);
        links.addAll(crawler.getLinks());
        try {
            PrintWriter printWriter=new PrintWriter("saved_links.txt");
            for (String link : links) {
                printWriter.println(link);
            }
            printWriter.flush();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return links;
    }

    public static Connection getConnection() {
        return connection;
    }
}
