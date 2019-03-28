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

import java.util.Iterator;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * <P>
 * Initial Date:  15.08.2005 <br>
 * @author gnaegi
 * @author guido
 */
public class UpgradeManagerImpl extends UpgradeManager {
	
	private static final OLog log = Tracing.createLoggerFor(UpgradeManagerImpl.class);
	
	/**
	 * used by spring
	 */
	public UpgradeManagerImpl() {
		//
	}

	/**
	 * Execute the post system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	@Override
	public void doPostSystemInitUpgrades() {
		UpgradesTask task = new UpgradesTask();
		CoreSpringFactory.getImpl(TaskExecutorManager.class).execute(task);
	}
	
	private class UpgradesTask implements Runnable {

		@Override
		public void run() {
			Iterator<OLATUpgrade> iter = upgrades.iterator();
			OLATUpgrade upgrade = null; 
			try {
				while (iter.hasNext()) {
					upgrade = iter.next();
					if (upgrade.doPostSystemInitUpgrade(UpgradeManagerImpl.this)) {
						log.audit("Successfully installed PostSystemInitUpgrade::" + upgrade.getVersion());
					}
					//just in case a doPostSystemInitUpgrade did forget it.
					DBFactory.getInstance().commitAndCloseSession();
				}
			} catch (Throwable e) {
				DBFactory.getInstance().rollbackAndCloseSession();
				log.warn("Error upgrading PostSystemInitUpgrade::" + (upgrade == null ? "NULL" : upgrade.getVersion()), e);
				abort(e);
			}
		}
	}
}
