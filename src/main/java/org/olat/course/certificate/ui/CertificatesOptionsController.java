
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
package org.olat.course.certificate.ui;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StreamedMediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.PDFCertificatesOptions;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.SerialNumberFormat;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesOptionsController extends FormBasicController {
	
	private FormToggle enabledEl;
	private MultipleSelectionElement pdfCertificatesEl;
	private TextElement certificationCustom1El;
	private TextElement certificationCustom2El;
	private TextElement certificationCustom3El;
	private MultipleSelectionElement validityEnabledEl;
	private IntegerElement validityTimelapseEl;
	private SingleSelection validityTimelapseUnitEl;
	private FormLayoutContainer templateCont;
	private FormLayoutContainer validityCont;
	private FormLink selectTemplateLink;
	private Link previewTemplateLink;
	
	private TextElement serialNumberStartEl;
	private FormToggle serialNumberEl;
	private TextElement nextValueEl;
	private TextElement serialNumberFormatEl;
	private StaticTextElement serialNumberFormatInfosEl;
	private FormLayoutContainer serialCont;

	private final boolean editable;
	private final RepositoryEntry entry;
	private CertificateTemplate selectedTemplate;
	private boolean currentValidityEnabled;
	
	private RepositoryEntryCertificateConfiguration certificateConfig;
	
	private CloseableModalController cmc;
	private CertificateChooserController certificateChooserCtrl;
	
	private static final String[] pdfCertificatesOptionsKeys = new String[] {
		PDFCertificatesOptions.auto.name(),
		PDFCertificatesOptions.manual.name()
	};
	
	private static final String[] timelapseUnitKeys = new String[] {
		CertificationTimeUnit.day.name(),
		CertificationTimeUnit.week.name(),
		CertificationTimeUnit.month.name(),
		CertificationTimeUnit.year.name()
	};
	
	private final String mapperUrl;
	private final boolean managedEff;
	
	@Autowired
	private CertificatesManager certificatesManager;
	
	public CertificatesOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.editable = editable;
		
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		mapperUrl = registerMapper(ureq, new TemplateMapper());
		managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		
		initForm(ureq);
		updateUI();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.certificate.title");
		formLayout.setElementCssClass("o_sel_certificate_settings");
		
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		
		enabledEl = uifactory.addToggleButton("enabled", "issue.certificate", translate("on"), translate("off"), formLayout);
		enabledEl.addActionListener(FormEvent.ONCLICK);
		enabledEl.setEnabled(editable && !managedEff);
		if (certificateConfig.isAutomaticCertificationEnabled() || certificateConfig.isManualCertificationEnabled()) {
			enabledEl.toggleOn();
		} else {
			enabledEl.toggleOff();
		}
		
		initTemplateForm(formLayout);
		initSerialNumber(formLayout);
		
		if(editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	private void initTemplateForm(FormItemContainer formLayout) {	
		String[] pdfCertificatesOptionsValues = new String[] {
				translate("pdf.certificates.auto"),
				translate("pdf.certificates.manual")
		};
		pdfCertificatesEl = uifactory.addCheckboxesVertical("pdf.certificates", formLayout, pdfCertificatesOptionsKeys, pdfCertificatesOptionsValues, 1);
		pdfCertificatesEl.setElementCssClass("o_sel_certificate_options");
		pdfCertificatesEl.select(PDFCertificatesOptions.auto.name(), certificateConfig.isAutomaticCertificationEnabled());
		pdfCertificatesEl.select(PDFCertificatesOptions.manual.name(), certificateConfig.isManualCertificationEnabled());
		pdfCertificatesEl.setMandatory(true);
		pdfCertificatesEl.setEnabled(editable && !managedEff);
		
		String templatePage = velocity_root + "/select_certificate.html";
		templateCont = FormLayoutContainer.createCustomFormLayout("template.cont", getTranslator(), templatePage);
		templateCont.setRootForm(mainForm);
		templateCont.contextPut("mapperUrl", mapperUrl);
		formLayout.add(templateCont);
		templateCont.setLabel("pdf.certificates.template", null);

		selectTemplateLink = uifactory.addFormLink("select", templateCont, Link.BUTTON);
		selectTemplateLink.setEnabled(editable);
		selectedTemplate = certificateConfig.getTemplate();
		if(selectedTemplate != null) {
			templateCont.contextPut("templateName", selectedTemplate.getName());
		} else {
			templateCont.contextPut("templateName", translate("default.template"));
		}

		previewTemplateLink = LinkFactory.createButton("preview", templateCont.getFormItemComponent(), this);
		previewTemplateLink.setTarget("preview");
		
		certificationCustom1El = uifactory.addTextElement("certificate.custom1", 1000, certificateConfig.getCertificateCustom1(), formLayout);
		certificationCustom1El.setEnabled(editable);
		certificationCustom2El = uifactory.addTextElement("certificate.custom2", 2000, certificateConfig.getCertificateCustom2(), formLayout);
		certificationCustom2El.setEnabled(editable);
		certificationCustom3El = uifactory.addTextElement("certificate.custom3", 3000, certificateConfig.getCertificateCustom3(), formLayout);
		certificationCustom3El.setEnabled(editable);
		
		currentValidityEnabled = certificateConfig.isValidityEnabled();
		validityEnabledEl = uifactory.addCheckboxesHorizontal("validity.period", formLayout, new String[]{ "xx" }, new String[]{ "" });
		validityEnabledEl.addActionListener(FormEvent.ONCHANGE);
		validityEnabledEl.setEnabled(editable);
		if(currentValidityEnabled) {
			validityEnabledEl.select("xx", true);
		}
		
		validityCont = FormLayoutContainer.createButtonLayout("recert", getTranslator());
		validityCont.setElementCssClass("o_inline_cont");
		validityCont.setLabel("validity.period", null);
		validityCont.setMandatory(true);
		validityCont.setRootForm(mainForm);
		formLayout.add(validityCont);
		
		int timelapse = certificateConfig.getValidityTimelapse();
		validityTimelapseEl = uifactory.addIntegerElement("timelapse", null, timelapse, validityCont);
		validityTimelapseEl.setDisplaySize(4);
		validityTimelapseEl.setEnabled(editable);
		
		String[] timelapseUnitValues = new String[] {
			translate("recertification.day"), translate("recertification.week"),
			translate("recertification.month"), translate("recertification.year")
		};
		CertificationTimeUnit timelapseUnit = certificateConfig.getValidityTimelapseUnit();
		validityTimelapseUnitEl = uifactory.addDropdownSingleselect("timelapse.unit", null, validityCont, timelapseUnitKeys, timelapseUnitValues, null);
		validityTimelapseUnitEl.setEnabled(editable);
		if(timelapseUnit != null) {
			validityTimelapseUnitEl.select(timelapseUnit.name(), true);
		} else {
			validityTimelapseUnitEl.select(CertificationTimeUnit.month.name(), true);
		}
	}
	
	private void initSerialNumber(FormItemContainer formLayout) {
		serialCont = uifactory.addDefaultFormLayout("serial", null, formLayout);
		serialCont.setFormTitle(translate("serial.number.title"));
		serialCont.setFormInfo(translate("serial.number.descr"));
		serialCont.setFormLayout("nolayout");
		
		serialNumberEl = uifactory.addToggleButton("serial.number.enabled", "serial.number.enabled", translate("on"), translate("off"), serialCont);
		serialNumberEl.toggle(certificateConfig.isSerialNumberEnabled());
		
		String text = "<div class='o_info_with_icon'>" + translate("serial.number.format.infos", new String[] { "${counter}", "${counter:N}", "${year}, ${month}, ${day}"}) + "</span>";
		serialNumberFormatInfosEl = uifactory.addStaticTextElement("format.infos", null, text, serialCont);
		serialNumberFormatInfosEl.setDomWrapperElement(DomWrapperElement.div);
		
		String format = certificateConfig.getSerialNumberFormat();
		serialNumberFormatEl = uifactory.addTextElement("serial.number.format", "serial.number.format", 255, format, serialCont);
		serialNumberFormatEl.setExampleKey("serial.number.format.hint", new String[] { "${year}", "${counter:5}"});

		long startNumber = certificateConfig.getSerialNumberStartNumber();
		long currentCounter = certificateConfig.getSerialNumberCounter();
		serialNumberStartEl = uifactory.addTextElement("serial.number.counter", "serial.number.counter", 255, Long.toString(startNumber), serialCont);
		serialNumberStartEl.setExampleKey("serial.number.counter.hint", new String[] { Long.toString(currentCounter) });
		serialNumberStartEl.setElementCssClass("form-inline");
		serialNumberStartEl.setDisplaySize(8);
		serialNumberStartEl.setMaxLength(8);
		
		nextValueEl = uifactory.addTextElement("serial.number.next", "serial.number.next", 255, "", serialCont);
		nextValueEl.setElementCssClass("o_certificate_next_value");
		nextValueEl.setEnabled(false);
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		
		pdfCertificatesEl.setVisible(enabled);
		
		templateCont.setVisible(enabled);
		selectTemplateLink.setEnabled(enabled && editable);
		if(selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
		} else {
			templateCont.contextPut("templateName", selectedTemplate.getName());
		}
		previewTemplateLink.setEnabled(enabled);
		
		certificationCustom1El.setVisible(enabled);
		certificationCustom2El.setVisible(enabled);
		certificationCustom3El.setVisible(enabled);
		
		validityEnabledEl.setVisible(enabled);
		validityEnabledEl.select(validityEnabledEl.getKey(0), currentValidityEnabled);
		boolean enableRecertification = enabled && validityEnabledEl.isAtLeastSelected(1);
		validityCont.setVisible(enableRecertification);
		validityTimelapseEl.setEnabled(enableRecertification && editable);
		validityTimelapseUnitEl.setEnabled(enableRecertification && editable);
		
		serialCont.setVisible(enabled);
		
		boolean serialNumberEnabled = serialNumberEl.isOn() && enabled;
		serialNumberFormatEl.setVisible(serialNumberEnabled);
		serialNumberStartEl.setVisible(serialNumberEnabled);
		nextValueEl.setVisible(serialNumberEnabled);
		serialNumberFormatInfosEl.setVisible(serialNumberEnabled);
		
		updateNextValue();
	}
	
	private void updateNextValue() {
		long currentCounter = certificateConfig.getSerialNumberCounter();
		if(certificateConfig.getSerialNumberStartNumber() > currentCounter) {
			currentCounter = certificateConfig.getSerialNumberStartNumber();
		} else {
			currentCounter++;
		}
		String format = serialNumberFormatEl.getValue();
		String nextValue = SerialNumberFormat.parse(format).generate(currentCounter, LocalDate.now());
		nextValueEl.setValue(nextValue);
		
		serialNumberStartEl.setExampleKey("serial.number.counter.hint",
				new String[] { Long.toString(certificateConfig.getSerialNumberCounter()) });
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == previewTemplateLink) {
			doPreviewTemplate(ureq);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == certificateChooserCtrl) {
			if(event == Event.DONE_EVENT) {
				doSetTemplate(certificateChooserCtrl.getSelectedTemplate());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(certificateChooserCtrl);
		removeAsListenerAndDispose(cmc);
		certificateChooserCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl || serialNumberEl == source) {
			updateUI();
		} else if (source == selectTemplateLink) {
			doSelectTemplate(ureq);
		} else if (source == validityEnabledEl) {
			currentValidityEnabled = validityEnabledEl.isAtLeastSelected(1);
			updateUI();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		certificateConfig = certificatesManager.getConfiguration(entry);
		
		pdfCertificatesEl.clearError();
		if (pdfCertificatesEl.isVisible()) {
			if (!pdfCertificatesEl.isAtLeastSelected(1)) {
				pdfCertificatesEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}
		
		validityCont.clearError();
		if(!validityTimelapseUnitEl.isOneSelected()) {
			validityCont.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (validityTimelapseEl.isVisible()) {
			if (!StringHelper.containsNonWhitespace(validityTimelapseEl.getValue())) {
				validityCont.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else if(!validateTimelapse(ureq, validityTimelapseEl, validityCont)) {
				allOk &= false;
			}
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
			serialNumberFormatEl.setErrorKey("error.counter.number.format", Integer.toString(Integer.MAX_VALUE));
			allOk &= false;
		} catch (Exception e) {
			serialNumberFormatEl.setErrorKey("error.counter");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateTimelapse(UserRequest ureq, TextElement el, FormItem errorEl) {
		boolean allOk = true;
		
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer intValue = Integer.parseInt(value);
					if (intValue.intValue() < 0) {
						allOk = false;
						errorEl.setErrorKey("error.positive.int");
					} else if(certificateConfig.isRecertificationEnabled() && certificateConfig.isRecertificationLeadTimeEnabled()) {
						String selectedUnit = validityTimelapseUnitEl.getSelectedKey();
						CertificationTimeUnit timeUnit = CertificationTimeUnit.valueOf(selectedUnit);
						Date nextRecertification = timeUnit.toDate(ureq.getRequestTimestamp(), intValue);
						nextRecertification = CalendarUtils.endOfDay(nextRecertification);
						long nextRecertificationInDays = DateUtils.countDays(ureq.getRequestTimestamp(), nextRecertification);
						if(certificateConfig.getRecertificationLeadTimeInDays() >= nextRecertificationInDays) {
							validityCont.setErrorKey("error.recertication.time");
							allOk &= false;
						}
					}
				} catch(Exception e) {
					allOk = false;
					errorEl.setErrorKey("error.positive.int");
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doChangeConfig(ureq);
	}
	
	private void doPreviewTemplate(UserRequest ureq) {
		if(selectedTemplate != null) {
			selectedTemplate = certificatesManager.getTemplateById(selectedTemplate.getKey());
		}
		String custom1 = certificationCustom1El.getValue();
		String custom2 = certificationCustom2El.getValue();
		String custom3 = certificationCustom3El.getValue();
		String serialNumber = null;
		/*
		if(serialNumberEl.isOn()
				&& StringHelper.containsNonWhitespace(serialNumberFormatEl.getValue())
				&& StringHelper.isLong(counterEl.getValue())) {
			SerialNumberFormat serialNumberFormat = SerialNumberFormat.parse(serialNumberFormatEl.getValue());
			serialNumber = serialNumberFormat.generate(Long.valueOf(counterEl.getValue()), LocalDate.now());
		}
		*/
		PreviewCertificate preview = certificatesManager.previewCertificate(selectedTemplate, entry, getLocale(),
				custom1, custom2, custom3, serialNumber);
		
		MediaResource resource = new PreviewMediaResource(preview);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doSetTemplate(CertificateTemplate template) {
		this.selectedTemplate = template;
		if(selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
		} else {
			templateCont.contextPut("templateName", template.getName());
		}
		previewTemplateLink.setEnabled(true);
	}
	
	private void doSelectTemplate(UserRequest ureq) {
		removeAsListenerAndDispose(certificateChooserCtrl);
		removeAsListenerAndDispose(cmc);
		
		certificateChooserCtrl = new CertificateChooserController(ureq, getWindowControl(), selectedTemplate);
		listenTo(certificateChooserCtrl);
		
		String title = translate("choose.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), certificateChooserCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doChangeConfig(UserRequest ureq) {
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		if (enabledEl.isOn()) {
			Collection<String> certificationOptions = pdfCertificatesEl.getSelectedKeys();
			certificateConfig.setAutomaticCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.auto.name()));
			certificateConfig.setManualCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.manual.name()));
			if(selectedTemplate != null) {
				selectedTemplate = certificatesManager.getTemplateById(selectedTemplate.getKey());
				certificateConfig.setTemplate(selectedTemplate);
			} else {
				certificateConfig.setTemplate(null);
			}
			
			certificateConfig.setCertificateCustom1(certificationCustom1El.getValue());
			certificateConfig.setCertificateCustom2(certificationCustom2El.getValue());
			certificateConfig.setCertificateCustom3(certificationCustom3El.getValue());
			
			boolean validityEnabled = validityEnabledEl.isEnabled() && validityEnabledEl.isAtLeastSelected(1);
			certificateConfig.setValidityEnabled(validityEnabled);
			
			if(validityEnabled) {
				int timelapse = validityTimelapseEl.getIntValue();
				certificateConfig.setValidityTimelapse(timelapse);
				
				if(validityTimelapseUnitEl.isOneSelected()) {
					String selectedUnit = validityTimelapseUnitEl.getSelectedKey();
					CertificationTimeUnit timeUnit = CertificationTimeUnit.valueOf(selectedUnit);
					certificateConfig.setValidityTimelapseUnit(timeUnit);
				} else {
					certificateConfig.setValidityTimelapseUnit(CertificationTimeUnit.month);
				}
			} else {
				certificateConfig.setValidityTimelapse(0);
				certificateConfig.setValidityTimelapseUnit(null);
				// If validity is disabled, recertification too
				certificateConfig.setRecertificationEnabled(false);
				certificateConfig.setRecertificationLeadTimeEnabled(false);
				certificateConfig.setRecertificationLeadTimeInDays(0);
			}
		} else {
			certificateConfig.setAutomaticCertificationEnabled(false);
			certificateConfig.setManualCertificationEnabled(false);
		}
		
		if(serialNumberEl.isOn() && enabledEl.isOn()) {
			certificateConfig.setSerialNumberEnabled(true);
			certificateConfig.setSerialNumberFormat(serialNumberFormatEl.getValue());
			certificateConfig.setSerialNumberStartNumber(Long.valueOf(serialNumberStartEl.getValue()));
		} else {
			certificateConfig.setSerialNumberEnabled(false);
			certificateConfig.setSerialNumberFormat(null);
		}
		
		certificateConfig = certificatesManager.updateConfiguration(certificateConfig);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		if(serialNumberEl.isOn()) {
			updateNextValue();
		}
	}
	
	public class TemplateMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource resource;
			if(selectedTemplate != null) {
				VFSLeaf templateLeaf = certificatesManager.getTemplateLeaf(selectedTemplate);
				if(templateLeaf.getName().equals("index.html") && templateLeaf instanceof JavaIOItem indexFile) {
					File templateDir = indexFile.getBasefile().getParentFile();
					resource = new ZippedDirectoryMediaResource(selectedTemplate.getName(), templateDir);
				} else {
					resource = new VFSMediaResource(templateLeaf); 
				}
			} else {
				InputStream stream = certificatesManager.getDefaultTemplate();
				resource = new StreamedMediaResource(stream, "Certificate_template.pdf", "application/pdf");
			}
			return resource;
		}
	}
}