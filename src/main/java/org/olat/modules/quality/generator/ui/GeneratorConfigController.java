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
package org.olat.modules.quality.generator.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.generator.manager.QualityGeneratorProviderFactory;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorConfigController extends FormBasicController {

	private TextElement titleEl;
	private MultiSelectionFilterElement organisationsEl;
	private StaticTextElement evaFormNotChoosen;
	private FormLink evaFormSelectLink;
	private FormLink evaFormPreviewLink;
	private FormLink evaFormReplaceLink;
	private FormLink evaFormEditLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController formSearchCtrl;
	
	private GeneratorSecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	private QualityGenerator generator;
	private List<Organisation> currentOrganisations;
	private RepositoryEntry formEntry;
	
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityGeneratorProviderFactory generatorProviderFactory;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private EvaluationFormManager evaluationManager;

	public GeneratorConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGeneratorRef generatorRef) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.generator = generatorService.loadGenerator(generatorRef);
		this.currentOrganisations = generatorService.loadGeneratorOrganisations(generatorRef);
		this.formEntry = this.generator.getFormEntry();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		QualityGeneratorProvider provider = generatorProviderFactory.getProvider(generator.getType());
		String providerType = provider != null? provider.getDisplayname(getLocale()): "???";
		uifactory.addStaticTextElement("generator.provider.name", providerType, formLayout);
		
		titleEl = uifactory.addTextElement("generator.title", 200, generator.getTitle(), formLayout);
		
		SelectionValues organisationSV = QualityUIFactory.getOrganisationSV(ureq.getUserSession(), currentOrganisations);
		organisationsEl = uifactory.addCheckboxesFilterDropdown("generator.organisations", "generator.organisations",
				formLayout, getWindowControl(), organisationSV);
		currentOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));

		evaFormNotChoosen = uifactory.addStaticTextElement("generator.form.not.selected", "generator.form",
				translate("generator.form.not.selected"), formLayout);
		evaFormPreviewLink = uifactory.addFormLink("generator.form", "", translate("generator.form"),
				formLayout, Link.NONTRANSLATED);
		evaFormPreviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");

		FormLayoutContainer formCont = FormLayoutContainer.createButtonLayout("form", getTranslator());
		formCont.setRootForm(mainForm);
		formLayout.add(formCont);
		evaFormSelectLink = uifactory.addFormLink("generator.form.select", formCont, "btn btn-default o_xsmall");
		evaFormReplaceLink = uifactory.addFormLink("generator.form.replace", formCont,
				"btn btn-default o_xsmall");
		evaFormEditLink = uifactory.addFormLink("generator.form.edit", formCont, "btn btn-default o_xsmall");
		
		updateUI();
	}

	public void onChanged(QualityGeneratorRef generatorRef, GeneratorSecurityCallback secCallback) {
		this.generator = generatorService.loadGenerator(generatorRef);
		this.formEntry = generator.getFormEntry();
		this.secCallback = secCallback;
		updateUI();
	}

	protected void updateUI() {
		boolean editGenerator = secCallback.canEditGenerator();
		titleEl.setEnabled(editGenerator);
		organisationsEl.setEnabled(editGenerator);
		organisationsEl.setVisible(organisationModule.isEnabled());
		
		boolean hasRepoConfig = formEntry != null;
		if (hasRepoConfig) {
			String displayname = StringHelper.escapeHtml(formEntry.getDisplayname());
			evaFormPreviewLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setGeneratorRefs(Collections.singletonList(generator));
		List<QualityGeneratorView> generators = generatorService.loadGenerators(searchParams);
		Long numOfDataCollections = generators.isEmpty()? 0l: generators.get(0).getNumberDataCollections();
		boolean editGeneratorForm = secCallback.canEditGeneratorForm(numOfDataCollections);
		evaFormNotChoosen.setVisible(!hasRepoConfig);
		evaFormSelectLink.setVisible(!hasRepoConfig);
		evaFormPreviewLink.setVisible(hasRepoConfig);
		evaFormReplaceLink.setVisible(hasRepoConfig && editGeneratorForm);
		evaFormEditLink.setVisible(hasRepoConfig && editGeneratorForm);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == evaFormReplaceLink || source == evaFormSelectLink) {
			doSelectFormEntry(ureq);
		} else if (source == evaFormEditLink) {
			doEditEvaluationForm(ureq);
		} else if (source == evaFormPreviewLink) {
			doPreviewEvaluationForm(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == formSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				formEntry = formSearchCtrl.getSelectedEntry();
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(cmc);
		formSearchCtrl = null;
		cmc = null;
	}
	
	private void doSelectFormEntry(UserRequest ureq) {
		formSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("generator.form.select"));
		this.listenTo(formSearchCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				formSearchCtrl.getInitialComponent(), true, translate("generator.form.select"));
		cmc.activate();
	}
	
	private void doEditEvaluationForm(UserRequest ureq) {
		if (formEntry != null) {
			String bPath = "[RepositoryEntry:" + formEntry.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
				FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationManager.loadStorage(formEntry);
		Controller previewCtrl =  new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage, null);
		stackPanel.pushController(translate("generator.form.preview.title"), previewCtrl);
	}

	public boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = validateFormLogic(ureq);
		
		allOk &= validateIsMandatory(titleEl);
		allOk &= validateIsMandatory(organisationsEl);
		
		if (formEntry == null) {
			evaFormNotChoosen.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}
		
		return allOk;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		organisationsEl.clearError();
		evaFormNotChoosen.clearError();
		
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		generator.setTitle(title);
		
		generator.setFormEntry(formEntry);
		generator = generatorService.updateGenerator(generator);
		
		if (organisationsEl.isVisible()) {
			currentOrganisations = QualityUIFactory.getSelectedOrganisations(organisationsEl, currentOrganisations);
			generatorService.updateGeneratorOrganisations(generator, currentOrganisations);
		}
		
		fireEvent(ureq, new GeneratorEvent(generator, GeneratorEvent.Action.CHANGED));
	}
}
