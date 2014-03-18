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

package org.olat.core.configuration;

import java.util.Properties;

import org.olat.core.util.event.MultiUserEvent;

/**
 * <h3>Description:</h3>
 * <p>
 * This event is fired when a PersitedProperties changesIt is mainly used to
 * synchronize cluster nodes.
 * <p>
 * Initial Date: 25.11.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class PersistedPropertiesChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 4165275870616317484L;
	private static final String COMMAND = "PersistedPropertiesChangedEvent";
	private final Properties changedProperties;

	/**
	 * Constructor
	 * 
	 * @param configuredProperties
	 */
	PersistedPropertiesChangedEvent(Properties configuredProperties) {
		super(COMMAND);
		this.changedProperties = configuredProperties;
	}

	/**
	 * @return The modified properties
	 */
	public Properties getChangedProperties() {
		return changedProperties;
	}

}
