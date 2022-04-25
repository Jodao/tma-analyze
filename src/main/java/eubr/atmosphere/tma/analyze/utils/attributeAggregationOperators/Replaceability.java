package eubr.atmosphere.tma.analyze.utils.attributeAggregationOperators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the AttributeAggregationOperator abstract class
 * @author João Ribeiro
 */
public class Replaceability extends AttributeAggregationOperator {
    
    private final Logger LOGGER = LoggerFactory.getLogger(Replaceability.class);
    
    public Replaceability(){
        super();
    }
    
    @Override
    public double aggregateChildMetricData(double nodeMetricData, double childMetricData, double childWeight) {
        //NOT IMPLEMENTED
        LOGGER.error("[ERROR] -> Replaceability attribute aggregation operator is not implemented");
        return 0; 
    }
    
}
