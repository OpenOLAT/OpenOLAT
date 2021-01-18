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

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.fileresource.types.WikiResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
/**
 * 
 * The Wiki Webservice<br />
 * allows the export of "normal" wikis ( in contrast to group-wikis) OO-112
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class WikiWebService {
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager resourceManager;

	/**
	 * will export the specified wiki (which must be a repo-entry-wiki) to a CP
	 * and serve as zip-file.<br />
	 * 
	 * @param wikiKey
	 *            part of the REST path, the resourceable-id / repo-entry-key /
	 *            softkey of the wiki resource.
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Operation(summary = "will export the specified wiki ", description = "will export the specified wiki (which must be a repo-entry-wiki) to a CP\n" + 
			" and serve as zip-file.<br>")
		
	@Produces({"application/zip", MediaType.APPLICATION_OCTET_STREAM })
	public Response exportWiki(@PathParam("wikiKey") String wikiKey, @Context HttpServletRequest request, @Context HttpServletResponse response) {
		RepositoryEntry wikiEntry = getExportableWikiRepoEntryByAnyKey(wikiKey);
		if(wikiEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if (isAllowedToExportWiki(wikiEntry, request)) {
			repositoryService.incrementDownloadCounter(wikiEntry);
			return WikiWebServiceHelper.serve(wikiEntry.getOlatResource(), request, response);
		}
		return Response.serverError().status(Status.FORBIDDEN).build();
	}

	/**
	 * gets an exportable wiki by it's key.<br />
	 * key can be either: resourceable id, softkey or repository-id<br />
	 * 
	 * this method also checks if the repo-Entry is marked as "canDownload".<br />
	 * 
	 * if no wiki with given key is found, or wiki is not marked as
	 * "canDownload" an exception is thrown
	 * 
	 * @param key
	 *            the resourceable id, softkey or repository-id
	 * @return the exportable wiki
	 */
	private RepositoryEntry getExportableWikiRepoEntryByAnyKey(String wikiKey) {
		// first try softkey
		RepositoryEntry re = repositoryManager.lookupRepositoryEntryBySoftkey(wikiKey, false);
		if (re != null && re.getCanDownload()) {
			return re;
		}

		try {
			Long key = Long.parseLong(wikiKey);
			// try repo key
			re = repositoryManager.lookupRepositoryEntry(key);
			if (re != null && re.getCanDownload()) {
				return re;
			}

			// null,try resourceable key
			OLATResourceable ores = resourceManager.findResourceable(key, WikiResource.TYPE_NAME);
			if (ores != null) {
				re = repositoryManager.lookupRepositoryEntry(ores, false);
				if (re != null && re.getCanDownload()) {
					return re;
				}
			}
		} catch (NumberFormatException nfe) {
			// wikiKey was not a Long number, ignore
		}
		return null;
	}

	/**
	 * check access of current REST user to the given wikiRepoEntry. Current
	 * REST user must be the owner of the repoEntry or an OpenOLAT author
	 * 
	 * @param wikiEntry
	 * @param request
	 * @return
	 */
	private boolean isAllowedToExportWiki(RepositoryEntry re, HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		Roles roles = RestSecurityHelper.getRoles(request);		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles, re);
		boolean canDownload = re.getCanDownload();
		if (reSecurity.isEntryAdmin()) {
			canDownload = true;
		} else if(!reSecurity.isAuthor()) {
			return false;
		}
		return canDownload;
	}
}
