public class DocumentLink implements Comparable {
    private final double tf_idf_score;

    public double getTf_idf_score() {
        return tf_idf_score;
    }

    public String getURL() {
        return URL;
    }

    private final String URL;

    public DocumentLink(double tf_idf_score, String URL) {
        this.tf_idf_score = tf_idf_score;
        this.URL = URL;
    }

    @Override
    public int compareTo(Object o) {
        DocumentLink documentLink=(DocumentLink)o;
        return -1*Double.compare(tf_idf_score, documentLink.getTf_idf_score());
    }
}
