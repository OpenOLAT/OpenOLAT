/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.ims.qti21.resultexport.QTI21NewExportController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 12, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentExportController extends FormBasicController {
	
	private String defaultTitle;
	protected TextElement titleEl;
	private SingleSelection withPdfEl;
	
	protected final IdentitiesList identities;
	protected final CourseEnvironment courseEnv;
	protected final CourseNode courseNode;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected ExportManager exportManager;
	
	public AssessmentExportController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			CourseNode courseNode, IdentitiesList identities) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21NewExportController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(AssessmentExportController.class, getLocale(), getTranslator()));
		this.identities = identities;
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("export.title");
		
		String text;
		int numOfUsers = identities.getIdentities().size();
		if(numOfUsers <= 1) {
			text = translate("export.participants.num.singular", Integer.toString(numOfUsers));
		} else {
			text = translate("export.participants.num.plural", Integer.toString(numOfUsers));
		}
		if (identities.isAll()) {
			text = translate("export.participants.num.all", text);
		}
		
		uifactory.addStaticTextElement("export.participants", "export.participants.label", text, formLayout);
		
		defaultTitle = getDefaultTitle(ureq, false);
		titleEl = uifactory.addTextElement("export.title.label", 96, defaultTitle, formLayout);
		titleEl.setMandatory(true);
		
		SelectionValues withPdfValues = new SelectionValues();
		withPdfValues.add(new SelectionValue("wo", translate("export.export.standard"), translate("export.export.standard.desc")));
		withPdfValues.add(new SelectionValue("with", translate("export.export.advanced"), translate("export.export.advanced.desc")));
		withPdfEl = uifactory.addCardSingleSelectHorizontal("export.export", formLayout, withPdfValues.keys(), withPdfValues.values(), withPdfValues.descriptions(), null);
		withPdfEl.addActionListener(FormEvent.ONCHANGE);
		withPdfEl.setElementCssClass("o_radio_cards_lg");
		withPdfEl.setVisible(pdfModule.isEnabled());
		withPdfEl.select("wo", true);
		
		FormSubmit submitButton = uifactory.addFormSubmitButton("export.start", formLayout);
		submitButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(withPdfEl == source) {
			titleEl.setValue(getDefaultTitle(ureq, isWithPdfs()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (!identities.isEmpty()) {
			boolean withPdfs = isWithPdfs();
			OLATResource resource = courseEnv.getCourseGroupManager().getCourseResource();
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			String title = titleEl.getValue();
			String description = buildDescription();
			String filename = FileUtils.normalizeFilename(title) + ".zip";
			Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(ureq.getRequestTimestamp(), 10));
			
			doStartExport(resource, entry, title, description, filename, expirationDate, withPdfs);
		} else {
			showWarning("error.no.assessed.users");
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * @param resource  
	 * @param entry 
	 * @param title 
	 * @param description 
	 * @param filename 
	 * @param expirationDate 
	 * @param withPdfs 
	 */
	protected void doStartExport(OLATResource resource, RepositoryEntry entry, String title, String description,
			String filename, Date expirationDate, boolean withPdfs) {
		//
	}

	protected boolean isWithPdfs() {
		return withPdfEl.isVisible() && "with".equals(withPdfEl.getSelectedKey());
	}
	
	private String getDefaultTitle(UserRequest ureq, boolean withPdf) {
		String date = Formatter.getInstance(getLocale()).formatDate(ureq.getRequestTimestamp());
		int numOfUsers = identities.getIdentities().size();
		String[] args = { Integer.toString(numOfUsers), date };
		
		String title;
		if(withPdf) {
			if(identities.isAll()) {
				title = translate("export.title.pdf.all", args);
			} else if (numOfUsers <= 1) {
				title = translate("export.title.pdf.participant", args);
			} else {
				title = translate("export.title.pdf.participants", args);
			}
		} else if(identities.isAll()) {
			title = translate("export.title.all", args);
		} else if (numOfUsers <= 1) {
			title = translate("export.title.participant", args);
		} else {
			title = translate("export.title.participants", args);
		}
		return title;
	}
	
	protected String buildDescription() {
		int numOfIdentities = identities.getNumOfIdentities();
		StringBuilder filters = new StringBuilder(128);
		if(identities.getHumanReadableFiltersValues() != null) {
			for(String filter:identities.getHumanReadableFiltersValues()) {
				if(filters.length() > 0) {
					filters.append(", ");
				}
				filters.append(filter);
			}
		}
		
		StringBuilder participants = new StringBuilder(numOfIdentities + 32);
		for(Identity participant:identities.getIdentities()) {
			if(participants.length() > 0) {
				participants.append("; ");
			}
			participants.append(userManager.getUserDisplayName(participant));	
		}
		
		String[] args = {
				Integer.toString(numOfIdentities),
				filters.toString(),
				participants.toString()
			};

		String i18nKey;
		if(identities.getHumanReadableFiltersValues() != null && !identities.getHumanReadableFiltersValues().isEmpty()) {
			if(numOfIdentities <= 1) {
				i18nKey = "export.description.participant.filter";
			} else {
				i18nKey = "export.description.participants.filter";
			}
		} else {
			if(numOfIdentities <= 1) {
				i18nKey = "export.description.participant";
			} else {
				i18nKey = "export.description.participants";
			}
		}
		return translate(i18nKey, args);
	}
	
}
