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
package org.olat.repository.ui.settings;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.ExecutionType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifecycleController extends FormBasicController {

	private static final String[] dateKeys = new String[]{ "none", "private", "public"};

	private TextElement location;
	private SingleSelection dateTypesEl;
	private SingleSelection publicDatesEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private FormLayoutContainer privateDatesCont;
	
	private RepositoryEntry repositoryEntry;
	
	private boolean usedInWizard;

	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEntryLifecycleController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		initForm(ureq);
	}
	
	public RepositoryEntryLifecycleController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10, null, rootForm);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		this.usedInWizard = true;
		initForm(ureq);
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Set up info page");
		formLayout.setElementCssClass("o_sel_edit_repositoryentry");
		if (!usedInWizard) {
			setFormTitle("details.execution.title");
		}

		initLifecycle(formLayout);
		
		location = uifactory.addTextElement("cif.location", "cif.location", 255, repositoryEntry.getLocation(), formLayout);
		location.setEnabled(!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.location));
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);

		if (!usedInWizard) {
			FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
			formLayout.add("buttonContainer", buttonContainer);
			buttonContainer.setElementCssClass("o_sel_repo_save_details");
			uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
			FormSubmit submit = uifactory.addFormSubmitButton("submit", buttonContainer);
			submit.setVisible(!managed);
		}
	}
	
	private void initLifecycle(FormItemContainer formLayout) {
		String[] dateValues = new String[] {
				translate("cif.dates.none"),
				translate("cif.dates.private"),
				translate("cif.dates.public")	
		};
		dateTypesEl = uifactory.addRadiosVertical("cif.dates", formLayout, dateKeys, dateValues);
		dateTypesEl.setElementCssClass("o_sel_repo_lifecycle_type");
		if(repositoryEntry.getLifecycle() == null) {
			dateTypesEl.select("none", true);
		} else if(repositoryEntry.getLifecycle().isPrivateCycle()) {
			dateTypesEl.select("private", true);
		} else {
			dateTypesEl.select("public", true);
		}
		dateTypesEl.addActionListener(FormEvent.ONCHANGE);

		List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycle> filteredCycles = new ArrayList<>();
		//just make the upcomming and acutual running cycles or the pre-selected visible in the UI
		LocalDateTime now = LocalDateTime.now();
		for(RepositoryEntryLifecycle cycle:cycles) {
			if(cycle.getValidTo() == null
					|| now.isBefore(LocalDateTime.ofInstant(cycle.getValidTo().toInstant(), ZoneId.systemDefault()))
					|| (repositoryEntry.getLifecycle() != null && repositoryEntry.getLifecycle().equals(cycle))) {
				filteredCycles.add(cycle);
			}
		}
		
		String[] publicKeys = new String[filteredCycles.size()];
		String[] publicValues = new String[filteredCycles.size()];
		int count = 0;		
		for(RepositoryEntryLifecycle cycle:filteredCycles) {
				publicKeys[count] = cycle.getKey().toString();
				
				StringBuilder sb = new StringBuilder(32);
				boolean labelAvailable = StringHelper.containsNonWhitespace(cycle.getLabel());
				if(labelAvailable) {
					sb.append(cycle.getLabel());
				}
				if(StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
					if(labelAvailable) sb.append(" - ");
					sb.append(cycle.getSoftKey());
				}
				publicValues[count++] = sb.toString();
		}
		publicDatesEl = uifactory.addDropdownSingleselect("cif.public.dates", formLayout, publicKeys, publicValues, null);

		String privateDatePage = velocity_root + "/cycle_dates.html";
		privateDatesCont = FormLayoutContainer.createCustomFormLayout("private.date", getTranslator(), privateDatePage);
		privateDatesCont.setRootForm(mainForm);
		privateDatesCont.setLabel("cif.private.dates", null);
		formLayout.add("private.date", privateDatesCont);
		
		startDateEl = uifactory.addDateChooser("date.start", "cif.date.start", null, privateDatesCont);
		startDateEl.setElementCssClass("o_sel_repo_lifecycle_validfrom");
		endDateEl = uifactory.addDateChooser("date.end", "cif.date.end", null, privateDatesCont);
		endDateEl.setElementCssClass("o_sel_repo_lifecycle_validto");
		
		if(repositoryEntry.getLifecycle() != null) {
			RepositoryEntryLifecycle lifecycle = repositoryEntry.getLifecycle();
			if(lifecycle.isPrivateCycle()) {
				startDateEl.setDate(lifecycle.getValidFrom());
				endDateEl.setDate(lifecycle.getValidTo());
			} else {
				String key = lifecycle.getKey().toString();
				for(String publicKey:publicKeys) {
					if(key.equals(publicKey)) {
						publicDatesEl.select(key, true);
						break;
					}
				}
			}
		}

		updateDatesVisibility();
	}

	private void updateDatesVisibility() {
		if(dateTypesEl.isOneSelected()) {
			String type = dateTypesEl.getSelectedKey();
			if("none".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(false);
			} else if("public".equals(type)) {
				publicDatesEl.setVisible(true);
				privateDatesCont.setVisible(false);
			} else if("private".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(true);
			}
		}
	}

	@Override
	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateTextElement(location, 255);
		
		if (publicDatesEl != null) {
			publicDatesEl.clearError();
			if(publicDatesEl.isEnabled() && publicDatesEl.isVisible()) {
				if(!publicDatesEl.isOneSelected()) {
					publicDatesEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}	
			}
		}

		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength) {
		boolean ok;
		if(el == null) {
			ok = true;
		} else {
			String val = el.getValue();
			el.clearError();
			if(val != null && val.length() > maxLength) {
				el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
				ok = false;
			} else {
				ok = true;
			}
		}
		return ok;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateTypesEl) {
			updateDatesVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (!usedInWizard) {
			String type = "none";
			if(dateTypesEl.isOneSelected()) {
				type = dateTypesEl.getSelectedKey();
			}
			
			if("none".equals(type)) {
				repositoryEntry.setLifecycle(null);
			} else if("public".equals(type)) {
				String key = publicDatesEl.getSelectedKey();
				if(StringHelper.isLong(key)) {
					Long cycleKey = Long.parseLong(key);
					RepositoryEntryLifecycle cycle = lifecycleDao.loadById(cycleKey);
					repositoryEntry.setLifecycle(cycle);
				}
			} else if("private".equals(type)) {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				RepositoryEntryLifecycle cycle = repositoryEntry.getLifecycle();
				if(cycle == null || !cycle.isPrivateCycle()) {
					String softKey = "lf_" + repositoryEntry.getSoftkey();
					cycle = lifecycleDao.create(repositoryEntry.getDisplayname(), softKey, true, start, end);
				} else {
					cycle.setValidFrom(start);
					cycle.setValidTo(end);
					cycle = lifecycleDao.updateLifecycle(cycle);
				}
				repositoryEntry.setLifecycle(cycle);
			}
	
			String loc = location.getValue().trim();
			repositoryEntry.setLocation(loc);
	
			repositoryEntry = repositoryManager.setDescriptionAndName(repositoryEntry,
					repositoryEntry.getDisplayname(), repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(),
					repositoryEntry.getDescription(), repositoryEntry.getObjectives(), repositoryEntry.getRequirements(),
					repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(), repositoryEntry.getLocation(),
					repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), null, null,
					repositoryEntry.getEducationalType());
			if(repositoryEntry == null) {
				showWarning("repositoryentry.not.existing");
				fireEvent(ureq, Event.CLOSE_EVENT);
			} else {
				fireEvent(ureq, Event.CHANGED_EVENT);
				MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, getIdentity(), Change.modifiedDescription, "authoring");
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public boolean saveToContext(UserRequest ureq, CopyCourseContext context) {
		if (validateFormLogic(ureq)) {
			context.setLocation(location.getValue().trim());
			
			switch(dateTypesEl.getSelectedKey()) {
				case "none":
					context.setExecutionType(ExecutionType.none);
					break;
				case "public":
					context.setExecutionType(ExecutionType.semester);
					context.setSemesterKey(Long.parseLong(publicDatesEl.getSelectedKey()));
					break;
				case "private":
					context.setExecutionType(ExecutionType.beginAndEnd);
					context.setBeginDate(startDateEl.getDate());
					context.setEndDate(endDateEl.getDate());
					break;
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public void loadFromContext(CopyCourseContext context) {
		if (context.getLocation() != null) {
			location.setValue(context.getLocation());
		}
		
		if (context.getExecutionType() != null) {
			switch (context.getExecutionType()) {
				case none:
					dateTypesEl.select("none", true);
					break;
				case beginAndEnd: 
					dateTypesEl.select("private", true);
					break;
				case semester: 
					dateTypesEl.select("public", true);
					break;
			}
		}
		
		if (dateTypesEl.getSelectedKey() != null) {
			switch (dateTypesEl.getSelectedKey()) {
				case "private": 
					startDateEl.setDate(context.getBeginDate());
					endDateEl.setDate(context.getEndDate());
					break;
				case "public": 
					if (context.getSemesterKey() != null) {
						publicDatesEl.select(context.getSemesterKey().toString(), true);
					}
					break;
			}
		}
		
		updateDatesVisibility();
	}
}