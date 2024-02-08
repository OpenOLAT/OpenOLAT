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
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.glossary.GlossaryItem;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
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
		GlossaryItemManager glossaryManager = CoreSpringFactory.getImpl(GlossaryItemManager.class);
		// security checks are done by MapperRegistry
		String[] parts = relPath.split("/");
		String glossaryId = parts[1];

		String glossaryFolderString = FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome() + "/" + glossaryId + "/"
				+ GlossaryMarkupItemController.INTERNAL_FOLDER_NAME;
		File glossaryFolderFile = new File(glossaryFolderString);
		if (!glossaryFolderFile.isDirectory()) {
			log.warn("GlossaryTerms delivery failed; path to glossaryFolder not existing: {}", relPath);
			return new NotFoundMediaResource();
		}
		VFSContainer glossaryFolder = new LocalFolderImpl(glossaryFolderFile);
		if (!glossaryManager.isFolderContainingGlossary(glossaryFolder)) {
			log.warn("GlossaryTerms delivery failed; glossaryFolder doesn't contain a valid Glossary: {}", glossaryFolder);
			return new NotFoundMediaResource();
		}
		
		List<GlossaryItem> glossaryItemArr = glossaryManager.getGlossaryItemListByVFSItem(glossaryFolder);
		JSONArray jsonObject = toJSON(glossaryItemArr);
		return new JSONMediaResource(jsonObject, "utf-8");
	}
	
	private JSONArray toJSON(List<GlossaryItem> glossaryItemsList) {
		JSONArray items = new JSONArray();
		for (GlossaryItem glossaryItem:glossaryItemsList) {
			List<String> allStrings = glossaryItem.getAllStringsToMarkup();
			JSONArray item = new JSONArray();
			for (String termFlexionSynonym: allStrings) {
				item.put(termFlexionSynonym);
			}
			items.put(item);
		}
		return items;
	}
}
