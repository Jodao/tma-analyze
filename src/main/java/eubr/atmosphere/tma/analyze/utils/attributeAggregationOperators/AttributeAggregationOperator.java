package eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators;

/**
 * Abstract class representing the methods an attribute aggregation operator must implement
 * @author João Ribeiro
 */
public abstract class AttributeAggregationOperator {
    
    public AttributeAggregationOperator(){
        
    }
    
    public abstract double aggregateChildMetricData(double nodeMetricData, double childMetricData, double childWeight);
}
