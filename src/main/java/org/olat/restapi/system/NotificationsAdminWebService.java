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

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.restapi.system.vo.NotificationsStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotificationsAdminWebService {
	
	private static final Logger log = Tracing.createLoggerFor(NotificationsAdminWebService.class);
	
	private final JobKey notificationsJobKey = new JobKey("org.olat.notifications.job.enabled", Scheduler.DEFAULT_GROUP);
	
	/**
	 * Return the status of the notifications job: running, stopped
	 * 
	 * @return The status of the notifications job
	 */
	@GET
	@Path("status")
	@Operation(summary = "Return the status", description = "Return the status of the notifications job: running, stopped")
	@ApiResponse(responseCode = "200", description = "The status of the notifications job")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatus() {
		return Response.ok(new NotificationsStatus(getJobStatus())).build();
	}
	
	/**
	 * Return the status of the notifications job: running, stopped
	 * 
	 * @return The status of the notifications job
	 */
	@GET
	@Path("status")
	@Operation(summary = "Return the status", description = "Return the status of the notifications job: running, stopped")
	@ApiResponse(responseCode = "200", description = "The status of the notifications job")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
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
	 * 
	 * @return The status of the notification
	 */
	@POST
	@Path("status")
	@Operation(summary = "Update the status", description = "Update the status of the notifications job: running, stopped. Running start the indexer, stopped, stop it")
	@ApiResponse(responseCode = "200", description = "The status of the notifications job")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")	
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