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

package org.olat.core.commons.chiefcontrollers;

import org.olat.core.gui.control.Event;
import org.olat.core.util.event.MultiUserEvent;

/**
 * One can send a message to the ChiefControllers which appears on top.
 * Typically this is used for setting maintenance messages, such as <i>system is
 * going down in 10 minutes</i>.
 * <p>
 * A ChiefController per se is not listening to such events. It must implement
 * <code>GenericEventListener</code> and add itself to the channel which can be 
 * found in the <code>ChiefControllerFactory</code>.
 * 
 * @see org.olat.core.util.event.MultiUserEvent
 * @see org.olat.core.util.event.OLATResourceableEventCenter
 * @see org.olat.core.commons.chiefcontrollers.ChiefControllerFactory
 *      <P>
 *      Initial Date: 13.06.2006 <br>
 * @author patrickb
 */
public class ChiefControllerMessageEvent extends MultiUserEvent {

	private static final long serialVersionUID = -2235582801661115222L;
	/**
	 * indicates removing clearing of maintenance message
	 */
	public static final ChiefControllerMessageEvent CLEAR = new ChiefControllerMessageEvent("CLR");
	/**
	 * 
	 */
	public static final String CHANGED_EVENT_CMD = Event.CHANGED_EVENT.getCommand();
	private String msg;
	//by default messages are show on all nodes in the cluster
	private boolean clusterWideMessage = true;
	
	public ChiefControllerMessageEvent(){
		this(Event.CHANGED_EVENT.getCommand());
	}
	
	/**
	 * 
	 * @param command
	 */
	private ChiefControllerMessageEvent(String command) {
		super(command);
		msg = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMsg() {
		return msg;
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void setMsg(String msg){
		this.msg = msg;
	}
	
	public String toString() {
		return "msg:"+msg+"|"+super.toString();
	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object event) {
		if(event!=null && event instanceof ChiefControllerMessageEvent) {
			ChiefControllerMessageEvent other = (ChiefControllerMessageEvent)event;
			boolean sameCommand = other.getCommand().equals(this.getCommand());
			String othersMsg = other.getMsg();
			boolean sameMsg = ((msg == null && othersMsg == null));//case for CLEAR
			sameMsg = sameMsg || (msg != null && msg.equals(othersMsg));//some msg compared to CLEAR or others
			sameMsg = sameMsg || (othersMsg != null && othersMsg.equals(msg));// CLEAR compared to other msg
			 
			boolean sameClusterWide = clusterWideMessage == other.isClusterWideMessage(); 
			return sameCommand && sameMsg && sameClusterWide;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isClusterWideMessage() {
		return clusterWideMessage;
	}

	/**
	 * 
	 * @param clusterWideMessage
	 */
	public void setClusterWideMessage(boolean clusterWideMessage) {
		this.clusterWideMessage = clusterWideMessage;
	}
	
	

	@Override
	public int hashCode() { 
		int hc = 5;
		int hcMul = 7;
		hc = hc * hcMul + (clusterWideMessage ? 0 : 1);
		hc = hc * hcMul + (msg == null ? 0 : msg.hashCode());
		return hc * hcMul + super.hashCode();
	}	
	
	
}
