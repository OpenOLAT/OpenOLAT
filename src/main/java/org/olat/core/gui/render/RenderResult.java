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

package org.olat.core.gui.render;

import org.olat.core.gui.media.AsyncMediaResponsible;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;

/**
 * @author Felix Jost
 */

public class RenderResult {
	private Exception renderException;
	private String logMsg;
	private AsyncMediaResponsible asyncMediaResponsible;
	private int nestedLevel = 0;
	private InterceptHandlerInstance interceptHandlerInstance;
	
	
	/**
	 */
	public RenderResult() {
		//
	}

	/**
	 * @return AsyncMediaResponsible
	 */
	public AsyncMediaResponsible getAsyncMediaResponsible() {
		return asyncMediaResponsible;
	}

	/**
	 * Sets the asyncMediaResponsible.
	 * 
	 * @param asyncMediaResponsible The asyncMediaResponsible to set
	 */
	public void setAsyncMediaResponsible(AsyncMediaResponsible asyncMediaResponsible) {
		this.asyncMediaResponsible = asyncMediaResponsible;
	}

	/**
	 * Returns the nestedLevel.
	 * 
	 * @return int
	 */
	public int getNestedLevel() {
		return nestedLevel;
	}

	/**
	 * 
	 */
	public void incNestedLevel() {
		this.nestedLevel++;
	}

	/**
	 * 
	 */
	public void decNestedLevel() {
		this.nestedLevel--;
	}

	/**
	 * @param logMsg
	 * @param renderException
	 */
	public void setRenderExceptionInfo(String logMsg, Exception renderException) {
		// only set it the first time (first error will be noted)
		if (this.logMsg == null) {
			this.logMsg = logMsg;
			this.renderException = renderException;
		}
	}

	/**
	 * @return String
	 */
	public String getLogMsg() {
		return logMsg;
	}

	/**
	 * @return Exception
	 */
	public Exception getRenderException() {
		return renderException;
	}

	/**
	 * @return Returns the InterceptHandlerInstance.
	 */
	public InterceptHandlerInstance getInterceptHandlerInstance() {
		return interceptHandlerInstance;
	}

	/**
	 * @param interceptHandlerInstance
	 */
	public void setInterceptHandlerRenderInstance(InterceptHandlerInstance interceptHandlerInstance) {
		this.interceptHandlerInstance = interceptHandlerInstance;
	}


}