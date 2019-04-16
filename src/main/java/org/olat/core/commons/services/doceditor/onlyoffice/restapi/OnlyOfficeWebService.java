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
package org.olat.core.commons.services.doceditor.onlyoffice.restapi;

import static org.olat.core.commons.services.doceditor.onlyoffice.restapi.CallbackResponseVO.error;
import static org.olat.core.commons.services.doceditor.onlyoffice.restapi.CallbackResponseVO.success;
import static org.olat.core.commons.services.doceditor.onlyoffice.restapi.CallbackVO.STATUS_READY_FOR_SAVING;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12. Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
@Path("/onlyoffice/files/{fileId}")
public class OnlyOfficeWebService {

	private static final OLog log = Tracing.createLoggerFor(OnlyOfficeWebService.class);
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired 
	private OnlyOfficeService onlyOfficeService;
	
	@POST
	@Path("/callback")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCallback(
			@PathParam("fileId") String fileId,
			CallbackVO callbackVO,
			@Context HttpHeaders httpHeaders) {
		log.debug("OnlyOffice REST post callback request for File ID: " + fileId);
		logRequestHeaders(httpHeaders);
		log.debug("OnlyOffice REST post callback " + callbackVO);
		
		if (!onlyOfficeModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if (!onlyOfficeService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		CallbackResponseVO responseVO;
		switch(callbackVO.getStatus()) {
		case STATUS_READY_FOR_SAVING:
			responseVO = updateContent(fileId, callbackVO);
			break;
		default:
			// nothing to do
			responseVO = success();
		}
		
		return Response.ok(responseVO).build();
	}

	private CallbackResponseVO updateContent(String fileId, CallbackVO callbackVO) {
		String IdentityId = callbackVO.getUsers()[0];
		Identity identity = onlyOfficeService.getIdentity(IdentityId);
		if (identity == null) {
			return error();
		}
		
		boolean canUpdate = onlyOfficeService.canUpdateContent(fileId, identity);
		if (!canUpdate) {
			log.debug("Access has not right to update file. File ID: " + fileId + ", identity: " + IdentityId);
			return error();
		}
		boolean updated = onlyOfficeService.updateContent(fileId, identity, callbackVO.getUrl());
		return updated? success(): error();
	}
	
	@GET
	@Path("/contents")
	public Response getFile(
			@PathParam("fileId") String fileId,
			@Context HttpHeaders httpHeaders) {
		log.debug("OnlyOffice REST get file contents request for File ID: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!onlyOfficeModule.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if (!onlyOfficeService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		File file = onlyOfficeService.getFile(fileId);
		return Response
				.ok(file)
				.type(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment;")
				.build();
	}

	private void logRequestHeaders(HttpHeaders httpHeaders) {
		if (log.isDebug()) {
			log.debug("REST Resquest headers:");
			for (Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue().stream().collect(Collectors.joining(", "));
				log.debug(name + ": " + value);
			}
		}
	}
	
}
