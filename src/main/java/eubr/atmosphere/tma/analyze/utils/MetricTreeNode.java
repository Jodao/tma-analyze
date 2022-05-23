package eubr.atmosphere.tma.analyze.utils;

import eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators.AttributeAggregationOperator;
import eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators.Neutrality;
import eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators.Replaceability;
import eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators.Simultaneity;
import java.util.ArrayList;
import java.util.HashMap;

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
    private AttributeAggregationOperator attributeAggregationOperator;

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

    public AttributeAggregationOperator getAttributeAggregationOperator() {
        return attributeAggregationOperator;
    }

    public void setAttributeAggregationOperator(int attributeAggregationOperator) {
        AttributeAggregationOperator aao = null;
        
        switch(attributeAggregationOperator){
            case 0: //NEUTRALITY
                aao = new Neutrality();
                break;
            case 1: //SIMULTANEITY
                aao = new Simultaneity();
                break;
            case 2: //REPLACEABILITY
                aao = new Replaceability();
                break;
        }
        
        this.attributeAggregationOperator = aao;
    }
    
    public HashMap<Integer,Double> convertToScoreKafka(){
        HashMap<Integer,Double> scoreToPublish = new HashMap() ;
        //start by adding the current class node info and then its childs
        scoreToPublish.put(metricId,metricData);
        for(MetricTreeNode child : childMetrics){
            addChildsToScoreKafka(scoreToPublish, child);
        }
        return scoreToPublish;
    }
    
    private void addChildsToScoreKafka(HashMap<Integer,Double> scoreToPublish, MetricTreeNode node){
        scoreToPublish.put(node.getMetricId(),node.getMetricData());
        for(MetricTreeNode child : node.getChildMetrics()){
            addChildsToScoreKafka(scoreToPublish, child);
        }
    }
}
