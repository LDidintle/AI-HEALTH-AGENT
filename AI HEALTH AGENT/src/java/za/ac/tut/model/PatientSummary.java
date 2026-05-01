package za.ac.tut.model;

import java.math.BigDecimal;

public class PatientSummary {

    private BigDecimal averagePulse;
    private BigDecimal averageTemperature;
    private BigDecimal averageSystolic;
    private BigDecimal averageDiastolic;
    private int readingCount;
    private String prediction;

    public BigDecimal getAveragePulse() { return averagePulse; }
    public void setAveragePulse(BigDecimal averagePulse) { this.averagePulse = averagePulse; }

    public BigDecimal getAverageTemperature() { return averageTemperature; }
    public void setAverageTemperature(BigDecimal averageTemperature) { this.averageTemperature = averageTemperature; }

    public BigDecimal getAverageSystolic() { return averageSystolic; }
    public void setAverageSystolic(BigDecimal averageSystolic) { this.averageSystolic = averageSystolic; }

    public BigDecimal getAverageDiastolic() { return averageDiastolic; }
    public void setAverageDiastolic(BigDecimal averageDiastolic) { this.averageDiastolic = averageDiastolic; }

    public int getReadingCount() { return readingCount; }
    public void setReadingCount(int readingCount) { this.readingCount = readingCount; }

    public String getPrediction() { return prediction; }
    public void setPrediction(String prediction) { this.prediction = prediction; }
}
