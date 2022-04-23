package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.analyze.database.DatabaseManager;
import eubr.atmosphere.tma.analyze.utils.LeafAttributeInfo;
import eubr.atmosphere.tma.analyze.utils.MetricTreeNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.TimeZone;

public class Main {

    /**
     * OBSERVATION_WINDOW: window that the readings will be used to calculate the
     * score (in seconds)
     */
    private static final int OBSERVATION_WINDOW = 30;

    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        Calendar startTimeStamp;
        Calendar endTimeStamp;
        
        //set sdf to use UTC time zone (which is the same as the one used by the database)
        //Thus, the local timestamp is converted into UTC timestamp
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        while (true) {
            endTimeStamp = Calendar.getInstance();
            startTimeStamp = (Calendar) endTimeStamp.clone();
            startTimeStamp.add(Calendar.SECOND, -OBSERVATION_WINDOW);
            
            //get list of currently monitored resources and their association with configuration profiles
            HashMap<Integer, ArrayList<Integer>> confProfilesAndResources = 
                    dbManager.getActiveResourcesAndTheirConfigurationProfiles();
            
            
            for(int confProfileId : confProfilesAndResources.keySet()){
                //get weighted metrics tree for the active configuration profiles
                MetricTreeNode confProfileTree = dbManager.getConfigurationProfile(confProfileId);
                //apply retrieved configuration profile to resources
                for(int resourceId : confProfilesAndResources.get(confProfileId)){
                    //invoke function to calculate metric data for the whole tree providing root node as input.
                    //if analyze was able to calculate the scores, i.e. there were no errors and there were values 
                    //to be used, then save metric data on database
                    if( calculateMetricScore(resourceId, confProfileTree, sdf.format(startTimeStamp.getTime()),
                            sdf.format(endTimeStamp.getTime())) == true ){
                        //apply weight on the root node
                        confProfileTree.setMetricData(confProfileTree.getMetricData() * confProfileTree.getWeight());
                        dbManager.saveMetricData(resourceId, confProfileTree, sdf.format(endTimeStamp.getTime()));
                    }
                    //If something went wrong provide some information.
                    else{
                        LOGGER.error("It was impossible to calculate metric data for resource " + resourceId 
                                + " applying configuration profile " + confProfileId + ", due to an error or "
                                + "unavailable data!");
                    }
                    
                }
            }
            
            
            try {
                //Sleep for the observation window and convert seconds to millis
               Thread.sleep(OBSERVATION_WINDOW*1000); //1000
            } 
            catch (InterruptedException e) {
                LOGGER.error("[ATMOSPHERE] Error when analyze was sleeping", e);
            }
        }
    }
    
    private static boolean calculateMetricScore(int resourceId, MetricTreeNode node, String startTimeStamp,
            String endTimeStamp){
        for(MetricTreeNode child : node.getChildMetrics()){
            //calculate score for child. If there was an error calculating it, function will return false
            //and thereby whole calculating process is stopped
            if( !calculateMetricScore(resourceId, child, startTimeStamp, endTimeStamp)  ){
                return false;
            }
            //Its time to apply the weights and thresholds on the child scores to get
            //data of current metric
            switch(node.getAttributeAggregationOperator()){
                case 0: //NEUTRALITY
                    node.setMetricData(  node.getMetricData()  +  (child.getMetricData() * child.getWeight())  );
                    break;
                case 1: //SIMULTANEITY
                    break;
                case 2: //REPLACEABILITY
                    break;
                default:
                    LOGGER.debug("There is no attribute aggregation operator such as ", node.getAttributeAggregationOperator());
                    return false;
            }
        }
        
        //In case of current node being a leaf, previous loop code won't execute once and the score 
        //has to be calculated considering different parameters
        if(node.getLeafAttribute() != null){
            //if there was an error or no values to use, it is returned false
            if(!calculateLeafMetricScore(resourceId, node, startTimeStamp, endTimeStamp)){
                return false;
            }
        }
        return true;
    }
    
    private static boolean calculateLeafMetricScore(int resourceId, MetricTreeNode leafNode, String startTimeStamp,
            String endTimeStamp){
        DatabaseManager dbManager = new DatabaseManager();
        ArrayList<Double> dataValues = 
                dbManager.getLeafAttributeData(resourceId, leafNode.getLeafAttribute(), startTimeStamp, endTimeStamp);
        
        if(dataValues == null || dataValues.isEmpty()){
            return false;
        }
        
        //apply different functions to normalize data depending on the specs of the leaf attribute.
        switch(leafNode.getLeafAttribute().getNormalizationMethod()){
            case "MIN-MAX":
                normalizationMinMax(dataValues, leafNode.getLeafAttribute());
                break;
            default:
                LOGGER.debug("There is no normalization method such as ",leafNode.getLeafAttribute().getNormalizationMethod());
                return false;
        }
        
        switch(leafNode.getLeafAttribute().getMetricAggregationOperator()){
            case 0: //"AVERAGE":
                leafNode.setMetricData(average(dataValues));
                break;
            case 1: //"MINIMUM":
                leafNode.setMetricData(Collections.min(dataValues));
                break;
            case 2: //"MAXIMUM":
                leafNode.setMetricData(Collections.max(dataValues));
                break;
            case 3: //"SUM":
                leafNode.setMetricData(sum(dataValues));
                break;
            default:
                LOGGER.debug("There is no metric aggregation operator such as ", leafNode.getLeafAttribute().getMetricAggregationOperator());
                return false;
        }
        return true;
    }
    
    private static void normalizationMinMax(ArrayList<Double> values, LeafAttributeInfo leafDetails) {
        double valuesRangeSize = leafDetails.getMaximumThreshold() - leafDetails.getMinimumThreshold();
        for(int i = 0; i < values.size(); i++){
            //Lower than minimum threshold
            if(values.get(i) <= leafDetails.getMinimumThreshold()){
                //0 => Benefit attribute
                if(leafDetails.getNormalizationKind() == 0){
                    values.set(i,0.0);
                }
                //1 => Cost attribute
                else{
                    values.set(i,1.0);
                }
            }
            //Higher than maximum threshold
            else if(values.get(i) >= leafDetails.getMaximumThreshold()){
                //0 => Benefit attribute
                if(leafDetails.getNormalizationKind() == 0){
                    values.set(i,1.0);
                }
                //1 => Cost attribute
                else{
                    values.set(i,0.0);
                }
            }
            //Inside thresholds interval
            else{
                //0 => Benefit attribute
                if(leafDetails.getNormalizationKind() == 0){
                    values.set(i,(values.get(i) - leafDetails.getMinimumThreshold())/valuesRangeSize);
                }
                //1 => Cost attribute
                else{
                    values.set(i,(leafDetails.getMaximumThreshold() - values.get(i))/valuesRangeSize);
                }
            }
        }
    }
    

    private static Double average(ArrayList<Double> values) {
            return sum(values) / values.size();
    }

    private static double sum(ArrayList<Double> values){
        double sum = 0.0;
        for(Double value : values)
            sum += value;
        return sum;
    }
    
    /* The following functions are not being used but might become helpful*/
    private static List<Double> normalizeData(ArrayList<Double> values) {
            LOGGER.debug(values.toString());
            Double mean = average(values);
            LOGGER.debug("Arithmetic Mean:" + mean);
            Double standardDeviation = getStandardDeviation(values, mean);
            LOGGER.debug("Standard Deviation:" + standardDeviation);
            List<Double> normalizedData = getNormalizedData(values, mean, standardDeviation);
            LOGGER.debug("Normalized Data:" + normalizedData);
            return normalizedData;
    }
    
    public static double getStandardDeviation(List<Double> values, Double mean) {
            double standardDeviation = 0.0;
            int length = values.size();
            for (int i = 0; i < length; i++) {
                    standardDeviation += Math.pow(values.get(i) - mean, 2);
            }

            return Math.sqrt(standardDeviation / length);
    }

    private static List<Double> getNormalizedData(List<Double> values, Double mean, Double standardDeviation) {
            List<Double> result = new ArrayList<Double>();

            int length = values.size();
            for (int i = 0; i < length; i++) {
                    standardDeviation += Math.pow(values.get(i) - mean, 2);
                    result.add((values.get(i) - mean) / standardDeviation);
            }
            return result;
    }

}