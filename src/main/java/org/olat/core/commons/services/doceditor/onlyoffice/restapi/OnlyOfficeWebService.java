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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.onlyoffice.Action;
import org.olat.core.commons.services.doceditor.onlyoffice.Callback;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.onlyoffice.model.CallbackImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 12. Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Hidden
@Service
@Path("/onlyoffice/files/{fileId}")
public class OnlyOfficeWebService {

	private static final Logger log = Tracing.createLoggerFor(OnlyOfficeWebService.class);
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired 
	private OnlyOfficeService onlyOfficeService;
	@Autowired 
	private OnlyOfficeSecurityService onlyOfficeSecurityService;
	
	@POST
	@Path("/callback")
	@Operation(summary = "Post Callback", description = "Post Callback")
	@ApiResponse(responseCode = "200", description = "The contents", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponseVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CallbackResponseVO.class)) })
	@ApiResponse(responseCode = "403", description = "Forbidden")
	@ApiResponse(responseCode = "404", description = "File not found")	
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCallback(
			@PathParam("fileId") String fileId,
			@QueryParam("versionControlled") boolean versionControlled,
			@Context HttpHeaders httpHeaders) {
		log.debug("ONLYOFFICE REST post callback request for File ID: {}", fileId);
		logRequestHeaders(httpHeaders);
		
		if (!onlyOfficeModule.isEnabled() || !onlyOfficeModule.isEditorEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if (!onlyOfficeService.fileExists(fileId)) {
			log.debug("File not found. File ID: {}", fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String authorisazion = httpHeaders.getHeaderString("Authorization");
		if (!StringHelper.containsNonWhitespace(authorisazion) || !authorisazion.startsWith("Bearer")) {
			log.debug("Missing or invalid authorization header. File ID: {}", fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		String jwtToken = authorisazion.substring(7); // The part after "Bearer "
		log.debug("JWT token: {}", jwtToken);
		Callback callback = onlyOfficeSecurityService.getPayload(jwtToken, CallbackImpl.class);
		if (callback == null) {
			log.debug("Error while converting JWT token to callback. File ID: {}", fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		log.debug("Callback: {}", callback);
		
		CallbackResponseVO responseVO;
		CallbackStatus status = CallbackStatus.valueOf(callback.getStatus());
		switch(status) {
		case Editing:
			// If a document is opened in view mode, ONLYOFFICE does not send an editing callback and therefore the document won't be locked.
			responseVO = doOpenCloseEditor(fileId, callback);
			break;
		case ClosedWithoutChanges:
			responseVO = doFinishContentUnchanged(fileId);
			break;
		case MustSave:
		case MustForceSave:
			responseVO = doFinishContentChanged(fileId, callback, versionControlled);
			break;
		case ErrorCorrupted:
			log.warn("ONLYOFFICE has reported that saving the document has failed. File ID: {}", fileId);
			responseVO = success();
			break;
		case ErrorCorruptedForce:
			log.warn("ONLYOFFICE has reported that saving the document has failed. File ID: {}", fileId);
			responseVO = success();
			break;
		case ErrorDocumentNotFound:
			// I never get that status, so I do not know, how to reproduce it.
			log.warn("ONLYOFFICE has reported that no doc with the specified key can be found. File ID: {}", fileId);
			responseVO = success();
			break;
		default:
			// nothing to do
			responseVO = success();
		}
		
		/*
		 * Some words about error handling, e.g. if OpenOlat is restarted during editing
		 * or OnlyOffice can not save the file to OpenOlat for some reasons.
		 * 
		 * In such a case OnlyOffice keeps the edited state of the document.
		 * If the document is opened again in OnlyOffice, the edited state is used
		 * instead of reloading the file from OpenOlat. So the edited state can
		 * be rescued and saved to OpenOlat. But of course, this works only,
		 * if the file is actually opened again in OnlyOffice.
		 */
		
		return Response.ok(responseVO).build();
	}

	private CallbackResponseVO doOpenCloseEditor(String fileId, Callback callback) {
		if (callback.getActions() == null || callback.getActions().isEmpty()) return error();
		
		String identityId = callback.getActions().get(0).getUserid();
		Identity identity = onlyOfficeService.getIdentity(identityId);
		if (identity == null) return error();
		
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		// I've never seen more then one action in a single callback.
		Action action = callback.getActions().get(0);
		
		boolean success = false;
		if (action.getType() == 0) { // OpenOffice closed
			boolean stillEditing = isStillEditing(callback, identityId);
			success = onlyOfficeService.editorClosed(vfsLeaf, identity, stillEditing);
		} else if (action.getType() == 1) {  // OpenOffice opened
			success = onlyOfficeService.editorOpened(vfsLeaf, identity, callback.getKey());
		}
		
		return success? success(): error();
	}

	private boolean isStillEditing(Callback callback, String identityId) {
		return Arrays.asList(callback.getUsers()).contains(identityId);
	}

	private CallbackResponseVO doFinishContentUnchanged(String fileId) {
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		onlyOfficeService.editorFinishedContentUnchanged(vfsLeaf);
		
		return success();
	}

	private CallbackResponseVO doFinishContentChanged(String fileId, Callback callback, boolean versionControlled) {
		Identity identity = getUpdateIdentity(callback);
		if (identity == null) return error();
		
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		boolean success = onlyOfficeService.editorFinishedContentChanged(vfsLeaf, identity, callback.getKey(),
				callback.getUrl(), versionControlled);
		
		return success? success(): error();
	}
	
	private Identity getUpdateIdentity(Callback callback) {
		Identity identity = null;
		if (callback.getActions() != null && !callback.getActions().isEmpty()) {
			String identityId = callback.getActions().get(0).getUserid();
			identity = onlyOfficeService.getIdentity(identityId);
		} else {
			// Fallback
			String identityId = callback.getUsers()[0];
			identity = onlyOfficeService.getIdentity(identityId);
		}
		return identity;
	}
	
	@GET
	@Path("/contents")
	@Operation(summary = "Retrieve content", description = "Retrieve the content of a file")
	@ApiResponse(responseCode = "200", description = "ONLYOFFICE REST get file contents request for File ID")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public Response getFile(
			@PathParam("fileId") String fileId,
			@Context HttpHeaders httpHeaders) {
		log.debug("ONLYOFFICE REST get file contents request for File ID: {}", fileId);
		logRequestHeaders(httpHeaders);
		
		if (!onlyOfficeModule.isEnabled() || !onlyOfficeModule.isEditorEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if (!onlyOfficeService.fileExists(fileId)) {
			log.debug("File not found. File ID: {}", fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String authorisazion = httpHeaders.getHeaderString("Authorization");
		if (!StringHelper.containsNonWhitespace(authorisazion) || !authorisazion.startsWith("Bearer")) {
			log.debug("Missing or invalid authorization header. File ID: {}", fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		String jwtToken = authorisazion.substring(7); // The part after "Bearer "
		log.debug("JWT token: {}", jwtToken);
		Object payload = onlyOfficeSecurityService.getPayload(jwtToken, Object.class);
		if (payload == null) {
			log.debug("Error while converting JWT token of content request. File ID: {}", fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		File file = onlyOfficeService.getFile(fileId);
		return Response
				.ok(file)
				.type(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment;")
				.build();
	}

	private void logRequestHeaders(HttpHeaders httpHeaders) {
		if (log.isDebugEnabled()) {
			log.debug("REST Resquest headers:");
			for (Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue().stream().collect(Collectors.joining(", "));
				log.debug("{}: {}", name,  value);
			}
		}
	}
	
}
