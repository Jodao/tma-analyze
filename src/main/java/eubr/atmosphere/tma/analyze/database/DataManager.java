package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.database.DatabaseManager;
import eubr.atmosphere.tma.utils.ResourceConsumptionScoreTalkConnect;
import eubr.atmosphere.tma.analyze.utils.Constants;
import eubr.atmosphere.tma.analyze.utils.PropertiesManager;

public class DataManager {

	private Connection connection = DatabaseManager.getConnectionInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);
	public final List<Integer> monitoredContainers = new ArrayList<Integer>();
	public final Integer probeIdResourceConsumption;

	private static final String METRIC_DATA_INSERT_SQL =
			"INSERT INTO MetricData(metricId, valueTime, value, resourceId) "
			+ "VALUES (?, FROM_UNIXTIME(?), ?, ?)";

	public DataManager(String monitoredContainersString) {
		this.connection = DatabaseManager.getConnectionInstance();
		this.probeIdResourceConsumption = Integer
				.parseInt(PropertiesManager.getInstance().getProperty("probeIdResourceConsumption"));
		String[] containers = monitoredContainersString.split(",");
		for (int i = 0; i < containers.length; i++)
			this.monitoredContainers.add(Integer.parseInt(containers[i]));
	}

	public ResourceConsumptionScoreTalkConnect getDataResourceConsumption(String stringTime, int resource) {
		String sql = "select descriptionId, resourceId,"
                        + "case descriptionId"
                        + " when 3 then (select value from Data where descriptionId = 3 and "
                        + "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i:%s\") >= ? order by valueTime desc limit 1)"
                        + " else avg(value) "
                        + "end as 'value' from Data " 
                        + "where DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i:%s\") >= ? AND probeId = ?"
                        + " group by descriptionId, resourceId;";
		if (this.connection != null) {
			return executeQueryResourceConsumption(stringTime, sql, resource);
		} else {
			LOGGER.error("The connection is null!");
			return null;
		}
	}

	private ResourceConsumptionScoreTalkConnect executeQueryResourceConsumption(String stringTime, String sql, int resource) {
		ResourceConsumptionScoreTalkConnect score = null;
		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, stringTime);
                        ps.setString(2, stringTime);
			ps.setInt(3, probeIdResourceConsumption);
			ResultSet rs = DatabaseManager.executeQuery(ps);

			if (rs.next()) {
				score = new ResourceConsumptionScoreTalkConnect();
				score.setResourceId(resource);
				score.setMetricId(Constants.resourceConsumptionMetricId);
                                
				do {
					int descriptionId = ((Integer) rs.getObject("descriptionId"));
					int resourceId = ((Integer) rs.getObject("resourceId"));
					Double value = ((Double) rs.getObject("value"));

					switch (descriptionId) {

					case Constants.cpuDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setCpuContainers(score.getCpuContainers() + value);
						}
						break;

					case Constants.memoryDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setMemoryContainers(score.getMemoryContainers() + value);
						}
						break;
                                        case Constants.numberContainersDescriptionId:
                                            if (isMonitorizedResource(resourceId)) {
							score.setNumberContainers(score.getNumberContainers() + value);
						}
						break;
					default:
						LOGGER.debug("Something is not right! {}, descriptionId: {}", stringTime, descriptionId);
						break;
					}
					// String valueTime = rs.getObject("valueTime").toString();
				} while (rs.next());			
			} else {
				LOGGER.info("No data on: " + stringTime);
			}
			return score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}

	public boolean isMonitorizedResource(int containerId) {
		return this.monitoredContainers.contains(containerId);
	}

	public List<Double> getValuesPeriod(String initialDateTime, String finalDateTime, int descriptionId,
			int resourceId) {
		String sql = "select * from Data " + "where " + "valueTime between ? and ? and " + "descriptionId = ? and "
				+ "resourceId = ? " + "order by valueTime;";

		List<Double> values = new ArrayList<Double>();

		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, initialDateTime);
			ps.setString(2, finalDateTime);
			ps.setInt(3, descriptionId);
			ps.setInt(4, resourceId);

			ResultSet rs = DatabaseManager.executeQuery(ps);

			while (rs.next()) {
				Double value = ((Double) rs.getObject("value"));
				values.add(value);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return values;
	}

	public int[] saveScore(ResourceConsumptionScoreTalkConnect score) {
		// The score will be saved in the MetricData table

		PreparedStatement ps;

		try {
			ps = DatabaseManager.getConnectionInstance().prepareStatement(METRIC_DATA_INSERT_SQL);

			ps.setInt(1, score.getMetricId());
			ps.setLong(2, score.getValueTime());
			ps.setDouble(3, score.getScore());
			ps.setInt(4, score.getResourceId());
                        ps.addBatch();
                        
			DatabaseManager databaseManager = new DatabaseManager();
			return databaseManager.executeBatch(ps);
		} catch (SQLException e) {
			LOGGER.error("[ATMOSPHERE] Error when inserting a metric data in the database.", e);
		}
		return null;
	}
}
