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

package org.olat.upgrade;

import java.util.List;

/**
 * Description:<br>
 * List of all upgrades available within this system. Used by sping framework to load all
 * upgrades from the configuration file
 * 
 * <P>
 * Initial Date:  15.08.2005 <br>
 * @author gnaegi
 */
public class UpgradesDefinitions {
	private List<OLATUpgrade> upgrades;
	
	/**
	 * [spring only]
	 */
	private UpgradesDefinitions() {
	// 
	}

	/**
	 * @return List of upgrades
	 */
	public List<OLATUpgrade> getUpgrades() {
		return upgrades;
	}

	/**
	 * @param upgrades List of upgrades, used by spring framework
	 */
	public void setUpgrades(List<OLATUpgrade> upgrades) {
		this.upgrades = upgrades;
	}
	
}
