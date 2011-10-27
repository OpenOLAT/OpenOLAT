//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc.provider.adobe;

import java.util.Date;
import java.util.List;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import de.bps.course.nodes.VCCourseNode;
import de.bps.course.nodes.vc.MeetingDate;
import de.bps.course.nodes.vc.provider.VCProviderFactory;

/**
 * 
 * Description:<br>
 * Cleanup unused Adobe Connect ressources:<br/>
 * - unused meetings<br/>
 * - temporary guest users
 * 
 * <P>
 * Initial Date:  04.01.2011 <br>
 * @author skoeber
 */
public class AdobeConnectCleanupJob extends QuartzJobBean {
	
	private OLog logger = Tracing.createLoggerFor(AdobeConnectCleanupJob.class);
	
	private String providerId;
	private boolean cleanupMeetings, cleanupModerators;
	private int daysToKeep;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		AdobeConnectProvider adobe = null;
		
		boolean success = VCProviderFactory.existsProvider(providerId);
		if(!success) return;//same as dummy job
		
		try {
			adobe = (AdobeConnectProvider) VCProviderFactory.createProvider(providerId);
		} catch(ClassCastException e) {
			throw new JobExecutionException("Invalid configuration: defined a virtual classroom cleanup job and provider implementation doesn't fit");
		}
		
		success = adobe.isProviderAvailable() && adobe.isEnabled();
		if(!success) {
			logger.debug("Tried to cleanup Adobe Connect meetings but it's actually not available");
			return;
		}

		/*
		 * the concrete jobs
		 */
		// cleanup unused meetings
		if(cleanupMeetings)	{
			logger.info("Start cleaning unused Adobe Connect meetings");
			cleanupMeetings(adobe, daysToKeep);
		}
		// cleanup unused moderator guest accounts
		if(cleanupModerators) {
			logger.info("Start cleaning unused Adobe Connect moderator guest accounts");
			cleanupModerators(adobe);
		}
	}

	/**
	 * @param adobe
	 */
	protected void cleanupMeetings(AdobeConnectProvider adobe, int daysToKeep) {
		boolean success = false;
		
		Date lowerLimit = new Date((new Date()).getTime() - (daysToKeep * 24*60*60*1000));
		
		// search all virtual classrooms with the used prefix
		List<String> roomIds = adobe.findClassrooms(AdobeConnectProvider.PREFIX);
		for(String roomId : roomIds) {
			// format is olat-courseId-nodeId, e.g. olat-82823405537032-82823405537043
			// load course and node
			String courseId = roomId.split("-")[1];
			String nodeId = roomId.split("-")[2];
			ICourse course = CourseFactory.loadCourse(Long.parseLong(courseId));
			CourseNode vcNode = course.getRunStructure().getNode(nodeId);
			if(!(vcNode instanceof VCCourseNode)) {
				logger.warn("Tried to cleanup Adobe Connect meeting for a non Adobe Connect course node: " + roomId);
				continue;
			}
			AdobeConnectConfiguration config = (AdobeConnectConfiguration) vcNode.getModuleConfiguration().get(VCCourseNode.CONF_VC_CONFIGURATION);
			if(config == null) {
				// invalid configuration, do nothing and continue
				continue;
			}
			
			boolean keep = false;
			for(MeetingDate date : config.getMeetingDates()) {
				if(keep) continue;
				Date end = date.getEnd();
				keep = lowerLimit.before(end);
			}
			
			// no planned date in the future, we can delete
			// build the correct roomId
			String toDelete = courseId + "-" + nodeId;
			if(!keep) success = adobe.removeClassroom(toDelete, config);
			
			if(!success) {
				logger.warn("Error when cleaning up Adobe Connect meeting \"" + roomId + "\"");
				continue;
			}
		}
	}
	
	protected void cleanupModerators(AdobeConnectProvider adobe) {
//		boolean success = false;
//		TODO implement
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public int getDaysToKeep() {
		return daysToKeep;
	}

	public void setDaysToKeep(int daysToKeep) {
		this.daysToKeep = daysToKeep;
	}

	public void setCleanupMeetings(boolean cleanupMeetings) {
		this.cleanupMeetings = cleanupMeetings;
	}

	public boolean isCleanupMeetings() {
		return cleanupMeetings;
	}

	public void setCleanupModerators(boolean cleanupModerators) {
		this.cleanupModerators = cleanupModerators;
	}

	public boolean isCleanupModerators() {
		return cleanupModerators;
	}

}
//</OLATCE-103>