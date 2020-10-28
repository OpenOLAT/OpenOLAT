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
package org.olat.core.commons.services.doceditor.collabora.restapi;

import static org.olat.core.commons.services.doceditor.DocEditorRestHelper.getAsIso8601;
import static org.olat.core.commons.services.doceditor.DocEditorRestHelper.getFirstRequestHeader;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 8 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Hidden
@Service
@Path("/collabora/wopi/files/{fileId}")
public class CollaboraWebService {

	private static final Logger log = Tracing.createLoggerFor(CollaboraWebService.class);
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired 
	private CollaboraService collaboraService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private DocEditorIdentityService identityService;
	
	@GET
	@Operation(summary = "Get file Info", description = "Get file Info")
	@ApiResponse(responseCode = "200", description = "The files", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CheckFileInfoVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CheckFileInfoVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")	
	@ApiResponse(responseCode = "403", description = "Forbidden")
	@ApiResponse(responseCode = "404", description = "File not found")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response checkFileInfo(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") Long accessKey,
			@Context HttpHeaders httpHeaders) {
		log.debug("Collabora REST CheckFileInfo request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = docEditorService.getAccess(() -> accessKey);
		if (access == null) {
			log.debug("No access for key. File ID: " + fileId + ", key: " + accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		VFSMetadata metadata = access.getMetadata();
		String ownerId = metadata.getAuthor() != null? identityService.getGlobalIdentityId(metadata.getAuthor()): null;
		CheckFileInfoVO checkFileInfoVO = CheckFileInfoVO.builder()
				.withBaseFileName(metadata.getFilename()) // suffix is mandatory
				.withOwnerId(ownerId)
				.withSize(metadata.getFileSize())
				.withUserId(identityService.getGlobalIdentityId(access.getIdentity()))
				.withUserFriendlyName(identityService.getUserDisplayName(access.getIdentity()))
				.withVersion(String.valueOf(metadata.getRevisionNr()))
				.withLastModifiedTime(getAsIso8601(metadata.getLastModified()))
				.withUserCanWrite(Mode.EDIT == access.getMode())
				.withDisablePrint(Boolean.FALSE)
				.withDisableExport(!access.isDownload())
				.withUserCanNotWriteRelative(Boolean.TRUE)
				.build();
		logCheckFileInfoResponse(checkFileInfoVO);
		
		return Response
				.ok(checkFileInfoVO)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@GET
	@Path("/contents")
	@Operation(summary = "Retrieve content", description = "Retrieve the content of a file")
	@ApiResponse(responseCode = "200", description = "The contents")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public Response getFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") Long accessKey,
			@Context HttpHeaders httpHeaders) {
		log.debug("Collabora REST GetFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = docEditorService.getAccess(() -> accessKey);
		if (access == null) {
			log.debug("No access for key. File ID: " + fileId + ", key: " + accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		return Response
				.ok(vfsLeaf.getInputStream())
				.type(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment;")
				.build();
	}
	
	@POST
	@Path("/contents")
	@Operation(summary = "Post content", description = "Post content to a file")
	@ApiResponse(responseCode = "200", description = "The contents", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = PutFileVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = PutFileVO.class)) })
	@ApiResponse(responseCode = "401", description = "No access for key")
	@ApiResponse(responseCode = "403", description = "Forbidden")
	@ApiResponse(responseCode = "404", description = "File not found")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") Long accessKey,
			@Context HttpHeaders httpHeaders,
			InputStream fileInputStream) {
		log.debug("Collabora REST PutFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = docEditorService.getAccess(() -> accessKey);
		if (access == null) {
			log.debug("No access for key. File ID: " + fileId + ", key: " + accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		boolean canUpdate = collaboraService.canUpdateContent(access, fileId);
		if (!canUpdate) {
			log.debug("Access has not right to update file. File ID: " + fileId + ", key: " + accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		// Further Headers see: https://github.com/LibreOffice/online/blob/master/wsd/reference.md#putfile-headers
		String timestamp = getFirstRequestHeader(httpHeaders, "X-LOOL-WOPI-Timestamp");
		log.debug("File changed at " + timestamp + ". File ID: " + fileId + ", key: " + accessKey);
		
		try {
			boolean updated = collaboraService.updateContent(access, fileInputStream);
			if (updated) {
				PutFileVO putFileVO = PutFileVO.builder()
					.withLastModifiedTime(getAsIso8601(new Date()))
					.build();
				logPutFileResponse(putFileVO);
				
				return Response
						.ok(putFileVO)
						.type(MediaType.APPLICATION_JSON)
						.build();
			}
			return Response
					.serverError()
					.status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (Exception e) {
			return Response
					.serverError()
					.status(Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	private void logRequestHeaders(HttpHeaders httpHeaders) {
		if (log.isDebugEnabled()) {
			log.debug("Collabora Resquest headers:");
			for (Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue().stream().collect(Collectors.joining(", "));
				log.debug("  " + name + ": " + value);
			}
		}
	}

	private void logCheckFileInfoResponse(CheckFileInfoVO checkFileInfoVO) {
		if (log.isDebugEnabled()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writeValueAsString(checkFileInfoVO);
				log.debug("Collabora REST CheckFileInfo response: " + json);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
		}
	}

	private void logPutFileResponse(PutFileVO putFileVO) {
		if (log.isDebugEnabled()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writeValueAsString(putFileVO);
				log.debug("Collabora REST PutFile response: " + json);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
		}
	}
	
}
