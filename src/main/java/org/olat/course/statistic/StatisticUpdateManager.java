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
package org.olat.course.statistic;

/**
 * The StatisticUpdateManager is a pod for registering all IStatisticUpdaters
 * and all it does is going through each of the IStatisticUpdaters to 
 * call its updateStatistic method.
 * <P>
 * Initial Date:  11.02.2010 <br>
 * @author Stefan
 */
public interface StatisticUpdateManager {
	
	public void setEnabled(boolean enabled);

	/**
	 * Add the given IStatisticUpdater to this StatisticUpdateManager.
	 * <p>
	 * Note that is the duty of the IStatisticUpdater to register itself
	 * with this StatisticUpdateManager!
	 * @param updater the updater to be added to this StatisticUpdateManager
	 */
	public void addStatisticUpdater(IStatisticUpdater updater);

	/**
	 * Sets the LAST_UPDATED property to the given value lastUpdated
	 * @param lastUpdated the new lastUpdated value
	 */
	public long getAndUpdateLastUpdated(long lastUpdated);
	
	/**
	 * Returns the existing LAST_UPDATED property's value - or -1 if it's not set
	 * @return the existing LAST_UPDATED property's value - or -1 if it's not set
	 */
	public long getLastUpdated();
	
	/**
	 * Update all statistics - that is, all that registered itself via addStatisticUpdater
	 * @return whether or not the update could be started (returns false when this manager
	 * is not enabled or when an update is already ongoing)
	 */
	public boolean updateStatistics(boolean fullRecalculation, Runnable finishedCallback);
	
	/**
	 * Returns whether this manager is enabled or not. It will be enabled on one node in a cluster only
	 * @return whether this manager is enabled or not. It will be enabled on one node in a cluster only
	 */
	public boolean isEnabled();

	/**
	 * Checks whether in this very moment an update is ongoing.
	 * <p>
	 * Note that one millisecond later, some other node or the cron might start an update - 
	 * so this is really just true for this very moment
	 * @return whether in this very moment an update is ongoing
	 */
	public boolean updateOngoing();
}
