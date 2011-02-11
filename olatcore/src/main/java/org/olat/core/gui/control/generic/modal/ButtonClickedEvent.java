package org.olat.core.gui.control.generic.modal;
/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/ 

import org.olat.core.gui.control.Event;

/**
 * <h3>Description:</h3>
 * This event holds position reference to the clicked button.
 * <p>
 * Use the DialogBoxUIFactory to check for this events, e.g. if it was a
 * yes-button event
 * <p>
 * Initial Date: 26.11.2007<br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class ButtonClickedEvent extends Event {
	int position;
	
	/**
	 * Constructor
	 * @param position The position of the button that has been clicked.
	 */
	ButtonClickedEvent(int position) {
		super("button.clicked.event");
		this.position = position;
	}

	/**
	 * @return the position of the clicked button
	 */
	public int getPosition() {
		return position;
	}
	
}
