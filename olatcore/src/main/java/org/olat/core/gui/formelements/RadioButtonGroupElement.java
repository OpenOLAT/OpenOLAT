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
 * Radio buttons are static single selection elements that are rendered as
 * a radio button group instead of a drop down list
 * <P>
 * Initial Date: Aug 6, 2004
 * @author patrick
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */

public class RadioButtonGroupElement extends StaticSingleSelectionElement {

	boolean renderVertical;
	/**
	 * noLabel means, that there will be no decription text left of
	 * the radio buttons. You can use this in a scenario like this:
	 * 
	 * Description	StaticHTMLElement describes something
	 * 
	 * 							O ThisIsThe1stoption
	 * 							O ThisIsThe2ndOption
	 * 							O ThisIsThe3rdOption
	 * 
	 * As you see, there is no second description since the HTML text already
	 * described everything. Used e,g, in external page config form. 
	 * 
	 */
	boolean noLabel;
	/**
	 * If HTML is allowed, HTML Tags from LocalStrings will not be escaped,
	 */
	boolean HTMLIsAllowed;

	/**
	 * 
	 * @param renderVertical
	 * @param labelKey
	 * @param keys
	 * @param values
	 */
	public RadioButtonGroupElement(boolean renderVertical, String labelKey, String[] keys, String[] values) {
		super(labelKey, keys, values);
		this.renderVertical = renderVertical;
		this.noLabel = false;
		this.HTMLIsAllowed = false;
	}

	/**
	 * @return true if radio buttons should be rendered vertical
	 */
	public boolean renderVertical() {
		return renderVertical;
	}

	/**
	 * @return Returns true if no description should be displayed.
	 */
	public boolean isNoLabel() {
		return noLabel;
	}

	/**
	 * @param noLabel Set to true if no description should be displayed.
	 */
	public void setNoLabel(boolean noDescription) {
		this.noLabel = noDescription;
	}

	/**
	 * @return Returns true, if HTML should be displayed (and therefore not be escaped).
	 */
	public boolean isHTMLIsAllowed() {
		return HTMLIsAllowed;
	}

	/**
	 * @param isAllowed Set this to true, if HTML should be displayed (and therefore not be escaped).
	 */
	public void setHTMLIsAllowed(boolean isAllowed) {
		HTMLIsAllowed = isAllowed;
	}

}