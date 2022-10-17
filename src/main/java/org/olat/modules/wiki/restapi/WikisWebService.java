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
package org.olat.modules.wiki.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.wiki.restapi.vo.WikiVO;
import org.olat.modules.wiki.restapi.vo.WikiVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.ACService;
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
 * The Wikis Webservice.<br />
 * OO-112
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
@Tag(name = "Repo")
@Path("repo/wikis")
	
@Component
public class WikisWebService {
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private ACService acService;

	/**
	 * export a specific wiki
	 * 
	 * @param wikiKey
	 * @return
	 */
	@Path("{wikiKey}")
	@Operation(summary = "export a specific wiki", description = "export a specific wiki")
	
	public WikiWebService getWiki() {
		WikiWebService wikiWebservice = new WikiWebService();
		CoreSpringFactory.autowireObject(wikiWebservice);
		return wikiWebservice;
	}

	/**
	 * get list of repo-entry wikis. Group-Wikis are not listed!
	 * 
	 * @param start
	 * @param limit
	 * @param httpRequest
	 * @return
	 */
	@GET
	@Operation(summary = "get list of repo-entry wikis", description = "get list of repo-entry wikis. Group-Wikis are not listed!")
	@ApiResponse(responseCode = "200", description = "Retrieve all the wikis", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = WikiVOes.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = WikiVOes.class))) })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getWikis(@Context HttpServletRequest httpRequest) {
		List<RepositoryEntry> res = getAccessibleWikiRepositoryEntries(httpRequest);
		WikiVO[] wikiVoArr = toWikiVOArray(res);
		WikiVOes voes = new WikiVOes();
		voes.setWikis(wikiVoArr);
		return Response.ok(voes).build();
	}
	
	/**
	 * returns all accessiblewikiRepositoryEntries of type wiki
	 * 
	 * @param httpRequest
	 * @return
	 */
	private List<RepositoryEntry> getAccessibleWikiRepositoryEntries(HttpServletRequest httpRequest){
		Roles roles = getRoles(httpRequest);
		Identity identity = getIdentity(httpRequest);
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles, WikiResource.TYPE_NAME);
		params.setOfferOrganisations(acService.getOfferOrganisations(identity));
		params.setOfferValidAt(new Date());
		return repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
	}

	private WikiVO[] toWikiVOArray(List<RepositoryEntry> entries) {
		int i = 0;
		WikiVO[] wikiVOs = new WikiVO[entries.size()];
		for (RepositoryEntry entry : entries) {
			wikiVOs[i++] = wikivoFromRepoEntry(entry);
		}
		return wikiVOs;
	}

	private WikiVO wikivoFromRepoEntry(RepositoryEntry entry) {
		WikiVO wiki = new WikiVO();
		wiki.setTitle(entry.getDisplayname());
		wiki.setKey(entry.getResourceableId());
		wiki.setSoftkey(entry.getSoftkey());
		return wiki;
	}
}