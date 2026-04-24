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
package org.olat.modules.webFeed.ui;

import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.webFeed.Item;

/**
 * 
 * Initial date: 23 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FeedHelper {
	
	private FeedHelper() {
		//
	}
	
	public static String getTitle(String entryTitle, Item item) {
		if(entryTitle == null) {
			String content = FilterFactory.getHtmlTagsFilter().filter(item.getContent());
			if(StringHelper.containsNonWhitespace(content)) {
				entryTitle = Formatter.truncate(content, 50, "\u2026");
			} else {
				String description = FilterFactory.getHtmlTagsFilter().filter(item.getDescription());
				if(StringHelper.containsNonWhitespace(description)) {
					entryTitle = Formatter.truncate(description, 50, "\u2026");
				}
			}
		}
		if(entryTitle == null) {
			entryTitle = "";
		}
		return entryTitle;
	}

}
