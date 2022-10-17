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

package org.olat.core.commons.services.notifications.restapi.vo;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.commons.services.notifications.SubscriptionInfo;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 oct. 2011 <br>
 *
 * @author srosse, stephane.rosseªfrentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "subscriptionInfoVO")
public class SubscriptionInfoVO {
	private Long key;
	private String type;
	private String title;
	
	@XmlElementWrapper(name="items")
	@XmlElement(name="item")
	private List<SubscriptionListItemVO> items = new ArrayList<>();
	
	public SubscriptionInfoVO() {
		//make JAXB happy
	}
	
	public SubscriptionInfoVO(SubscriptionInfo info) {
		key = info.getKey();
		type = info.getType();
		title = info.getTitle(SubscriptionInfo.MIME_PLAIN);
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<SubscriptionListItemVO> getItems() {
		return items;
	}

	public void setItems(List<SubscriptionListItemVO> items) {
		this.items = items;
	}
}