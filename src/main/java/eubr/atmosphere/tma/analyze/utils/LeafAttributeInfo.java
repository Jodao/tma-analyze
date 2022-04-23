package eubr.atmosphere.tma.analyze.utils;

/**
 * This class holds additional information in the case a Quality Model's tree node is a leaf attribute.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */

public class LeafAttributeInfo{
    private int metricAggregationOperator;
    private int descriptionId;
    private int numSamples;
    private String normalizationMethod;
    private int normalizationKind;
    private double minimumThreshold;
    private double maximumThreshold;

    public LeafAttributeInfo() {
    }
    
    public LeafAttributeInfo(int metricAggregationOperator, int descriptionId, int numSamples,
            String normalizationMethod, int normalizationKind, float minimumThreshold, float maximumThreshold) {
    this.metricAggregationOperator =   metricAggregationOperator;
    this.descriptionId = descriptionId;
    this.numSamples =   numSamples;
    this.normalizationMethod =   normalizationMethod;
    this.normalizationKind =   normalizationKind;
    this.minimumThreshold =   minimumThreshold;
    this.maximumThreshold =   maximumThreshold;
    }

    public int getMetricAggregationOperator() {
        return metricAggregationOperator;
    }

    public void setMetricAggregationOperator(int metricAggregationOperator) {
        this.metricAggregationOperator = metricAggregationOperator;
    }
    
    public int getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(int descriptionId) {
        this.descriptionId = descriptionId;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public String getNormalizationMethod() {
        return normalizationMethod;
    }

    public void setNormalizationMethod(String normalizationMethod) {
        this.normalizationMethod = normalizationMethod;
    }

    public int getNormalizationKind() {
        return normalizationKind;
    }

    public void setNormalizationKind(int normalizationKind) {
        this.normalizationKind = normalizationKind;
    }

    public double getMinimumThreshold() {
        return minimumThreshold;
    }

    public void setMinimumThreshold(double minimumThreshold) {
        this.minimumThreshold = minimumThreshold;
    }

    public double getMaximumThreshold() {
        return maximumThreshold;
    }

    public void setMaximumThreshold(double maximumThreshold) {
        this.maximumThreshold = maximumThreshold;
    }

    
    
}
