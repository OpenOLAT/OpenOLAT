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

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Provider;

/**
 * Description:<br>
 * As the upgrade manager only runs on one node this dummy gets called on the other nodes
 * 
 * <P>
 * Initial Date:  11.09.2008 <br>
 * @author guido
 */
public class UpgradeManagerDummy extends UpgradeManager {

	/**
	 * Execute the pre system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public void doPreSystemInitUpgrades() {
		//nothing to do
	}

	/**
	 * Execute the post system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public void doPostSystemInitUpgrades() {
		//nothing to do
	}

	/**
	 * @see org.olat.upgrade.UpgradeManager#init()
	 */
	public void init() {
		//nothing to do
	}

}
