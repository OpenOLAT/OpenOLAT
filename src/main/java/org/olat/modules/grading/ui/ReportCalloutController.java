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

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
	private FormLink customButton;
	private DateChooser fromDatesEl;
	private DateChooser toDatesEl;
	
	private Identity grader;
	private RepositoryEntry referenceEntry;
	
	public ReportCalloutController(UserRequest ureq, WindowControl wControl, RepositoryEntry referenceEntry, Identity grader) {
		super(ureq, wControl, "report_callout");
		this.grader = grader;
		this.referenceEntry = referenceEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		lastMonthLink = uifactory.addFormLink("report.last.month", formLayout, Link.LINK);
		lastMonthLink.setIconLeftCSS("o_icon o_icon_download");
		lastYearLink = uifactory.addFormLink("report.last.year", formLayout, Link.LINK);
		lastYearLink.setIconLeftCSS("o_icon o_icon_download");
		
		FormLayoutContainer customCont = FormLayoutContainer.createDefaultFormLayout("custom", getTranslator());
		formLayout.add(customCont);
		customCont.setRootForm(mainForm);
		
		fromDatesEl = uifactory.addDateChooser("from", "report.custom.dates.from", null, customCont);
		toDatesEl = uifactory.addDateChooser("to", "report.custom.dates.to", null, customCont);
		customButton = uifactory.addFormLink("report.custom", customCont, Link.BUTTON);
		customButton.setIconLeftCSS("o_icon o_icon_download");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(lastMonthLink == source) {
			doLastMonthReport(ureq);
		} else if(lastYearLink == source) {
			doLastYearReport(ureq);
		} else if(customButton == source) {
			doCustomReport(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCustomReport(UserRequest ureq) {
		Date start = fromDatesEl.getDate();
		Date end = toDatesEl.getDate();
		end = CalendarUtils.endOfDay(end);
		doReport(ureq, start, end);
	}
	
	private void doLastMonthReport(UserRequest ureq) {
		Calendar cal = Calendar.getInstance();
		Date end = CalendarUtils.endOfDay(cal.getTime());
		cal.add(Calendar.MONTH, -1);
		Date start = CalendarUtils.startOfDay(cal.getTime());
		doReport(ureq, start, end);
	}
	
	private void doLastYearReport(UserRequest ureq) {
		Calendar cal = Calendar.getInstance();
		Date end = CalendarUtils.endOfDay(cal.getTime());
		cal.add(Calendar.YEAR, -1);
		Date start = CalendarUtils.startOfDay(cal.getTime());
		doReport(ureq, start, end);
	}
	
	private void doReport(UserRequest ureq, Date start, Date end) {
		Identity manager = null;
		if(referenceEntry == null && grader == null) {
			manager = getIdentity();
		}
		String label = getLabel(start, end);
		Roles roles = ureq.getUserSession().getRoles();
		ReportResource resource = new ReportResource(roles, label, start, end, referenceEntry, grader, manager, getTranslator());
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
