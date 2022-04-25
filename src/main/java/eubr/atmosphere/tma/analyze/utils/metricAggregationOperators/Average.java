package eubr.atmosphere.tma.analyze.utils.metricAggregationOperators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the MetricAggregationOperator abstract class
 * @author João Ribeiro
 */
public class Average extends MetricAggregationOperator{

    private final Logger LOGGER = LoggerFactory.getLogger(Average.class);
    
    public Average(){
        super();
    }
    
    @Override
    public double aggregateData(ArrayList<Double> normalizedDataValues) {
        return BigDecimal.valueOf(
                //perform calculation and save it in a BidDecimal to later round it up due to inaccuracy of double
                new Sum().aggregateData(normalizedDataValues) / normalizedDataValues.size()
        ).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
