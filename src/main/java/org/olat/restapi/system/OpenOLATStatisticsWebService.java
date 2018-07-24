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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.admin.sysinfo.model.SessionsStats;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupService;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.system.vo.OpenOLATStatisticsVO;
import org.olat.restapi.system.vo.RepositoryStatisticsVO;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.restapi.system.vo.TasksVO;
import org.olat.restapi.system.vo.UserStatisticsVO;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenOLATStatisticsWebService implements Sampler {

	private final IndexerWebService indexerWebService = new IndexerWebService();

	/**
	 * Return the statistics about OpenOLAT, users count, courses count... 
	 * @response.representation.200.qname {http://www.example.com}releaseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The verison of the instance
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_OO_STATSVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about OpenOLAT
	 */
	@GET
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
	 * @response.representation.200.qname {http://www.example.com}releaseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The verison of the instance
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_OO_USERSSTATSVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about OpenOLAT users
	 */
	@GET
	@Path("users")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserStatistics() {
		UserStatisticsVO stats = getUserStatisticsVO();
		return Response.ok(stats).build();
	}

	/**
	 * Return the statistics about the repository, courses count, published courses... 
	 * @response.representation.200.qname {http://www.example.com}releaseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The verison of the instance
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_OO_REPOSTATSVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about the repository
	 */
	@GET
	@Path("repository")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRepositoryStatistics() {
		RepositoryStatisticsVO stats = getRepositoryStatisticsVO();
		return Response.ok(stats).build();
	}

	/**
	 * Return the statistics about the indexer
	 * @response.representation.200.qname {http://www.example.com}releaseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The verison of the instance
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_OO_INDEXERSTATSVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about the indexer
	 */
	@Path("indexer")
	public IndexerWebService getIndexerStatistics() {
		return indexerWebService;
	}

	/**
	 * Return some statistics about session.
	 * @response.representation.200.qname {http://www.example.com}sessionVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc A short summary about sessions
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_SESSIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about sessions
	 */
	@GET
	@Path("sessions")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSessions() {
		SessionsVO vo = getSessionsVO();
		return Response.ok(vo).build();
	}

	/**
	 * Return some statistics about long running tasks.
	 * @response.representation.200.qname {http://www.example.com}taskVOes
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc A short summary about sessions
	 * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_SESSIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param request The HTTP request
	 * @return The statistics about sessions
	 */
	@GET
	@Path("tasks")
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
		long invitationsCount = CoreSpringFactory.getImpl(InvitationDAO.class).countInvitations();
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
		stats.setTotalGroupCount(countGroups);
		return stats;
	}

	private RepositoryStatisticsVO getRepositoryStatisticsVO() {
		RepositoryStatisticsVO stats = new RepositoryStatisticsVO();
		RepositoryManager repoMgr = CoreSpringFactory.getImpl(RepositoryManager.class);
		int allCourses = repoMgr.countByType(CourseModule.ORES_TYPE_COURSE);
		int publishedCourses = repoMgr.countPublished(CourseModule.ORES_TYPE_COURSE);
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
