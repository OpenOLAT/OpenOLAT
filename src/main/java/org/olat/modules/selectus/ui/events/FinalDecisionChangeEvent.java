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

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FinalDecisionChangeEvent extends MultiUserEvent  {

	private static final long serialVersionUID = 9208385756101684581L;
	public static final String FINAL_DECISION = "final-decision";
	
	private final Long applicationKey;
	private final int decision;
	private final Long emitterKey;
	
	public FinalDecisionChangeEvent(Long applicationKey, int decision, Long emitterKey) {
		super(FINAL_DECISION);
		this.decision = decision;
		this.applicationKey = applicationKey;
		this.emitterKey = emitterKey;
	}

	public int getDecision() {
		return decision;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Long getEmitterKey() {
		return emitterKey;
	}
}
