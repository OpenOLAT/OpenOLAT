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

package org.olat.core.gui;

import org.olat.core.util.Formatter;

/**
 * @author Felix Jost
 */
public class GUIMessage {
	private String info;
	private String warn;
	private String error;
	private String title;

	/**
	 * Constructor for GUIMessage.
	 */
	public GUIMessage() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Method setInfo.
	 * 
	 * @param info
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * after calling this method, the info is cleared so it is only rendered once
	 * and therefore only displayed once to the user
	 * 
	 * @return the info (may be null)
	 */
	public String renderInfo() {
		String result = getInfo();
		setInfo(null);
		return Formatter.escapeDoubleQuotes(result).toString();
	}

	/**
	 * after calling this method, the warning is cleared so it is only rendered
	 * once and therefore only displayed once to the user
	 * 
	 * @return
	 */
	public String renderWarn() {
		String result = getWarn();
		setWarn(null);
		return Formatter.escapeDoubleQuotes(result).toString();		
	}

	/**
	 * after calling this method, the error is cleared so it is only rendered once
	 * and therefore only displayed once to the user
	 * 
	 * @return
	 */
	public String renderError() {
		String result = getError();
		setError(null);
		return Formatter.escapeDoubleQuotes(result).toString();
	}

	/**
	 * @return
	 */
	public boolean hasInfo() {
		return info != null;
	}

	/**
	 * @return
	 */
	public boolean hasWarn() {
		return warn != null;
	}

	/**
	 * @return
	 */
	public boolean hasError() {
		return error != null;
	}

	/**
	 * @return
	 */
	private String getError() {
		return error;
	}

	/**
	 * @return
	 */
	private String getWarn() {
		return warn;
	}

	/**
	 * @return
	 */
	private String getInfo() {
		return info;
	}

	/**
	 * @param string
	 */
	public void setError(String string) {
		error = string;
	}

	/**
	 * @param string
	 */
	public void setWarn(String string) {
		warn = string;
	}

	/**
	 * @return
	 */
	public boolean hasContent() {
		return hasInfo() || hasWarn() || hasError();
	}

}