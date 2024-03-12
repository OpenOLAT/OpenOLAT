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
package org.olat.core.commons.services.export.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportArchivesWebService {
	
	protected final RepositoryEntry entry;
	
	@Autowired
	protected ExportManager exportManager;
	@Autowired
	protected RepositoryService repositoryService;
	
	public ExportArchivesWebService(RepositoryEntry entry) {
		this.entry = entry;	
	}
	
	@GET
	@Operation(summary = "Get the list of archives of the course", description = "Get the list of archives of the course")
	@ApiResponse(responseCode = "200", description = "Get the repository resource", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ExportMetadataVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ExportMetadataVOes.class)) })
	@ApiResponse(responseCode = "404", description = "The repository entry not found")
	@ApiResponse(responseCode = "403", description = "Not enough permissions to access the list")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getArchives(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(entry, null,
				List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL));
		List<ExportMetadata> metadataList = exportManager.searchMetadata(params);
		
		ExportMetadataVOes metadataVoes = new ExportMetadataVOes();
		metadataVoes.setTotalCount(metadataList.size());
		metadataVoes.setExportMetadata(toArrayOfVOes(metadataList));
		return Response.ok(metadataVoes).build();
	}
	
	private ExportMetadataVO[] toArrayOfVOes(List<ExportMetadata> coursRepos) {
		int i=0;
		ExportMetadataVO[] entryVOs = new ExportMetadataVO[coursRepos.size()];
		for (ExportMetadata repoE : coursRepos) {
			entryVOs[i++] = ExportMetadataVO.valueOf(repoE);
		}
		return entryVOs;
	}
	
	protected final boolean isManager(HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		Identity identity = ureq.getIdentity();
		return repositoryService.hasRoleExpanded(identity, entry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				GroupRoles.owner.name());
	}
}
