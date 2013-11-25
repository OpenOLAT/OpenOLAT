//<OLATCE-103>
/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.adobe;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.bps.course.nodes.VCCourseNode;
import de.bps.course.nodes.vc.MeetingDate;
import de.bps.course.nodes.vc.provider.VCProvider;
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
public class AdobeConnectCleanupJob extends JobWithDB {
	
	private OLog logger = Tracing.createLoggerFor(AdobeConnectCleanupJob.class);
	
	private String providerId;
	private boolean cleanupMeetings, cleanupModerators;
	private int daysToKeep;

	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		if(!VCProviderFactory.existsProvider(providerId)) {
			return;//same as dummy job
		}
		
		VCProvider provider = VCProviderFactory.createProvider(providerId);
		if(!provider.isEnabled()) {
			return;
		}
		if(!(provider instanceof AdobeConnectProvider)) {
			logger.error("Invalid configuration: defined a virtual classroom cleanup job and provider implementation doesn't fit");
			return;
		}
		AdobeConnectProvider adobe = (AdobeConnectProvider)provider;
		if(!adobe.isProviderAvailable()) {
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