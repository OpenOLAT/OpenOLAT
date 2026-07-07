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
package org.olat.modules.certificationprogram.ui;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.SerialNumberFormat;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.course.certificate.ui.PreviewMediaResource;
import org.olat.course.certificate.ui.UploadCertificateTemplateController;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.manager.CertificationProgramXStream;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCertificationProgramCertificateController extends FormBasicController {
	
	private static final String DEFAULT_KEY = "def";
	private static final String SYSTEM_KEY = "system";
	private static final String CUSTOM_KEY = "custom";
	
	private TextElement certificationCustom1El;
	private TextElement certificationCustom2El;
	private TextElement certificationCustom3El;

	private FormLink uploadTemplateLink;
	private SingleSelection templateTypeEl;
	private SingleSelection systemTemplatesEl;
	private TextElement templateFileEl;
	private Link previewTemplateLink;
	private FormLayoutContainer templateCont;
	
	private FormToggle withPrintTemplateEl;
	private FormLink uploadPrintTemplateLink;
	private SingleSelection printTemplateTypeEl;
	private SingleSelection systemPrintTemplatesEl;
	private TextElement templatePrintFileEl;
	private Link previewPrintTemplateLink;
	private FormLayoutContainer printTemplateCont;

	private FormToggle serialNumberEl;
	private TextElement nextValueEl;
	private TextElement serialNumberStartEl;
	private TextElement serialNumberFormatEl;
	private StaticTextElement serialNumberFormatInfosEl;
	private FormLayoutContainer serialCont;

	private final boolean editable;
	private CertificateTemplate uploadedTemplate;
	private CertificateTemplate uploadedPrintTemplate;
	private CertificationProgram certificationProgram;
	private final List<CertificateTemplate> templates;
	
	private CloseableModalController cmc;
	private UploadCertificateTemplateController certificateUploadCtrl;
	private UploadCertificateTemplateController printCertificateUploadCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCertificationProgramCertificateController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl);
		editable = secCallback.canEditCertificationProgram();
		this.certificationProgram = certificationProgram;

		templates = certificatesManager.getTemplates();
		
		initForm(ureq);
		updateUI();
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("certification.program.certificate");
		
		initTemplateForm(formLayout);
		initPrintTemplateForm(formLayout);
		
		uifactory.addSpacerElement("variables-spacer", formLayout, false);
		
		certificationCustom1El = uifactory.addTextElement("certificate.custom1", 1000, certificationProgram.getCertificateCustom1(), formLayout);
		certificationCustom1El.setEnabled(editable);
		certificationCustom2El = uifactory.addTextElement("certificate.custom2", 2000, certificationProgram.getCertificateCustom2(), formLayout);
		certificationCustom2El.setEnabled(editable);
		certificationCustom3El = uifactory.addTextElement("certificate.custom3", 3000, certificationProgram.getCertificateCustom3(), formLayout);
		certificationCustom3El.setEnabled(editable);
		
		initSerialNumber(formLayout);
		
		if(editable) {
			FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private void initTemplateForm(FormItemContainer formLayout) {
		String templatePage = velocity_root + "/select_certificate.html";
		templateCont = uifactory.addCustomFormLayout("template.cont", null, templatePage, formLayout);
		templateCont.setLabel("certificate.pdf.template", null);
		
		CertificateTemplate template = certificationProgram.getTemplate();
		
		SelectionValues templateTypesPK = new SelectionValues();
		templateTypesPK.add(SelectionValues.entry(SYSTEM_KEY, translate("select.system.template")));
		templateTypesPK.add(SelectionValues.entry(CUSTOM_KEY, translate("select.custom.template")));
		templateTypeEl = uifactory.addButtonGroupSingleSelectHorizontal("template.type", templateCont, templateTypesPK);
		templateTypeEl.addActionListener(FormEvent.ONCHANGE);
		templateTypeEl.setElementCssClass("o_button_group_vertical");
		templateTypeEl.setEnabled(editable);
		if(template != null && !template.isPublicTemplate()) {
			templateTypeEl.select(CUSTOM_KEY, true);
		} else {
			templateTypeEl.select(SYSTEM_KEY, true);
		}

		String templateName = template == null || template.isPublicTemplate()
				? null
				: template.getName();
		templateFileEl = uifactory.addTextElement("template.file", 128, templateName, templateCont);
		templateFileEl.setDomReplacementWrapperRequired(false);
		templateFileEl.setEnabled(false);
		templateFileEl.setVisible(false);
		
		SelectionValues templatesPK = new SelectionValues();
		templatesPK.add(SelectionValues.entry(DEFAULT_KEY, "Default"));
		for(CertificateTemplate t:templates) {
			templatesPK.add(SelectionValues.entry(t.getKey().toString(), t.getName()));
		}
		systemTemplatesEl = uifactory.addDropdownSingleselect("public.templates", templateCont, templatesPK.keys(), templatesPK.values(), null);
		systemTemplatesEl.setDomReplacementWrapperRequired(false);
		if(template == null) {
			systemTemplatesEl.select(DEFAULT_KEY, true);	
		} else if(template != null && template.isPublicTemplate()) {
			systemTemplatesEl.select(template.getKey().toString(), true);
		} else {
			systemTemplatesEl.setVisible(false);
			templateFileEl.setVisible(true);
		}
		
		uploadTemplateLink = uifactory.addFormLink("upload", "upload", null, templateCont, Link.BUTTON);
		uploadTemplateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		uploadTemplateLink.setElementCssClass("input-group-addon");
		uploadTemplateLink.setDomReplacementWrapperRequired(false);
		
		previewTemplateLink = LinkFactory.createButton("preview", templateCont.getFormItemComponent(), this);
		previewTemplateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		previewTemplateLink.setElementCssClass("input-group-addon o_certificate_preview_button");
		previewTemplateLink.setTarget("preview");
	}
	
	private void initPrintTemplateForm(FormItemContainer formLayout) {
		withPrintTemplateEl = uifactory.addToggleButton("certificate.print.template.enable", "certificate.print.template.enable", translate("on"), translate("off"), formLayout);
		withPrintTemplateEl.toggle(certificationProgram.isPrintTemplateEnabled());
		
		String templatePage = velocity_root + "/select_print_certificate.html";
		printTemplateCont = uifactory.addCustomFormLayout("print.template.cont", null, templatePage, formLayout);
		printTemplateCont.setLabel("certificate.pdf.print.template", null);
		
		CertificateTemplate printTemplate = certificationProgram.getPrintTemplate();
		
		SelectionValues templateTypesPK = new SelectionValues();
		templateTypesPK.add(SelectionValues.entry(SYSTEM_KEY, translate("select.system.template")));
		templateTypesPK.add(SelectionValues.entry(CUSTOM_KEY, translate("select.custom.template")));
		printTemplateTypeEl = uifactory.addButtonGroupSingleSelectHorizontal("print.template.type", printTemplateCont, templateTypesPK);
		printTemplateTypeEl.addActionListener(FormEvent.ONCHANGE);
		printTemplateTypeEl.setElementCssClass("o_button_group_vertical");
		printTemplateTypeEl.setEnabled(editable);
		if(printTemplate != null && !printTemplate.isPublicTemplate()) {
			printTemplateTypeEl.select(CUSTOM_KEY, true);
		} else {
			printTemplateTypeEl.select(SYSTEM_KEY, true);
		}
	
		String templateName = printTemplate == null || printTemplate.isPublicTemplate()
				? null
				: printTemplate.getName();
		templatePrintFileEl = uifactory.addTextElement("print.template.file", null, 128, templateName, printTemplateCont);
		templatePrintFileEl.setDomReplacementWrapperRequired(false);
		templatePrintFileEl.setElementCssClass("input");
		templatePrintFileEl.setEnabled(false);
		templatePrintFileEl.setVisible(false);
		
		SelectionValues templatesPK = new SelectionValues();
		templatesPK.add(SelectionValues.entry(DEFAULT_KEY, "Default"));
		for(CertificateTemplate t:templates) {
			templatesPK.add(SelectionValues.entry(t.getKey().toString(), t.getName()));
		}
		systemPrintTemplatesEl = uifactory.addDropdownSingleselect("print.public.templates", "public.templates", printTemplateCont, templatesPK.keys(), templatesPK.values(), null);
		systemPrintTemplatesEl.setDomReplacementWrapperRequired(false);
		systemPrintTemplatesEl.setElementCssClass("input");
		if(printTemplate == null) {
			systemPrintTemplatesEl.select(DEFAULT_KEY, true);	
		} else if(printTemplate != null && printTemplate.isPublicTemplate()) {
			systemPrintTemplatesEl.select(printTemplate.getKey().toString(), true);
		} else {
			systemPrintTemplatesEl.setVisible(false);
			templatePrintFileEl.setVisible(true);
		}
		
		uploadPrintTemplateLink = uifactory.addFormLink("print.upload", "upload", null, printTemplateCont, Link.BUTTON);
		uploadPrintTemplateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		uploadPrintTemplateLink.setElementCssClass("input-group-addon");
		uploadPrintTemplateLink.setDomReplacementWrapperRequired(false);
		
		previewPrintTemplateLink = LinkFactory.createButton("print.preview", "preview", printTemplateCont.getFormItemComponent(), this);
		previewPrintTemplateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		previewPrintTemplateLink.setElementCssClass("input-group-addon o_certificate_preview_button");
		previewPrintTemplateLink.setTarget("preview");
	}
	
	private void initSerialNumber(FormItemContainer formLayout) {
		serialCont = uifactory.addDefaultFormLayout("serial", null, formLayout);
		serialCont.setFormTitle(translate("serial.number.title"));
		serialCont.setFormInfo(translate("serial.number.descr"));
		serialCont.setFormLayout("nolayout");
		
		serialNumberEl = uifactory.addToggleButton("serial.number.enabled", "serial.number.enabled", translate("on"), translate("off"), serialCont);
		serialNumberEl.toggle(certificationProgram.isSerialNumberEnabled());
		
		String text = "<div class='o_info_with_icon'>" + translate("serial.number.format.infos", new String[] { "${counter}", "${counter:N}", "${year}, ${month}, ${day}"}) + "</span>";
		serialNumberFormatInfosEl = uifactory.addStaticTextElement("format.infos", null, text, serialCont);
		serialNumberFormatInfosEl.setDomWrapperElement(DomWrapperElement.div);
		
		String format = certificationProgram.getSerialNumberFormat();
		serialNumberFormatEl = uifactory.addTextElement("serial.number.format", "serial.number.format", 255, format, serialCont);
		serialNumberFormatEl.setExampleKey("serial.number.format.hint", new String[] { "${year}", "${counter:5}"});
		serialNumberFormatEl.setMandatory(true);

		long startNumber = certificationProgram.getSerialNumberStartNumber();
		long currentCounter = certificationProgram.getSerialNumberCounter();
		serialNumberStartEl = uifactory.addTextElement("serial.number.counter", "serial.number.counter", 255, Long.toString(startNumber), serialCont);
		serialNumberStartEl.setExampleKey("serial.number.counter.hint", new String[] { Long.toString(currentCounter) });
		serialNumberStartEl.setElementCssClass("form-inline");
		serialNumberStartEl.setDisplaySize(8);
		serialNumberStartEl.setMaxLength(8);
		serialNumberStartEl.setMandatory(true);
		
		nextValueEl = uifactory.addTextElement("serial.number.next", "serial.number.next", 255, "", serialCont);
		nextValueEl.setElementCssClass("o_certificate_next_value");
		nextValueEl.setEnabled(false);
	}
	
	private void updateUI() {
		boolean systemTemplate = templateTypeEl.isOneSelected() && SYSTEM_KEY.equals(templateTypeEl.getSelectedKey());
		systemTemplatesEl.setVisible(systemTemplate);
		templateFileEl.setVisible(!systemTemplate);	
		uploadTemplateLink.setVisible(!systemTemplate);
		
		CertificateTemplate selectedtemplate = getSelectedTemplate();
		previewTemplateLink.setEnabled(systemTemplate || selectedtemplate != null);
		
		boolean printEnabled = withPrintTemplateEl.isOn();
		uploadPrintTemplateLink.setVisible(printEnabled);
		printTemplateTypeEl.setVisible(printEnabled);
		systemPrintTemplatesEl.setVisible(printEnabled);
		templatePrintFileEl.setVisible(printEnabled);
		previewPrintTemplateLink.setVisible(printEnabled);
		printTemplateCont.setVisible(printEnabled);
		
		if(printEnabled) {
			boolean systemPrintTemplate = printTemplateTypeEl.isOneSelected() && SYSTEM_KEY.equals(printTemplateTypeEl.getSelectedKey());
			systemPrintTemplatesEl.setVisible(systemPrintTemplate);
			templatePrintFileEl.setVisible(!systemPrintTemplate);
			uploadPrintTemplateLink.setVisible(!systemPrintTemplate);	
			
			CertificateTemplate selectedPrintTemplate = getSelectedPrintTemplate();
			previewPrintTemplateLink.setEnabled(systemPrintTemplate || selectedPrintTemplate != null);
		}
		
		boolean serialNumberEnabled = serialNumberEl.isOn();
		serialNumberFormatEl.setVisible(serialNumberEnabled);
		serialNumberStartEl.setVisible(serialNumberEnabled);
		nextValueEl.setVisible(serialNumberEnabled);
		serialNumberFormatInfosEl.setVisible(serialNumberEnabled);
		
		updateNextValue();
	}
	
	private void updateNextValue() {
		long currentCounter = certificationProgram.getSerialNumberCounter();
		if(certificationProgram.getSerialNumberStartNumber() > currentCounter) {
			currentCounter = certificationProgram.getSerialNumberStartNumber();
		} else {
			currentCounter++;
		}
		String format = serialNumberFormatEl.getValue();
		String nextValue = SerialNumberFormat.parse(format).generate(currentCounter, LocalDate.now());
		nextValueEl.setValue(nextValue);

		serialNumberStartEl.setExampleKey("serial.number.counter.hint",
				new String[] { Long.toString(certificationProgram.getSerialNumberCounter()) });
	}
	
	private CertificateTemplate getSelectedTemplate() {
		return getSelected(templateTypeEl, systemTemplatesEl, certificationProgram.getTemplate(), uploadedTemplate);
	}
	
	private CertificateTemplate getSelectedPrintTemplate() {
		return getSelected(printTemplateTypeEl, systemPrintTemplatesEl,  certificationProgram.getPrintTemplate(), uploadedPrintTemplate);
	}
	
	private CertificateTemplate getSelected(SingleSelection typeEl, SingleSelection systemEl, CertificateTemplate current, CertificateTemplate uploaded) {
		if(typeEl.isOneSelected() && SYSTEM_KEY.equals(typeEl.getSelectedKey())) {
			String selectedKey = systemEl.isOneSelected()
					? systemEl.getSelectedKey()
					: DEFAULT_KEY;
			if(DEFAULT_KEY.equals(selectedKey)) {
				return null;
			}
			return templates.stream()
					.filter(template -> selectedKey.equals(template.getKey().toString()))
					.findFirst().orElse(null);
		} else if(typeEl.isOneSelected() && CUSTOM_KEY.equals(typeEl.getSelectedKey())) {
			if(uploaded != null) {
				return uploaded;
			}
			if(current != null && !current.isPublicTemplate()) {
				return current;
			}	
		}
		return null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == previewTemplateLink) {
			doPreviewTemplate(ureq);
		} else if(source == previewPrintTemplateLink) {
			doPreviewPrintTemplate(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == certificateUploadCtrl) {
			if(event == Event.DONE_EVENT) {
				doSetTemplate(certificateUploadCtrl.getTemplate());
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == printCertificateUploadCtrl) {
			if(event == Event.DONE_EVENT) {
				doSetPrintTemplate(printCertificateUploadCtrl.getTemplate());
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(printCertificateUploadCtrl);
		removeAsListenerAndDispose(certificateUploadCtrl);
		removeAsListenerAndDispose(cmc);
		printCertificateUploadCtrl = null;
		certificateUploadCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(withPrintTemplateEl == source || templateTypeEl == source || printTemplateTypeEl == source
				|| serialNumberEl == source) {
			updateUI();
		} else if(uploadTemplateLink == source) {
			doUploadTemplate(ureq);
		} else if(uploadPrintTemplateLink == source) {
			doUploadPrintTemplate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		CertificateTemplate selectedTemplate = getSelectedTemplate();
		allOk &= validateFormLogic(templateCont, templateTypeEl, systemTemplatesEl,  selectedTemplate);

		printTemplateCont.clearError();
		if(withPrintTemplateEl.isOn()) {
			CertificateTemplate selectedPrintTemplate = getSelectedPrintTemplate();
			allOk &= validateFormLogic(printTemplateCont, printTemplateTypeEl, systemPrintTemplatesEl,  selectedPrintTemplate);
		}
		
		if(serialNumberEl.isOn()) {
			serialNumberFormatEl.clearError();
			if(!StringHelper.containsNonWhitespace(serialNumberFormatEl.getValue())) {
				serialNumberFormatEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!validateFormat()) {
				allOk &= false;
			}
			
			serialNumberStartEl.clearError();
			if(!StringHelper.containsNonWhitespace(serialNumberStartEl.getValue())) {
				serialNumberStartEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!StringHelper.isLong(serialNumberStartEl.getValue())) {
				serialNumberStartEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		}

		return allOk;
	}
	
	private boolean validateFormat() {
		boolean allOk = true;
		
		try {
			if(!SerialNumberFormat.parse(serialNumberFormatEl.getValue()).hasCounter()) {
				serialNumberFormatEl.setErrorKey("error.counter.mandatory");
				allOk &= false;
			} else if(SerialNumberFormat.parse(serialNumberFormatEl.getValue()).maxPad() > 16) {
				serialNumberFormatEl.setErrorKey("error.counter.number.format");
				allOk &= false;
			}
		} catch (NumberFormatException e) {
			serialNumberFormatEl.setErrorKey("error.counter.number.format");
			allOk &= false;
		} catch (Exception e) {
			serialNumberFormatEl.setErrorKey("error.counter");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateFormLogic(FormLayoutContainer container, SingleSelection typeEl,
			SingleSelection systemTemplateEl, CertificateTemplate selectedTemplate) {
		boolean allOk = true;
		
		container.clearError();
		if(typeEl.isOneSelected()) {
			if(CUSTOM_KEY.equals(typeEl.getSelectedKey())) {
				if(selectedTemplate == null) {
					container.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			} else if(!systemTemplateEl.isOneSelected()) {
				container.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		} else {
			container.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		certificationProgram = certificationProgramService.getCertificationProgram(certificationProgram);

		String beforeXml = CertificationProgramXStream.toXml(certificationProgram);
		
		certificationProgram.setCertificateCustom1(certificationCustom1El.getValue());
		certificationProgram.setCertificateCustom2(certificationCustom2El.getValue());
		certificationProgram.setCertificateCustom3(certificationCustom3El.getValue());

		CertificateTemplate selectedTemplate = getSelectedTemplate();
		certificationProgram.setTemplate(selectedTemplate);
		
		boolean withPrint = withPrintTemplateEl.isOn();
		certificationProgram.setPrintTemplateEnabled(withPrint);
		if(withPrint) {
			CertificateTemplate selectedPrintTemplate = getSelectedPrintTemplate();
			certificationProgram.setPrintTemplate(selectedPrintTemplate);
		} else {
			certificationProgram.setPrintTemplate(null);
		}
		
		if(serialNumberEl.isOn()) {
			certificationProgram.setSerialNumberEnabled(true);
			certificationProgram.setSerialNumberFormat(serialNumberFormatEl.getValue());
			certificationProgram.setSerialNumberStartNumber(Long.valueOf(serialNumberStartEl.getValue()));
		} else {
			certificationProgram.setSerialNumberEnabled(false);
			certificationProgram.setSerialNumberFormat(null);
		}
		
		certificationProgram = certificationProgramService.updateCertificationProgram(certificationProgram);
		dbInstance.commitAndCloseSession();
		
		String afterXml = CertificationProgramXStream.toXml(certificationProgram);
		if(!Objects.equals(beforeXml, afterXml)) {
			certificationProgramService.log(null, certificationProgram, CertificationProgramLogAction.edit_certification_program,
					null, beforeXml, null, afterXml, null, null, getIdentity());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		if(serialNumberEl.isOn()) {
			updateNextValue();
		}
	}
	
	private void doUploadTemplate(UserRequest ureq) {
		certificateUploadCtrl = new UploadCertificateTemplateController(ureq, getWindowControl(), false);
		listenTo(certificateUploadCtrl);
		
		String title = translate("choose.certificate.template.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), certificateUploadCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doUploadPrintTemplate(UserRequest ureq) {
		printCertificateUploadCtrl = new UploadCertificateTemplateController(ureq, getWindowControl(), false);
		listenTo(printCertificateUploadCtrl);
		
		String title = translate("choose.certificate.template.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), printCertificateUploadCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doSetTemplate(CertificateTemplate template) {
		this.uploadedTemplate = template;
		previewTemplateLink.setEnabled(true);
		templateFileEl.setValue(template.getName());
	}
	
	private void doSetPrintTemplate(CertificateTemplate template) {
		this.uploadedPrintTemplate = template;
		previewPrintTemplateLink.setEnabled(true);
		templatePrintFileEl.setValue(template.getName());
	}
	
	private void doPreviewTemplate(UserRequest ureq) {
		CertificateTemplate selectedTemplate = getSelectedTemplate();
		doPreviewTemplate(ureq, selectedTemplate);
	}
	
	private void doPreviewPrintTemplate(UserRequest ureq) {
		CertificateTemplate selectedPrintTemplate = getSelectedPrintTemplate();
		doPreviewTemplate(ureq, selectedPrintTemplate);
	}

	private void doPreviewTemplate(UserRequest ureq, CertificateTemplate template) {
		String custom1 = certificationCustom1El.getValue();
		String custom2 = certificationCustom2El.getValue();
		String custom3 = certificationCustom3El.getValue();
		String serialNumber = null;
		if(serialNumberEl.isOn()
				&& StringHelper.containsNonWhitespace(serialNumberFormatEl.getValue())
				&& StringHelper.isLong(serialNumberStartEl.getValue())) {
			SerialNumberFormat serialNumberFormat = SerialNumberFormat.parse(serialNumberFormatEl.getValue());
			serialNumber = serialNumberFormat.generate(Long.valueOf(serialNumberStartEl.getValue()), LocalDate.now());
		}
		
		PreviewCertificate preview = certificatesManager.previewCertificate(template, certificationProgram, getLocale(),
				custom1, custom2, custom3, serialNumber);
		MediaResource resource = new PreviewMediaResource(preview);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
