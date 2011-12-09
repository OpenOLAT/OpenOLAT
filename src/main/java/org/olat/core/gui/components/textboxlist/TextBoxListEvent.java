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
package org.olat.core.gui.components.textboxlist;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * Event sent by TextBoxListComponent if changes occured or submitted.
 *  
 * contains a list with all Elements -> getAllItems() 
 * another list keeps all added records (which were added manually, means not from autocompletion list) -> getNewOnly()
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListEvent extends Event {

	private static final String TEXTBOXLISTCHANGE = "textboxlistchange";
	private List<String> allItems;
	private List<String> newOnly;

	public TextBoxListEvent(List<String> allItems, List<String> newOnly) {
		super(TEXTBOXLISTCHANGE);
		this.allItems = allItems;
		this.newOnly = newOnly;
	}

	/**
	 * @return Returns the all items which were in the component
	 */
	public List<String> getAllItems() {
		return allItems;
	}

	/**
	 * @return Returns all added records (which were added manually, means not from autocompletion list)
	 */
	public List<String> getNewOnly() {
		return newOnly;
	}
}
