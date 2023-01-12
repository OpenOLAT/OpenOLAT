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
package org.olat.course.wizard.ui;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.PDFCertificatesOptions;
import org.olat.course.certificate.ui.CertificateChooserController;
import org.olat.course.certificate.ui.CertificatesOptionsController;
import org.olat.course.wizard.CertificateDefaults;
import org.olat.course.wizard.CourseWizardService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;

/**
 * 
 * Initial date: 11 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CertificateController extends StepFormBasicController {

	public static final String RUN_CONTEXT_KEY = "certificate";
	
	private MultipleSelectionElement pdfCertificatesEl;
	private FormLayoutContainer configsCont;
	private FormLayoutContainer certificateCont;
	
	private StaticTextElement certificateTemplateEl;
	private TextElement certificationCustom1El;
	private TextElement certificationCustom2El;
	private TextElement certificationCustom3El;
	private FormLink selectTemplateLink;

	private CertificateChooserController certificateChooserCtrl;
	
	private final RepositoryEntry entry;
	private final CertificateDefaults context;

	private static final String[] pdfCertificatesOptionsKeys = new String[] {
		PDFCertificatesOptions.auto.name(),
		PDFCertificatesOptions.manual.name()
	};

	public CertificateController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, RepositoryEntry entry) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "certificate");
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CertificatesOptionsController.class, getLocale(), getTranslator()));
		this.entry = entry;
		context = (CertificateDefaults)getOrCreateFromRunContext(RUN_CONTEXT_KEY, CertificateDefaults::new);
		
		initForm(ureq);
		updateUI(ureq, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.certificate.title");
		
		configsCont = FormLayoutContainer.createDefaultFormLayout("configs", getTranslator());
		formLayout.add("configs", configsCont);
		configsCont.setRootForm(mainForm);
		
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		String[] pdfCertificatesOptionsValues = new String[] {
				translate("pdf.certificates.auto"),
				translate("pdf.certificates.manual")
		};
		pdfCertificatesEl = uifactory.addCheckboxesVertical("pdf.certificates", configsCont, pdfCertificatesOptionsKeys, pdfCertificatesOptionsValues, 1);
		pdfCertificatesEl.addActionListener(FormEvent.ONCHANGE);
		pdfCertificatesEl.setEnabled(!managedEff);
		pdfCertificatesEl.select(PDFCertificatesOptions.auto.name(), context.isAutomaticCertificationEnabled());
		pdfCertificatesEl.select(PDFCertificatesOptions.manual.name(), context.isManualCertificationEnabled());
		
		certificateCont = FormLayoutContainer.createDefaultFormLayout("certificate", getTranslator());
		formLayout.add("certificate", certificateCont);
		certificateCont.setRootForm(mainForm);
		
		certificateTemplateEl = uifactory.addStaticTextElement("pdf.certificates.template", "", certificateCont);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		certificateCont.add(buttonsCont);
		selectTemplateLink = uifactory.addFormLink("replace", buttonsCont, Link.BUTTON);
		
		certificationCustom1El = uifactory.addTextElement("certificate.custom1", 1000, context.getCertificateCustom1(), certificateCont);
		certificationCustom2El = uifactory.addTextElement("certificate.custom2", 2000, context.getCertificateCustom2(), certificateCont);
		certificationCustom3El = uifactory.addTextElement("certificate.custom3", 3000, context.getCertificateCustom3(), certificateCont);
	}

	private void updateUI(UserRequest ureq, boolean select) {
		if (select) {
			removeAsListenerAndDispose(certificateChooserCtrl);
			certificateChooserCtrl = new CertificateChooserController(ureq, getWindowControl(), context.getTemplate());
			listenTo(certificateChooserCtrl);
			flc.put("selection", certificateChooserCtrl.getInitialComponent());
			
			configsCont.setVisible(false);
			certificateCont.setVisible(false);
		} else {
			if (certificateChooserCtrl != null) {
				certificateChooserCtrl.getInitialComponent().setVisible(false);
			}
			configsCont.setVisible(true);
			String name = context.getTemplate() != null? context.getTemplate().getName(): translate("certificate.no.template");
			certificateTemplateEl.setValue(name);
			
			boolean certificateEnabled = pdfCertificatesEl.isAtLeastSelected(1);
			certificateCont.setVisible(certificateEnabled);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		certificateTemplateEl.clearError();
		if (pdfCertificatesEl.isAtLeastSelected(1) && context.getTemplate() == null) {
			certificateTemplateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == certificateChooserCtrl) {
			CertificateTemplate template = certificateChooserCtrl.getSelectedTemplate();
			context.setTemplate(template);
			updateUI(ureq, false);
			flc.setDirty(true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == selectTemplateLink) {
			updateUI(ureq, true);
		} else if(source == pdfCertificatesEl) {
			updateUI(ureq, false);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (!configsCont.isVisible()) {
			showInfo("error.select.template");
			return;
		}
		
		Collection<String> certificationOptions = pdfCertificatesEl.getSelectedKeys();
		context.setAutomaticCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.auto.name()));
		context.setManualCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.manual.name()));
		
		if (pdfCertificatesEl.isAtLeastSelected(1)) {
			context.setCertificateCustom1(certificationCustom1El.getValue());
			context.setCertificateCustom2(certificationCustom2El.getValue());
			context.setCertificateCustom3(certificationCustom3El.getValue());
		} else {
			context.setCertificateCustom1(null);
			context.setCertificateCustom2(null);
			context.setCertificateCustom3(null);
			context.setTemplate(null);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
