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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:<br>
 * Data contaner for an upgrade version to store detailed information about the
 * state of the upgrade. When an upgrade is completed the isInstallationCompetet
 * should be set to true. Use the getDataValue und setDataValue to store data
 * about subtasks during the upgrade (partial upgrades done in case of a failure
 * of a later part)
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
public class UpgradeHistoryData {

	private Map<String, Serializable> upgradeData;
	boolean installationComplete = false;

	/**
	 * Constructor, initializes everything
	 */
	public UpgradeHistoryData() {
		upgradeData = new HashMap<>();
		installationComplete = false;
	}

	/**
	 * @return true: installation is completed, false: not completed
	 */
	public boolean isInstallationComplete() {
		return installationComplete;
	}

	/**
	 * @param installationComplete true: installation is completed, false: not completed
	 */
	public void setInstallationComplete(boolean installationComplete) {
		this.installationComplete = installationComplete;
	}

	/**
	 * @param key of value to be saved in dava value store
	 * @return Object of data value store
	 */
	public Object getDataValue(String key) {
		return upgradeData.get(key);
	}

	/**
	 * @param key
	 * @param value
	 */
	public void setDataValue(String key, Serializable value) {
		upgradeData.put(key, value);
	}

	/**
	 * @param key of value to be saved in dava value store
	 * @return boolean value in store, return false if value was not found
	 */
	public boolean getBooleanDataValue(String key) {
		Boolean val = (Boolean) upgradeData.get(key); 
		return (val == null ? false : val.booleanValue());
	}

	/**
	 * @param key
	 * @param value
	 */
	public void setBooleanDataValue(String key, boolean value) {
		upgradeData.put(key, Boolean.valueOf(value));
	}

}
