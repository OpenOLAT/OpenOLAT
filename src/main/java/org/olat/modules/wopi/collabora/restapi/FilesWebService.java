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
package org.olat.modules.wopi.collabora.restapi;

import static org.olat.modules.wopi.WopiRestHelper.getFirstRequestHeader;
import static org.olat.modules.wopi.WopiRestHelper.getLastModifiedAsIso6801;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.collabora.CollaboraModule;
import org.olat.modules.wopi.collabora.CollaboraService;
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
		
		if (!collaboraService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		File file = collaboraService.getFile(fileId);
		CheckFileInfoVO checkFileInfoVO = CheckFileInfoVO.builder()
				.withBaseFileName(file.getName()) // suffix is mandatory
				.withOwnerId("1")
				.withSize(file.length())
				.withUserId("2")
				.withUserFriendlyName("Alice")
				.withVersion(UUID.randomUUID().toString())
				.withLastModifiedTime(getLastModifiedAsIso6801(file))
				.withUserCanWrite(Boolean.TRUE)
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
		
		if (!collaboraService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		File file = collaboraService.getFile(fileId);
		return Response
				.ok(file)
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
		
		if (!collaboraService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Access access = collaboraService.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		// Further Headers see: https://github.com/LibreOffice/online/blob/master/wsd/reference.md#putfile-headers
		String timestamp = getFirstRequestHeader(httpHeaders, "X-LOOL-WOPI-Timestamp");
		log.debug("File changed at " + timestamp + ". File ID: " + fileId + ", token: " + accessToken);
		
		try {
			File file = collaboraService.getFile(fileId);
			Files.deleteIfExists(file.toPath());
			Files.copy(fileInputStream, file.toPath());
			
			PutFileVO putFileVO = PutFileVO.builder()
				.withLastModifiedTime(getLastModifiedAsIso6801(file))
				.build();
			logPutFileResponse(putFileVO);
			
			return Response
					.ok(putFileVO)
					.type(MediaType.APPLICATION_JSON)
					.build();
			
		} catch (IOException e) {
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
