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
* <p>
*/ 

package org.olat.course.assessment;

import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * TODO: guido Class Description for UpdateEfficiencyStatementsWorker
 */
public class UpdateEfficiencyStatementsWorker implements Runnable {
	private List identities;
	private OLATResourceable ores;
	
	/**
	 * 
	 * @param course
	 * @param re
	 */
	public UpdateEfficiencyStatementsWorker(OLATResourceable ores) {
		this.ores = ores;
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry re = rm.lookupRepositoryEntry(ores, false);
		// get all users who already have an efficiency statement
		identities = EfficiencyStatementManager.getInstance().findIdentitiesWithEfficiencyStatements(re.getKey());
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean success = false;
		try{
			EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
			esm.updateEfficiencyStatements(ores, identities, true);
			// close db session in this thread
			DBFactory.getInstance(false).commitAndCloseSession();
			success = true;
		} finally {
			// close db session in this thread
			if (!success) {
				DBFactory.getInstance(false).rollbackAndCloseSession();
			}
		}
	}

}
