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
package org.olat.modules.curriculum.ui.component;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 20 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PreviousEvent extends Event {

	private static final long serialVersionUID = -7672843364001117384L;

	public static final String PREVIOUS_EVENT = "previous";

	private final Identity identity;
	
	public PreviousEvent(Identity identity) {
		super(PREVIOUS_EVENT);
		this.identity = identity;
	}

	public Identity getIdentity() {
		return identity;
	}
}
