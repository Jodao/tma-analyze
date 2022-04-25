package eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the AttributeAggregationOperator abstract class
 * @author João Ribeiro
 */
public class Neutrality extends AttributeAggregationOperator {
    
    private final Logger LOGGER = LoggerFactory.getLogger(Neutrality.class);
    
    public Neutrality(){
        super();
    }
    
    @Override
    public double aggregateChildMetricData(double nodeMetricData, double childMetricData, double childWeight) {
        return nodeMetricData  +  (childMetricData * childWeight); 
    }
    
}
