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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.fileresource.types;

import java.io.File;

import org.olat.core.commons.controllers.linkchooser.SuffixFilter;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;

/**
 * Description:<br>
 * TODO: guido Class Description for WikiResource
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 */
public class WikiResource extends FileResource {

	public static final String TYPE_NAME = "FileResource.WIKI";

	/**
	 * Standard constructor.
	 */
	public WikiResource() {
		super.setTypeName(TYPE_NAME);
	}

	public static boolean validate(File directory) {
		if (directory != null) {
			String dirName = directory.getName().toLowerCase();
			if (dirName.endsWith(".zip")) return false; // direct the import to the
																									// unzip step first
			// check for at least the index page file and the corresponding property
			// file
			String[] suffixes = { WikiManager.WIKI_FILE_SUFFIX, WikiManager.WIKI_PROPERTIES_SUFFIX };
			File[] files = directory.listFiles(new SuffixFilter(suffixes));
			if (files == null) {
				return false;
			}
			boolean indexAvailable = false;
			boolean indexPropAvailable = false;
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.getName().equals(WikiManager.generatePageId(WikiPage.WIKI_INDEX_PAGE) + "." + WikiManager.WIKI_FILE_SUFFIX)) indexAvailable = true;
				if (file.getName().equals(WikiManager.generatePageId(WikiPage.WIKI_INDEX_PAGE) + "." + WikiManager.WIKI_PROPERTIES_SUFFIX)) indexPropAvailable = true;
			}
			if (indexAvailable && indexPropAvailable) return true;
		}
		return false;
	}

}
