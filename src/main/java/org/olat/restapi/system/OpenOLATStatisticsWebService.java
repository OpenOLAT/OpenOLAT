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

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.admin.sysinfo.model.SessionsStats;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupService;
import org.olat.modules.invitation.InvitationService;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.OpenOLATStatisticsVO;
import org.olat.restapi.system.vo.RepositoryStatisticsVO;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.restapi.system.vo.TasksVO;
import org.olat.restapi.system.vo.UserStatisticsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenOLATStatisticsWebService implements Sampler {

	private final IndexerWebService indexerWebService = new IndexerWebService();

	/**
	 * Return the statistics about OpenOLAT, users count, courses count... 
	 * 
	 * @param request The HTTP request
	 * @return The statistics about OpenOLAT
	 */
	@GET
	@Operation(summary = "Return the statistics about OpenOLAT", description = "Return the statistics about OpenOLAT, users count, courses count...")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OpenOLATStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OpenOLATStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatistics() {
		OpenOLATStatisticsVO stats = new OpenOLATStatisticsVO();
		stats.setSessions(getSessionsVO());
		stats.setUserStatistics(getUserStatisticsVO());
		stats.setRepositoryStatistics(getRepositoryStatisticsVO());
		stats.setIndexerStatistics(indexerWebService.getIndexerStatistics());
		return Response.ok(stats).build();
	}

	/**
	 * Return the statistics about OpenOLAT users
	 * 
	 * @param request The HTTP request
	 * @return The statistics about OpenOLAT users
	 */
	@Tag(name = "Users")
	@GET
	@Path("users")
	@Operation(summary = "Return the statistics about OpenOLAT users", description = "Return the statistics about OpenOLAT users")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserStatistics() {
		UserStatisticsVO stats = getUserStatisticsVO();
		return Response.ok(stats).build();
	}

	/**
	 * Return the statistics about the repository, courses count, published courses... 
	 * 
	 * @param request The HTTP request
	 * @return The statistics about the repository
	 */
	@Tag(name = "Repo")
	@GET
	@Path("repository")
	@Operation(summary = "Return the statistics about the repository", description = "Return the statistics about the repository, courses count, published courses... ")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRepositoryStatistics() {
		RepositoryStatisticsVO stats = getRepositoryStatisticsVO();
		return Response.ok(stats).build();
	}

	/**
	 * Return the statistics about the indexer
	 * 
	 * @param request The HTTP request
	 * @return The statistics about the indexer
	 */
	@Path("indexer")
	@Operation(summary = "Return the statistics about the repository", description = "Return the statistics about the repository, courses count, published courses... ")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = IndexerStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = IndexerStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public IndexerWebService getIndexerStatistics() {
		return indexerWebService;
	}

	/**
	 * Return some statistics about session.
	 * 
	 * @param request The HTTP request
	 * @return The statistics about sessions
	 */
	@GET
	@Path("sessions")
	@Operation(summary = "Return some statistics about session", description = "Return some statistics about session")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = SessionsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = SessionsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSessions() {
		SessionsVO vo = getSessionsVO();
		return Response.ok(vo).build();
	}

	/**
	 * Return some statistics about long running tasks.
	 * 
	 * @param request The HTTP request
	 * @return The statistics about sessions
	 */
	@GET
	@Path("tasks")
	@Operation(summary = "Return some statistics about long running tasks", description = "Return some statistics about long running tasks")
	@ApiResponse(responseCode = "200", description = "A short summary about sessions", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TasksVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TasksVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTasks() {
		TasksVO vo = getTasksVO();
		return Response.ok(vo).build();
	}

	private UserStatisticsVO getUserStatisticsVO() {
		UserStatisticsVO stats = new UserStatisticsVO();
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);

		// activeUserCount="88" // registered and activated identities, same as in GUI
		long countActiveUsers = securityManager.countIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		stats.setActiveUserCount(countActiveUsers);

		// active last day
		Calendar lastDay = Calendar.getInstance();
		lastDay.add(Calendar.DATE, -1);
		long activeUserCountDay = securityManager.countUniqueUserLoginsSince(lastDay.getTime());
		stats.setActiveUserCountLastDay(activeUserCountDay);

		// active last week
		Calendar lastWeek = Calendar.getInstance();
		lastWeek.add(Calendar.DATE, -7);
		long activeUserCountWeek = securityManager.countUniqueUserLoginsSince(lastWeek.getTime());
		stats.setActiveUserCountLastWeek(activeUserCountWeek);

		// active last month
		Calendar lastMonth = Calendar.getInstance();
		lastMonth.add(Calendar.MONTH, -1);
		long activeUserCountMonth = securityManager.countUniqueUserLoginsSince(lastMonth.getTime());
		stats.setActiveUserCountLastMonth(activeUserCountMonth);

		// active last 6 month
		Calendar last6Month = Calendar.getInstance();
		last6Month.add(Calendar.MONTH, -6);
		long activeUserCount6Month = securityManager.countUniqueUserLoginsSince(last6Month.getTime());
		stats.setActiveUserCountLast6Month(activeUserCount6Month);

		// externalUserCount="12" // EP invite identities, later maybe also used in courses for MOOCS, external experts etc)
		long invitationsCount = CoreSpringFactory.getImpl(InvitationService.class).countInvitations();
		stats.setExternalUserCount(invitationsCount);

		// blockedUserCount="0" // identities in login blocked state
		long blockedUserCount = securityManager.countIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_LOGIN_DENIED);	
		stats.setBlockedUserCount(blockedUserCount);
		// deletedUserCount="943" // deleted identities
		long deletedUserCount = securityManager.countIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_DELETED);	
		stats.setDeletedUserCount(deletedUserCount);

		// totalUserCount="1043" // Sum of all above
		long countUsers = securityManager.countIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, null, null);
		stats.setTotalUserCount(countUsers);

		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		long countGroups = bgs.countBusinessGroups(null, null);
		DBFactory.getInstance().commitAndCloseSession();
		stats.setTotalGroupCount(countGroups);
		return stats;
	}

	private RepositoryStatisticsVO getRepositoryStatisticsVO() {
		RepositoryStatisticsVO stats = new RepositoryStatisticsVO();
		RepositoryManager repoMgr = CoreSpringFactory.getImpl(RepositoryManager.class);
		int allCourses = repoMgr.countByType(CourseModule.ORES_TYPE_COURSE);
		int publishedCourses = repoMgr.countPublished(CourseModule.ORES_TYPE_COURSE);
		DBFactory.getInstance().commitAndCloseSession();
		stats.setCoursesCount(allCourses);
		stats.setPublishedCoursesCount(publishedCourses);
		return stats;
	}

	private TasksVO getTasksVO() {
		TasksVO tasks = new TasksVO();
		List<String> longRunningTaskList = WorkThreadInformations.getLongRunningTasks();
		String[] longRunningTasks = longRunningTaskList.toArray(new String[longRunningTaskList.size()]);
		tasks.setLongRunningTasks(longRunningTasks);
		return tasks;
	}

	protected SessionsVO getSessionsVO() {
		SessionsVO vo = new SessionsVO();

		SessionStatsManager sessionStatsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		UserSessionManager sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		vo.setCount(sessionManager.getUserSessionsCnt());

		Set<UserSession> userSessions = sessionManager.getAuthenticatedUserSessions();
		int webdavcount = 0;
		int secureWebdavCount = 0;
		int authenticatedcount = 0;
		int secureAuthenticatedCount = 0;
		int restCount = 0;
		int secureRestCount = 0;
		for (UserSession usess:userSessions) {
			SessionInfo sessInfo = usess.getSessionInfo();
			if (sessInfo.isWebDAV()) {
				webdavcount++;
				if (sessInfo.isSecure()) {
					secureWebdavCount++;
				}
			} else if (sessInfo.isREST()) {
				restCount++;
				if (sessInfo.isSecure()) {
					secureRestCount++;
				}
			} else {
				authenticatedcount++;
				if (sessInfo.isSecure()) {
					secureAuthenticatedCount++;
				}
			}
		}

		vo.setAuthenticatedCount(authenticatedcount);
		vo.setSecureAuthenticatedCount(secureAuthenticatedCount);
		vo.setWebdavCount(webdavcount);
		vo.setSecureWebdavCount(secureWebdavCount);
		vo.setRestCount(restCount);
		vo.setSecureRestCount(secureRestCount);
		//Instant messaging
		vo.setInstantMessagingCount(-1);

		SessionsStats statsLastMinute = sessionStatsManager.getSessionsStatsLast(60);
		SessionsStats statsLast5Minutes = sessionStatsManager.getSessionsStatsLast(300);
		vo.setAuthenticatedClickCountLastMinute(statsLastMinute.getAuthenticatedClickCalls());
		vo.setAuthenticatedClickCountLastFiveMinutes(statsLast5Minutes.getAuthenticatedPollerCalls());
		vo.setAuthenticatedPollCountLastMinute(statsLastMinute.getAuthenticatedPollerCalls());
		vo.setAuthenticatedPollCountLastFiveMinutes(statsLast5Minutes.getAuthenticatedPollerCalls());
		vo.setRequestLastMinute(statsLastMinute.getRequests());
		vo.setRequestLastFiveMinutes(statsLast5Minutes.getRequests());
		vo.setConcurrentDispatchThreads(sessionStatsManager.getConcurrentCounter());	
		return vo;
	}

	@Override
	public void takeSample() {
		SessionStatsManager manager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		if(manager != null) {//check if the manager is loaded
			manager.takeSample();
		}
	}
}
