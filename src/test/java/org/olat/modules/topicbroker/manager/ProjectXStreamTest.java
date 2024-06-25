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
package org.olat.modules.topicbroker.manager;

import org.junit.Test;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBBrokerImpl;
import org.olat.modules.topicbroker.model.TBTopicImpl;

/**
 * 
 * Initial date: 3 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.co
 *
 */
public class ProjectXStreamTest {

	@Test
	public void shouldReadWriteTopic() {
		TBBroker broker = new TBBrokerImpl();
		TBTopicImpl topic = new TBTopicImpl();
		topic.setBroker(broker);
		
		String xml = TopicBrokerXStream.toXml(topic);
		
		TopicBrokerXStream.fromXml(xml, TBTopic.class);
	}


}
