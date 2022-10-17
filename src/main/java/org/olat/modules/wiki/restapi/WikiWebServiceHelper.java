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

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiToCPResource;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * Exports a given wikiResource (specified by it's Repository-Entry OR
 * LearningGroup) to a CP and serves the File
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class WikiWebServiceHelper {

	/**
	 * exports the wiki-Resource and serves the zip file. The given
	 * OLATResourceable can be the repository-entry of the wiki or the
	 * businessGroup (if it is a group-wiki)
	 * 
	 * @param wikiResource
	 * @param request
	 * @param response
	 * @return
	 */
	public static Response serve(OLATResourceable wikiResource, HttpServletRequest request, HttpServletResponse response) {
		Identity ident = RestSecurityHelper.getIdentity(request);
		Translator translator = Util.createPackageTranslator(WikiMainController.class, new Locale(ident.getUser().getPreferences().getLanguage()));

		// make mediaResource
		MediaResource cpMediaResource = new WikiToCPResource(wikiResource, ident, translator);
		// use servletUtil, so file gets deleted afterwards
		ServletUtil.serveResource(request, response, cpMediaResource);
		return Response.ok().build();
	}
}
