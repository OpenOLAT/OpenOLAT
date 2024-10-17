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
package org.olat.modules.curriculum.ui.event;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.ContextEntry;

/**
 * 
 * Initial date: 17 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ActivateEvent extends Event {
	
	private static final long serialVersionUID = 3623617142191135078L;

	private static final String ACTIVATE_EVENT = "activate-event";
	
	private final List<ContextEntry> entries;
	
	public ActivateEvent(List<ContextEntry> entries) {
		super(ACTIVATE_EVENT);
		this.entries = entries;
	}
	
	public List<ContextEntry> getEntries() {
		return entries;
	}
}
