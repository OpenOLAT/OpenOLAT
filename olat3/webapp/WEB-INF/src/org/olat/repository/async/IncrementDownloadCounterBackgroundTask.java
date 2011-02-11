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
package org.olat.repository.async;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.async.AbstractBackgroundTask;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.delete.service.RepositoryDeletionManager;
import org.olat.testutils.codepoints.server.Codepoint;


/**
 * @author Christian Guretzki
 */
public class IncrementDownloadCounterBackgroundTask extends AbstractBackgroundTask {
	private static Logger log = Logger.getLogger(IncrementDownloadCounterBackgroundTask.class.getName());
	
	private RepositoryEntry repositoryEntry;
	
	public IncrementDownloadCounterBackgroundTask(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}
	public void executeTask() {
		log.debug("IncrementDownloadCounterBackgroundTask executing with repositoryEntry=" + repositoryEntry);
		// this code must not be synchronized because in case of exception we try it again
		// this code must not have any error handling or retry, this will be done in super class
		if ( RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey()) != null ) {
			RepositoryEntry reloadedRe = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry, true);
			reloadedRe.incrementDownloadCounter();
			reloadedRe.setLastUsage(new Date());
			LifeCycleManager lcManager = LifeCycleManager.createInstanceFor(reloadedRe);
			if (lcManager.lookupLifeCycleEntry(RepositoryDeletionManager.SEND_DELETE_EMAIL_ACTION) != null) {
				Tracing.logAudit("Repository-Deletion: Remove from delete-list repositoryEntry=" + reloadedRe, RepositoryManager.class);
				LifeCycleManager.createInstanceFor(reloadedRe).deleteTimestampFor(RepositoryDeletionManager.SEND_DELETE_EMAIL_ACTION);
			}
			Codepoint.hierarchicalCodepoint(IncrementDownloadCounterBackgroundTask.class, "executeTask-before-update", 1);
			RepositoryManager.getInstance().updateRepositoryEntry(reloadedRe);
		} else {
			log.info("Could not executeTask, because repositoryEntry does no longer exist");
		}
	}

}
