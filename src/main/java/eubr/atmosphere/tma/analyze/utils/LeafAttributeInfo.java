package eubr.atmosphere.tma.analyze.utils;

import eubr.atmosphere.tma.analyze.utils.metricAggregationOperators.Average;
import eubr.atmosphere.tma.analyze.utils.metricAggregationOperators.Maximum;
import eubr.atmosphere.tma.analyze.utils.metricAggregationOperators.MetricAggregationOperator;
import eubr.atmosphere.tma.analyze.utils.metricAggregationOperators.Minimum;
import eubr.atmosphere.tma.analyze.utils.metricAggregationOperators.Sum;
import eubr.atmosphere.tma.analyze.utils.normalizationMethods.MinMax;
import eubr.atmosphere.tma.analyze.utils.normalizationMethods.NormalizationMethod;

/**
 * This class holds additional information in the case a Quality Model's tree node is a leaf attribute.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */

public class LeafAttributeInfo{
    private MetricAggregationOperator metricAggregationOperator;
    private int descriptionId;
    private int numSamples;
    private NormalizationMethod normalizationMethod;
    private int normalizationKind;
    private double minimumThreshold;
    private double maximumThreshold;

    public LeafAttributeInfo() {
    }
    
    public LeafAttributeInfo(int metricAggregationOperator, int descriptionId, int numSamples,
            String normalizationMethod, int normalizationKind, float minimumThreshold, float maximumThreshold) {
                    
        this.metricAggregationOperator = defineMetricAggregationOperator(metricAggregationOperator);
        this.descriptionId = descriptionId;
        this.numSamples = numSamples;
        this.normalizationMethod = defineNormalizationMethod(normalizationMethod);
        this.normalizationKind = normalizationKind;
        this.minimumThreshold = minimumThreshold;
        this.maximumThreshold = maximumThreshold;
    }

    public MetricAggregationOperator getMetricAggregationOperator() {
        return metricAggregationOperator;
    }

    public void setMetricAggregationOperator(int metricAggregationOperator) {
        this.metricAggregationOperator = defineMetricAggregationOperator(metricAggregationOperator);
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

    public NormalizationMethod getNormalizationMethod() {
        return normalizationMethod;
    }

    public void setNormalizationMethod(String normalizationMethod) {
        this.normalizationMethod = defineNormalizationMethod(normalizationMethod);
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

    private NormalizationMethod defineNormalizationMethod(String normalizationMethod){
        NormalizationMethod nm = null;
        
        switch(normalizationMethod){
            case "MIN-MAX":
                nm = new MinMax();
                break;
        }
        
        return nm;
    }
    
    private MetricAggregationOperator defineMetricAggregationOperator(int metricAggregationOperator){
        MetricAggregationOperator mao = null;
        
        switch(metricAggregationOperator){
            case 0: //"AVERAGE":
                mao = new Average();
                break;
            case 1: //"MINIMUM":
                mao = new Minimum();
                break;
            case 2: //"MAXIMUM":
                mao = new Maximum();
                break;
            case 3: //"SUM":
                mao = new Sum();
                break;
        }
        
        return mao;
    }
    
}
