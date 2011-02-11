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

import org.olat.core.util.notifications.SubscriptionInfo;

/**
 * Description:<br>
 * a part of a List-item
 * 
 * <P>
 * Initial Date:  02.12.2009 <br>
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public abstract class SubscriptionInfoItem {

	
	protected String content;
	protected String iconCssClass;
	
	public SubscriptionInfoItem(String content, String iconCssClass){
		this.content = content;
		this.iconCssClass = iconCssClass;
	}
	
	public String getInfoContent(String mimeType) {
		if (mimeType.endsWith(SubscriptionInfo.MIME_HTML)) {
			return getHTMLContent();
		} else {
			return getPlaintextContent();
		}
	}
	
	abstract String getHTMLContent();
	
	private String getPlaintextContent(){
		return content;			
	}
	
}
