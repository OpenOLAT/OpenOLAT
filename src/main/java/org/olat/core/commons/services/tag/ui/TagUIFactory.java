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
package org.olat.core.commons.services.tag.ui;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagUIFactory {
	
	public static String getFormattedTags(Locale locale, List<Tag> tags) {
		if (tags == null || tags.isEmpty()) return null;
		
		Collator collator = Collator.getInstance(locale);
		tags.sort((t1, t2) -> collator.compare(t1.getDisplayName(), t2.getDisplayName()));
		StringBuilder sb = new StringBuilder();
		sb.append("<span class=\"o_tag_selection_tags\">");
		for (Tag tag : tags) {
			sb.append("<span class=\"o_tag o_selection_tag\">").append(StringHelper.escapeHtml(tag.getDisplayName())).append("</span>");
		}
		sb.append("</span>");
		return sb.toString();
	}

}
