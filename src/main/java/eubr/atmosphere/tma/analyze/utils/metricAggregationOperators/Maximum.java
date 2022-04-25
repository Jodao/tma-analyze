package eubr.atmosphere.tma.analyze.utils.metricAggregationOperators;

import java.util.ArrayList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the MetricAggregationOperator abstract class
 * @author João Ribeiro
 */
public class Maximum extends MetricAggregationOperator{

    private final Logger LOGGER = LoggerFactory.getLogger(Maximum.class);
    
    public Maximum(){
        super();
    }
    
    @Override
    public double aggregateData(ArrayList<Double> normalizedDataValues) {
        return Collections.max(normalizedDataValues);
    }
    
}
