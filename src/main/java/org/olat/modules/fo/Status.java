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
*/

package org.olat.modules.fo;


/**
 * 
 * Description:<br>
 * Encapsulates the status of a <code>Message</code>, and
 * converts the statusCode integer into a meaningful Status object,
 * and vice versa. 
 * 
 * <P>
 * Initial Date:  06.07.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class Status {

	private boolean sticky; //0001
	private boolean hidden; //0010
	private boolean closed; //0100
	private boolean moved;	//1000

	public Status() {
		//empty contructor
	}
	
	/**
	 * Converts the input statusCode integer into a <code>Status</code> object.
	 * @param statusCode
	 * @return a Status object
	 */
	public static Status getStatus(int statusCode) {
		if(statusCode<0 || statusCode>15) {
			throw new IllegalArgumentException("statusCode not supported: " + statusCode);
		}
		Status status = new Status();
		if((statusCode & 8) == 8) {
			status.setMoved(true);
		}
		if((statusCode & 4) == 4) {
			status.setClosed(true);
		}
		if((statusCode & 2) == 2) {
			status.setHidden(true);
		}
		if((statusCode & 1) == 1 ) {
			status.setSticky(true);
		}
		return status;
	}
	
	/**
	 * Converts the input <code>Status</code> object into an integer value.
	 * @param status
	 * @return
	 */
	public static int getStatusCode(Status status) {
		int statusCode = 0;
		if(status.isMoved()) {
			statusCode+=8;
		}
		if(status.isClosed()) {
			statusCode+=4;
		}
		if(status.isHidden()) {
			statusCode+=2;
		}
		if(status.isSticky()) {
			statusCode+=1;
		}		
		return statusCode;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	
	public boolean isMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}
}
