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
package org.olat.core.commons.services.doceditor.office365.restapi;

import static org.olat.core.commons.services.doceditor.wopi.WopiRestHelper.getAsIso8601;

import java.io.InputStream;
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
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.commons.services.doceditor.office365.Office365Module;
import org.olat.core.commons.services.doceditor.office365.Office365Service;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The simplest way to test this class is to use the interactice WOPI Validation application.
 * https://wopi.readthedocs.io/en/latest/build_test_ship/validator.html#interactive-wopi-validation
 * 
 * Initial date: 26.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Office365")
@Service
@Path("/office365/wopi/files/{fileId}")
public class Office365WebService {

	private static final Logger log = Tracing.createLoggerFor(Office365WebService.class);
	
	@Autowired
	private Office365Module office365Module;
	@Autowired 
	private Office365Service office365Service;
	@Autowired
	private DocEditorIdentityService identityService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkFileInfo(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context UriInfo uriInfo,
			@Context HttpHeaders httpHeaders) {
		log.debug("WOPI REST CheckFileInfo request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!office365Module.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		String requestUrl = uriInfo.getRequestUri().toString();
		String timeStamp = httpHeaders.getHeaderString("X-WOPI-TimeStamp");
		String proofKey = httpHeaders.getHeaderString("X-WOPI-Proof");
		String oldProofKey = httpHeaders.getHeaderString("X-WOPI-ProofOld");
		boolean proofVerified = office365Service.verifyProofKey(requestUrl, accessToken, timeStamp, proofKey, oldProofKey);
		if (!proofVerified) {
			log.debug("Proof not verified. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		Access access = office365Service.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = office365Service.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String userId = identityService.getGlobalIdentityId(access.getIdentity());
		VFSMetadata metadata = access.getMetadata();
		// ownerId is mandatory (this hack seens to work)
		String ownerId = metadata.getAuthor() != null
				? identityService.getGlobalIdentityId(metadata.getAuthor())
				: userId;
		CheckFileInfoVO checkFileInfoVO = CheckFileInfoVO.builder()
				.withBaseFileName(metadata.getFilename()) // suffix is mandatory
				.withOwnerId(ownerId)
				.withSize(metadata.getFileSize())
				.withUserId(userId)
				.withUserFriendlyName(identityService.getUserDisplayName(access.getIdentity()))
				.withVersion(String.valueOf(metadata.getRevisionNr()))
				.withLastModifiedTime(getAsIso8601(metadata.getLastModified()))
				.withSupportsGetLock(true)
				.withSupportsLocks(true)
				.withSupportsExtendedLockLength(true)
				.withSupportsUpdate(true)
				.withSupportsRename(false)
				.withUserCanWrite(access.isCanEdit())
				.withUserCanNotWriteRelative(true)
				.build();
		logCheckFileInfoResponse(checkFileInfoVO);
		
		return Response
				.ok(checkFileInfoVO)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	@POST
	public Response post(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context UriInfo uriInfo,
			@Context HttpHeaders httpHeaders) {
		log.debug("WOPI REST post request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!office365Module.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		String requestUrl = uriInfo.getRequestUri().toString();
		String timeStamp = httpHeaders.getHeaderString("X-WOPI-TimeStamp");
		String proofKey = httpHeaders.getHeaderString("X-WOPI-Proof");
		String oldProofKey = httpHeaders.getHeaderString("X-WOPI-ProofOld");
		boolean proofVerified = office365Service.verifyProofKey(requestUrl, accessToken, timeStamp, proofKey, oldProofKey);
		if (!proofVerified) {
			log.debug("Proof not verified. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		Access access = office365Service.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = office365Service.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String xWopiOverride = httpHeaders.getHeaderString("X-WOPI-Override");
		if (!StringHelper.containsNonWhitespace(xWopiOverride)) {
			log.debug("Missing or invalid X-WOPI-Override header. File ID: " + fileId);
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		switch (xWopiOverride) {
		case "LOCK": {
			return lock(vfsLeaf, access, httpHeaders);
		}
		case "UNLOCK": {
			return unlock(vfsLeaf, access, httpHeaders);
		}
		case "REFRESH_LOCK": {
			return refreshLock(vfsLeaf, access, httpHeaders);
		}
		case "GET_LOCK": {
			return getLock(vfsLeaf, access);
		}
		default:
			return Response.serverError().status(Status.NOT_IMPLEMENTED).build();
		}
	}
	
	private Response lock(VFSLeaf vfsLeaf, Access access, HttpHeaders httpHeaders) {
		String lockToken = httpHeaders.getHeaderString("X-WOPI-Lock");
		if (!StringHelper.containsNonWhitespace(lockToken)) {
			log.debug("Missing or invalid X-WOPI-Lock header. File ID: " + access.getMetadata().getUuid());
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		String oldLockToken = httpHeaders.getHeaderString("X-WOPI-OldLock");
		if (StringHelper.containsNonWhitespace(oldLockToken)) {
			office365Service.unlock(vfsLeaf, oldLockToken);
		}

		boolean isLockedForMe = office365Service.isLockedForMe(vfsLeaf, access.getIdentity());
		if (isLockedForMe) {
			String currentLockToken = office365Service.getLockToken(vfsLeaf);
			return Response.serverError()
					.status(Status.CONFLICT)
					.header("X-WOPI-Lock", currentLockToken)
					.build();
		}
		
		String currentLockToken = office365Service.getLockToken(vfsLeaf);
		if (currentLockToken == null) {
			if(office365Service.lock(vfsLeaf, access.getIdentity(), lockToken)) {
				String itemVersion = String.valueOf(access.getMetadata().getRevisionNr());
				return Response.ok()
						.header("X-WOPI-ItemVersion", itemVersion)
						.build();
			} else {
				return Response.serverError()
						.status(Status.CONFLICT)
						.header("X-WOPI-Lock", currentLockToken)
						.build();
			}
		}
		
		if (lockToken.equals(currentLockToken)) {
			return refreshLock(vfsLeaf, access, httpHeaders);
		}

		currentLockToken = StringHelper.blankIfNull(currentLockToken);	
		return Response.serverError()
				.status(Status.CONFLICT)
				.header("X-WOPI-Lock", currentLockToken)
				.build();
	}

	private Response unlock(VFSLeaf vfsLeaf, Access access, HttpHeaders httpHeaders) {
		String lockToken = httpHeaders.getHeaderString("X-WOPI-Lock");
		if (!StringHelper.containsNonWhitespace(lockToken)) {
			log.debug("Missing or invalid X-WOPI-Lock header. File ID: " + access.getMetadata().getUuid());
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		boolean canUnlock = office365Service.canUnlock(vfsLeaf, lockToken);
		if (canUnlock) {
			office365Service.unlock(vfsLeaf, lockToken);
			String itemVersion = String.valueOf(access.getMetadata().getRevisionNr());
			return Response.ok()
					.header("X-WOPI-ItemVersion", itemVersion)
					.build();
		}
		
		String currentLockToken = office365Service.getLockToken(vfsLeaf);
		currentLockToken = StringHelper.blankIfNull(currentLockToken);
		return Response.serverError()
				.status(Status.CONFLICT)
				.header("X-WOPI-Lock", currentLockToken)
				.build();
	}
	
	private Response refreshLock(VFSLeaf vfsLeaf, Access access, HttpHeaders httpHeaders) {
		String lockToken = httpHeaders.getHeaderString("X-WOPI-Lock");
		if (!StringHelper.containsNonWhitespace(lockToken)) {
			log.debug("Missing or invalid X-WOPI-Lock header. File ID: " + access.getMetadata().getUuid());
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		String currentLockToken = office365Service.getLockToken(vfsLeaf);
		if (lockToken.equals(currentLockToken)) {
			office365Service.refreshLock(vfsLeaf, lockToken);
			return Response.ok().build();
		}

		currentLockToken = StringHelper.blankIfNull(currentLockToken);	
		return Response.serverError()
				.status(Status.CONFLICT)
				.header("X-WOPI-Lock", currentLockToken)
				.build();
	}
	
	private Response getLock(VFSLeaf vfsLeaf, Access access) {
		boolean isLockedForMe = office365Service.isLockedForMe(vfsLeaf, access.getIdentity());
		if (isLockedForMe) {
			return Response.serverError()
					.status(Status.CONFLICT)
					.build();
		}
		
		String currentLockToken = office365Service.getLockToken(vfsLeaf);
		currentLockToken = StringHelper.blankIfNull(currentLockToken);
		return Response.ok()
				.header("X-WOPI-Lock", currentLockToken)
				.build();
	}

	@GET
	@Path("/contents")
	public Response getFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context UriInfo uriInfo,
			@Context HttpHeaders httpHeaders) {
		log.debug("WOPI REST GetFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!office365Module.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		String requestUrl = uriInfo.getRequestUri().toString();
		String timeStamp = httpHeaders.getHeaderString("X-WOPI-TimeStamp");
		String proofKey = httpHeaders.getHeaderString("X-WOPI-Proof");
		String oldProofKey = httpHeaders.getHeaderString("X-WOPI-ProofOld");
		boolean proofVerified = office365Service.verifyProofKey(requestUrl, accessToken, timeStamp, proofKey, oldProofKey);
		if (!proofVerified) {
			log.debug("Proof not verified. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		Access access = office365Service.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = office365Service.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String itemVersion = String.valueOf(access.getMetadata().getRevisionNr());
		return Response
				.ok(vfsLeaf.getInputStream())
				.type(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment;")
				.header("X-WOPI-ItemVersion", itemVersion)
				.build();
	}
	
	@POST
	@Path("/contents")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putFile(
			@PathParam("fileId") String fileId,
			@QueryParam("access_token") String accessToken,
			@Context UriInfo uriInfo,
			@Context HttpHeaders httpHeaders,
			InputStream fileInputStream) {
		log.debug("WOPI REST PutFile request for file: " + fileId);
		logRequestHeaders(httpHeaders);
		
		if (!office365Module.isEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		String requestUrl = uriInfo.getRequestUri().toString();
		String timeStamp = httpHeaders.getHeaderString("X-WOPI-TimeStamp");
		String proofKey = httpHeaders.getHeaderString("X-WOPI-Proof");
		String oldProofKey = httpHeaders.getHeaderString("X-WOPI-ProofOld");
		boolean proofVerified = office365Service.verifyProofKey(requestUrl, accessToken, timeStamp, proofKey, oldProofKey);
		if (!proofVerified) {
			log.debug("Proof not verified. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		Access access = office365Service.getAccess(accessToken);
		if (access == null) {
			log.debug("No access for token. File ID: " + fileId + ", token: " + accessToken);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = office365Service.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: " + fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String lockToken = httpHeaders.getHeaderString("X-WOPI-Lock");
		String currentLockToken = office365Service.getLockToken(vfsLeaf);
		boolean updateLockToken = StringHelper.containsNonWhitespace(lockToken) && lockToken.equals(currentLockToken);
		boolean fileIs0bytes = access.getMetadata().getFileSize() == 0l;
		boolean newCreated = !StringHelper.containsNonWhitespace(lockToken) && fileIs0bytes;
		boolean lockTokenInValid = !updateLockToken && !newCreated;
		if (lockTokenInValid) {
			log.debug("Current lock token (" + currentLockToken + ") not equals lock token from header (" + lockToken
					+ "). File ID: " + access.getMetadata().getUuid());
			currentLockToken = StringHelper.blankIfNull(currentLockToken);
			return Response.serverError()
					.status(Status.CONFLICT)
					.header("X-WOPI-Lock", currentLockToken)
					.build();
		}
		
		try {
			boolean updated = office365Service.updateContent(access, fileInputStream);
			if (updated) {
				String itemVersion = String.valueOf(access.getMetadata().getRevisionNr());
				return Response.ok()
						.header("X-WOPI-ItemVersion", itemVersion)
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
			log.debug("WOPI Resquest headers:");
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
				log.debug("WOPI REST CheckFileInfo response: " + json);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
		}
	}

}
