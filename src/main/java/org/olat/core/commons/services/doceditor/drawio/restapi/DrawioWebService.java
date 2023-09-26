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
package org.olat.core.commons.services.doceditor.drawio.restapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.doceditor.drawio.DrawioService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * 
 * Initial date: 22. Sept 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Hidden
@Service
@Path("/drawio/files/{fileId}")
public class DrawioWebService {

	private static final Logger log = Tracing.createLoggerFor(DrawioWebService.class);
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioService drawioService;
	@Autowired
	private DocEditorService docEditorService;
	
	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfo(
			@PathParam("fileId") Long fileId,
			@QueryParam("access_token") Long accessKey,
			@Context HttpServletRequest request) {
		log.debug("Drawio info request for File ID: {}", fileId);
		
		return get(request, fileId, accessKey, false);
	}
	
	@GET
	@Path("/content")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFile(
			@PathParam("fileId") Long fileId,
			@QueryParam("access_token") Long accessKey,
			@Context HttpServletRequest request) {
		log.debug("Drawio content request for File ID: {}", fileId);
		
		return get(request, fileId, accessKey, true);
	}

	private Response get(HttpServletRequest request, Long fileId, Long accessKey, boolean loadXml) {
		if (!drawioModule.isEnabled() || !drawioModule.isCollaborationEnabled()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Access access = docEditorService.getAccess(() -> accessKey);
		if (access == null) {
			log.debug("No access for token. File ID: {}, token: {}", fileId, accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = RestSecurityHelper.getIdentity(request);
		if (identity == null || !identity.getKey().equals(access.getIdentity().getKey())) {
			log.debug("Request identity ({}) does not match access identity ({})", identity, access.getIdentity());
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if (!access.getMetadata().getKey().equals(fileId)) {
			log.debug("File ID does not match access token File ID: {}, token: {}", fileId, accessKey);
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSLeaf vfsLeaf = docEditorService.getVfsLeaf(access);
		if (vfsLeaf == null) {
			log.debug("File not found. File ID: {}", fileId);
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		FileInfoVO fileInfoVO = new FileInfoVO();
		addFileInfos(fileInfoVO, access, vfsLeaf, loadXml);
		
		return Response.ok(fileInfoVO).build();
	}

	private void addFileInfos(FileInfoVO fileInfoVO, Access access, VFSLeaf vfsLeaf, boolean addXml) {
		VFSMetadata vfsMetadata = access.getMetadata();
		fileInfoVO.setId(vfsMetadata.getKey());
		fileInfoVO.setSize(vfsMetadata.getFileSize());
		fileInfoVO.setWriteable(Mode.EDIT == access.getMode());
		fileInfoVO.setMime("application/x-drawio");
		fileInfoVO.setPath(vfsMetadata.getRelativePath());
		fileInfoVO.setName(vfsMetadata.getFilename());
		fileInfoVO.setMtime(vfsLeaf.getLastModified());
		fileInfoVO.setEtag("E" + fileInfoVO.getMtime());
		fileInfoVO.setVersionsEnabled(false); // ignored by draw.io?
		fileInfoVO.setCreated(0);
		fileInfoVO.setVer(2);
		fileInfoVO.setInstanceId(WebappHelper.getInstanceId());
		
		if (addXml) {
			String xml = drawioService.getContent(vfsLeaf);
			fileInfoVO.setXml(xml);
		}
	}
}
