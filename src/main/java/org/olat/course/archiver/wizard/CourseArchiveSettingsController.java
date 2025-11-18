/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.wizard;

import java.util.Collection;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.archiver.wizard.CourseArchiveContext.LogSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveSettingsController extends StepFormBasicController {
	
	private static final String ITEMCOLS = "itemcols";
	private static final String POINTCOL = "pointcol";
	private static final String TIMECOLS = "timecols";
	private static final String COMMENTCOL = "commentcol";
	private static final String CUSTOMIZE = "customize";
	private static final String STANDARD = "standard";
	private static final String RESULTS_WITH_PDF = "withpdf";
	private static final String RESULTS_WITHOUT_PDF = "withoutpdf";
	private static final String RESULTS_WITH_ESSAY_PDF = "withessaypdf";
	
	private SingleSelection logEl;
	private SingleSelection withPdfEl;
	private MultipleSelectionElement pdfOptionsEl;
	private SingleSelection customizeElementEl;
	private FormLayoutContainer qtiSettingsCont;
	private MultipleSelectionElement downloadOptionsEl;
	
	private final boolean administrator;
	private final boolean customization;
	private final CourseArchiveOptions archiveOptions;
	private final BulkCoursesArchivesContext bulkArchivesContext;
	
	@Autowired
	private PdfModule pdfModule;
	
	public CourseArchiveSettingsController(UserRequest ureq, WindowControl wControl,
			CourseArchiveContext archiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		bulkArchivesContext = null;
		customization = archiveContext.hasCustomization();
		administrator = archiveContext.isAdministrator();
		archiveOptions = archiveContext.getArchiveOptions();
		initForm(ureq);
	}
	
	public CourseArchiveSettingsController(UserRequest ureq, WindowControl wControl,
			BulkCoursesArchivesContext bulkArchiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		customization = true;
		bulkArchivesContext = bulkArchiveContext;
		administrator = ureq.getUserSession().getRoles()
				.hasSomeRoles(OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
		archiveOptions = bulkArchivesContext.getArchiveOptions();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer logSettingsCont = uifactory.addDefaultFormLayout("logCont", null, formLayout);
		
		SelectionValues typesPK = new SelectionValues();
		typesPK.add(SelectionValues.entry(LogSettings.ANONYMOUS.name(), translate("archive.log.anonymous"),
				translate("archive.log.anonymous.desc"), null, null, true));
		typesPK.add(SelectionValues.entry(LogSettings.PERSONALISED.name(), translate("archive.log.personalised"),
				translate("archive.log.personalised.desc"), null, null, true));
		logEl = uifactory.addCardSingleSelectHorizontal("archive.log", "archive.log", logSettingsCont, typesPK);
		logEl.setVisible(administrator && archiveOptions.getArchiveType() == ArchiveType.COMPLETE);
		if(archiveOptions.getLogSettings() != null) {
			logEl.select(archiveOptions.getLogSettings().name(), true);
		} else {
			logEl.select(LogSettings.ANONYMOUS.name(), true);
		}
		
		SelectionValues customizePK = new SelectionValues();
		customizePK.add(SelectionValues.entry(STANDARD, translate("customize.element.standard")));
		customizePK.add(SelectionValues.entry(CUSTOMIZE, translate("customize.element.customize")));
		customizeElementEl = uifactory.addRadiosVertical("customize.element", "customize.element", logSettingsCont,
				customizePK.keys(), customizePK.values());
		customizeElementEl.addActionListener(FormEvent.ONCHANGE);
		customizeElementEl.setVisible(customization);
		if(archiveOptions.isCustomize()) {
			customizeElementEl.select(CUSTOMIZE, true);
		} else {
			customizeElementEl.select(STANDARD, true);
		}
		
		// Custom QTI settings
		qtiSettingsCont = uifactory.addDefaultFormLayout("qtiCont", null, formLayout);
		qtiSettingsCont.setFormTitle(translate("customize.qti.settings"));
		
		SelectionValues qtiOptionPK = new SelectionValues();
		qtiOptionPK.add(SelectionValues.entry(ITEMCOLS, translate("form.itemcols")));
		qtiOptionPK.add(SelectionValues.entry(POINTCOL, translate("form.pointcol")));
		qtiOptionPK.add(SelectionValues.entry(TIMECOLS, translate("form.timecols")));
		qtiOptionPK.add(SelectionValues.entry(COMMENTCOL, translate("form.commentcol")));
		downloadOptionsEl = uifactory.addCheckboxesVertical("setting", "form.title", qtiSettingsCont, qtiOptionPK.keys(), qtiOptionPK.values(), 1);
		downloadOptionsEl.select(ITEMCOLS, archiveOptions.isItemColumns());
		downloadOptionsEl.select(POINTCOL, archiveOptions.isPointColumn());
		downloadOptionsEl.select(TIMECOLS, archiveOptions.isTimeColumns());
		downloadOptionsEl.select(COMMENTCOL, archiveOptions.isCommentColumn());
		
		SelectionValues withPdfValues = new SelectionValues();
		withPdfValues.add(new SelectionValue(RESULTS_WITHOUT_PDF, translate("export.export.standard"), translate("export.export.standard.desc")));
		withPdfValues.add(new SelectionValue(RESULTS_WITH_PDF, translate("export.export.advanced"), translate("export.export.advanced.desc")));
		withPdfEl = uifactory.addCardSingleSelectHorizontal("export.export", qtiSettingsCont, withPdfValues.keys(), withPdfValues.values(), withPdfValues.descriptions(), null);
		withPdfEl.addActionListener(FormEvent.ONCHANGE);
		withPdfEl.setVisible(pdfModule.isEnabled());
		if(archiveOptions.isResultsWithPDFs()) {
			withPdfEl.select(RESULTS_WITH_PDF, true);
		} else {
			withPdfEl.select(RESULTS_WITHOUT_PDF, true);
		}
		
		SelectionValues pdfOptionsValues = new SelectionValues();
			pdfOptionsValues.add(new SelectionValue(RESULTS_WITH_ESSAY_PDF, translate("export.options.essay.pdf")));
		pdfOptionsEl = uifactory.addCheckboxesVertical("export.options", qtiSettingsCont, pdfOptionsValues.keys(), pdfOptionsValues.values(), 1);
		
		updateUI();
	}
	
	private void updateUI() {
		qtiSettingsCont.setVisible(customization && customizeElementEl.isVisible()
				&& customizeElementEl.isOneSelected() && CUSTOMIZE.equals(customizeElementEl.getSelectedKey()));
		pdfOptionsEl.setVisible(withPdfEl.isVisible() && withPdfEl.isOneSelected() && withPdfEl.isKeySelected(RESULTS_WITH_PDF));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		logEl.clearError();
		if(!logEl.isOneSelected()) {
			logEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		withPdfEl.clearError();
		if(qtiSettingsCont.isVisible() && withPdfEl.isVisible() &&  !withPdfEl.isOneSelected()) {
			withPdfEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == customizeElementEl) {
			if(customizeElementEl.isOneSelected() && CUSTOMIZE.equals(customizeElementEl.getSelectedKey())) {
				// Set defaults
				downloadOptionsEl.select(ITEMCOLS, archiveOptions.isItemColumns());
				downloadOptionsEl.select(POINTCOL, archiveOptions.isPointColumn());
				downloadOptionsEl.select(TIMECOLS, archiveOptions.isTimeColumns());
				downloadOptionsEl.select(COMMENTCOL, archiveOptions.isCommentColumn());
			}
			updateUI();
		} else if(withPdfEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(logEl.isVisible()) {
			LogSettings logSetting = LogSettings.valueOf(logEl.getSelectedKey());
			archiveOptions.setLogSettings(logSetting);
			archiveOptions.setLogFiles(true);
			archiveOptions.setLogFilesAuthors(true);
			archiveOptions.setLogFilesUsers(logSetting == LogSettings.PERSONALISED);	
			archiveOptions.setLogFilesStatistics(true);
			archiveOptions.setCourseChat(true);
			archiveOptions.setCourseResults(true);
		}
		
		if(customizeElementEl.isVisible() && customizeElementEl.isOneSelected()
				&& CUSTOMIZE.equals(customizeElementEl.getSelectedKey())) {
			Collection<String> options = downloadOptionsEl.getSelectedKeys();
			archiveOptions.setItemColumns(options.contains(ITEMCOLS));
			archiveOptions.setPointColumn(options.contains(POINTCOL));
			archiveOptions.setTimeColumns(options.contains(TIMECOLS));
			archiveOptions.setCommentColumn(options.contains(COMMENTCOL));
			
			boolean withPDFs = withPdfEl.isOneSelected() && RESULTS_WITH_PDF.equals(withPdfEl.getSelectedKey());
			archiveOptions.setCustomize(true);
			archiveOptions.setResultsWithPDFs(withPDFs);
			boolean withEssayPdfs = pdfOptionsEl.isVisible() && pdfOptionsEl.isKeySelected(RESULTS_WITH_ESSAY_PDF);
			archiveOptions.setWithEssayPdfs(withEssayPdfs);
		} else {
			archiveOptions.setItemColumns(true);
			archiveOptions.setPointColumn(true);
			archiveOptions.setTimeColumns(true);
			archiveOptions.setCommentColumn(true);
			archiveOptions.setCustomize(false);
			archiveOptions.setResultsWithPDFs(false);
			archiveOptions.setWithEssayPdfs(false);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
