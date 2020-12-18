/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.wizard.ui;

import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.wizard.ui.RepositoryEntryOverviewController.MoreFigures;

/**
 * 
 * Initial date: 10 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReferencableEntriesStepController extends StepFormBasicController {
	
	public static final Event SERACH_STARTED_EVENT = new Event("search.startet");
	public static final Event SERACH_CANCELLED_EVENT = new Event("search.cancelled");
	public static final Event SERACH_DONE_EVENT = new Event("search.done");
	
	private RepositoryEntryOverviewController overviewCtrl;
	private ReferencableEntriesSearchController searchCtrl;
	
	private final String limitTypes;
	private final MoreFigures moreFigures;
	private final ReferencableEntryContext entryContext;
	private boolean search;

	public ReferencableEntriesStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, String runContextKey, Supplier<Object> contextCreator, String limitTypes,
			MoreFigures moreFigures) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "referenceable");
		this.limitTypes = limitTypes;
		this.moreFigures = moreFigures;
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		entryContext = (ReferencableEntryContext)getOrCreateFromRunContext(runContextKey, contextCreator);
		
		overviewCtrl = new RepositoryEntryOverviewController(ureq, wControl);
		listenTo(overviewCtrl);
		flc.put("overview", overviewCtrl.getInitialComponent());
		
		initForm(ureq);
		setSearch(ureq, entryContext.getReferencedEntry() == null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}
	
	public RepositoryEntry getEntry() {
		return entryContext.getReferencedEntry();
	}
	
	public boolean isSearch() {
		return search;
	}
	
	private void setSearch(UserRequest ureq, boolean search) {
		this.search = search;
		if (search) {
			removeAsListenerAndDispose(searchCtrl);
			boolean showCancel = entryContext.getReferencedEntry() != null;
			searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[] {limitTypes}, "select", false, false, false, false, false, showCancel);
			listenTo(searchCtrl);
			flc.put("search", searchCtrl.getInitialComponent());
			
			overviewCtrl.getInitialComponent().setVisible(false);
		} else {
			if (searchCtrl != null) {
				searchCtrl.getInitialComponent().setVisible(false);
			}
			overviewCtrl.getInitialComponent().setVisible(true);
			overviewCtrl.setRepositoryEntry(ureq, entryContext.getReferencedEntry(), moreFigures);
			flc.setDirty(true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == overviewCtrl) {
			if (event == RepositoryEntryOverviewController.REPLACE_EVENT) {
				setSearch(ureq, true);
				fireEvent(ureq, SERACH_STARTED_EVENT);
			}
		} else if (source == searchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				RepositoryEntry entry = searchCtrl.getSelectedEntry();
				entryContext.setReferencedEntry(entry);
				setSearch(ureq, false);
				flc.setDirty(true);
				fireEvent(ureq, SERACH_DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				setSearch(ureq, false);
				flc.setDirty(true);
				fireEvent(ureq, SERACH_CANCELLED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (entryContext.getReferencedEntry() == null) {
			showInfo("error.select.referenceable");
		} else {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
