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
package org.olat.core.commons.services.csp.ui.event;

import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NextEntryEvent extends Event {

	private static final long serialVersionUID = 2593090663934151479L;

	public static final String NEXT_EVENT = "next-log-entry";
	
	private final CSPLog entry;
	
	public NextEntryEvent(CSPLog entry) {
		super(NEXT_EVENT);
		this.entry = entry;
	}

	public CSPLog getEntry() {
		return entry;
	}
}
