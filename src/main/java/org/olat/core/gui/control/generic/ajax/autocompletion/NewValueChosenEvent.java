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
 */
package org.olat.core.gui.control.generic.ajax.autocompletion;

import java.util.Collections;

/**
 * Description:<br>
 * This Event is triggered when a new value is entered into the autocomplete input that is not
 * part of the suggested entries.
 * <p>
 * Initial Date:  31.05.2016 <br>
 *
 * @author Daniel Haag
 */
public class NewValueChosenEvent extends EntriesChosenEvent {

	private static final long serialVersionUID = -7082754083015536137L;

	/**
	 * @param newValue
	 */
	NewValueChosenEvent(String newValue) {
		super(Collections.singletonList(newValue));
	}

}
