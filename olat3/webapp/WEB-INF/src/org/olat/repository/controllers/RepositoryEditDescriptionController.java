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

package org.olat.repository.controllers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDetailsFormController;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEditDescriptionController extends BasicController {
	private VelocityContainer chdesctabVC;
	private Controller repoEntryDetailsFormCtr;
	private Controller imageUploadController;
	private TabbedPane tabbedPane;
	private RepositoryEntry repositoryEntry;
	private VelocityContainer descVC;

	private static final int picUploadlimitKB = 1024;

	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEditDescriptionController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean isSubWorkflow) {
		super(ureq, wControl);
		setBasePackage(RepositoryManager.class);
		this.repositoryEntry = entry;
		// wrapper velocity container with a tabbed pane
		descVC = createVelocityContainer("bgrep");
		descVC.contextPut("title", entry.getDisplayname());
		tabbedPane = new TabbedPane("descTB", ureq.getLocale());
		chdesctabVC = createVelocityContainer("changedesctab1");
		chdesctabVC.contextPut("id", entry.getResourceableId() == null ? "-" : entry.getResourceableId().toString());
		chdesctabVC.contextPut("initialauthor", entry.getInitialAuthor());
		descVC.contextPut("disabledforwardreason", translate("disabledforwardreason"));
		// repo entry details form
		repoEntryDetailsFormCtr = new RepositoryEntryDetailsFormController(ureq, getWindowControl(), entry, isSubWorkflow);
		listenTo(repoEntryDetailsFormCtr);
		chdesctabVC.put("repoEntryDetailsFormCtr", repoEntryDetailsFormCtr.getInitialComponent());
		// file upload form - should be refactored to RepositoryEntryDetailsFormController, need more time to do this
		imageUploadController = new RepositoryEntryImageController(ureq, wControl, entry, getTranslator(), picUploadlimitKB);
		listenTo(imageUploadController);
		chdesctabVC.put("imageupload", imageUploadController.getInitialComponent());

		tabbedPane.addTab(translate("table.header.description"), chdesctabVC);
		tabbedPane.addListener(this);
		descVC.put("descTB", tabbedPane);
		putInitialPanel(descVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.imageUploadController) {
			if (event.equals(Event.DONE_EVENT)) fireEvent(ureq, Event.CHANGED_EVENT);
			
		} else if (source == this.repoEntryDetailsFormCtr) { // process details form events
			
			if (event.equals(Event.CANCELLED_EVENT)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);

			} else if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
				fireEvent(ureq, Event.DONE_EVENT);
				descVC.contextPut("title", getRepositoryEntry().getDisplayname());
			}
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

}
