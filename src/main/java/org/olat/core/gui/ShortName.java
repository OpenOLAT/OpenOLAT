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

/**
 * ShortName is an interface to implement if you would like to be able to
 * present your business object (or other) in a non-html, one line String. e.g.
 * Dropdown lists can accept a List of ShortNames <br>
 * e.g used for: when you want to use the table with filter controller and you
 * wan't to use your objects as filters.
 * <P>
 * 
 * Initial Date: Nov 9, 2004
 * @author gnaegi
 */
public interface ShortName {

	/**
	 * Get the display value that represents this object. (non-html, just a
	 * plain-vanilla string, should be on one line.) [used by
	 * velocity:tablewithfilter.html]
	 * 
	 * @return the short name
	 */
	public String getShortName();

}