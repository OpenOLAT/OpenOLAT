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
package org.olat.portfolio.ui.artefacts.view;

import org.olat.core.gui.control.Event;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * event used, when an artefact got deleted in gui
 * 
 * <P>
 * Initial Date:  12.01.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactDeletedEvent extends Event {

	private static final long serialVersionUID = -3990634156779087562L;
	public static final String ARTEFACT_DELETED = "artefactDeleted";
	private final Long oldArtefactKey;
	private final AbstractArtefact artefact;

	public EPArtefactDeletedEvent(AbstractArtefact artefact) {
		super(ARTEFACT_DELETED);
		this.artefact = artefact;
		this.oldArtefactKey = artefact.getKey();
	}

	/**
	 * @return Returns the oldArtefactKey.
	 */
	public Long getOldArtefactKey() {
		return oldArtefactKey;
	}

	/**
	 * @return Returns the artefact.
	 */
	public AbstractArtefact getArtefact() {
		return artefact;
	}
}
