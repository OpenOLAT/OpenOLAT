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
package org.olat.core.commons.services.notifications.model;

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
	 * @see org.olat.core.commons.services.notifications.model.SubscriptionInfoItem#getHTMLContent()
	 */
	@Override
	String getHTMLContent() {
		if (iconCssClass == null) {
			return "<h4>" + content + "</h4>";
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("<h4><i class=\"o_icon o_icon-fw ");
			sb.append(iconCssClass);
			sb.append("\"> </i>"); // space required by antisamy
			sb.append(content);
			sb.append("</h4>");
			return sb.toString();
		}
	}

}
