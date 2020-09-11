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
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import org.olat.core.commons.services.doceditor.onlyoffice.Callback;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.lock.LockResult;
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
		log.debug("ONLYOFFICE REST post callback request for File ID: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!onlyOfficeModule.isEnabled() || !onlyOfficeModule.isEditorEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if (!onlyOfficeService.fileExists(fileId)) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String autorisazion = httpHeaders.getHeaderString("Authorization");
		if (!StringHelper.containsNonWhitespace(autorisazion) || !autorisazion.startsWith("Bearer")) {
			log.debug("Missing or invalid authorization header. File ID: " + fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		String jwtToken = autorisazion.substring(7); // The part after "Bearer "
		log.debug("JWT token: " + jwtToken);
		Callback callback = onlyOfficeSecurityService.getCallback(jwtToken);
		if (callback == null) {
			log.debug("Error while converting JWT token to callback. File ID: " + fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		if (log.isDebugEnabled()) log.debug("Callback: " + callback);
		
		CallbackResponseVO responseVO;
		CallbackStatus status = CallbackStatus.valueOf(callback.getStatus());
		switch(status) {
		case Editing:
			// If a document is opened in view mode, ONLYOFFICE does not send an editing callback and therefore the document won't be locked.
			responseVO = lock(fileId, callback);
			break;
		case ClosedWithoutChanges:
			// This callback is called
			//     A) if a user closes a document without changes.
			//     B) if a user does not edit a document for about one minute.
			// Case B) results in a opened, unlocked file. If the user starts to edit the file again callback "Editing" is called and the file is locked again.
			// However, is is possible to edit the file in the meantime in another editor and all changes made in ONLYOFFICE are lost!
			// (This is the same implementation like in Alfresco, ownCloud, Nextcloud etc.)
			responseVO = unlock(fileId, callback);
			break;
		case MustSave:
		case MustForceSave:
			responseVO = updateContent(fileId, callback, versionControlled);
			break;
		case ErrorCorrupted:
			log.warn("ONLYOFFICE has reported that saving the document has failed. File ID: " + fileId);
			responseVO = success();
			break;
		case ErrorCorruptedForce:
			log.warn("ONLYOFFICE has reported that saving the document has failed. File ID: " + fileId);
			responseVO = success();
			break;
		case ErrorDocumentNotFound:
			// I never get that status, so I do not know, how to reproduce it.
			log.warn("ONLYOFFICE has reported that no doc with the specified key can be found. File ID: " + fileId);
			responseVO = success();
			break;
		default:
			// nothing to do
			responseVO = success();
		}
		
		return Response.ok(responseVO).build();
	}

	private CallbackResponseVO lock(String fileId, Callback callback) {
		String IdentityId = callback.getUsers()[0];
		Identity identity = onlyOfficeService.getIdentity(IdentityId);
		if (identity == null) return error();
		
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		boolean isLockedForMe = onlyOfficeService.isLockedForMe(vfsLeaf, identity);
		if (isLockedForMe) return error();
		
		boolean canUpdate = onlyOfficeService.canUpdateContent(vfsLeaf, identity, callback.getKey());
		if (!canUpdate) {
			log.debug("ONLYOFFICE has no right to update file. File ID: " + fileId + ", identity: " + IdentityId);
			return error();
		}
		
		LockResult lock = onlyOfficeService.lock(vfsLeaf, identity);
		return lock != null? success(): error();
	}

	private CallbackResponseVO unlock(String fileId, Callback callback) {
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		// Every user which opens the document sets a lock, i.e. adds a new token to the lock.
		// Because we are not able to get the LockInfo at that place, we do not unlock for every user who closes the document.
		// We let all lock (tokens) until the last user closes the document and then we remove the lock and all its tokens.
		boolean lastUser = callback.getUsers() == null || callback.getUsers().length == 0;
		if (lastUser) {
			onlyOfficeService.unlock(vfsLeaf);
		}
		return success();
	}

	private CallbackResponseVO updateContent(String fileId, Callback callback, boolean versionControlled) {
		String IdentityId = callback.getUsers()[0];
		Identity identity = onlyOfficeService.getIdentity(IdentityId);
		if (identity == null) return error();
		
		VFSLeaf vfsLeaf = onlyOfficeService.getVfsLeaf(fileId);
		if (vfsLeaf == null) return error();
		
		boolean canUpdate = onlyOfficeService.canUpdateContent(vfsLeaf, identity, callback.getKey());
		if (!canUpdate) {
			log.debug("ONLYOFFICE has no right to update file. File ID: " + fileId + ", identity: " + IdentityId);
			return error();
		}
		boolean updated = onlyOfficeService.updateContent(vfsLeaf, identity, callback.getUrl(), versionControlled);
		onlyOfficeService.unlock(vfsLeaf);
		return updated? success(): error();
	}
	
	@GET
	@Path("/contents")
	@Operation(summary = "Retrieve content", description = "Retrieve the content of a file")
	@ApiResponse(responseCode = "200", description = "ONLYOFFICE REST get file contents request for File ID")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public Response getFile(
			@PathParam("fileId") String fileId,
			@Context HttpHeaders httpHeaders) {
		log.debug("ONLYOFFICE REST get file contents request for File ID: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!onlyOfficeModule.isEnabled() || !onlyOfficeModule.isEditorEnabled()) {
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
		if (log.isDebugEnabled()) {
			log.debug("REST Resquest headers:");
			for (Entry<String, List<String>> entry : httpHeaders.getRequestHeaders().entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue().stream().collect(Collectors.joining(", "));
				log.debug(name + ": " + value);
			}
		}
	}
	
}
