package eubr.atmosphere.tma.analyze.utils.metricAggregationOperators;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the MetricAggregationOperator abstract class
 * @author João Ribeiro
 */
public class Sum extends MetricAggregationOperator{

    private final Logger LOGGER = LoggerFactory.getLogger(Sum.class);
    
    public Sum(){
        super();
    }
    
    @Override
    public double aggregateData(ArrayList<Double> normalizedDataValues) {
        double sum = 0.0;
        for(Double value : normalizedDataValues)
            sum += value;
        return sum;
    }
    
}
