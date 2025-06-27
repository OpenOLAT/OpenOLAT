/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.webFeed.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class PodcastCommentNotificationsHandler extends FeedCommentNotificationsHandler {

	public static final String TYPE = "Comment.PODCAST";
	public static final String CSS_CLASS_ICON_BLOG = "o_podcast_icon";
	public static final String NOTIFICATIONS_HEADER_COMMENT_BLOG = "notifications.header.comment.podcast";
	
	@Autowired
	private FeedManager feedManager;
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getIconCss() {
		return CSSHelper.getIconCssClassFor(getCssClassIcon());
	}
	
	@Override
	protected String getCssClassIcon() {
		return CSS_CLASS_ICON_BLOG;
	}

	@Override
	public String getDisplayName(Publisher publisher) {
		return RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(publisher.getResId());
	}

	@Override
	public String getAdditionalDescriptionI18nKey(Locale locale) {
		return null;
	}

	@Override
	protected String getHeader(Translator translator, String title) {
		return translator.translate(NOTIFICATIONS_HEADER_COMMENT_BLOG,  new String[]{ title });
	}

	@Override
	protected List<Item> getItems(Publisher p) {
		OLATResourceable feedOres = OresHelper.createOLATResourceableInstance(PodcastFileResource.TYPE_NAME, Long.valueOf(p.getData()));
		Feed feed = feedManager.loadFeed(feedOres);
		if(feed == null) {
			return List.of();
		}
		return feedManager.loadItems(feed);
	}
}
