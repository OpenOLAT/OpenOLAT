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
package org.olat.course.nodes.gta.ui.events;

import org.olat.core.id.Identity;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial date: 31.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskMultiUserEvent extends MultiUserEvent {

	private static final long serialVersionUID = 3633136669745883215L;
	
	public static final String SUMBIT_TASK = "submit-task";
	public static final String SUBMIT_REVISION = "submit-revision";
	
	private final Long forIdentityKey;
	private final Long forGroupKey;
	private final Long emitterKey;
	
	public TaskMultiUserEvent(String cmd, Identity forIdentity, BusinessGroup forGroup, Identity emitter) {
		super(cmd);
		this.forGroupKey = forGroup == null ? null : forGroup.getKey();
		this.forIdentityKey = forIdentity == null ? null : forIdentity.getKey();
		this.emitterKey = emitter == null ? null : emitter.getKey();
	}

	public Long getForGroupKey() {
		return forGroupKey;
	}

	public Long getForIdentityKey() {
		return forIdentityKey;
	}

	public Long getEmitterKey() {
		return emitterKey;
	}
}
