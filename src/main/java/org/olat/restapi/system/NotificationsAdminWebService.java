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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.system;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.restapi.system.vo.NotificationsStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotificationsAdminWebService {
	
	private static final Logger log = Tracing.createLoggerFor(NotificationsAdminWebService.class);
	
	private final JobKey notificationsJobKey = new JobKey("org.olat.notifications.job.enabled", Scheduler.DEFAULT_GROUP);
	
	/**
	 * Return the status of the notifications job: running, stopped
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The status of the notifications job
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @return The status of the notifications job
	 */
	@GET
	@Path("status")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatus() {
		return Response.ok(new NotificationsStatus(getJobStatus())).build();
	}
	
	/**
	 * Return the status of the notifications job: running, stopped
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The status of the notifications job
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @return The status of the notifications job
	 */
	@GET
	@Path("status")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getPlainTextStatus() {
		return Response.ok(getJobStatus()).build();
	}
	
	private String getJobStatus() {
		try {
			Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
			List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
			for(JobExecutionContext job:jobs) {
				if("org.olat.notifications.job.enabled".equals(job.getJobDetail().getKey().getName())) {
					return "running";
				}
			}
			return "stopped";
		} catch (SchedulerException e) {
			log.error("", e);
			return "error";
		}
	}
	
	/**
	 * Update the status of the notifications job: running, stopped.
	 * Running start the indexer, stopped, stop it.
   * @response.representation.200.doc The status has changed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @return The status of the notification
	 */
	@POST
	@Path("status")
	public Response setStatus(@FormParam("status") String status) {
		if("running".equals(status)) {
			try {
				CoreSpringFactory.getImpl(Scheduler.class).triggerJob(notificationsJobKey);
			} catch (SchedulerException e) {
				log.error("", e);
			}
		}
		return Response.ok().build();
	}
}