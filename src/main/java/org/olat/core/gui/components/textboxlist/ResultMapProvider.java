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
package org.olat.core.gui.components.textboxlist;

import java.util.Map;

/**
 * Description:<br>
 * ResultMapProvider should be used to override locally with a concrete
 * implementation providing results to the textboxlistcomponent.
 * 
 * <P>
 * Initial Date: 26.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface ResultMapProvider {

	/**
	 * adds values for TextBoxList auto-completion for the given
	 * searchValue (which will be the user input) to the resMap.<br />
	 * Key-String of the resulting Map is the caption, Value-String is the value<br />
	 * (i.e.: in autocompletion-dropdown, caption is displayed, value is
	 * submitted)
	 * 
	 * @param searchValue
	 * @param resMap
	 */
	public void getAutoCompleteContent(String searchValue, Map<String, String> resMap);

}
