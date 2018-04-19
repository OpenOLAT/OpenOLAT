package org.olat.core.commons.services.csp.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPManager;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPLogCleanup extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		CoreSpringFactory.getImpl(CSPManager.class).cleanup();
	}
}
