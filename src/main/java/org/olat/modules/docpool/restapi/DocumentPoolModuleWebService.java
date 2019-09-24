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
package org.olat.modules.docpool.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.restapi.TaxonomyWebService;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 5 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Docpool")
@Component
@Path("docpool")
public class DocumentPoolModuleWebService {
	
	/**
	 * Return the configuration of the taxonomy module.
	 * 
	 * @response.representation.200.qname {http://www.example.com}taxonomyModuleConfigurationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The configuration of the taxonomy module
	 * @response.representation.200.example {@link org.olat.modules.docpool.restapi.Examples#SAMPLE_DOCUMENTPOOLMODULECONFIGURATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest  The HTTP request
	 * @return The module configuration
	 */
	@GET
	@Path("module/configuration")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getModuleConfiguration(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isSystemAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		DocumentPoolModule taxonomyModule = CoreSpringFactory.getImpl(DocumentPoolModule.class);
		DocumentPoolModuleConfigurationVO configVO = new DocumentPoolModuleConfigurationVO();
		configVO.setEnabled(taxonomyModule.isEnabled());
		configVO.setTaxonomyTreeKey(taxonomyModule.getTaxonomyTreeKey());
		return Response.ok(configVO).build();
	}
	
	@Path("{taxonomyKey}")
	public TaxonomyWebService getTaxonomyWebService(@PathParam("taxonomyKey") Long taxonomyKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isSystemAdmin()) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		if(taxonomyKey == null || taxonomyKey.longValue() <= 0) {
			throw new WebApplicationException(Response.serverError().status(Status.BAD_REQUEST).build());
		}
		
		TaxonomyService taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
		Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(taxonomyKey));
		if(taxonomy == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		return new TaxonomyWebService(taxonomy);
	}
}
