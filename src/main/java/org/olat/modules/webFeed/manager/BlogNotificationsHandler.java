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
import org.olat.fileresource.types.BlogFileResource;
import org.springframework.stereotype.Service;
/**
*
* Initial date: 27.04.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
@Service
public class BlogNotificationsHandler extends FeedNotificationsHandler {
	
	private static final String NOTIFICATIONS_HEADER_BLOG = "notifications.header.blog";
	private static final String CSS_CLASS_ICON_BLOG = "o_blog_icon";
	

	@Override
	protected String getCssClassIcon() {
		return CSS_CLASS_ICON_BLOG;
	}

	@Override
	protected String getHeader(Translator translator, String title) {
		return translator.translate(NOTIFICATIONS_HEADER_BLOG,  new String[]{ title });
	}

	@Override
	public String getType() {
		return BlogFileResource.TYPE_NAME;// FileResource.BLOG
	}
}
