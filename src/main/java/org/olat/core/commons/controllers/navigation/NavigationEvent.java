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
package org.olat.core.commons.controllers.navigation;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * This Event is fired when a link in the navigation controller is clicked. The
 * corresponding selected items are passed to the listening controller by this
 * event.
 * <P>
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author gwassmann
 */
public class NavigationEvent extends Event {

	private static final long serialVersionUID = -1854984960347993034L;
	private List<? extends Dated> selectedItems;

	/**
	 * @param command
	 */
	public NavigationEvent(List<? extends Dated> selectedItems) {
		super("items_selected");
		this.selectedItems = selectedItems;
	}

	/**
	 * @return The selected items 
	 */
	public List<? extends Dated> getSelectedItems() {
		return selectedItems;
	}
}
