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

package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * Search form for Business groups
 * 
 * <P>
 * Initial Date:  21 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-1,2: access control of resources
public class AdminBusinessGroupSearchController extends BusinessGroupSearchController{

	/**
	 * Generic search form.
	 * @param name Internal form name.
	 * @param translator Translator
	 * @param withCancel Display a cancel button?
	 * @param isAdmin Is calling identity an administrator? If yes, allow search by ID
	 * @param limitTypes Limit searches to specific types.
	 */
	public AdminBusinessGroupSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, true);
	}

}