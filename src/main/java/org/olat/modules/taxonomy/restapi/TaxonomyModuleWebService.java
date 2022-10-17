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
package org.olat.modules.taxonomy.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 5 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Taxonomy")
@Path("taxonomy")
@Component
public class TaxonomyModuleWebService {
	
	@Autowired
	private TaxonomyService taxonomyService;

	@GET
	@Path("/")
	@Operation(summary = "Return the list of taxonomy objects in the system", description = "Return the list of taxonomy objects in the system")
	@ApiResponse(responseCode = "200", description = "A taxonomy", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TaxonomyVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TaxonomyVOes.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getTaxonomyList(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isSystemAdmin()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		int numOfTaxonomies = taxonomyList.size();
		TaxonomyVOes taxonomyVOes = new TaxonomyVOes();
		TaxonomyVO[] taxonomyArr = new TaxonomyVO[numOfTaxonomies];
		for(int i=numOfTaxonomies; i-->0; ) {
			taxonomyArr[i] = new TaxonomyVO(taxonomyList.get(i));
		}
		taxonomyVOes.setTaxonomies(taxonomyArr);
		taxonomyVOes.setTotalCount(numOfTaxonomies);
		return Response.ok(taxonomyVOes).build();
	}
	
	@Path("{taxonomyKey}")
	public TaxonomyWebService getTaxonomyWebService(@PathParam("taxonomyKey") Long taxonomyKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isSystemAdmin()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		if(taxonomyKey == null || taxonomyKey.longValue() <= 0) {
			throw new WebApplicationException(Response.serverError().status(Status.BAD_REQUEST).build());
		}
		
		Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(taxonomyKey));
		if(taxonomy == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		return new TaxonomyWebService(taxonomy);
	}
}
