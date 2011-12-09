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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.wiki;

/**
 * Description:<br>
 * Conjuction between a wiki implementation and the wikiMarkupComponent. The
 * component asks the wikiImpl wheather a page already exists or not to render
 * the link appropriate.
 * 
 * @see org.olat.core.gui.components.wikiToHtml.WikiMarkupComponent for
 *      implementation details of the htmlToWiki component.
 * or
 * @see org.olat.modules.wiki.WikiMainController for implementaition details of the whole wiki GUI and controller logic
 *      <P>
 *      Initial Date: Jun 21, 2006 <br>
 * @author guido
 */
public interface WikiContainer {

	public static final String MEDIA_FOLDER_NAME = "media";

	/**
	 * pass the page name string to the wikiImpl so a lookup can be done
	 * @param string
	 * @return true if page exists
	 */
	boolean pageExists(String string);
	

	/**
	 * @param pageName
	 * @return an encoded base64 name which is save to use as unique filename.
	 */
	public String generatePageId(String pageName);

}
