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
package org.olat.modules.webFeed.manager;

import org.olat.core.gui.translator.Translator;
import org.olat.fileresource.types.PodcastFileResource;
import org.springframework.stereotype.Service;
/**
*
* Initial date: 11.05.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
@Service
public class PodcastNotificationsHandler extends FeedNotificationsHandler {
	
	private static final String NOTIFICATIONS_HEADER_PODCAST = "notifications.header.podcast";
	private static final String CSS_CLASS_ICON_PODCAST = "o_podcast_icon";

	@Override
	protected String getCssClassIcon() {
		return CSS_CLASS_ICON_PODCAST;
	}

	@Override
	protected String getHeader(Translator translator, String displayName) {
		return translator.translate(NOTIFICATIONS_HEADER_PODCAST, new String[]{ displayName });
	}

	@Override
	public String getType() {
		return PodcastFileResource.TYPE_NAME;// FileResource.PODCAST
	}
}