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

public class Examples {

	public static final PublisherVO SAMPLE_PUBLISHERVO =  new PublisherVO();
	
	public static final SubscriptionInfoVO SAMPLE_INFOVO = new SubscriptionInfoVO();
	public static final SubscriptionInfoVOes SAMPLE_INFOVOes = new SubscriptionInfoVOes();

	static {
		SAMPLE_PUBLISHERVO.setResName("BusinessGroup");
		SAMPLE_PUBLISHERVO.setResId(357886347l);
		SAMPLE_PUBLISHERVO.setSubidentifier("toolforum");
		SAMPLE_PUBLISHERVO.setData("3456");
		SAMPLE_PUBLISHERVO.setType("Forum");
		SAMPLE_PUBLISHERVO.setBusinessPath("[BusinessGroup:357886347][toolforum:0]");
		
		SAMPLE_INFOVO.setTitle("Infos");
		SAMPLE_INFOVOes.getSubscriptionInfos().add(SAMPLE_INFOVO);
	}
}
