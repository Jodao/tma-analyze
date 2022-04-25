package eubr.atmosphere.tma.analyze.utils.normalizationMethods;

import eubr.atmosphere.tma.analyze.utils.LeafAttributeInfo;
import java.util.ArrayList;

/**
 * Abstract class representing the methods a normalization method must implement
 * @author João Ribeiro
 */
public abstract class NormalizationMethod {
    public NormalizationMethod(){
        
    }
    
    //this method gets as input the reference to the array containing the original values, i.e. the values before
    //the normalization process. It doesnt return anything because it directly changes the values on the array, and
    //therefore updating it accordingly with the normalization process
    public abstract void normalize(ArrayList<Double> originalValuesArray, LeafAttributeInfo leafDetails);
}
