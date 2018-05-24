package org.olat.user.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataExportService;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataExportTask implements /* Long */ Runnable {

	private static final long serialVersionUID = 6931074116105090545L;

	private static final OLog log = Tracing.createLoggerFor(UserDataExportTask.class);
	
	private final Long exportKey;
	
	public UserDataExportTask(Long exportKey) {
		this.exportKey = exportKey;
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		UserDataExportService exportService = CoreSpringFactory.getImpl(UserDataExportService.class);
		exportService.exportData(exportKey);
		log.info("Finished data export thread for=" + exportKey + " in " + (System.currentTimeMillis() - startTime) + " (ms)");
	}
}
