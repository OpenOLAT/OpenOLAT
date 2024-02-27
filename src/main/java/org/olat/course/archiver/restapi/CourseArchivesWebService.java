/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.restapi.ExportArchivesWebService;
import org.olat.core.commons.services.export.restapi.ExportMetadataVO;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.archiver.CourseArchiveListController;
import org.olat.course.archiver.wizard.CourseArchiveContext;
import org.olat.course.archiver.wizard.CourseArchiveExportTask;
import org.olat.course.archiver.wizard.CourseArchiveOptions;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Course - Archives")
public class CourseArchivesWebService extends ExportArchivesWebService {
	
	public static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	@Autowired
	private CourseModule courseModule;
	
	public CourseArchivesWebService(RepositoryEntry entry) {
		super(entry);	
	}

	@POST
	@Path("")
	@Operation(summary = "Start a new course archive", description = "Start a new course archive")
	@ApiResponse(responseCode = "200", description = "The export informations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ExportMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ExportMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The course could not be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postArchive(CourseArchiveOptionsVO archiveOptionsVo, @Context HttpServletRequest httpRequest) {
		if(isManager(httpRequest)) {
			return startArchive(archiveOptionsVo, httpRequest);
		}
		return Response.serverError().status(Status.FORBIDDEN).build();
	}
	
	@PUT
	@Path("")
	@Operation(summary = "Start a new course archive with default options", description = "Start a new course archive with default options")
	@ApiResponse(responseCode = "200", description = "The export informations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ExportMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ExportMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The course could not be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putArchive(@Context HttpServletRequest httpRequest) {
		if(isManager(httpRequest)) {
			return startArchive(null, httpRequest);
		}
		return Response.serverError().status(Status.FORBIDDEN).build();
	}
	
	@GET
	@Path("{archiveKey}/file")
	@Operation(summary = "Start a new course archive", description = "Start a new course archive")
	@ApiResponse(responseCode = "200", description = "The export informations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ExportMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ExportMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The course could not be found")
	@Produces({ "application/zip" })
	public Response getArchive(@PathParam("archiveKey") Long archiveKey, @Context HttpServletRequest httpRequest,
			@Context HttpServletResponse httpResponse) {
		if(isManager(httpRequest)) {
			
			ExportMetadata metadata = exportManager.getExportMetadataByKey(archiveKey);
			if(metadata == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			File file = VFSManager.olatRootFile(metadata.getFilePath());
			if(file == null || !file.exists()) {
				return Response.serverError().status(Status.NO_CONTENT).build();
			}

			httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(metadata.getFilename()));			
			httpResponse.setHeader("Content-Description", StringHelper.urlEncodeUTF8(metadata.getFilename()));
			httpResponse.setContentLengthLong(file.length());
			
			return Response.ok(file).cacheControl(cc).build(); // success
		}
		return Response.serverError().status(Status.FORBIDDEN).build();
	}
	
	private Response startArchive(CourseArchiveOptionsVO archiveOptionsVo, HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		Identity identity = ureq.getIdentity();
		Locale locale = ureq.getLocale();
		Roles roles = ureq.getUserSession().getRoles();
		
		CourseArchiveOptions options = getArchiveOptions(archiveOptionsVo, identity, roles, locale);
		CourseArchiveExportTask task = new CourseArchiveExportTask(options, OresHelper.clone(entry.getOlatResource()), locale);

		Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(ureq.getRequestTimestamp(), courseModule.getCourseArchiveRetention()));
		
		ExportMetadata metadata = exportManager.startExport(task, options.getTitle(), null, options.getFilename(),
				ArchiveType.COMPLETE, expirationDate, true,
				entry, CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT, ureq.getIdentity());
		
		ExportMetadataVO metadataVo = ExportMetadataVO.valueOf(metadata);
		return Response.ok(metadataVo).build();
	}
	
	private CourseArchiveOptions getArchiveOptions(CourseArchiveOptionsVO archiveOptionsVo, Identity identity, Roles roles, Locale locale) {
		CourseArchiveContext archiveContext = CourseArchiveContext.defaultValues(entry, identity, roles, repositoryService);
		CourseArchiveOptions options = archiveContext.getArchiveOptions();
		
		if(archiveOptionsVo != null) {
			if(archiveOptionsVo.getTitle() != null) {
				options.setTitle(archiveOptionsVo.getTitle());
			}
			if(archiveOptionsVo.getFilename() != null) {
				options.setFilename(CourseArchiveExportTask.getFilename(archiveOptionsVo.getFilename()));
			}
			
			if(archiveOptionsVo.getResultsWithPDFs() != null) {
				options.setResultsWithPDFs(archiveOptionsVo.getResultsWithPDFs().booleanValue());
			}
			
			if(archiveOptionsVo.getLogFiles() != null) {
				options.setLogFiles(archiveOptionsVo.getLogFiles().booleanValue());
			}
			if(archiveOptionsVo.getCourseResults() != null) {
				options.setCourseResults(archiveOptionsVo.getCourseResults().booleanValue());
			}
			if(archiveOptionsVo.getCourseChat() != null) {
				options.setCourseChat(archiveOptionsVo.getCourseChat().booleanValue());
			}
			
			if(archiveContext.isAllowedLogAuthors() &&  archiveOptionsVo.getLogFilesAuthors() != null) {
				options.setLogFilesAuthors(archiveOptionsVo.getLogFilesAuthors().booleanValue());
			}
			if(archiveContext.isAllowedLogUsers() && archiveOptionsVo.getLogFilesUsers() != null) {
				options.setLogFilesUsers(archiveOptionsVo.getLogFilesUsers().booleanValue());
			}
			if(archiveContext.isAllowedLogStatistics() && archiveOptionsVo.getLogFilesStatistics() != null) {
				options.setLogFilesStatistics(archiveOptionsVo.getLogFilesStatistics().booleanValue());
			}
			
			if(archiveOptionsVo.getLogFilesStartDate() != null) {
				options.setLogFilesStartDate(archiveOptionsVo.getLogFilesStartDate());
			}
			if(archiveOptionsVo.getLogFilesEndDate() != null) {
				options.setLogFilesEndDate(archiveOptionsVo.getLogFilesEndDate());
			}
		}
		
		if(options.getTitle() == null) {
			options.setTitle(CourseArchiveExportTask.getArchiveName(entry, options.getArchiveType(), locale));
		}
		if(options.getFilename() == null) {
			options.setFilename(CourseArchiveExportTask.getFilename(options.getTitle()));
		}
		return options;
	}
}
