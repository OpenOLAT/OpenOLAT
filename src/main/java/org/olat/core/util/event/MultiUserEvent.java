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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.event;

import org.olat.core.gui.control.Event;

/**
 * Description:<BR/>
 * an event which is fired to potentially more than just one user.
 * must be serializable to support multiple server installations.
 * 
 * MultiUserEvents should also contain only simple messages.
 * That is, no veto, or confirm, or addSomethingTo() should be offered.
 * The publish/subscribe works as a fire & forget mechanism, especially when employed over several java vms.
 * 
 * <P/>
 * Initial Date:  02.09.2004
 *
 * @author Felix Jost 
 */
public class MultiUserEvent extends Event {
	
	//used to mark event
	//comparing VM_MARKER == vm_marker gives the info if the (deserialized) event comes
	//from the same node. Most of the time such a comparison is wrong, especially for strings
	//here it is used as "feature".
	protected static String VM_MARKER = "VM_MARKER";
	protected String vm_marker; 
	
	/**
	 * @param command
	 */
	public MultiUserEvent(String command) {
		super(command);
		vm_marker = VM_MARKER;
	}
	
	/**
	 * should be overridden by subclasses for debug info
	 */
	public String toString() {
		return "MUE:com="+getCommand();
	}

	/**
	 * check if event in the same VM
	 * this is used only in ClusterLocker to check if Release All Locks or users should be executed. (release locks for a 
	 * use is a "single node service")
	 * FIXME:2008-11-27:pb == comparison to see if in the same VM. 
	 * @return
	 */
	public boolean isEventOnThisNode() {
		return vm_marker == VM_MARKER;
	}
	
	@Override
	public int hashCode() { 
		int hc = 3;
		int hcMul = 7;
		hc = hc * hcMul + vm_marker.hashCode();
		return hc * hcMul + super.hashCode();
	}
	
}

