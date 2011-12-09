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

import org.olat.core.gui.control.Event;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class TableEvent extends Event{
	private static final long serialVersionUID = 4709852364630387174L;
	
	private int rowId;
	private String actionId;

	/**
	 * @param command
	 * @param rowId
	 * @param actionId
	 */
	public TableEvent(final String command, final int rowId, final String actionId) {
		super(command);
		if(actionId == null){
			throw new IllegalArgumentException("null not allowed for actionId");
		}
		this.rowId = rowId;
		this.actionId = actionId;
	}

	/**
	 * @return String
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * @return int
	 */
	public int getRowId() {
		return rowId;
	}
	
	public String toString() {
		return "cmd:"+getCommand()+", rowId:"+rowId+", actionId:"+actionId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		TableEvent other = (TableEvent)obj;
		if(other.getCommand() == null && this.getCommand() != null){
			return false;
		}else if (other.getCommand() != null && this.getCommand() == null){
			return false;
		}
		
		return other.getCommand().equals(this.getCommand()) && other.actionId.equals(actionId) && other.rowId == rowId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 17;
		int result = super.hashCode();
		result = prime * result + rowId + actionId.hashCode();
		return result;
	}
}