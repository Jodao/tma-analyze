package eubr.atmosphere.tma.analyze;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.utils.ResourceConsumptionScoreTalkConnect;
import eubr.atmosphere.tma.analyze.database.DataManager;
import eubr.atmosphere.tma.analyze.utils.PropertiesManager;

public class Main {

	/**
	 * OBSERVATION_WINDOW: window that the readings will be used to calculate the
	 * score (in minutes)
	 */
	private static int OBSERVATION_WINDOW = 1;
	private static int OBSERVATION_WINDOW_SECONDS = 30;
	private static int OBSERVATION_WINDOW_DAY_FOR_SECURITYSCORE = 1;

	private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

	private static KafkaManager kafkaManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String monitoredContainers = PropertiesManager.getInstance().getProperty("monitoredContainers");
		DataManager dataManager = new DataManager(monitoredContainers);
		kafkaManager = new KafkaManager();

		while (true) {
			Calendar initialDate = Calendar.getInstance();

			initialDate.add(Calendar.SECOND, -OBSERVATION_WINDOW_SECONDS);
			calculateScoreNonNormalized(dataManager, initialDate, monitoredContainers);			
		
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Calculates the score without normalizing the data in advance. It assumes that
	 * the value is already the mean of the last minute.
	 * 
	 * @param dataManager object used to manipulate the database
	 * @param initialDate initial date of the search
	 */
	private static void calculateScoreNonNormalized(DataManager dataManager, Calendar initialDate,
			String monitoredContainersString) {
		String strDate = sdf.format(initialDate.getTime());
		String[] containers = monitoredContainersString.split(",");
		ResourceConsumptionScoreTalkConnect resourceConsumptionScore = dataManager.getDataResourceConsumption(strDate,
				Integer.parseInt(containers[0]));
                
		if (resourceConsumptionScore != null && resourceConsumptionScore.isValid()) {
			resourceConsumptionScore.setValueTime(initialDate.getTimeInMillis() / 1000);
                        dataManager.saveScore(resourceConsumptionScore);
                      
			LOGGER.info("resourceScore: {}", resourceConsumptionScore.toString());
			try {
				kafkaManager.addItemKafka(resourceConsumptionScore);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}
