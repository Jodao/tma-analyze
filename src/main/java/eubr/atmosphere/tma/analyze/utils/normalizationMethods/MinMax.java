package eubr.atmosphere.tma.analyze.utils.normalizationMethods;

import eubr.atmosphere.tma.analyze.utils.LeafAttributeInfo;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a concrete implementation of the NormalizationMethod abstract class
 * @author João Ribeiro
 */
public class MinMax extends NormalizationMethod{
    private final Logger LOGGER = LoggerFactory.getLogger(MinMax.class);
    
    public MinMax(){
        super();
    }
    
    @Override
    public void normalize(ArrayList<Double> originalValuesArray, LeafAttributeInfo leafDetails) {
        double valuesRangeSize = leafDetails.getMaximumThreshold() - leafDetails.getMinimumThreshold();
        //0 => Benefit attribute
        if(leafDetails.getNormalizationKind() == 0){
            normalizeAsBenefitAttribute(originalValuesArray, valuesRangeSize, leafDetails.getMaximumThreshold(),
                    leafDetails.getMinimumThreshold());
        }
        //1 => Cost attribute
        else{
            normalizeAsCostAttribute(originalValuesArray, valuesRangeSize, leafDetails.getMaximumThreshold(),
                    leafDetails.getMinimumThreshold());
        }
    }
    
    private void normalizeAsBenefitAttribute(ArrayList<Double> originalValuesArray, double valuesRangeSize, 
            double maximumThreshold, double minimumThreshold){
        for(int i = 0; i < originalValuesArray.size(); i++){
            //Lower than minimum threshold
            if(originalValuesArray.get(i) <= minimumThreshold){
                originalValuesArray.set(i,0.0);
            }
            //Higher than maximum threshold
            else if(originalValuesArray.get(i) >= maximumThreshold){
                originalValuesArray.set(i,1.0);
            }
            //Inside thresholds interval
            else{
                originalValuesArray.set(i,(originalValuesArray.get(i) - minimumThreshold)/valuesRangeSize);
            }
        }
    }
    
    private void normalizeAsCostAttribute(ArrayList<Double> originalValuesArray, double valuesRangeSize, 
            double maximumThreshold, double minimumThreshold){
        for(int i = 0; i < originalValuesArray.size(); i++){
            //Lower than minimum threshold
            if(originalValuesArray.get(i) <= minimumThreshold){
                originalValuesArray.set(i,1.0);
            }
            //Higher than maximum threshold
            else if(originalValuesArray.get(i) >= maximumThreshold){
                originalValuesArray.set(i,0.0);
            }
            //Inside thresholds interval
            else{
                originalValuesArray.set(i,(maximumThreshold - originalValuesArray.get(i))/valuesRangeSize);
            }
        }
    }
    
}
