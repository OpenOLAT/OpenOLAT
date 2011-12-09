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
package org.olat.course.nodes.en;

/**
 * 
 * Description:<br>
 * Data-object to return the result of a enroll-call.
 * Return states : 
 * - enrolled<br>
 * - enrolled in waiting-list<br>
 * - !isEnrolled && !isInWaitingList => can not enroll, see error-message. 
 * 
 * <P>
 * Initial Date:  23.11.2006 <br>
 * @author guretzki
 */
public class EnrollStatus {
	
	private String errorMessage;
	private boolean isEnrolled;
	private boolean isInWaitingList;

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setIsInWaitingList(boolean isInWaitingList) {
		this.isInWaitingList = isInWaitingList;
	}

	public void setIsEnrolled(boolean isEnrolled) {
		this.isEnrolled = isEnrolled;
	}

	public boolean isEnrolled() {
		return isEnrolled;
	}

	public boolean isInWaitingList() {
		return isInWaitingList;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
