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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.table;

import java.util.BitSet;

import org.olat.core.gui.control.Event;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class TableMultiSelectEvent extends Event {
	private static final long serialVersionUID = 394924378743922579L;
	private String action;
	private BitSet selection;
	
	/**
	 * @param command
	 * @param rowId
	 * @param actionId
	 */
	public TableMultiSelectEvent(final String command, final String action, final BitSet selection) {
		super(command);
		assert(action != null && selection != null);
		this.action = action;
		this.selection = selection;
	}

	/**
	 * Returns the action that triggered the event.
	 * 
	 * @return
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * Return the selected rows. Each bit in the BitSet corresponds
	 * to the row in the table data model.
	 * 
	 * @return BitSet The selected entries.
	 */
	public BitSet getSelection() {
		return selection;
	}

	@Override
	public boolean equals(final Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		TableMultiSelectEvent other = (TableMultiSelectEvent)obj;
		if(other.getCommand() == null && getCommand() != null){
			return false;
		}else if (other.getCommand() != null && getCommand() == null){
			return false;
		}
		boolean isSame = other.action.equals(action);
		isSame = isSame && other.selection.equals(selection);
		isSame = isSame && other.getCommand().equals(getCommand());
		return isSame;
	}
	
	@Override
	public int hashCode() {
		final int prime = 17;
		int result = super.hashCode();
		result = prime * result + action.hashCode() + selection.hashCode();
		return result;
	}

}