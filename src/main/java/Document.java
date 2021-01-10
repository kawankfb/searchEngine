import java.util.concurrent.atomic.AtomicInteger;

public class Document {
    private static final AtomicInteger nextId=new AtomicInteger(0);
    private int id;
    private String URL;
    private String body;

    public Document(String URL, String body) {
        this.URL = URL;
        this.body = body;
        id=nextId.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getURL() {
        return URL;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", URL='" + URL + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

}
