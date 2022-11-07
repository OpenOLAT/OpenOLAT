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
package org.olat.core.gui.control.generic.textmarker;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.glossary.GlossaryItem;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
class GlossaryDefinitionMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(GlossaryDefinitionMapper.class);
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		GlossaryItemManager gIM = CoreSpringFactory.getImpl(GlossaryItemManager.class);

		String[] parts = relPath.split("/");
		String glossaryId = parts[1];
		String glossaryFolderString = FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome() + "/" + glossaryId + "/"
				+ GlossaryMarkupItemController.INTERNAL_FOLDER_NAME;
		File glossaryFolderFile = new File(glossaryFolderString);
		if (!glossaryFolderFile.isDirectory()) {
			log.warn("GlossaryDefinition delivery failed; path to glossaryFolder not existing: " + relPath);
			return new NotFoundMediaResource();
		}
		VFSContainer glossaryFolder = new LocalFolderImpl(glossaryFolderFile);
		if (!gIM.isFolderContainingGlossary(glossaryFolder)) {
			log.warn("GlossaryDefinition delivery failed; glossaryFolder doesn't contain a valid Glossary: " + glossaryFolder);
			return new NotFoundMediaResource();
		}

		String glossaryMainTerm = parts[2];
		if(parts.length > 2) {//this handle / or \ in a term
			for(int i=3; i<parts.length; i++) {
				glossaryMainTerm += "/" + parts[i];
			}
		}
		//cut away ".html" if necessary
		if(glossaryMainTerm.endsWith(".html")) {
			glossaryMainTerm = glossaryMainTerm.substring(0, glossaryMainTerm.length() - 5);
		}
		glossaryMainTerm = glossaryMainTerm.toLowerCase();
		
		Set<String> alternatives = new HashSet<>();
		prepareAlternatives(glossaryMainTerm, alternatives);

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

		List<GlossaryItem> glossItems = gIM.getGlossaryItemListByVFSItem(glossaryFolder);
		GlossaryItem foundItem = null;
		for (GlossaryItem glossaryItem : glossItems) {
			String item = glossaryItem.getGlossTerm().toLowerCase();
			if (alternatives.contains(item)) {
				foundItem = glossaryItem;
				break;
			}
		}
		if (foundItem == null) {
			return new NotFoundMediaResource();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<dd><dt>").append(foundItem.getGlossTerm()).append("</dt><dl>")
		  .append(foundItem.getGlossDef()).append("</dl></dd>");
		String filteredHtml = StringHelper.xssScan(sb);
		resource.setData(filteredHtml);
		resource.setEncoding("utf-8");

		if (log.isDebugEnabled()) log.debug("loaded definition for " + glossaryMainTerm);
		return resource;
	}
	
	private void prepareAlternatives(String term, Set<String> alternatives) {
		alternatives.add(term);
		if(term.indexOf('+') >= 0) {
			String alt = term.replace('+',' ');
			prepareAlternatives(alt, alternatives);
		}
		if(term.indexOf('/') >= 0) {
			String alt = term.replace('/', '\\');
			prepareAlternatives(alt, alternatives);
		}
	}
}
