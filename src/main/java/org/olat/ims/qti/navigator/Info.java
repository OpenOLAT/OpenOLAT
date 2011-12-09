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

package org.olat.ims.qti.navigator;

import java.io.Serializable;

import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.Output;

public class Info implements Serializable {
	private int status = QTIConstants.STATUS_NONE;
	private int message = QTIConstants.MESSAGE_NONE;
	private int error = QTIConstants.ERROR_NONE;
	
	private boolean renderItems = false;

	private boolean feedback = false;
	private boolean hint = false;
	private boolean solution = false;
	
	private Output currentOutput;
	
	/**
	 * @return int
	 */
	public int getError() {
		return error;
	}

	/**
	 * @return int
	 */
	public int getMessage() {
		return message;
	}

	/**
	 * @return int
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the error.
	 * @param error The error to set
	 */
	public void setError(int error) {
		this.error = error;
	}

	/**
	 * Sets the message.
	 * @param message The message to set
	 */
	public void setMessage(int message) {
		this.message = message;
	}

	/**
	 * Sets the status.
	 * @param status The status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	public boolean containsError() {
		return (error != QTIConstants.ERROR_NONE);
	}
	
	public boolean containsMessage() {
		return (message != QTIConstants.MESSAGE_NONE);
	}
	
	public void clear() { // clear everthing except of status
		error = QTIConstants.ERROR_NONE;
		message = QTIConstants.MESSAGE_NONE;
		renderItems = false;
		feedback = false;
		hint = false;
		solution = false;
		currentOutput = null;
	}
	

	/**
	 * Returns the renderItems.
	 * @return boolean
	 */
	public boolean isRenderItems() {
		return renderItems;
	}

	/**
	 * Sets the renderItems.
	 * @param renderItems The renderItems to set
	 */
	public void setRenderItems(boolean renderItems) {
		this.renderItems= renderItems;
	}

	/**
	 * @return boolean
	 */
	public boolean isFeedback() {
		return feedback;
	}

	/**
	 * @return boolean
	 */
	public boolean isHint() {
		return hint;
	}

	/**
	 * @return boolean
	 */
	public boolean isSolution() {
		return solution;
	}

	/**
	 * Sets the feedback of the item
	 * @param feedback The feedback to set
	 */
	public void setFeedback(boolean itemfeedback) {
		this.feedback = itemfeedback;
	}

	/**
	 * Sets the hint.
	 * @param hint The hint to set
	 */
	public void setHint(boolean hint) {
		this.hint = hint;
	}

	/**
	 * Sets the solution.
	 * @param solution The solution to set
	 */
	public void setSolution(boolean solution) {
		this.solution = solution;
	}

	/**
	 * @return Output
	 */
	public Output getCurrentOutput() {
		return currentOutput;
	}

	/**
	 * Sets the currentOutput.
	 * @param currentOutput The currentOutput to set
	 */
	public void setCurrentOutput(Output currentOutput) {
		this.currentOutput = currentOutput;
	}

}
