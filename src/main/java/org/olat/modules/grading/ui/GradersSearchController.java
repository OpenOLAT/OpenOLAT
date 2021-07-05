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
package org.olat.modules.grading.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.ui.component.IdentityComparator;
import org.olat.modules.grading.ui.component.RepositoryEntryComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradersSearchController extends FormBasicController {

	private FormLink searchButton;
	private SingleSelection gradersEl;
	private DateChooser gradingDatesEl;
	private MultipleSelectionElement statusEl;
	private SingleSelection referenceEntriesEl;

	private List<Identity> graders;
	private List<RepositoryEntry> referenceEntries;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	
	public GradersSearchController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "graders_search", rootForm);
		
		initForm(ureq);
		loadSearchLists();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initRightForm(formLayout);
		initLeftForm(formLayout);

		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.setElementCssClass("btn-primary");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void initRightForm(FormItemContainer formLayout) {
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right", getTranslator());
		formLayout.add(rightContainer);
		rightContainer.setRootForm(mainForm);
		
		SelectionValues statusKeys = new SelectionValues();
		for(GraderStatus status: GraderStatus.values()) {
			statusKeys.add(SelectionValues.entry(status.name(), translate("search.grader.status.".concat(status.name()))));
		}
		statusEl = uifactory.addCheckboxesDropdown("status", "search.grader.status", rightContainer, statusKeys.keys(), statusKeys.values());
		
		referenceEntriesEl = uifactory.addDropdownSingleselect("search.reference.entries", rightContainer,
				new String[0], new String[0]);
	}
	
	private void initLeftForm(FormItemContainer formLayout) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left", getTranslator());
		formLayout.add(leftContainer);
		leftContainer.setRootForm(mainForm);
		
		gradersEl = uifactory.addDropdownSingleselect("search.graders", leftContainer,
				new String[0], new String[0]);
		
		gradingDatesEl = uifactory.addDateChooser("gradingDate", "search.grading.dates", null, leftContainer);
		gradingDatesEl.setSecondDate(true);
		gradingDatesEl.setSeparator("search.grading.dates.sep");
	}
	
	private void loadSearchLists() {
		referenceEntries = gradingService.getReferenceRepositoryEntriesWithGrading(getIdentity());
		if(referenceEntries != null && !referenceEntries.isEmpty()) {
			Collections.sort(referenceEntries, new RepositoryEntryComparator(getLocale()));
			
			SelectionValues entriesKeyValues = new SelectionValues();
			entriesKeyValues.add(SelectionValues.entry("all", translate("show.all")));
			referenceEntries.forEach(entry
					-> entriesKeyValues.add(SelectionValues.entry(entry.getKey().toString(), entry.getDisplayname())));
			referenceEntriesEl.setKeysAndValues(entriesKeyValues.keys(), entriesKeyValues.values(), null);
		}
		
		graders = gradingService.getGraders(getIdentity());
		if(graders != null && !graders.isEmpty()) {
			Collections.sort(graders, new IdentityComparator());
			
			SelectionValues gradersKeyValues = new SelectionValues();
			gradersKeyValues.add(SelectionValues.entry("all", translate("show.all")));
			graders.forEach(identity
					-> gradersKeyValues.add(SelectionValues.entry(identity.getKey().toString(), userManager.getUserDisplayName(identity))));
			gradersEl.setKeysAndValues(gradersKeyValues.keys(), gradersKeyValues.values(), null);
		}
	}
	
	public Date getGradingFrom() {
		return gradingDatesEl.getDate();
	}
	
	public Date getGradingTo() {
		Date to = gradingDatesEl.getSecondDate();
		return CalendarUtils.endOfDay(to);
	}
	
	public List<GraderStatus> getGraderStatus() {
		List<GraderStatus> statusList = new ArrayList<>();
		if(statusEl.isAtLeastSelected(1)) {
			Collection<String> selectedKeys = statusEl.getSelectedKeys();
			for(String selectedKey:selectedKeys) {
				statusList.add(GraderStatus.valueOf(selectedKey));
			}
		}
		return statusList;
	}
	
	public Identity getGrader() {
		if(graders != null && gradersEl.isOneSelected()
				&& StringHelper.isLong(gradersEl.getSelectedKey())) {
			Long graderKey = Long.valueOf(gradersEl.getSelectedKey());
			return graders.stream().filter(identity -> graderKey.equals(identity.getKey()))
					.findFirst().orElse(null);
		}
		return null;
	}
	
	public RepositoryEntry getReferenceEntry() {
		if(referenceEntries != null && referenceEntriesEl.isOneSelected()
				&& StringHelper.isLong(referenceEntriesEl.getSelectedKey())) {
			Long entryKey = Long.valueOf(referenceEntriesEl.getSelectedKey());
			return referenceEntries.stream().filter(entry -> entry.getKey().equals(entryKey))
					.findFirst().orElse(null);
		}
		return null;
	}

	@Override
	protected void doDispose() {
		//
	}
	@Override
	protected void formOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(searchButton == source) {
			doSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		gradingDatesEl.setDate(null);
		gradingDatesEl.setSecondDate(null);
		statusEl.uncheckAll();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSearch(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
