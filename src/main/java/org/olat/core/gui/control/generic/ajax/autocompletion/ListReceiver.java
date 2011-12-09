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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.gui.control.generic.ajax.autocompletion;

/**
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date: 06.10.2006 <br>
 * 
 * @author Felix Jost, Florian Gnägi
 */
public interface ListReceiver {

	/**
	 * Add a list entry without icon representation
	 * 
	 * @param key The identifier of the entry. This is also used as a visible
	 *          identifier
	 * @param value The value of the entry in a string representation.
	 */
	public void addEntry(String key, String value);

	/**
	 * Add a list entry with optional icon representation and optional displayKey
	 * 
	 * @param key The identifier of the entry
	 * @param displayKey The optional identifier which can also be displayed in
	 *          the GUI when. This can be the same as the key, but while the key
	 *          should be some object identifier the displayKey is something that
	 *          is purely for the user to identify the object. E.g. the user key
	 *          is the database ID while for humans the user name is more
	 *          meaningful.
	 * @param displayText The value of the entry in a string representation.
	 * @param iconCssClass An optional CSS class that provides a background icon
	 *          or NULL to not use a class
	 */
	public void addEntry(String key, String displayKey, String displayText, String iconCssClass);

}
