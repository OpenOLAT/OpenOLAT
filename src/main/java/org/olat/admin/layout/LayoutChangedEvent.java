/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.layout;

import org.olat.core.util.event.MultiUserEvent;

/**
 * Description:<br>
 * this event gets fired on customizing settings by CustomizingAdminController
 * 
 * <P>
 * Initial Date:  20.06.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class LayoutChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 6963077725749903124L;
	
	public static final String LAYOUTSETTINGSCHANGED = "LayoutSettingsChanged";
	
	public LayoutChangedEvent(String command) {
		super(command);
	}
	
	public String toString(){
		return getCommand();
	}
	
}
