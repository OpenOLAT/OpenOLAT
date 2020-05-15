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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.logging.log4j.Logger;
import org.jamwiki.DataHandler;
import org.jamwiki.WikiMediaDimension;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.utils.InterWikiHandler;
import org.jamwiki.utils.PseudoTopicHandler;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.springframework.util.StringUtils;

/**
 * Implementation of the Datahandler Interface from the jamwiki engine. It
 * provides methods of checking whether a wiki topic exists and for lookup of
 * files like images used in a wiki topic.
 * 
 * @author guido
 */
public class OlatWikiDataHandler implements DataHandler {
	
	private static final Logger log = Tracing.createLoggerFor(OlatWikiDataHandler.class);

	private OLATResourceable ores;
	private String imageUri;
	private static final String IMAGE_NAMESPACE = "Image:";
	private static final String MEDIA_NAMESPACE = "Media:";

	/**
	 * @param ores
	 * @param imageUri
	 */
	protected OlatWikiDataHandler(OLATResourceable ores, String imageUri) {
		this.ores = ores;
		this.imageUri = imageUri;
	}

	@Override
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception {
		String decodedName = null;

		Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
		try {
			decodedName = URLDecoder.decode(topicName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//
		}
		if (log.isDebugEnabled()) {
			log.debug("page name not normalized: {}", topicName);
			log.debug("page name normalized: {}", FilterUtil.normalizeWikiLink(topicName));
			try {
				log.debug("page name urldecoded name: {}", URLDecoder.decode(topicName, "utf-8"));
				log.debug("page name urldecoded and normalized: {}", FilterUtil.normalizeWikiLink(URLDecoder.decode(topicName, "utf-8")));
				log.debug("page name urldecoded normalized and transformed to id: {}", wiki.generatePageId(FilterUtil.normalizeWikiLink(decodedName)));
			} catch (UnsupportedEncodingException e) {
				//
			}
		}
		if(decodedName == null) {
			return null;
		}

		Topic topic = new Topic();
		if (decodedName.startsWith(IMAGE_NAMESPACE)) {
			String imageName = topicName.substring(IMAGE_NAMESPACE.length());
			if (!wiki.mediaFileExists(imageName)) return null;
			topic.setName(imageName);
			topic.setTopicType(Topic.TYPE_IMAGE);
			return topic;
		} else if (decodedName.startsWith(MEDIA_NAMESPACE)) {
			String mediaName = topicName.substring(MEDIA_NAMESPACE.length(), topicName.length());
			if (!wiki.mediaFileExists(mediaName)) {
				return null;
			}
			topic.setName(mediaName);
			
			String type = mediaName.toLowerCase();
			if(type.endsWith(".mp4") || type.endsWith(".m4v") || type.endsWith(".mov")) {
				topic.setTopicType(Topic.TYPE_VIDEO);
			} else if(type.endsWith(".mp3") || type.endsWith(".aac") || type.endsWith(".m4a")) {
				topic.setTopicType(Topic.TYPE_AUDIO);
			} else {
				topic.setTopicType(Topic.TYPE_FILE);
			}
			return topic;
		}
		if (wiki.pageExists(wiki.generatePageId(FilterUtil.normalizeWikiLink(decodedName)))) {
			topic.setName(topicName);
			return topic;
		}
		return null;
	}

	@Override
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		WikiFile wikifile = new WikiFile();
		if (topicName.startsWith(IMAGE_NAMESPACE)) {
			topicName = topicName.substring(IMAGE_NAMESPACE.length());
		} else if (topicName.startsWith(MEDIA_NAMESPACE)) {
			topicName = topicName.substring(MEDIA_NAMESPACE.length(), topicName.length());
		}
		topicName = topicName.replace(" ", "_"); //topic name comes in with "_" replaced as normal but it the image case it does not make sense
		wikifile.setFileName(topicName);
		wikifile.setUrl(this.imageUri + topicName);
		wikifile.setAbsUrl(WikiManager.getInstance().getMediaFolder(ores).getBasefile().getAbsolutePath());
		return wikifile;
	}

	@Override
	public boolean exists(String virtualWiki, String topic) {
		if (!StringUtils.hasText(topic)) {
			return false;
		}
		if (PseudoTopicHandler.isPseudoTopic(topic)) {
			return true;
		}

		try {
			if (InterWikiHandler.isInterWiki(topic)) {
				return true;
			}
		} catch (Exception | Error e) {
			log.warn("Cannot initialize InterWikiHandler", e);
		}

		Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
		if (topic.startsWith(IMAGE_NAMESPACE) || topic.startsWith(MEDIA_NAMESPACE)) {
			return wiki.pageExists(topic);
		}
		String pageId = WikiManager.generatePageId(FilterUtil.normalizeWikiLink(topic));
		return wiki.pageExists(pageId);
	}

	@Override
	public WikiMediaDimension getImageDimension(File file) {
		Size size = CoreSpringFactory.getImpl(ImageService.class).getSize(file, null);
		if(size == null) {
			return null;
		}
		return new WikiMediaDimension(size.getWidth(), size.getHeight());
	}

	@Override
	public WikiMediaDimension getVideoDimension(File file) {
		VFSLeaf leaf = new LocalFileImpl(file);
		Size size = CoreSpringFactory.getImpl(MovieService.class).getSize(leaf, "mp4");
		if(size == null) {
			return null;
		}
		return new WikiMediaDimension(size.getWidth(), size.getHeight());
	}
}
