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

package org.olat.core.util.i18n.ui;

import org.olat.core.gui.control.Event;
import org.olat.core.util.i18n.I18nItem;

/**
 * <h3>Description:</h3> This event is fired when a I18nItem changes. It is used
 * in the translation tool to communicate between the controllers
 * <p>
 * Initial Date: 10.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class I18nItemChangedEvent extends Event {

	private static final long serialVersionUID = 8044524802341281294L;
	public static final String COMMAND = "I18nItemChangedEvent";
	private I18nItem changedItem;

	/**
	 * Constructor
	 * 
	 * @param changedItem The changed I18nItem
	 */
	I18nItemChangedEvent(I18nItem changedItem) {
		super(COMMAND);
		this.changedItem = changedItem;
	}

	/**
	 * @return The changed I18nItem
	 */
	I18nItem getChangedItem() {
		return changedItem;
	}

}
