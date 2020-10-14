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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.restapi.system.vo.StatusVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


/**
 * 
 * Initial date: 15.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatusWebservice {
	
	private static final Logger log = Tracing.createLoggerFor(StatusWebservice.class);

	private static final String PING_REF = "REST-Ping";
	private static final OLATResourceable PING_RESOURCE = OresHelper.createOLATResourceableInstance(PING_REF, 42l);
	
	/**
	 * Return the statistics about runtime: uptime, classes loaded, memory
	 * summary, threads count...
	 * 
	 * @param request The HTTP request
	 * @return The informations about runtime, uptime, classes loaded, memory summary...
	 */
	@GET
	@Operation(summary = "Return the statistics about runtime", description = "Return the statistics about runtime: uptime, classes loaded, memory\n" + 
			"	  summary, threads count...")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = StatusVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = StatusVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSystemSummaryVO() {
		StatusVO stats = new StatusVO();
		
		//File
		try {
			long startFile = System.nanoTime();
			File infoFile = setInfoFiles("ping");
			WorkThreadInformations.unset();
			stats.setWriteFileInMilliseconds(CodeHelper.nanoToMilliTime(startFile));
			stats.setWriteFile(infoFile.exists());
			Files.deleteIfExists(infoFile.toPath());
		} catch (Exception e) {
			stats.setWriteFile(false);
			stats.setWriteFileInMilliseconds(-1l);
			log.error("", e);
		}
		
		//Datebase
		try {
			stats.setWriteDb(true);
			
			PropertyManager propertyManager = CoreSpringFactory.getImpl(PropertyManager.class);
			List<Property> props = propertyManager.findProperties((Identity)null, (BusinessGroup)null, PING_RESOURCE, PING_REF, PING_REF);
			if(props != null && !props.isEmpty()) {
				for(Property prop:props) {
					propertyManager.deleteProperty(prop);
				}
			}
			DBFactory.getInstance().commit();
			
			long startDB = System.nanoTime();
			Property prop = propertyManager.createPropertyInstance(null, null, PING_RESOURCE, PING_REF, PING_REF, 0f, 0l, "-", "-");
			DBFactory.getInstance().commit();
			stats.setWriteDbInMilliseconds(CodeHelper.nanoToMilliTime(startDB));

			propertyManager.deleteProperty(prop);
			DBFactory.getInstance().commit();
		} catch (Exception e) {
			stats.setWriteDb(false);
			stats.setWriteDbInMilliseconds(-1l);
			log.error("", e);
		}
		
		//Secure authenticated user
		UserSessionManager sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		Set<UserSession> userSessions = sessionManager.getAuthenticatedUserSessions();
		int secureAuthenticatedCount = 0;
		for (UserSession usess:userSessions) {
			SessionInfo sessInfo = usess.getSessionInfo();
			if (sessInfo.isWebDAV() || sessInfo.isREST()) {
				//
			} else if (sessInfo.isSecure()) {
				secureAuthenticatedCount++;
			}
		}
		stats.setSecureAuthenticatedCount(secureAuthenticatedCount);
		
		//Concurrent dispatch threads
		SessionStatsManager sessionStatsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		stats.setConcurrentDispatchThreads(sessionStatsManager.getConcurrentCounter());

		return Response.ok(stats).build();
	}
	
	/**
	 * The method return an exception if something happens.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public static File setInfoFiles(String filePath) throws IOException {
		File file = new File(WebappHelper.getUserDataRoot(), "threadInfos");
		if(!file.exists()) {
			file.mkdirs();
		}
		File infoFile = new File(file, Thread.currentThread().getName());
		FileUtils.save(new FileOutputStream(infoFile), filePath, "UTF-8");
		return infoFile;
	}
}
