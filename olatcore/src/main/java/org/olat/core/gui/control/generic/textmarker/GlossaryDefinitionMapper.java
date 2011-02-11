/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.textmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.glossary.GlossaryItem;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * delivers definition for a term as HTML ext autoloads content when hovering a
 * highlighted term
 * 
 * <P>
 * Initial Date: 05.02.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class GlossaryDefinitionMapper extends LogDelegator implements Mapper {
	
	/**
	 * @see org.olat.core.dispatcher.mapper.Mapper#handle(java.lang.String,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	public MediaResource handle(String relPath, HttpServletRequest request) {
		GlossaryItemManager gIM = GlossaryItemManager.getInstance();

		String[] parts = relPath.split("/");
		String glossaryId = parts[1];
		String glossaryFolderString = FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome() + "/" + glossaryId + "/"
				+ GlossaryMarkupItemController.INTERNAL_FOLDER_NAME;
		File glossaryFolderFile = new File(glossaryFolderString);
		if (!glossaryFolderFile.isDirectory()) {
			logWarn("GlossaryDefinition delivery failed; path to glossaryFolder not existing: " + relPath, null);
			return new NotFoundMediaResource(relPath);
		}
		VFSContainer glossaryFolder = new LocalFolderImpl(glossaryFolderFile);
		if (!gIM.isFolderContainingGlossary(glossaryFolder)) {
			logWarn("GlossaryDefinition delivery failed; glossaryFolder doesn't contain a valid Glossary: " + glossaryFolder, null);
			return new NotFoundMediaResource(relPath);
		}

		// cut away ".html"
		String glossaryMainTerm = parts[2].substring(0, parts[2].length() - 5).replace("+", " ");

		// Create a media resource
		StringMediaResource resource = new StringMediaResource() {
			@Override
			public void prepare(HttpServletResponse hres) {
			// don't use normal string media headers which prevent caching,
			// use standard browser caching based on last modified timestamp
			}
		};
		resource.setLastModified(gIM.getGlossaryLastModifiedTime(glossaryFolder));
		resource.setContentType("text/html");

		ArrayList<GlossaryItem> glossItems = gIM.getGlossaryItemListByVFSItem(glossaryFolder);
		String description = "<dd><dt>" + glossaryMainTerm + "</dt>";
		// FIXME: have a way not to loop over all items, but get by Term
		boolean foundADescription = false;
		for (Iterator<GlossaryItem> iterator = glossItems.iterator(); iterator.hasNext();) {
			GlossaryItem glossaryItem = iterator.next();
			if (glossaryItem.getGlossTerm().toLowerCase().equals(glossaryMainTerm.toLowerCase())) {
				description += "<dl>" + glossaryItem.getGlossDef() + "</dl>";
				foundADescription = true;
				break;
			}
		}
		description += "</dd>"; 
		if (!foundADescription) return new NotFoundMediaResource(relPath);
		
		resource.setData(description);
		resource.setEncoding("utf-8");

		if (isLogDebugEnabled()) logDebug("loaded definition for " + glossaryMainTerm, null);
		return resource;
	}

}
