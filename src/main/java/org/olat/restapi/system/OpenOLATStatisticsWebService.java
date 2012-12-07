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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.search.SearchServiceStatus;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupService;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.OpenOLATStatisticsVO;
import org.olat.restapi.system.vo.RepositoryStatisticsVO;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.restapi.system.vo.UserStatisticsVO;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceStatusImpl;
import org.olat.search.service.indexer.FullIndexerStatus;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenOLATStatisticsWebService {
	
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
		stats.setUserStatistics(getUserStatistics());
		stats.setRepositoryStatistics(getRepositoryStatistics());
		stats.setIndexerStatistics(getIndexerStatistics());
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
	public Response getUserStatistics(@Context HttpServletRequest request) {
		UserStatisticsVO stats = getUserStatistics();
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
	public Response getRepositoryStatistics(@Context HttpServletRequest request) {
		RepositoryStatisticsVO stats = getRepositoryStatistics();
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
	@GET
	@Path("indexer")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getIndexerStatistics(@Context HttpServletRequest request) {
		IndexerStatisticsVO stats = getIndexerStatistics();
		return Response.ok(stats).build();
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
	public Response getSessions(@Context HttpServletRequest request) {
		SessionsVO vo = getSessionsVO();
		return Response.ok(vo).build();
	}
	
	private UserStatisticsVO getUserStatistics() {
		UserStatisticsVO stats = new UserStatisticsVO();
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		long countUsers = securityManager.countIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, null, null, null);
		stats.setTotalUserCount(countUsers);
		
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		long countGroups = bgs.countBusinessGroups(null, null);
		stats.setTotalGroupCount(countGroups);
		return stats;
	}
	
	private RepositoryStatisticsVO getRepositoryStatistics() {
		RepositoryStatisticsVO stats = new RepositoryStatisticsVO();
		RepositoryManager repoMgr = CoreSpringFactory.getImpl(RepositoryManager.class);
		int allCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_OWNERS);
		int publishedCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_USERS);
		stats.setCoursesCount(allCourses);
		stats.setPublishedCoursesCount(publishedCourses);
		return stats;
	}
	
	private IndexerStatisticsVO getIndexerStatistics() {
		IndexerStatisticsVO stats = new IndexerStatisticsVO();

		SearchServiceStatus status = SearchServiceFactory.getService().getStatus();
		if(status instanceof SearchServiceStatusImpl) {
			SearchServiceStatusImpl statusImpl = (SearchServiceStatusImpl)status;
			FullIndexerStatus fStatus = statusImpl.getFullIndexerStatus();
			stats.setIndexedDocumentCount(fStatus.getDocumentCount());
			stats.setExcludedDocumentCount(fStatus.getExcludedDocumentCount());
			stats.setIndexSize(fStatus.getIndexSize());
			stats.setIndexingTime(fStatus.getIndexingTime());
			stats.setFullIndexStartedAt(fStatus.getFullIndexStartedAt());
			stats.setDocumentQueueSize(fStatus.getDocumentQueueSize());
			stats.setRunningFolderIndexerCount(fStatus.getNumberRunningFolderIndexer());
			stats.setAvailableFolderIndexerCount(fStatus.getNumberAvailableFolderIndexer());
			stats.setLastFullIndexTime(fStatus.getLastFullIndexTime());
		}
		stats.setStatus(status.getStatus());

		return stats;
	}
	
	private SessionsVO getSessionsVO() {
		SessionsVO vo = new SessionsVO();

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
		int imConnections = CoreSpringFactory.getImpl(InstantMessagingService.class).getNumOfconnectedUsers();
		vo.setInstantMessagingCount(imConnections);
		
		return vo;
	}

}
