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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StreamedMediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.PDFCertificatesOptions;
import org.olat.course.certificate.RecertificationTimeUnit;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.config.ui.CourseOptionsController;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesOptionsController extends FormBasicController {

	private MultipleSelectionElement pdfCertificatesEl;
	private MultipleSelectionElement efficencyEl;
	private MultipleSelectionElement reCertificationEl;
	private IntegerElement reCertificationTimelapseEl;
	private SingleSelection reCertificationTimelapseUnitEl;
	private FormLayoutContainer templateCont, recertificationCont;
	private FormLink selectTemplateLink;
	private Link previewTemplateLink;

	private final boolean editable;
	private CourseConfig courseConfig;
	private final RepositoryEntry entry;
	private CertificateTemplate selectedTemplate;
	
	private CloseableModalController cmc;
	private CertificateChooserController certificateChooserCtrl;
	private DialogBoxController enableEfficiencyDC, disableEfficiencyDC;

	private static final String[] pdfCertificatesOptionsKeys = new String[] {
		PDFCertificatesOptions.auto.name(),
		PDFCertificatesOptions.manual.name()
	};
	
	private static final String[] timelapseUnitKeys = new String[] {
		RecertificationTimeUnit.day.name(),
		RecertificationTimeUnit.week.name(),
		RecertificationTimeUnit.month.name(),
		RecertificationTimeUnit.year.name()
	};
	
	private final String mapperUrl;
	private LockResult lockEntry;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CertificatesOptionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseOptionsController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.courseConfig = courseConfig;
		this.entry = entry;
		
		mapperUrl = registerMapper(ureq, new TemplateMapper());
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(entry.getOlatResource(), getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);
		this.editable = (lockEntry != null && lockEntry.isSuccess()) && editable;

		initForm (ureq);
		
		if(lockEntry != null && !lockEntry.isSuccess()) {
			String lockerName = "???";
			if(lockEntry.getOwner() != null) {
				lockerName = userManager.getUserDisplayName(lockEntry.getOwner());
			}
			showWarning("error.editoralreadylocked", new String[] { lockerName });
		}
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.certificates.title");
		setFormTitleIconCss("o_icon o_icon_certificate");
		setFormDescription("options.certificates.descr");
		setFormContextHelp("Course Settings#_leistungsnachweis");
		formLayout.setElementCssClass("o_sel_course_certificates");
		
		boolean effEnabled = courseConfig.isEfficencyStatementEnabled();
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		efficencyEl = uifactory.addCheckboxesHorizontal("effIsOn", "chkbx.efficency.onoff", formLayout, new String[] {"xx"}, new String[] {""});
		efficencyEl.addActionListener(FormEvent.ONCHANGE);
		efficencyEl.select("xx", effEnabled);
		efficencyEl.setEnabled(editable && !managedEff);
		
		
		String[] pdfCertificatesOptionsValues = new String[] {
				translate("pdf.certificates.auto"),
				translate("pdf.certificates.manual")
		};
		pdfCertificatesEl = uifactory.addCheckboxesVertical("pdf.certificates", formLayout, pdfCertificatesOptionsKeys, pdfCertificatesOptionsValues, 1);
		pdfCertificatesEl.addActionListener(FormEvent.ONCHANGE);
		pdfCertificatesEl.select(PDFCertificatesOptions.auto.name(), courseConfig.isAutomaticCertificationEnabled());
		pdfCertificatesEl.select(PDFCertificatesOptions.manual.name(), courseConfig.isManualCertificationEnabled());
		pdfCertificatesEl.setEnabled(editable && !managedEff);
		
		String templatePage = velocity_root + "/select_certificate.html";
		templateCont = FormLayoutContainer.createCustomFormLayout("template.cont", getTranslator(), templatePage);
		templateCont.setRootForm(mainForm);
		templateCont.contextPut("mapperUrl", mapperUrl);
		formLayout.add(templateCont);
		templateCont.setLabel("pdf.certificates.template", null);

		selectTemplateLink = uifactory.addFormLink("select", templateCont, Link.BUTTON);
		selectTemplateLink.setEnabled(editable);
		Long templateId = courseConfig.getCertificateTemplate();
		boolean hasTemplate = templateId != null && templateId.longValue() > 0;
		if(hasTemplate) {
			selectedTemplate = certificatesManager.getTemplateById(templateId);
			if(selectedTemplate != null) {
				templateCont.contextPut("templateName", selectedTemplate.getName());
			} else {
				templateCont.contextPut("templateName", translate("default.template"));
			}
		} else {
			templateCont.contextPut("templateName", translate("default.template"));
		}

		previewTemplateLink = LinkFactory.createButton("preview", templateCont.getFormItemComponent(), this);
		previewTemplateLink.setTarget("preview");
		
		boolean reCertificationEnabled = courseConfig.isRecertificationEnabled();
		reCertificationEl = uifactory.addCheckboxesHorizontal("recertification", formLayout, new String[]{ "xx" }, new String[]{ "" });
		reCertificationEl.addActionListener(FormEvent.ONCHANGE);
		reCertificationEl.setEnabled(editable);
		if(reCertificationEnabled) {
			reCertificationEl.select("xx", true);
		}
		
		String recertificationPage = velocity_root + "/recertification.html";
		recertificationCont = FormLayoutContainer.createCustomFormLayout("timelapse.cont", getTranslator(), recertificationPage);
		recertificationCont.setRootForm(mainForm);
		formLayout.add(recertificationCont);
		
		int timelapse = courseConfig.getRecertificationTimelapse();
		reCertificationTimelapseEl = uifactory.addIntegerElement("timelapse", null, timelapse, recertificationCont);
		reCertificationTimelapseEl.setDomReplacementWrapperRequired(false);
		reCertificationTimelapseEl.setDisplaySize(4);
		reCertificationTimelapseEl.setEnabled(editable);
		
		String[] timelapseUnitValues = new String[] {
			translate("recertification.day"), translate("recertification.week"),
			translate("recertification.month"), translate("recertification.year")
		};
		RecertificationTimeUnit timelapseUnit = courseConfig.getRecertificationTimelapseUnit();
		reCertificationTimelapseUnitEl = uifactory.addDropdownSingleselect("timelapse.unit", null, recertificationCont, timelapseUnitKeys, timelapseUnitValues, null);
		reCertificationTimelapseUnitEl.setDomReplacementWrapperRequired(false);
		reCertificationTimelapseUnitEl.setEnabled(editable);
		if(timelapseUnit != null) {
			reCertificationTimelapseUnitEl.select(timelapseUnit.name(), true);
		} else {
			reCertificationTimelapseUnitEl.select(RecertificationTimeUnit.month.name(), true);
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
		boolean none = !pdfCertificatesEl.isAtLeastSelected(1);
		
		templateCont.setVisible(!none);
		selectTemplateLink.setEnabled(!none && editable);
		if(none || selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
			previewTemplateLink.setEnabled(false);
		} else {
			templateCont.contextPut("templateName", selectedTemplate.getName());
			previewTemplateLink.setEnabled(true);
		}
		
		reCertificationEl.setVisible(!none);
		
		boolean enableRecertification = !none && reCertificationEl.isAtLeastSelected(1);
		recertificationCont.setVisible(enableRecertification);
		reCertificationTimelapseEl.setEnabled(enableRecertification && editable);
		reCertificationTimelapseUnitEl.setEnabled(enableRecertification && editable);
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
		if (source == disableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig(ureq);
			}
		} else if (source == enableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {				
				doChangeConfig(ureq);
			}
		} else if(source == certificateChooserCtrl) {
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
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == selectTemplateLink) {
			doSelectTemplate(ureq);
		} else if(source == reCertificationEl) {
			updateUI();
		} else if(source == pdfCertificatesEl) {
			updateUI();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doPreviewTemplate(UserRequest ureq) {
		selectedTemplate = certificatesManager.getTemplateById(selectedTemplate.getKey());
		File preview = certificatesManager.previewCertificate(selectedTemplate, entry, getLocale());
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
	
	private void doSave(UserRequest ureq) {
		boolean confirmUpdateStatement = courseConfig.isEfficencyStatementEnabled() != efficencyEl.isSelected(0);
		if(confirmUpdateStatement) {
			if (courseConfig.isEfficencyStatementEnabled()) {
				// a change from enabled Efficiency to disabled
				disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
			} else {
				// a change from disabled Efficiency
				enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
			}
		} else {
			doChangeConfig(ureq);
		}
	}
	
	private void doChangeConfig(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		boolean enableEfficiencyStatment = efficencyEl.isSelected(0);
		boolean updateStatement = courseConfig.isEfficencyStatementEnabled() != enableEfficiencyStatment;
		courseConfig.setEfficencyStatementIsEnabled(enableEfficiencyStatment);
		
		Collection<String> certificationOptions = pdfCertificatesEl.getSelectedKeys();
		courseConfig.setAutomaticCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.auto.name()));
		courseConfig.setManualCertificationEnabled(certificationOptions.contains(PDFCertificatesOptions.manual.name()));
		if(selectedTemplate != null) {
			Long templateId = selectedTemplate.getKey();
			courseConfig.setCertificateTemplate(templateId);
		} else {
			courseConfig.setCertificateTemplate(null);
		}

		boolean recertificationEnabled = reCertificationEl.isEnabled() && reCertificationEl.isAtLeastSelected(1);
		courseConfig.setRecertificationEnabled(recertificationEnabled);

		if(recertificationEnabled) {
			int timelapse = reCertificationTimelapseEl.getIntValue();
			courseConfig.setRecertificationTimelapse(timelapse);
			
			if(reCertificationTimelapseUnitEl.isOneSelected()) {
				String selectedUnit = reCertificationTimelapseUnitEl.getSelectedKey();
				RecertificationTimeUnit timeUnit = RecertificationTimeUnit.valueOf(selectedUnit);
				courseConfig.setRecertificationTimelapseUnit(timeUnit);
			} else {
				courseConfig.setRecertificationTimelapseUnit(RecertificationTimeUnit.month);
			}
		} else {
			courseConfig.setRecertificationTimelapse(0);
			courseConfig.setRecertificationTimelapseUnit(null);
		}

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if(updateStatement) {
			if(enableEfficiencyStatment) {
	            // first create the efficiencies, send event to agency (all courses add link)
				RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				List<Identity> identitiesWithData = course.getCourseEnvironment().getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(null);
				efficiencyStatementManager.updateEfficiencyStatements(courseRe, identitiesWithData);							
			} else {
	            // delete really the efficiencies of the users.
				RepositoryEntry courseRepoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
				efficiencyStatementManager.deleteEfficiencyStatementsFromCourse(courseRepoEntry.getKey());						
			}
			
			//inform everybody else		
			EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
			CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigType.efficiencyStatement, course.getResourceableId());
			eventBus.fireEventToListenersOf(courseConfigEvent, course);
			
			ILoggingAction loggingAction = enableEfficiencyStatment ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	public class TemplateMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource resource;
			if(selectedTemplate != null) {
				VFSLeaf templateLeaf = certificatesManager.getTemplateLeaf(selectedTemplate);
				if(templateLeaf.getName().equals("index.html") && templateLeaf instanceof JavaIOItem) {
					JavaIOItem indexFile = (JavaIOItem)templateLeaf;
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
		private static final OLog log = Tracing.createLoggerFor(PreviewMediaResource.class);
		private File preview;
		
		public PreviewMediaResource(File preview) {
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
			return preview.length();
		}

		@Override
		public InputStream getInputStream() {
			try {
				return new FileInputStream(preview);
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
			FileUtils.deleteDirsAndFiles(preview.getParentFile(), true, true);
		}
	}
}