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
* Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
* <p>
*/
package org.olat.core.util.notifications.items;

/**
 * Description:<br>
 * TitleItem 
 * 
 * <P>
 * Initial Date:  02.12.2009 <br>
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class TitleItem extends SubscriptionInfoItem {

	public TitleItem(String content, String iconCssClass) {
		super(content, iconCssClass);
	}

	/**
	 * @see org.olat.core.util.notifications.items.SubscriptionInfoItem#getHTMLContent()
	 */
	@Override
	String getHTMLContent() {
		if (iconCssClass == null) {
			return "<h4>" + content + "</h4>";
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("<h4 class=\"b_with_small_icon_left ");
			sb.append(iconCssClass);
			sb.append("\">");
			sb.append(content);
			sb.append("</h4>");
			return sb.toString();
		}
	}

}
