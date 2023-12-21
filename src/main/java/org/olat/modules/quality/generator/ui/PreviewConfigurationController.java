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
package org.olat.modules.quality.generator.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewConfigurationController extends FormBasicController {
	
	private static final DateFormat startCompareFormat = new SimpleDateFormat("yyyyMMddhhmm");
	
	private final TooledStackedPanel stackPanel;
	private FormLink evaFormPreviewLink;
	private DateChooser startEl;
	private DateChooser deadlineEl;
	private MultiSelectionFilterElement organisationsEl;
	private FormLayoutContainer buttonLayout;
	private FormLink resetLink;
	
	private final QualityPreview preview;
	
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private UserManager userManager;

	public PreviewConfigurationController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, QualityPreview preview) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QualityUIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.preview = preview;
		stackPanel.addListener(this);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("data.collection.title", preview.getTitle(), formLayout);
		
		startEl = uifactory.addDateChooser("data.collection.start", preview.getStart(), formLayout);
		startEl.setDateChooserTimeEnabled(true);
		startEl.addActionListener(FormEvent.ONCHANGE);
		
		deadlineEl = uifactory.addDateChooser("data.collection.deadline", preview.getDeadline(), formLayout);
		deadlineEl.setDateChooserTimeEnabled(true);
		deadlineEl.setEnabled(false);
		
		evaFormPreviewLink = uifactory.addFormLink("data.collection.form", null, translate("data.collection.form"),
				formLayout, Link.NONTRANSLATED);
		evaFormPreviewLink.setI18nKey(StringHelper.escapeHtml(preview.getFormEntry().getDisplayname()));
		evaFormPreviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		SelectionValues organisationSV = QualityUIFactory.getOrganisationSV(ureq.getUserSession(), preview.getOrganisations());
		organisationsEl = uifactory.addCheckboxesFilterDropdown("data.collection.organisations",
				"data.collection.organisations", formLayout, getWindowControl(), organisationSV);
		organisationsEl.setEnabled(false);
		preview.getOrganisations().forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
		
		uifactory.addStaticTextElement("data.collection.topic.type.select", translate(preview.getTopicType().getI18nKey()), formLayout);
		
		uifactory.addStaticTextElement("data.collection.topic.custom.text", getTopic(preview), formLayout);
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		resetLink = uifactory.addFormLink("preview.reset", buttonLayout, Link.BUTTON);
	}
	
	private String getTopic(QualityPreview preview) {
		return switch (preview.getTopicType()) {
		case CUSTOM -> preview.getTopicCustom();
		case IDENTIY -> userManager.getUserDisplayName(preview.getTopicIdentity().getKey());
		case ORGANISATION -> preview.getTopicOrganisation().getDisplayName();
		case CURRICULUM -> preview.getTopicCurriculum().getDisplayName();
		case CURRICULUM_ELEMENT -> preview.getTopicCurriculumElement().getDisplayName();
		case REPOSITORY -> preview.getTopicRepositoryEntry().getDisplayname();
		default -> null;
		};
	}

	public void setReadOnly(boolean blacklisted) {
		startEl.setEnabled(!blacklisted);
		buttonLayout.setVisible(!blacklisted);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == startEl) {
			doMoveDeadline();
		} else if (source == evaFormPreviewLink) {
			doPreviewEvaluationForm(ureq);
		} else if (source == resetLink) {
			doReset();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startEl.clearError();
		if (startEl.isEnabled() && startEl.getDate() == null) {
			startEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (startCompareFormat.format(startEl.getDate()).equals(startCompareFormat.format(preview.getStart()))) {
			generatorService.deleteOverride(preview.getIdentifier());
		} else {
			QualityGeneratorOverride override = generatorService.getOverride(preview.getIdentifier());
			if (override == null) {
				override = generatorService.createOverride(preview.getIdentifier(), preview.getGenerator(), preview.getGeneratorProviderKey());
			}
			override.setStart(startEl.getDate());
			generatorService.updateOverride(override);
		}
	}

	private void doMoveDeadline() {
		Date start = startEl.getDate();
		if (start != null) {
			long duration = ChronoUnit.MINUTES.between(DateUtils.toLocalDateTime(preview.getStart()), DateUtils.toLocalDateTime(preview.getDeadline()));
			Date deadline = DateUtils.toDate(DateUtils.toLocalDateTime(start).plus(duration, ChronoUnit.MINUTES));
			deadlineEl.setDate(deadline);
		}
	}
	
	private void doReset() {
		startEl.setDate(preview.getStart());
		deadlineEl.setDate(preview.getDeadline());
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		RepositoryEntry reloadedFormEntry = repositoryManager.lookupRepositoryEntry(preview.getFormEntry().getKey());
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(reloadedFormEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(reloadedFormEntry);
		Controller previewCtrl =  new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage, null);
		stackPanel.pushController(translate("data.collection.form.preview.title"), previewCtrl);
	}

}
