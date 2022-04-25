package eubr.atmosphere.tma.analyze.utils.metricAggregationOperators;

import java.util.ArrayList;

/**
 * Abstract class representing the methods a metric aggregation operator must implement
 * @author João Ribeiro
 */
public abstract class MetricAggregationOperator {
    
    public MetricAggregationOperator(){
        
    }
    
    public abstract double aggregateData(ArrayList<Double> normalizedDataValues);
}
