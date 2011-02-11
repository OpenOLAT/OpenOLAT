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

package org.olat.core.gui.formelements;

/**
 * Initial Date: 08.08.2003
 * @author Mike Stock Comment:
 */
public class LinkElement extends AbstractFormElement {

	private String URL;
	private String linkName;
	private String origUrl, origLinkName;

	/**
	 * @param labelKey
	 * @param URL
	 * @param linkName
	 */
	public LinkElement(String labelKey, String URL, String linkName) {
		this.setLabelKey(labelKey);
		this.setURL(URL);
		this.setLinkName(linkName);
	}

	/**
	 * Ignore
	 * 
	 * @param values
	 */
	public void setValues(String[] values) {
	//
	}

	/**
	 * @return
	 */
	public String getURL() {
		return URL;
	}

	/**
	 * @param string
	 */
	public void setURL(String string) {
		URL = string;
		// Remember original value for dirty evaluation
		if (origUrl == null) origUrl = new String(string);
	}

	/**
	 * @return
	 */
	public String getLinkName() {
		return linkName;
	}

	/**
	 * @param string
	 */
	public void setLinkName(String string) {
		linkName = string;
		// Remember original value for dirty evaluation
		if (origLinkName == null) origLinkName = new String(string);
	}

	/**
	 * @see org.olat.core.gui.formelements.FormElement#isDirty()
	 */
	public boolean isDirty() {
		if (origLinkName.equals(linkName) && origUrl.equals(URL)) return false;
		return true;
	}

}