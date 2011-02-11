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
* 
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik
* 
* Author Marcel Karras (toka@freebits.de)
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.helper.catalog;

import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.ui.CatalogAjaxAddController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.repository.RepositoryEntry;

import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * Description:<br>
 * Controller for inserting the given course into the catalog.
 * 
 * <P>
 * Initial Date: 02.12.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class CatalogInsertController extends CatalogAjaxAddController {

	private CourseCreationConfiguration courseConfig;

	public CatalogInsertController(UserRequest ureq, WindowControl control, RepositoryEntry repositoryEntry,
			CourseCreationConfiguration courseConfig) {
		super(ureq, control, repositoryEntry);

		this.courseConfig = courseConfig;

		cancelLink.setVisible(false);
		selectLink.setVisible(false);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == treeCtr) {
			if (event instanceof TreeNodeClickedEvent) {
				TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
				// build new entry for this catalog level
				CatalogManager cm = CatalogManager.getInstance();
				String nodeId = clickedEvent.getNodeId();
				Long newParentId = Long.parseLong(nodeId);
				CatalogEntry newParent = cm.loadCatalogEntry(newParentId);
				// check first if this repo entry is already attached to this new parent
				List<CatalogEntry> existingChildren = cm.getChildrenOf(newParent);
				for (CatalogEntry existingChild : existingChildren) {
					RepositoryEntry existingRepoEntry = existingChild.getRepositoryEntry();
					if (existingRepoEntry != null && existingRepoEntry.equalsByPersistableKey(toBeAddedEntry)) {
						return;
					}
				}
				// don't create entry right away, user must select submit button first
				this.selectedParent = newParent;
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}

	}

	/**
	 * initialize the controller or re-initialize with existing configuration
	 */
	public void init() {
		if (getCourseCreationConfiguration().getSelectedCatalogEntry() != null) {
			this.selectedParent = getCourseCreationConfiguration().getSelectedCatalogEntry();
			treeCtr.selectPath(CatalogHelper.getPath(this.selectedParent));
		}
	}

	private CourseCreationConfiguration getCourseCreationConfiguration() {
		return this.courseConfig;
	}

	/**
	 * @return the selected catalogEntry
	 */
	public CatalogEntry getSelectedParent() {
		return this.selectedParent;
	}
}
