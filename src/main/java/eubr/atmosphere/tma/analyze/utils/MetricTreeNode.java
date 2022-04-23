package eubr.atmosphere.tma.analyze.utils;

import java.util.ArrayList;

/**
 * This class represents a Quality Model's metrics tree node.
 * <p>
 * @author João Ribeiro <jdribeiro@student.dei.uc.pt>
 */
public class MetricTreeNode{
    private int metricId;
    private double weight;
    private double threshold;
    private ArrayList<MetricTreeNode> childMetrics;
    private LeafAttributeInfo leafAttribute;
    private double metricData = 0.0;
    private int attributeAggregationOperator;

    public MetricTreeNode() {
    }
    
    public MetricTreeNode(int metricId) {
    this.metricId = metricId;
    }

   
    public int getMetricId() {
        return metricId;
    }

    public void setMetricId(int metricId) {
        this.metricId = metricId;
    }
    
    public ArrayList<MetricTreeNode> getChildMetrics() {
        return childMetrics;
    }

    public void setChildMetrics(ArrayList<MetricTreeNode> childMetrics) {
        this.childMetrics = childMetrics;
    }

    public LeafAttributeInfo getLeafAttribute() {
        return leafAttribute;
    }

    public void setLeafAttribute(LeafAttributeInfo leafAttribute) {
        this.leafAttribute = leafAttribute;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getMetricData() {
        return metricData;
    }

    public void setMetricData(double metricData) {
        this.metricData = metricData;
    }

    public int getAttributeAggregationOperator() {
        return attributeAggregationOperator;
    }

    public void setAttributeAggregationOperator(int attributeAggregationOperator) {
        this.attributeAggregationOperator = attributeAggregationOperator;
    }
    
}
