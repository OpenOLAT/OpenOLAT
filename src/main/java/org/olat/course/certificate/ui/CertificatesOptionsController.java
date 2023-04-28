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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
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


	@Autowired
	private CertificatesManager certificatesManager;
	
	public CertificatesOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.editable = editable;
		
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		mapperUrl = registerMapper(ureq, new TemplateMapper());
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.certificate.title");
		formLayout.setElementCssClass("o_sel_certificate_settings");
		
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		
		enabledEl = uifactory.addToggleButton("enabled", translate("issue.certificate"), "&nbsp;&nbsp;", formLayout, null, null);
		enabledEl.addActionListener(FormEvent.ONCLICK);
		enabledEl.setEnabled(editable && !managedEff);
		if (certificateConfig.isAutomaticCertificationEnabled() || certificateConfig.isManualCertificationEnabled()) {
			enabledEl.toggleOn();
		} else {
			enabledEl.toggleOff();
		}
		
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

		if(editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		
		updateUI();
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		
		pdfCertificatesEl.setVisible(enabled);
		
		templateCont.setVisible(enabled);
		selectTemplateLink.setEnabled(enabled && editable);
		if(!enabled || selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
			previewTemplateLink.setEnabled(false);
		} else {
			templateCont.contextPut("templateName", selectedTemplate.getName());
			previewTemplateLink.setEnabled(true);
		}
		
		certificationCustom1El.setVisible(enabled);
		certificationCustom2El.setVisible(enabled);
		certificationCustom3El.setVisible(enabled);
		
		validityEnabledEl.setVisible(enabled);
		validityEnabledEl.select(validityEnabledEl.getKey(0), currentValidityEnabled);
		boolean enableRecertification = enabled && validityEnabledEl.isAtLeastSelected(1);
		validityCont.setVisible(enableRecertification);
		validityTimelapseEl.setEnabled(enableRecertification && editable);
		validityTimelapseUnitEl.setEnabled(enableRecertification && editable);
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
		if (source == enabledEl) {
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
		boolean allOk = true;
		
		pdfCertificatesEl.clearError();
		if (pdfCertificatesEl.isVisible()) {
			if (!pdfCertificatesEl.isAtLeastSelected(1)) {
				pdfCertificatesEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}
		
		validityCont.clearError();
		if (validityTimelapseEl.isVisible()) {
			if (!StringHelper.containsNonWhitespace(validityTimelapseEl.getValue())) {
				validityCont.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else {
				allOk &= validateInt(validityTimelapseEl, validityCont);
			}
		}
		
		return allOk;
	}
	
	private boolean validateInt(TextElement el, FormItem errorEl) {
		boolean allOk = true;
		
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer intValue = Integer.parseInt(value);
					if (intValue.intValue() < 0) {
						allOk = false;
						errorEl.setErrorKey("error.positive.int");
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
		selectedTemplate = certificatesManager.getTemplateById(selectedTemplate.getKey());
		String custom1 = certificationCustom1El.getValue();
		String custom2 = certificationCustom2El.getValue();
		String custom3 = certificationCustom3El.getValue();
		PreviewCertificate preview = certificatesManager.previewCertificate(selectedTemplate, entry, getLocale(), custom1, custom2, custom3);
		MediaResource resource = new PreviewMediaResource(preview);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doSetTemplate(CertificateTemplate template) {
		this.selectedTemplate = template;
		if(selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
			previewTemplateLink.setEnabled(false);
		} else {
			templateCont.contextPut("templateName", template.getName());
			previewTemplateLink.setEnabled(true);
		}
	}
	
	private void doSelectTemplate(UserRequest ureq) {
		removeAsListenerAndDispose(certificateChooserCtrl);
		removeAsListenerAndDispose(cmc);
		
		certificateChooserCtrl = new CertificateChooserController(ureq, getWindowControl(), selectedTemplate);
		listenTo(certificateChooserCtrl);
		
		String title = translate("choose.title");
		cmc = new CloseableModalController(getWindowControl(), "close", certificateChooserCtrl.getInitialComponent(), true, title);
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
		
		certificateConfig = certificatesManager.updateConfiguration(certificateConfig);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
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
	
	private static class PreviewMediaResource implements MediaResource {
		private static final Logger log = Tracing.createLoggerFor(PreviewMediaResource.class);
		private PreviewCertificate preview;
		
		public PreviewMediaResource(PreviewCertificate preview) {
			this.preview = preview;
		}
		
		@Override
		public long getCacheControlDuration() {
			return 0;
		}

		@Override
		public boolean acceptRanges() {
			return true;
		}
		
		@Override
		public String getContentType() {
			return "application/type";
		}

		@Override
		public Long getSize() {
			return preview.getCertificate().length();
		}

		@Override
		public InputStream getInputStream() {
			try {
				return new FileInputStream(preview.getCertificate());
			} catch (FileNotFoundException e) {
				log.error("", e);
				return null;
			}
		}

		@Override
		public Long getLastModified() {
			return null;
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setHeader("Content-Disposition", "filename*=UTF-8''Certificate_preview.pdf");
		}

		@Override
		public void release() {
			FileUtils.deleteDirsAndFiles(preview.getTmpDirectory(), true, true);
		}
	}
}