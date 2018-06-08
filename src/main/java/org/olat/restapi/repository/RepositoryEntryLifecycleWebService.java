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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("repo/lifecycle")
public class RepositoryEntryLifecycleWebService {
	
	/**
	 * List all public lifecycles
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType text/plain, text/html, application/xml, application/json
	 * @response.representation.200.doc List all entries in the repository
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVOes}
	 * @param uriInfo The URI information
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getPublicLifeCycles(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isLearnResourceManager() && !roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntryLifecycleDAO lifeCycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		List<RepositoryEntryLifecycle> publicLifeCycles = lifeCycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycleVO> voList = new ArrayList<>(publicLifeCycles.size());
		for(RepositoryEntryLifecycle lifeCycle: publicLifeCycles) {
			voList.add(new RepositoryEntryLifecycleVO(lifeCycle));
		}
		
		RepositoryEntryLifecycleVO[] voes = voList.toArray(new RepositoryEntryLifecycleVO[voList.size()]);
		return Response.ok(voes).build();
	}
}
