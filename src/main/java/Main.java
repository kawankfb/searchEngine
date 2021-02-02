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
    private static final String baseURL="http://urmia.ac.ir/";

    public static void setConnection(Connection connection) {
        Main.connection = connection;
    }

    private static Connection connection=null;
    public static void main(String[] args) {
        TF_IDF_Index index=new TF_IDF_Index();
        Javalin app=Javalin.create().start(7000);
        RequestHandler requestHandler=new RequestHandler(index);
        app.get("/search/:query",requestHandler::normalSearch);
        app.get("/",requestHandler::homePage);
        app.get("/exact_search/:query",requestHandler::exactSearch);
        app.get("/spellchecker/:query",requestHandler::getTopQuerySuggestions);
        app.post("/document",requestHandler::addDocument);
        app.post("/result",requestHandler::showSearchResult);
        app.post("/exact_result",requestHandler::showExactSearchResult);
        app.post("/spell_result",requestHandler::showSpellCorrectionResult);
        app.delete("/document",requestHandler::deleteUrl);
        app.post("/delete",requestHandler::deleteUrl);
        app.put("/document",requestHandler::updateDocument);
        app.post("/update",requestHandler::updateDocument);
        app.post("/add",requestHandler::addDocument);
        app.exception(AppException.class,(e, ctx) -> {
            ctx.contentType("application/json");
            ctx.status(e.getHttpCode());
            ctx.result(e.getMessage());
        });

        ArrayList<String> links=new ArrayList<>();
        links = getLinks();


        ArrayList<String> possibleMySQLAddresses=new ArrayList<>();
        possibleMySQLAddresses.add("localhost");
        possibleMySQLAddresses.add("127.0.0.1");
        possibleMySQLAddresses.add("172.17.0.1");
        possibleMySQLAddresses.add("172.17.0.2");
        possibleMySQLAddresses.add("172.17.0.3");
        possibleMySQLAddresses.add("0.0.0.0");
        for (String possibleMySQLAddress : possibleMySQLAddresses) {
            try {
                String url = "jdbc:mysql://"+possibleMySQLAddress+":3306/tf_idf";
                String username = "root";
                String password = "kawan1378";
                Connection connection = DriverManager.getConnection(url, username, password);
                setConnection(connection);
                System.out.println("Database connected!");
                break;
            } catch (SQLException e) {
                System.out.println("Cannot connect the database! : "+possibleMySQLAddress);
            }
        }

       index.addAllDocuments(links);


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
        crawler.getPageLinks(Main.baseURL,3);
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
