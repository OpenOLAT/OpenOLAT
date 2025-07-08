/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, httpss://www.frentix.com
 *
 */
@Service
public class TopicBrokerModule extends AbstractSpringModule {
	
	private static final String KEY_MAX_DURATION_MILLIS = "process.max.duration.millis";
	
	@Value("${topic.broker.process.max.duration.millis:5000}")
	private int processMaxDurationMillis;
	
	@Autowired
	public TopicBrokerModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String processMaxDurationMillisObj = getStringPropertyValue(KEY_MAX_DURATION_MILLIS, true);
		if (StringHelper.isLong(processMaxDurationMillisObj)) {
			processMaxDurationMillis = Integer.valueOf(processMaxDurationMillisObj);
		}
	}
	
	public int getProcessMaxDurationMillis() {
		return processMaxDurationMillis;
	}

	public void setProcessMaxDurationMillis(int processMaxDurationMillis) {
		this.processMaxDurationMillis = processMaxDurationMillis;
		setStringProperty(KEY_MAX_DURATION_MILLIS, String.valueOf(processMaxDurationMillis), true);
	}
	
}
