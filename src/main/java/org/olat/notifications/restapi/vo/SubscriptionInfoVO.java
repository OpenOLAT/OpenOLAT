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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.notifications.restapi.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "subscriptionInfoVO")
public class SubscriptionInfoVO {
	
	private String title;
	
	@XmlElementWrapper(name="items")
	@XmlElement(name="item")
	private List<SubscriptionListItemVO> items = new ArrayList<SubscriptionListItemVO>();
	
	public SubscriptionInfoVO() {
		//make JAXB happy
	}
	
	public SubscriptionInfoVO(SubscriptionInfo info) {
		title = info.getTitle(SubscriptionInfo.MIME_PLAIN);
		if(info.getSubscriptionListItems() != null) {
			for(SubscriptionListItem item:info.getSubscriptionListItems()) {
				items.add(new SubscriptionListItemVO(item));
			}
		}
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