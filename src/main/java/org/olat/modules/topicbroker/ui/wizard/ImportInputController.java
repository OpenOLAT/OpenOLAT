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
package org.olat.modules.topicbroker.ui.wizard;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TopicBrokerExportService;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImportInputController extends StepFormBasicController {
	
	private static final String TOPIC_IMPORT_TEMPLATE_XLSX = "topic_import_template.xlsx";
	static final String FILES_IMPORT_TEMPLATE_NAME = "files_import_template";
	private static final String FILES_IMPORT_TEMPLATE_XLSX = FILES_IMPORT_TEMPLATE_NAME + ".zip";
	
	private FormLink topicsDownloadLink;
	private TextAreaElement topicsEl;
	private FormLink filesDownloadLink;
	private FileElement filesEl;
	
	private final ImportContext importContext;
	private final List<TBCustomFieldDefinition> definitions;
	private File tempDir;

	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private TopicBrokerExportService topicBrokerExportService;

	public ImportInputController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		
		importContext = (ImportContext)getFromRunContext("importContext");
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(importContext.getBroker());
		definitions = topicBrokerService.getCustomFieldDefinitions(definitionSearchParams).stream()
				.filter(definition -> TBCustomFieldType.text == definition.getType())
				.sorted((d1, d2) -> Integer.compare(d1.getSortOrder(), d2.getSortOrder()))
				.toList();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topicsLayout = FormLayoutContainer.createDefaultFormLayout("importLayout", getTranslator());
		topicsLayout.setFormTitle(translate("import.topics.title"));
		topicsLayout.setFormDescription(translate("import.topics.desc", TBUIFactory.getImportColumns(getTranslator(), definitions)));
		topicsLayout.setFormContextHelp("manual_user/learningresources/Course_Element_Topic_Broker/#topic_broker_export_import_topics");
		topicsLayout.setRootForm(formLayout.getRootForm());
		formLayout.add(topicsLayout);
		
		topicsDownloadLink = uifactory.addFormLink("templateDownloadLink", null, "import.topics.template", topicsLayout, Link.LINK + Link.NONTRANSLATED);
		topicsDownloadLink.setI18nKey(TOPIC_IMPORT_TEMPLATE_XLSX);
		topicsDownloadLink.setIconLeftCSS("o_icon o_filetype_xls o_icon-lg");
		
		topicsEl = uifactory.addTextAreaElement("import.topics.topics", "import.topics.topics", -1, 10, -1, false, true, true, null, topicsLayout);
		topicsEl.setStripedBackgroundEnabled(true);
		topicsEl.setFixedFontWidth(true);
		
		FormLayoutContainer filesLayout = FormLayoutContainer.createDefaultFormLayout("filesLayout", getTranslator());
		filesLayout.setFormTitle(translate("import.files.title"));
		filesLayout.setFormDescription(translate("import.files.desc"));
		filesLayout.setRootForm(formLayout.getRootForm());
		formLayout.add(filesLayout);
		
		filesDownloadLink = uifactory.addFormLink("filesDownloadLink", null, "import.files.template", filesLayout, Link.LINK + Link.NONTRANSLATED);
		filesDownloadLink.setI18nKey(FILES_IMPORT_TEMPLATE_XLSX);
		filesDownloadLink.setIconLeftCSS("o_icon o_filetype_zip o_icon-lg");
		
		filesEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "import.files.file", filesLayout);
		filesEl.limitToMimeType(Collections.singleton("application/zip"), "error.mimetype", new String[]{ "ZIP" });
		filesEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == topicsDownloadLink) {
			doDownloadTopicsTemplate(ureq);
		} else if (source == filesDownloadLink) {
			doDownloadFilesTemplate(ureq);
		} else if (source == filesEl) {
			if (filesEl.getUploadFile() != null) {
				filesEl.clearError();
				boolean allOk = filesEl.validate();
				if (allOk) {
					allOk &= ZipUtil.isReadable(filesEl.getUploadFile());
					if (!allOk) {
						filesEl.setErrorKey("error.import.zip");
					}
				}
				if (allOk) {
					extractZip();
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		topicsEl.clearError();
		if (!StringHelper.containsNonWhitespace(topicsEl.getValue()) && filesEl.getUploadFile() == null) {
			topicsEl.setErrorKey("form.legende.mandatory");
			filesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		importContext.setInput(topicsEl.getValue());
		importContext.setTempFilesDir(tempDir);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private void doDownloadTopicsTemplate(UserRequest ureq) {
		MediaResource importTemplate = topicBrokerExportService.createTopicImportTemplateMediaResource(ureq,
				importContext.getBroker(), TOPIC_IMPORT_TEMPLATE_XLSX);
		ureq.getDispatchResult().setResultingMediaResource(importTemplate);
	}

	private void doDownloadFilesTemplate(UserRequest ureq) {
		MediaResource filesTemplate = topicBrokerExportService.createFilesImportTemplateMediaResource(ureq,
				importContext.getBroker(), FILES_IMPORT_TEMPLATE_XLSX);
		ureq.getDispatchResult().setResultingMediaResource(filesTemplate);
	}
	
	private boolean extractZip() {
		// Clean up previously extracted files
		FileUtils.deleteDirsAndFiles(tempDir, true, true);
		tempDir = null;
		
		if (filesEl.getUploadFile() == null || !filesEl.getUploadFile().exists()) {
			return true;
		}
		
		try {
			tempDir = new File(WebappHelper.getTmpDir() + "/" + UUID.randomUUID().toString());
			tempDir.mkdir();
			ZipUtil.unzip(filesEl.getUploadFile(), tempDir);
		} catch (Exception e) {
			filesEl.setErrorKey("error.import.zip");
			return false;
		}
		
		return true;
	}

}
