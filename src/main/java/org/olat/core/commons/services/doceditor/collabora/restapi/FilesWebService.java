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

import static org.olat.core.commons.services.doceditor.wopi.WopiRestHelper.getAsIso8601;
import static org.olat.core.commons.services.doceditor.wopi.WopiRestHelper.getFirstRequestHeader;

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

import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 8 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
@Path("/collabora/wopi/files/{fileId}")
public class FilesWebService {

	private static final OLog log = Tracing.createLoggerFor(FilesWebService.class);
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired 
	private CollaboraService collaboraService;
	@Autowired
	private UserManager userManager;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkFileInfo(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context HttpHeaders httpHeaders) {
		log.debug("WOPI REST CheckFileInfo request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = collaboraService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		VFSMetadata metadata = access.getMetadata();
		String ownerId = metadata.getAuthor() != null? metadata.getAuthor().getKey().toString(): null;
		CheckFileInfoVO checkFileInfoVO = CheckFileInfoVO.builder()
				.withBaseFileName(metadata.getFilename()) // suffix is mandatory
				.withOwnerId(ownerId)
				.withSize(metadata.getFileSize())
				.withUserId(access.getIdentity().getKey().toString())
				.withUserFriendlyName(userManager.getUserDisplayName(access.getIdentity()))
				.withVersion(String.valueOf(metadata.getRevisionNr()))
				.withLastModifiedTime(getAsIso8601(metadata.getLastModified()))
				.withUserCanWrite(access.isCanEdit())
				.withDisablePrint(Boolean.FALSE)
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
	public Response getFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context HttpHeaders httpHeaders) {
		log.debug("WOPI REST GetFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = collaboraService.getVfsLeaf(access);
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
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context HttpHeaders httpHeaders,
			InputStream fileInputStream) {
		log.debug("WOPI REST PutFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!collaboraModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = collaboraService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		boolean canUpdate = collaboraService.canUpdateContent(access, fileId);
		if (!canUpdate) {
			log.debug("Access has not right to update file. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		// Further Headers see: https://github.com/LibreOffice/online/blob/master/wsd/reference.md#putfile-headers
		String timestamp = getFirstRequestHeader(httpHeaders, "X-LOOL-WOPI-Timestamp");
		log.debug("File changed at " + timestamp + ". File ID: " + fileId + ", token: " + accessToken);
		
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
		if (log.isDebug()) {
			log.debug("WOPI Resquest headers:");
			for (Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue().stream().collect(Collectors.joining(", "));
				log.debug(name + ": " + value);
			}
		}
	}

	private void logCheckFileInfoResponse(CheckFileInfoVO checkFileInfoVO) {
		if (log.isDebug()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writeValueAsString(checkFileInfoVO);
				log.debug("WOPI REST CheckFileInfo response: " + json);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
		}
	}

	private void logPutFileResponse(PutFileVO putFileVO) {
		if (log.isDebug()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writeValueAsString(putFileVO);
				log.debug("WOPI REST PutFile response: " + json);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
		}
	}
	
}
