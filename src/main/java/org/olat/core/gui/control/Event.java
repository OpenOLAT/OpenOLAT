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

package org.olat.core.gui.control;

import java.io.Serializable;

/**
 * enclosing_type Description: <br>
 * Events must be serializable and short; it is the best if you manage to use
 * only Strings
 * 
 * @author Felix Jost
 */
public class Event implements Serializable {
	private static final long serialVersionUID = -107618726848894274L;
	
	public static final Event DONE_EVENT = new Event("done");
	
	public static final Event BACK_EVENT = new Event("back");
	
	public static final Event CLOSE_EVENT = new Event("close");
	
	public static final Event CANCELLED_EVENT = new Event("cancelled");
	
	public static final Event CHANGED_EVENT = new Event("changed");
	
	public static final Event FAILED_EVENT = new Event("failed");
	
	private final String command;
	
	
	public Event(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String toString() {
		return "com:" + command + "," + super.toString();
	}
	
	
	/**
	 * overwritten equals method return also true
	 * for:
	 * Event eventA = new Event(test);
	 * eventA.equals(new Event("test"))
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		Event other = (Event) obj;
		if (command == null) {
			if (other.command != null){
				return false;
			}
		} else if (!command.equals(other.command)){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		return result;
	}
	
	
}