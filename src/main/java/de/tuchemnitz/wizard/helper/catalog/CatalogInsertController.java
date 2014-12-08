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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.helper.catalog;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.catalog.CatalogEntryAddController;

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
public class CatalogInsertController extends CatalogEntryAddController {

	private CatalogEntry selectedParent;
	private final RepositoryEntry toBeAddedEntry;
	private CourseCreationConfiguration courseConfig;

	public CatalogInsertController(UserRequest ureq, WindowControl control, RepositoryEntry repositoryEntry,
			CourseCreationConfiguration courseConfig) {
		super(ureq, control, repositoryEntry, false, true);
		toBeAddedEntry = repositoryEntry;
		this.courseConfig = courseConfig;
		this.selectionTree.clearSelection();
	}
	
	@Override
	protected VelocityContainer createVelocityContainer(String page) {
		setTranslator(Util.createPackageTranslator(CatalogEntryAddController.class, getLocale()));
		velocity_root = Util.getPackageVelocityRoot(CatalogEntryAddController.class);
		return super.createVelocityContainer(page);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof MenuTree) {

			TreeEvent te = (TreeEvent) event;
			if(MenuTree.COMMAND_TREENODE_EXPANDED.equals(te.getCommand())) {
				// build new entry for this catalog level
				String nodeId = selectionTree.getSelectedNodeId();
				if(nodeId == null) {
					selectedParent = null;
				} else if(StringHelper.isLong(nodeId)) {
					Long newParentId = Long.parseLong(nodeId);
					CatalogEntry newParent = catalogManager.loadCatalogEntry(newParentId);
					// check first if this repo entry is already attached to this new parent
					List<CatalogEntry> existingChildren = catalogManager.getChildrenOf(newParent);
					for (CatalogEntry existingChild : existingChildren) {
						RepositoryEntry existingRepoEntry = existingChild.getRepositoryEntry();
						if (existingRepoEntry != null && existingRepoEntry.equalsByPersistableKey(toBeAddedEntry)) {
							return;
						}
					}
					// don't create entry right away, user must select submit button first
					selectedParent = newParent;
				}
			}
		}
		super.event(ureq, source, event);
	}

	/**
	 * initialize the controller or re-initialize with existing configuration
	 */
	public void init() {
		if (getCourseCreationConfiguration().getSelectedCatalogEntry() != null) {
			selectedParent = getCourseCreationConfiguration().getSelectedCatalogEntry();
		}
	}

	private CourseCreationConfiguration getCourseCreationConfiguration() {
		return courseConfig;
	}

	/**
	 * @return the selected catalogEntry
	 */
	public CatalogEntry getSelectedParent() {
		return selectedParent;
	}
}
