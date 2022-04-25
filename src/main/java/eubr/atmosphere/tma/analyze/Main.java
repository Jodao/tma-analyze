package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.analyze.database.DatabaseManager;
import eubr.atmosphere.tma.analyze.utils.MetricTreeNode;
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
            //Its time to apply the weights and thresholds on the child scores to get data of current metric. This 
            //might change and the set of the value may have to be done after iterating all child metrics due to other
            //operators that may be implemented in the future.
            node.setMetricData(
                    node.getAttributeAggregationOperator().
                            aggregateChildMetricData(node.getMetricData(), child.getMetricData(), child.getWeight())
            );
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
        leafNode.getLeafAttribute().getNormalizationMethod().normalize(dataValues, leafNode.getLeafAttribute());
        
        //apply aggregation method on normalized data
        leafNode.setMetricData(
                leafNode.getLeafAttribute().getMetricAggregationOperator().aggregateData(dataValues)
        );
        
        return true;
    }

}