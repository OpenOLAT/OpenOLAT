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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:<br>
 * delivers a js-file with all Terms as Arrays
 * 
 * <P>
 * Initial Date: 12.03.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class GlossaryTermMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(GlossaryTermMapper.class);

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		GlossaryItemManager gIM = CoreSpringFactory.getImpl(GlossaryItemManager.class);
		// security checks are done by MapperRegistry
		String[] parts = relPath.split("/");
		String glossaryId = parts[1];

		String glossaryFolderString = FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome() + "/" + glossaryId + "/"
				+ GlossaryMarkupItemController.INTERNAL_FOLDER_NAME;
		File glossaryFolderFile = new File(glossaryFolderString);
		if (!glossaryFolderFile.isDirectory()) {
			log.warn("GlossaryTerms delivery failed; path to glossaryFolder not existing: " + relPath);
			return new NotFoundMediaResource();
		}
		VFSContainer glossaryFolder = new LocalFolderImpl(glossaryFolderFile);
		if (!gIM.isFolderContainingGlossary(glossaryFolder)) {
			log.warn("GlossaryTerms delivery failed; glossaryFolder doesn't contain a valid Glossary: " + glossaryFolder);
			return new NotFoundMediaResource();
		}

		// Create a media resource
		StringMediaResource resource = new StringMediaResource() {
			@Override
			public void prepare(HttpServletResponse hres) {
			// don't use normal string media headers which prevent caching,
			// use standard browser caching based on last modified timestamp
			}
		};

		resource.setLastModified(gIM.getGlossaryLastModifiedTime(glossaryFolder));
		resource.setContentType("text/javascript");
		// Get data
		String glossaryArrayData = TextMarkerJsGenerator.loadGlossaryItemListAsJSArray(glossaryFolder);

		resource.setData(glossaryArrayData);
		// UTF-8 encoding used in this js file since explicitly set in the ajax
		// call (usually js files are 8859-1)
		resource.setEncoding("utf-8");
		return resource;
	}

}
