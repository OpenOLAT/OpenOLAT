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

package org.olat.modules.wiki.gui.components.wikiToHtml;

import org.jamwiki.DataHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.utils.NamespaceHandler;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.wiki.WikiManager;

/**
 * Description: Implementation of the jamwiki datahander for static export
 * purpose. The wiki gets exported to a static html export which can be viewed
 * offline.
 * 
 * @author guido
 */
public class StaticExportWikiDataHandler implements DataHandler {

	private OLATResourceable ores;
	
	public void setWiki(OLATResourceable ores) {
		this.ores = ores;
	}
	
	@Override
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception {
		Topic topic = new Topic();
		if (topicName.startsWith("Image:")) {
			String imageName = topicName.substring(NamespaceHandler.NAMESPACE_IMAGE.length() + 1);
			topic.setName(imageName);
			topic.setTopicType(Topic.TYPE_IMAGE);
			return topic;
		}
			topic.setName(topicName);
			return topic;
	}

	@Override
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		WikiFile wikifile = new WikiFile();
		if (topicName.startsWith("Image:")) {
			topicName = topicName.substring(NamespaceHandler.NAMESPACE_IMAGE.length() + 1);
		} else if (topicName.startsWith("Media:")) {
			topicName = topicName.substring(6, topicName.length());
		}
		topicName = topicName.replace(" ", "_"); //topic name comes in with "_" replaced as normal but it the image case it does not make sense
		wikifile.setFileName(topicName);
		wikifile.setUrl(topicName);
		//grabbing images for resizing
		wikifile.setAbsUrl(WikiManager.getInstance().getMediaFolder(ores).getBasefile().getAbsolutePath());
		return wikifile;
	}

	

	public boolean exists(String virtualWiki, String topic) {
		return true;
	}


}
