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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.core.gui.control.generic.ajax.autocompletion;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  06.10.2006 <br>
 *
 * @author Felix Jost
 */
public class EntriesChosenEvent extends Event {

	private final List entries;

	/**
	 * @param command
	 */
	EntriesChosenEvent(List entries) {
		super("chosenentries");
		this.entries = entries;
	}

	/**
	 * @return Returns the entries. never null, but may be an empty list. an item in the list contains a key, which is a string.
	 */
	public List getEntries() {
		return entries;
	}

}
