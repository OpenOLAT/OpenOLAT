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
package org.olat.restapi.repository;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Repo")
@Component
@Path("repo/lifecycle")
public class RepositoryEntryLifecycleWebService {
	
	@Autowired
	private RepositoryEntryLifecycleDAO lifeCycleDao;
	
	/**
	 * List all public lifecycles
	 * 
	 * @param uriInfo The URI information
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Operation(summary = "List all public lifecycles", description = "List all public lifecycles")
	@ApiResponse(responseCode = "200", description = "List all entries in the repository", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class))) })
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getPublicLifeCycles(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isLearnResourceManager() && !roles.isAdministrator()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<RepositoryEntryLifecycle> publicLifeCycles = lifeCycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycleVO> voList = new ArrayList<>(publicLifeCycles.size());
		for(RepositoryEntryLifecycle lifeCycle: publicLifeCycles) {
			voList.add(new RepositoryEntryLifecycleVO(lifeCycle));
		}
		
		RepositoryEntryLifecycleVO[] voes = voList.toArray(new RepositoryEntryLifecycleVO[voList.size()]);
		return Response.ok(voes).build();
	}
}
