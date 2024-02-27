package org.olat.core.commons.services.export.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExpiredExportJob extends JobWithDB {
	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		exportManager.deleteExpiredExports();
		DBFactory.getInstance().commitAndCloseSession();
	}

}
