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
* <p>
*/ 

package org.olat.core.commons.services.notifications;

/**
 * Description:
 * This item contains the title for the resource, as also a top-level link to it.
 * SubsInfo holds all the detail info, if one/multiple news are available.
 *  
 * <P/>
 * Initial Date:  Mar 9, 2005
 *
 * @author Felix Jost 
 */
public class SubscriptionItem {
	private String title;
	private String link;
	private String description;
	private SubscriptionInfo subsInfo = null;
	
	/**
	 * @param title
	 * @param link
	 * @param description
	 */
	public SubscriptionItem(String title, String link, String description) {
		super();
		this.title = title;
		this.link = link;
		this.description = description;
	}
	
	/**
	 * @return the description in html
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return the html-link
	 */
	public String getLink() {
		return link;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param subsInfo The subsInfo to set.
	 */
	public void setSubsInfo(SubscriptionInfo subsInfo) {
		this.subsInfo = subsInfo;
	}

	/**
	 * @return Returns the subsInfo.
	 */
	public SubscriptionInfo getSubsInfo() {
		return subsInfo;
	}

}
