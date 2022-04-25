package eubr.atmosphere.tma.analyze.database;

import eubr.atmosphere.tma.analyze.utils.LeafAttributeInfo;
import eubr.atmosphere.tma.analyze.utils.MetricTreeNode;
import eubr.atmosphere.tma.analyze.utils.PropertiesManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String METRIC_DATA_INSERT_SQL =
                    "INSERT INTO MetricData(metricId, valueTime, value, resourceId) "
                    + "VALUES (?, ?, ?, ?)";
   
    public DatabaseManager(){
    }
    
    //=========================== DATABASE CONNECTION RESOURCES MANAGEMENT METHODS =================================
    
    private Connection getConnectionInstance(){
        // This will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Setup the connection with the DB
        Connection conn = null;
        try {
            String connString = PropertiesManager.getInstance().getProperty("connectionString");
            String user = PropertiesManager.getInstance().getProperty("user");
            String password = PropertiesManager.getInstance().getProperty("password");

            conn = DriverManager.getConnection(connString,user,password);
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when connecting to the database.", e);
        }
        return conn; 
    }
    
    private ResultSet executeStatementQuery(Statement stmt, String sql) throws SQLException{
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when executing statement on the database.", e);
        }
        return rs;
    }
    
    
    private ResultSet executePreparedStatementQuery(PreparedStatement ps) throws SQLException{
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when executing prepared statement on the database.", e);
        }
        return rs;
    }
    
    
    private int[] executeBatch(PreparedStatement ps) throws SQLException{
        try {
            return ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when executing batch statement on the database.", e);
        }
        return null;
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                LOGGER.error("[ATMOSPHERE] Error when closing the connection to the database.", ex);
            }
        }
    }
        
    public HashMap<Integer, ArrayList<Integer>> getActiveResourcesAndTheirConfigurationProfiles(){
        //get configuration profiles and their associated resources
        String sqlQuery = "SELECT cp.configurationProfileID, r.resourceId "
                + "FROM ConfigurationProfile cp, Resource r "
                + "WHERE r.configurationProfileID = cp.configurationProfileID AND r.active = true";
        
        HashMap<Integer, ArrayList<Integer>> confProfilesAndResources = new HashMap();
        Connection conn = getConnectionInstance();
        
        try{
            try(Statement stmt = conn.createStatement()){
                
                ResultSet rs = executeStatementQuery(stmt,sqlQuery);
                while(rs.next()){
                    //get configuration profile associated resources
                    ArrayList<Integer> listOfAssociatedResources = 
                            confProfilesAndResources.get(rs.getInt("configurationProfileID"));

                    if(listOfAssociatedResources == null){
                        confProfilesAndResources.put(
                                rs.getInt("configurationProfileID"),
                                new ArrayList(Arrays.asList(rs.getInt("resourceId")))
                        );
                    }
                    else{
                        listOfAssociatedResources.add(rs.getInt("resourceId"));
                    }
                }
            }
        }
        catch(Exception e) {
            LOGGER.error("[ATMOSPHERE] Error when reading the configuration profiles and associated "
                    + "resources from the database.", e);
            confProfilesAndResources = null;
        } finally {
            close(conn);
        }
        return confProfilesAndResources;
    }

    public MetricTreeNode getConfigurationProfile(int confProfileId){
        //get root metric id by configuration profile id
        String SQL_GET_ROOT_METRIC_ID_BY_CONF_PROFILE_ID = 
                "SELECT qm.metricId FROM ConfigurationProfile cp, QualityModel qm "
                + "WHERE cp.qualityModelId = qm.qualityModelId AND cp.configurationProfileID = ?";
        
        String SQL_GET_METRIC_WEIGHT_AND_THRESHOLD_BY_CONF_PROFILE_ID_AND_METRIC_ID = 
                "SELECT weight,threshold FROM Preference "
                + "WHERE metricId = ? AND configurationProfileID = ?";
        
        String SQL_GET_LEAF_ATTRIBUTE_BY_METRIC_ID = "SELECT * FROM LeafAttribute WHERE metricId = ?";
        
        MetricTreeNode rootMetric = new MetricTreeNode();
        Connection conn = getConnectionInstance();
        
        try{
            try(PreparedStatement ps = conn.prepareStatement(SQL_GET_ROOT_METRIC_ID_BY_CONF_PROFILE_ID)){
                ps.setInt(1, confProfileId);
                ResultSet rs = executePreparedStatementQuery(ps);
                rs.next();
                rootMetric.setMetricId(rs.getInt("metricId"));
                try(PreparedStatement ps2 = 
                        conn.prepareStatement(SQL_GET_METRIC_WEIGHT_AND_THRESHOLD_BY_CONF_PROFILE_ID_AND_METRIC_ID)){
                    
                    ps2.setInt(1, rootMetric.getMetricId());
                    ps2.setInt(2, confProfileId);
                    
                    ResultSet rs2 = executePreparedStatementQuery(ps2);
                    rs2.next();
                    rootMetric.setWeight(rs2.getFloat("weight"));
                    rootMetric.setThreshold(rs2.getFloat("threshold"));
                }
                
            }
        }
        catch(Exception e) {
            LOGGER.error("[ATMOSPHERE] Error when reading the configuration profile metrics tree from the database.", e);
            rootMetric = null;
        } finally {
            close(conn);
        }
        
        //This if becomes necessary in the case something goes wrong and rootMetric gets the value null in the
        //catch clause
        if(rootMetric != null){
            
            rootMetric.setChildMetrics(getChildMetrics(confProfileId, rootMetric));
            
            //there was an error getting the childs, and thereby represent erroneous state by using null
            if(rootMetric.getChildMetrics() == null){
                rootMetric = null;
            }
            //for the case where the rootMetric is the only metric of the tree and, thereby, it is a leaf attribute
            else if(rootMetric.getChildMetrics().isEmpty()){
                conn = getConnectionInstance();
                //get leaf attribute
                try(PreparedStatement ps = conn.prepareStatement(SQL_GET_LEAF_ATTRIBUTE_BY_METRIC_ID)){
                    ps.setInt(1, rootMetric.getMetricId());
                    ResultSet rs = executePreparedStatementQuery(ps);
                    rs.next();
                    rootMetric.setLeafAttribute(
                            new LeafAttributeInfo(
                                    rs.getInt("metricAggregationOperator"),
                                    rs.getInt("descriptionId"),
                                    rs.getInt("numSamples"),
                                    rs.getString("normalizationMethod"),
                                    rs.getInt("normalizationKind"),
                                    rs.getFloat("minimumThreshold"),
                                    rs.getFloat("maximumThreshold")
                            )
                    );
                } 
                catch (SQLException ex) {
                    LOGGER.error("[ATMOSPHERE] Error when reading the configuration profile metrics tree "
                            + "from the database.", ex);
                    rootMetric = null;
                } 
                finally {
                    close(conn);
                }
            }
        }
        return rootMetric;
    }
    
    private ArrayList<MetricTreeNode> getChildMetrics(int confProfileId, MetricTreeNode parentMetric){
        
        String SQL_GET_METRIC_WEIGHT_AND_THRESHOLD_BY_CONF_PROFILE_ID_AND_METRIC_ID = 
                "SELECT weight,threshold FROM Preference "
                + "WHERE metricId = ? AND configurationProfileID = ?";
        
        String SQL_GET_CHILD_METRICS_BY_METRIC_ID = 
                "SELECT childMetric, attributeAggregationOperator FROM CompositeAttribute WHERE parentMetric  = ? ";
        
        String SQL_GET_LEAF_ATTRIBUTE_BY_METRIC_ID = "SELECT * FROM LeafAttribute WHERE metricId = ?";
        
        ArrayList<MetricTreeNode> childMetrics = new ArrayList();
        Connection conn = getConnectionInstance();
        
        //assume parentMetric is a leaft attribute, but if a child is found in the iteration of the sql 
        //result, set it to false
        boolean isLeafAttribute = true;
        
        try{
            try(PreparedStatement ps = conn.prepareStatement(SQL_GET_CHILD_METRICS_BY_METRIC_ID)){
                ps.setInt(1, parentMetric.getMetricId());
                ResultSet rs = executePreparedStatementQuery(ps);
                
                while(rs.next()){
                    if(isLeafAttribute){
                        //make these assignments only once, when there were found child metrics
                        isLeafAttribute = false;
                        parentMetric.setAttributeAggregationOperator(rs.getInt("attributeAggregationOperator"));
                    }
                    
                    MetricTreeNode childNode = new MetricTreeNode();
                    childNode.setMetricId(rs.getInt("childMetric"));
                    
                    //get weight and threshold of the metric
                    try(PreparedStatement ps2 = 
                        conn.prepareStatement(SQL_GET_METRIC_WEIGHT_AND_THRESHOLD_BY_CONF_PROFILE_ID_AND_METRIC_ID)){
                    
                        ps2.setInt(1, childNode.getMetricId());
                        ps2.setInt(2, confProfileId);

                        ResultSet rs2 = executePreparedStatementQuery(ps2);
                        rs2.next();
                        childNode.setWeight(rs2.getFloat("weight"));
                        childNode.setThreshold(rs2.getFloat("threshold"));
                    }
                    
                    childMetrics.add(childNode);
                }
            }
            
            if(isLeafAttribute){
                //parent metric has no childs, thereby it is a leaf attribute
                try(PreparedStatement ps = conn.prepareStatement(SQL_GET_LEAF_ATTRIBUTE_BY_METRIC_ID)){
                    ps.setInt(1, parentMetric.getMetricId());
                    ResultSet rs = executePreparedStatementQuery(ps);
                    rs.next();
                    parentMetric.setLeafAttribute(
                            new LeafAttributeInfo(
                                    rs.getInt("metricAggregationOperator"),
                                    rs.getInt("descriptionId"),
                                    rs.getInt("numSamples"),
                                    rs.getString("normalizationMethod"),
                                    rs.getInt("normalizationKind"),
                                    rs.getFloat("minimumThreshold"),
                                    rs.getFloat("maximumThreshold")
                            )
                    );
                }
            }
        }
        catch(Exception e) {
            LOGGER.error("[ATMOSPHERE] Error when reading the parent's child metrics and their weights "
                    + "from the database.", e);
            childMetrics = null;
        } finally {
            close(conn);
        }
        
        //This if becomes necessary in the case something goes wrong and childMetric gets the value null in the
        //catch clause
        if(childMetrics != null){
            //for each child metric execute this function recursively to find the childs of the parent's childs
            for(MetricTreeNode child : childMetrics){
                child.setChildMetrics(getChildMetrics(confProfileId,child));
                //if something went wront getting the childs, return null to represent an erroneous state
                if(child.getChildMetrics() == null){
                    childMetrics = null;
                    break;
                }
            }
        }
        
        return childMetrics;
    }
    
    
    //it has to return the list of values [MAKE THE FUNCTION RETURN ARRAYLIST OF FLOAT]
    public ArrayList<Double> getLeafAttributeData(int resourceId, LeafAttributeInfo leafDetails, String startTimeStamp,
            String endTimeStamp){
        //get configuration profiles and their associated resources
        String sqlQuery = "SELECT value FROM Data WHERE descriptionId = ? AND resourceId = ? "
                + "AND valueTime >= ? AND valueTime <= ? ORDER BY valueTime DESC LIMIT ?";
        
        Connection conn = getConnectionInstance();
        ArrayList<Double> dataValues = new ArrayList();
        
        try{
            try(PreparedStatement ps = conn.prepareStatement(sqlQuery)){
                ps.setInt(1,leafDetails.getDescriptionId());
                ps.setInt(2,resourceId);
                ps.setString(3,startTimeStamp);
                ps.setString(4,endTimeStamp);
                ps.setInt(5,leafDetails.getNumSamples());
                ResultSet rs = executePreparedStatementQuery(ps);
                while(rs.next()){
                    dataValues.add(rs.getDouble("value"));
                }
            }
        }
        catch(Exception e) {
            dataValues = null;
            LOGGER.error("[ATMOSPHERE] Error when retrieving Leaf Attribute's associated `Data´ table information"
                    + "from the database", e);
        } finally {
            close(conn);
        }
        
        return dataValues;
    }
    
    //valueTime is the final timestamp of the window where Metric Data is being calculated
    public int saveMetricData(int resourceId, MetricTreeNode metric, String valueTime){
        Connection conn = getConnectionInstance();
        try{
            //disable auto-commit and start transaction
            conn.setAutoCommit(false);
            
            try(PreparedStatement ps = conn.prepareStatement(METRIC_DATA_INSERT_SQL)){
                addBatchOnTreeTraversal(ps, resourceId, metric, valueTime); 
                executeBatch(ps);
                //Finish transaction by committing inserts on MetricData table
                conn.commit();
            }
            //re-enable auto-commit
            conn.setAutoCommit(true);
        }
        catch (Exception e) {
            LOGGER.error("[ATMOSPHERE] Error when saving Metric Data for a Configuration Profile on the database", e);
            return -1;
        }
        finally{
            close(conn);
        }
        
        
        return 0;
    }
    
    private void addBatchOnTreeTraversal(PreparedStatement ps, int resourceId, MetricTreeNode metric, String valueTime) 
            throws SQLException{
        ps.setInt(1, metric.getMetricId());
        ps.setString(2, valueTime);
        ps.setDouble(3, metric.getMetricData());
        ps.setInt(4, resourceId);
        ps.addBatch();
        
        for(MetricTreeNode child : metric.getChildMetrics()){
            addBatchOnTreeTraversal(ps,resourceId, child, valueTime);
        }
    }
    
}
