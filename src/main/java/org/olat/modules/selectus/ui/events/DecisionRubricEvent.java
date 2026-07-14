/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;

import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 27 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricEvent extends MultiUserEvent {

	private static final long serialVersionUID = 6833772049693307570L;
	public static final String RUBRIC_CHANGED = "rubric-changed";
	
	private Long identitySenderKey;
	private Long rubricKey;
	
	public DecisionRubricEvent(DecisionRubric rubric, Long identitySenderKey) {
		super(RUBRIC_CHANGED);
		this.rubricKey = rubric.getKey();
		this.identitySenderKey = identitySenderKey;
	}

	public Long getIdentitySenderKey() {
		return identitySenderKey;
	}
	
	public Long getRubricKey() {
		return rubricKey;
	}
}
