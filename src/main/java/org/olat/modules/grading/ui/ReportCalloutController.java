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

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 7 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReportCalloutController extends FormBasicController {
	
	private FormLink lastMonthLink;
	private FormLink lastYearLink;
	private FormLink exportButton;
	private DateChooser dateRangeEl;
	private FormToggle closedAssignmentEl;
	private FormLayoutContainer predefinedCont;
	
	private Identity grader;
	private RepositoryEntry referenceEntry;
	
	public ReportCalloutController(UserRequest ureq, WindowControl wControl, RepositoryEntry referenceEntry, Identity grader) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.grader = grader;
		this.referenceEntry = referenceEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		closedAssignmentEl = uifactory.addToggleButton("completed.order", "completed.order", translate("on"), translate("off"), formLayout);
		closedAssignmentEl.toggle(true);
		
		predefinedCont = uifactory.addInlineFormLayout("predefined.ranges", "predefined.ranges", formLayout);
		lastMonthLink = uifactory.addFormLink("report.last.month", predefinedCont, Link.BUTTON_XSMALL);
		lastYearLink = uifactory.addFormLink("report.last.year", predefinedCont, Link.BUTTON_XSMALL);
		
		dateRangeEl = uifactory.addDateChooser("report.close", "report.close", null, formLayout);
		dateRangeEl.setElementCssClass("o_date_scope_range");
		dateRangeEl.setSeparator("to.separator");
		dateRangeEl.setMandatory(true);
		dateRangeEl.setSecondDate(true);

		FormLayoutContainer buttonsCont = uifactory.addInlineFormLayout("buttons", null, formLayout);
		exportButton = uifactory.addFormLink("export", buttonsCont, Link.BUTTON);
		exportButton.setIconLeftCSS("o_icon o_icon_download");
		exportButton.setPrimary(true);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		boolean onlyCompleteAssignment = closedAssignmentEl.isOn();
		predefinedCont.setVisible(onlyCompleteAssignment);
		lastMonthLink.setVisible(onlyCompleteAssignment);
		lastYearLink.setVisible(onlyCompleteAssignment);
		dateRangeEl.setVisible(onlyCompleteAssignment);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(lastMonthLink == source) {
			doLastMonthRange();
		} else if(lastYearLink == source) {
			doLastYearRange();
		} else if(closedAssignmentEl == source) {
			updateUI();
		} else if(exportButton == source) {
			if(validateFormLogic(ureq)) {
				doReport(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(closedAssignmentEl.isOn()) {
			if(dateRangeEl.getDate() == null || dateRangeEl.getSecondDate() == null) {
				dateRangeEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	private void doLastMonthRange() {
		Calendar cal = Calendar.getInstance();
		dateRangeEl.setSecondDate(cal.getTime());
		cal.add(Calendar.MONTH, -1);
		dateRangeEl.setDate(cal.getTime());
	}
	
	private void doLastYearRange() {
		Calendar cal = Calendar.getInstance();
		dateRangeEl.setSecondDate(cal.getTime());
		cal.add(Calendar.YEAR, -1);
		dateRangeEl.setDate(cal.getTime());
	}
	
	private void doReport(UserRequest ureq) {
		Identity manager = null;
		if(referenceEntry == null && grader == null) {
			manager = getIdentity();
		}
		
		Date start = dateRangeEl.getDate();
		Date end = dateRangeEl.getSecondDate();
		Date endInclusive = end == null
				? null
				: DateUtils.addDays(end, 1);// full day inclusive
		boolean onlyClosedAssignments = closedAssignmentEl.isOn();

		String label = getLabel(start, end);
		Roles roles = ureq.getUserSession().getRoles();
		ReportResource resource = new ReportResource(roles, label, start, endInclusive, onlyClosedAssignments, referenceEntry,
				grader, manager, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(resource);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private String getLabel(Date start, Date end) {
		StringBuilder sb = new StringBuilder();
		sb.append("Report");
		if(referenceEntry != null) {
			sb.append("_")
			  .append(StringHelper.transformDisplayNameToFileSystemName(referenceEntry.getDisplayname()));
		}
		
		if(grader != null) {
			String firstName = grader.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(firstName)) {
				sb.append("_").append(StringHelper.transformDisplayNameToFileSystemName(firstName));
			}
			String lastName = grader.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(lastName)) {
				sb.append("_").append(StringHelper.transformDisplayNameToFileSystemName(lastName));
			}
		}
		if(start != null) {
			sb.append("_").append(Formatter.formatDateFilesystemSave(start));
		}
		if(end != null) {
			sb.append("_").append(Formatter.formatDateFilesystemSave(end));
		}
		sb.append(".xlsx");
		return sb.toString();
	}
}
