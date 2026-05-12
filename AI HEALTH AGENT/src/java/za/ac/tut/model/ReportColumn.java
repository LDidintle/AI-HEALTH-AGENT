package za.ac.tut.model;

public class ReportColumn {

    private final String key;
    private final String label;

    public ReportColumn(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }
}
