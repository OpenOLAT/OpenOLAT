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
package org.olat.repository.async;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;


/**
 * @author Christian Guretzki
 */
public class SetPropertiesBackgroundTask extends AbstractBackgroundTask {
	private static OLog log = Tracing.createLoggerFor(SetPropertiesBackgroundTask.class);
	
	private RepositoryEntry repositoryEntry;

	private boolean canCopy;
	private boolean canReference;
	private boolean canLaunch;
	private boolean canDownload;
	
	
	public SetPropertiesBackgroundTask(RepositoryEntry repositoryEntry, boolean canCopy, boolean canReference, boolean canLaunch, boolean canDownload) {
		this.repositoryEntry = repositoryEntry;
		this.canCopy      = canCopy;
		this.canReference = canReference;
		this.canLaunch    = canLaunch;
		this.canDownload  = canDownload;
	}
	public void executeTask() {
		log.debug("SetPropertiesBackgroundTask executing with repositoryEntry=" + repositoryEntry);
		// this code must not be synchronized because in case of exception we try it again
		// this code must not have any error handling or retry, this will be done in super class
		if ( RepositoryManager.getInstance().lookupRepositoryEntry(repositoryEntry.getKey()) != null ) {
			RepositoryEntry reloadedRe = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry, true);
			reloadedRe.setCanCopy(canCopy);
			reloadedRe.setCanReference(canReference);
			reloadedRe.setCanLaunch(canLaunch);
			reloadedRe.setCanDownload(canDownload);
			RepositoryManager.getInstance().updateRepositoryEntry(reloadedRe);
		} else {
			log.info("Could not executeTask, because repositoryEntry does no longer exist");
		}
	}

}
