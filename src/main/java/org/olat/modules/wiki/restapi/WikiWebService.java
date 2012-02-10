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

import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.commons.modules.bc.vfs.OlatRootFileImpl;
import org.olat.core.gui.media.CleanupAfterDeliveryFileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.fileresource.types.WikiResource;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.modules.fo.restapi.ForumWebService;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiToCPExport;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * The Wiki Webservice<br />
 * OO-112
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class WikiWebService {
	private static final OLog log = Tracing.createLoggerFor(ForumWebService.class);

	/**
	 * will export the specified wiki to a CP and serve as zip-file.<br />
	 * this handles two scenarios:
	 * <ul>
	 * <li>1. wiki is a "normal" wiki from the repository</li>
	 * <li>2. wiki is in a group</li>
	 * </ul>
	 * 
	 * @param wikiKey
	 *            part of the REST path, the resourceable-id / repo-entry-key /
	 *            softkey of the wiki resource. For group-wikis, this must be
	 *            the group-id
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Produces({ "application/zip", MediaType.APPLICATION_OCTET_STREAM })
	public Response exportWiki(@PathParam("wikiKey") String wikiKey, @Context HttpServletRequest request, @Context HttpServletResponse response) {
		if (wikiKey == null)
			return Response.serverError().status(Status.BAD_REQUEST).build();

		// first check, if given key is a businessgroup-id
		BusinessGroup bGroup = null;
		try {
			final BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
			bGroup = bgm.loadBusinessGroup(Long.parseLong(wikiKey), false);
		} catch (NumberFormatException nfe) {
			// do nothing
		}
		if (bGroup != null)
			return serveGroupWiki(bGroup, request, response);

		// it's not a group-wiki, try to get the wiki with the specified key
		RepositoryEntry re = getExportableWikiRepoEntryByAnyKey(wikiKey);
		if (re == null) {
			// means, wiki not found (e.g. key wrong, or not marked as
			// downloadable )
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		return serveRepoEntryWiki(re, request, response);
	}

	/**
	 * 
	 * @param wikiEntry
	 * @param request
	 * @param response
	 * @return
	 */
	private Response serveRepoEntryWiki(RepositoryEntry wikiEntry, HttpServletRequest request, HttpServletResponse response) {
		Identity ident = RestSecurityHelper.getIdentity(request);

		// check if current REST user is allowed to export the wiki resource
		boolean isAuthor = RestSecurityHelper.isAuthor(request);
		boolean isOwner = RepositoryManager.getInstance().isOwnerOfRepositoryEntry(ident, wikiEntry);
		if (!(isAuthor | isOwner)) {
			// current REST user is neither author nor owner -> do not allow
			// export
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		// count download
		RepositoryManager.getInstance().incrementDownloadCounter(wikiEntry);
		return serve(wikiEntry, ident, request, response);
	}

	/**
	 * 
	 * @param group
	 * @param request
	 * @param response
	 * @return
	 */
	private Response serveGroupWiki(BusinessGroup group, HttpServletRequest request, HttpServletResponse response) {
		Identity ident = RestSecurityHelper.getIdentity(request);

		// check if current REST user is in group
		if (!BusinessGroupManagerImpl.getInstance().isIdentityInBusinessGroup(ident, group)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		return serve(group, ident, request, response);
	}

	/**
	 * finally exports the ores and serves the zip file
	 * 
	 * @param ores
	 * @param ident
	 * @param request
	 * @param response
	 * @return
	 */
	private Response serve(OLATResourceable ores, Identity ident, HttpServletRequest request, HttpServletResponse response) {
		// ok, we have the wiki/group, export it.
		Translator translator = Util.createPackageTranslator(WikiMainController.class, new Locale(ident.getUser().getPreferences().getLanguage()));
		WikiToCPExport exportUtil = new WikiToCPExport(ores, ident, translator);
		LocalFileImpl tmpExport = new OlatRootFileImpl("/tmp/" + ident.getKey() + "-" + ores.getResourceableId() + "-restexport.zip", null);
		exportUtil.archiveWikiToCP(tmpExport);

		// export is done, serve the file
		File baseFile = tmpExport.getBasefile();
		if (baseFile.exists() && baseFile.canRead()) {
			// make mediaResource
			MediaResource cpMediaResource = new CleanupAfterDeliveryFileMediaResource(baseFile);
			// use servletUtil, so file gets deleted afterwards
			ServletUtil.serveResource(request, response, cpMediaResource);
			return Response.ok().build();
		} else {
			log.warn("Exported wiki to " + baseFile.getAbsolutePath() + " but now it's not readable for serving to client...");
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	}

	/**
	 * gets an exportable wiki by it's key.<br />
	 * key can be either: resourceable id, softkey or repository-id<br />
	 * 
	 * this method also checks if the repo-Entry is marked as "canDownload".<br />
	 * 
	 * if no wiki with given key is found, or wiki is not marked as
	 * "canDownload", null is returned
	 * 
	 * @param key
	 *            the resourceable id, softkey or repository-id
	 * @return the exportable wiki or null
	 */
	private static RepositoryEntry getExportableWikiRepoEntryByAnyKey(String wikiKey) {
		RepositoryEntry re = null;
		try {
			Long key = Long.parseLong(wikiKey);
			// try repo key
			re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
			if (re != null && re.getCanDownload()) {
				return re;
			}

			// null,try resourceable key
			OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(key, WikiResource.TYPE_NAME);
			if (ores != null) {
				re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
				if (re != null && re.getCanDownload())
					return re;
			}

		} catch (NumberFormatException nfe) {
			// do nothing
		}

		// try softkey
		re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(wikiKey, false);
		if (re != null && re.getCanDownload()) {
			return re;
		}

		return null;
	}

}
