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

package org.olat.course.assessment.manager;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.assessment.EfficiencyStatementEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * A worker which updates the efficicency statements 
 */
public class UpdateEfficiencyStatementsWorker implements Runnable {
	private static final Logger log = Tracing.createLoggerFor(UpdateEfficiencyStatementsWorker.class);
	private final OLATResourceable ores;
	
	/**
	 * 
	 * @param course
	 * @param re
	 */
	public UpdateEfficiencyStatementsWorker(OLATResourceable ores) {
		this.ores = ores;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean success = false;
		try{
			RepositoryManager rm = CoreSpringFactory.getImpl(RepositoryManager.class);
			EfficiencyStatementManager esm = CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
			RepositoryEntry re = rm.lookupRepositoryEntry(ores, false);
			
			List<Identity> identities = esm.findIdentitiesWithEfficiencyStatements(re.getKey());
			esm.updateEfficiencyStatements(re, identities);
			// close db session in this thread
			DBFactory.getInstance().commitAndCloseSession();
			success = true;
			
			EfficiencyStatementEvent finishedEvent = new EfficiencyStatementEvent(EfficiencyStatementEvent.CMD_FINISHED, ores.getResourceableId());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(finishedEvent, ores);
		} catch(Exception ex) {
			log.error("Unexpected exception updating the efficiency statements of " + ores, ex);
		} finally {
			// close db session in this thread
			if (!success) {
				DBFactory.getInstance().rollbackAndCloseSession();
			}
		}
	}
}