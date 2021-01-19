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
package org.olat.modules.quality.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;
import static org.olat.modules.quality.ui.QualityUIFactory.emptyArray;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumTreeModel;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.user.UserManager;
import org.olat.user.ui.organisation.OrganisationTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionConfigurationController extends FormBasicController {

	private TextElement titleEl;
	private FormLink evaFormPreviewLink;
	private FormLink evaFormReplaceLink;
	private FormLink evaFormEditLink;
	private DateChooser startEl;
	private DateChooser deadlineEl;
	private MultipleSelectionElement organisationsEl;
	private SingleSelection topicTypeEl;
	private TextElement topicCustomTextEl;
	private StaticTextElement topicIdentityNameEl;
	private FormLink topicIdentitySelectLink;
	private SingleSelection topicOrganisationEl;
	private SingleSelection topicCurriculumEl;
	private SingleSelection topicCurriculumElementEl;
	private FormLink topicRepositorySelectLink;
	private StaticTextElement topicRepositoryNameEl;
	private FormLayoutContainer buttonLayout;

	private CloseableModalController cmc;
	private ReferencableEntriesSearchController formSearchCtrl;
	private TopicIdentitySearchController topicIdentitySearchCtrl;
	private ReferencableEntriesSearchController topicRepositorySearchCtrl;
	private RepositoryEntry formEntry;
	private boolean formEntryChanged = false;
	private List<Organisation> currentOrganisations;
	private QualityDataCollectionTopicType topicType;
	private Identity topicIdentity;
	private Organisation topicOrganisation;
	private Curriculum topicCurriculum;
	private CurriculumElement topicCurriculumElement;
	private RepositoryEntry topicRepository;
	
	private final TooledStackedPanel stackPanel;
	private DataCollectionSecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public DataCollectionConfigurationController(UserRequest ureq, WindowControl wControl,
			DataCollectionSecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection, boolean validate) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = qualityService.loadDataCollectionByKey(dataCollection);
		this.formEntry = qualityService.loadFormEntry(dataCollection);
		this.topicType = dataCollection.getTopicType();
		this.topicIdentity = dataCollection.getTopicIdentity();
		this.topicOrganisation = dataCollection.getTopicOrganisation();
		this.topicCurriculum = dataCollection.getTopicCurriculum();
		this.topicCurriculumElement = dataCollection.getTopicCurriculumElement();
		if (topicCurriculumElement != null) {
			this.topicCurriculum = topicCurriculumElement.getCurriculum();
		}
		this.topicRepository = this.dataCollection.getTopicRepositoryEntry();
		this.currentOrganisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		
		initForm(ureq);
		if (validate) {
			validateFormLogic(ureq);
			validateExtendedFormLogic(true);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("data.collection.title", 200, dataCollection.getTitle(), formLayout);

		startEl = uifactory.addDateChooser("data.collection.start", dataCollection.getStart(), formLayout);
		startEl.setDateChooserTimeEnabled(true);

		deadlineEl = uifactory.addDateChooser("data.collection.deadline", dataCollection.getDeadline(), formLayout);
		deadlineEl.setDateChooserTimeEnabled(true);

		evaFormPreviewLink = uifactory.addFormLink("data.collection.form", "", translate("data.collection.form"),
				formLayout, Link.NONTRANSLATED);
		evaFormPreviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");

		FormLayoutContainer formCont = FormLayoutContainer.createButtonLayout("form", getTranslator());
		formCont.setRootForm(mainForm);
		formLayout.add(formCont);
		evaFormReplaceLink = uifactory.addFormLink("data.collection.form.replace", formCont,
				"btn btn-default o_xsmall");
		evaFormEditLink = uifactory.addFormLink("data.collection.form.edit", formCont, "btn btn-default o_xsmall");
		
		organisationsEl = uifactory.addCheckboxesDropdown("data.collection.organisations", formLayout);
		if (organisationModule.isEnabled()) {
			QualityUIFactory.initOrganisations(ureq.getUserSession(), organisationsEl, currentOrganisations);
			organisationsEl.addActionListener(FormEvent.ONCLICK);
		}
		
		// topic
		QualityDataCollectionTopicType actual = dataCollection.getTopicType();
		topicTypeEl = uifactory.addDropdownSingleselect("data.collection.topic.type.select", formLayout,
				QualityUIFactory.getTopicTypeKeys(actual),
				QualityUIFactory.getTopicTypeValues(getTranslator(), actual));
		topicTypeEl.enableNoneSelection();
		topicTypeEl.addActionListener(FormEvent.ONCHANGE);
		if (topicType != null) {
			topicTypeEl.select(QualityUIFactory.getTopicTypeKey(topicType), true);
		}
		// topic custom
		topicCustomTextEl = uifactory.addTextElement("data.collection.topic.custom.text", 200,
				dataCollection.getTopicCustom(), formLayout);
		// topic identity
		topicIdentityNameEl = uifactory.addStaticTextElement("data.collection.topic.identity.name", null, formLayout);
		topicIdentitySelectLink = uifactory.addFormLink("data.collection.topic.identity.select", formLayout,
				"btn btn-default o_xsmall");
		// topic organisation
		topicOrganisationEl = uifactory.addDropdownSingleselect("data.collection.topic.organisation", formLayout, emptyArray(), emptyArray());
		// topic curriculum
		topicCurriculumEl = uifactory.addDropdownSingleselect("data.collection.topic.curriculum", formLayout, emptyArray(), emptyArray());
		topicCurriculumEl.addActionListener(FormEvent.ONCHANGE);
		// topic curriculum element
		topicCurriculumElementEl = uifactory.addDropdownSingleselect("data.collection.topic.curriculum.element", formLayout, emptyArray(), emptyArray());
		// topic repository
		topicRepositoryNameEl = uifactory.addStaticTextElement("data.collection.topic.repository.name", null, formLayout);
		topicRepositorySelectLink = uifactory.addFormLink("data.collection.topic.repository.select", formLayout,
				"btn btn-default o_xsmall");

		buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

		updateUI();
	}
	
	public void onChanged(QualityDataCollection dataCollection, DataCollectionSecurityCallback secCallback) {
		this.dataCollection = dataCollection;
		this.secCallback = secCallback;
		startEl.setDate(dataCollection.getStart());
		deadlineEl.setDate(dataCollection.getDeadline());
		updateUI();
	}
	
	private void updateUI() {
		boolean updateBaseConfiguration = secCallback.canUpdateBaseConfiguration();
		titleEl.setEnabled(updateBaseConfiguration);
		evaFormReplaceLink.setVisible(updateBaseConfiguration);
		evaFormEditLink.setVisible(updateBaseConfiguration);
		startEl.setEnabled(updateBaseConfiguration);
		deadlineEl.setEnabled(updateBaseConfiguration);
		buttonLayout.setVisible(updateBaseConfiguration);
		organisationsEl.setEnabled(updateBaseConfiguration);
		organisationsEl.setVisible(organisationModule.isEnabled());
		topicTypeEl.setEnabled(updateBaseConfiguration);
		topicCustomTextEl.setEnabled(updateBaseConfiguration);
		topicIdentityNameEl.setEnabled(updateBaseConfiguration);
		topicOrganisationEl.setEnabled(updateBaseConfiguration);
		topicCurriculumEl.setEnabled(updateBaseConfiguration);
		topicCurriculumElementEl.setEnabled(updateBaseConfiguration);
		topicRepositoryNameEl.setEnabled(updateBaseConfiguration);
		
		String displayname = StringHelper.escapeHtml(formEntry.getDisplayname());
		evaFormPreviewLink.setI18nKey(displayname);
		flc.setDirty(true);
		
		updateTopicUI();
	}

	private void updateTopicUI() {
		dataCollection = qualityService.loadDataCollectionByKey(dataCollection);
		topicCustomTextEl.setVisible(false);
		topicIdentityNameEl.setVisible(false);
		topicIdentitySelectLink.setVisible(false);
		topicOrganisationEl.setVisible(false);
		topicCurriculumEl.setVisible(false);
		topicCurriculumElementEl.setVisible(false);
		topicRepositoryNameEl.setVisible(false);
		topicRepositorySelectLink.setVisible(false);
		if (topicTypeEl.isOneSelected()) {
			String selectedKey = topicTypeEl.getSelectedKey();
			topicType = QualityUIFactory.getTopicTypeEnum(selectedKey);
			switch (topicType) {
			case CUSTOM: 
				topicCustomTextEl.setVisible(true);
				break;
			case IDENTIY: 
				String userName = topicIdentity != null
					? userManager.getUserDisplayName(topicIdentity)
					: translate("data.collection.topic.identity.none");
				topicIdentityNameEl.setValue(userName);
				topicIdentityNameEl.setVisible(true);
				topicIdentitySelectLink.setVisible(secCallback.canUpdateBaseConfiguration());
				break;
			case ORGANISATION:
				List<Organisation> organisations = organisationService.getOrganisations(getIdentity(),
						OrganisationRoles.administrator, OrganisationRoles.qualitymanager);
				OrganisationTreeModel organisationModel = new OrganisationTreeModel();
				organisationModel.loadTreeModel(organisations);
				KeysValues organistionKeysValues = QualityUIFactory.getOrganisationKeysValues(organisationModel, topicOrganisation);
				topicOrganisationEl.setKeysAndValues(organistionKeysValues.getKeys(), organistionKeysValues.getValues(), null);
				if (topicOrganisation != null) {
					topicOrganisationEl.select(QualityUIFactory.getOrganisationKey(topicOrganisation), true);
				}
				topicOrganisationEl.setVisible(true);
				break;
			case CURRICULUM:
				CurriculumSearchParameters params = new CurriculumSearchParameters();
				if (organisationsEl.isVisible()) {
					params.setOrganisations(QualityUIFactory.getSelectedOrganisationRefs(organisationsEl));
				}
				List<Curriculum> curriculums = curriculumService.getCurriculums(params);
				KeysValues curriculumKeysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, topicCurriculum);
				topicCurriculumEl.setKeysAndValues(curriculumKeysValues.getKeys(), curriculumKeysValues.getValues(), null);
				if (topicCurriculum != null) {
					topicCurriculumEl.select(QualityUIFactory.getCurriculumKey(topicCurriculum), true);
				}
				topicCurriculumEl.setVisible(true);
				break;
			case CURRICULUM_ELEMENT:
				CurriculumSearchParameters params2 = new CurriculumSearchParameters();
				if (organisationsEl.isVisible()) {
					params2.setOrganisations(QualityUIFactory.getSelectedOrganisationRefs(organisationsEl));
				}
				List<Curriculum> curriculums2 = curriculumService.getCurriculums(params2);
				KeysValues curriculumKeysValues2 = QualityUIFactory.getCurriculumKeysValues(curriculums2, topicCurriculum);
				topicCurriculumEl.setKeysAndValues(curriculumKeysValues2.getKeys(), curriculumKeysValues2.getValues(), null);
				if (topicCurriculum != null) {
					topicCurriculumEl.select(QualityUIFactory.getCurriculumKey(topicCurriculum), true);
					List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(topicCurriculum, CurriculumElementStatus.values());
					CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel(topicCurriculum);
					curriculumTreeModel.loadTreeModel(curriculumElements);
					KeysValues curriculumElementKeysValues = QualityUIFactory.getCurriculumElementKeysValues(curriculumTreeModel, topicCurriculumElement);
					topicCurriculumElementEl.setKeysAndValues(curriculumElementKeysValues.getKeys(), curriculumElementKeysValues.getValues(), null);
					if (topicCurriculumElement != null) {
						topicCurriculumElementEl.select(QualityUIFactory.getCurriculumElementKey(topicCurriculumElement), true);
					}
				}
				topicCurriculumEl.setVisible(true);
				topicCurriculumElementEl.setVisible(true);
				break;
			case REPOSITORY:
				String repositoryName = topicRepository != null
						? topicRepository.getDisplayname()
						: translate("data.collection.topic.repository.none");
				topicRepositoryNameEl.setValue(repositoryName);
				topicRepositoryNameEl.setVisible(true);
				topicRepositorySelectLink.setVisible(secCallback.canUpdateBaseConfiguration());
				break;
			}
		}
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == evaFormReplaceLink) {
			doSelectFormEntry(ureq);
		} else if (source == evaFormEditLink) {
			doEditEvaluationForm(ureq);
		} else if (source == evaFormPreviewLink) {
			doPreviewEvaluationForm(ureq);
		} else if (source == organisationsEl) {
			updateTopicUI();
		} else if (source == topicTypeEl) {
			updateTopicUI();
		} else if (source == topicIdentitySelectLink) {
			doSelectTopicIdentity(ureq);
		} else if (source == topicCurriculumEl) {
			if (topicCurriculumEl.isOneSelected()) {
				topicCurriculumElement = null;
				String curriculumKey = topicCurriculumEl.getSelectedKey();
				CurriculumRef curriculumRef = QualityUIFactory.getCurriculumRef(curriculumKey);
				topicCurriculum = curriculumService.getCurriculum(curriculumRef);
				updateTopicUI();
			}
		} else if (source == topicRepositorySelectLink) {
			doSelectTopicRepository(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == formSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry selectedEntry = formSearchCtrl.getSelectedEntry();
				doUpdateFormEntry(selectedEntry);
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == topicIdentitySearchCtrl) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
				topicIdentity = singleEvent.getChosenIdentity();
				updateTopicUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == topicRepositorySearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				topicRepository = topicRepositorySearchCtrl.getSelectedEntry();
				updateTopicUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(topicRepositorySearchCtrl);
		removeAsListenerAndDispose(topicIdentitySearchCtrl);
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(cmc);
		topicRepositorySearchCtrl = null;
		topicIdentitySearchCtrl = null;
		formSearchCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deadlineEl.clearError();
		organisationsEl.clearError();
		topicTypeEl.clearError();
		topicCustomTextEl.clearError();
		topicIdentityNameEl.clearError();
		topicCurriculumEl.clearError();
		topicCurriculumElementEl.clearError();
		topicRepositoryNameEl.clearError();
		
		titleEl.clearError();
		String value = titleEl.getValue();
		if (value != null && value.length() > 200) {
			String[] lengths = new String[]{ Integer.toString(200), Integer.toString(value.length())};
			titleEl.setErrorKey("error.input.toolong", lengths);
			allOk = false;
		}
		
		deadlineEl.clearError();
		Date start = startEl.getDate();
		Date deadline = deadlineEl.getDate();
		if (start != null && deadline != null && deadline.before(start)) {
			deadlineEl.setErrorKey("error.deadline.before.start", null);
			allOk = false;
		}
		
		return allOk;
	}
	
	boolean validateExtendedFormLogic(boolean validateStart) {
		boolean allOk = true;
		
		if (!titleEl.hasError()) {
			String value = titleEl.getValue();
			if (!StringHelper.containsNonWhitespace(value)) {
				titleEl.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		
		if (validateStart) {
			Date value = startEl.getDate();
			if (value == null) {
				startEl.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		
		if (!deadlineEl.hasError()) {
			Date deadline = deadlineEl.getDate();
			if (deadline == null) {
				deadlineEl.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		
		if (organisationsEl.isVisible() && !organisationsEl.isAtLeastSelected(1)) {
			organisationsEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}
		
		if (topicType == null) {
			topicTypeEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		} else {
			switch (topicType) {
				case CUSTOM: {
					String custom = topicCustomTextEl.getValue();
					if (!StringHelper.containsNonWhitespace(custom)) {
						topicCustomTextEl.setErrorKey("form.mandatory.hover", null);
						allOk = false;
					}
					break;
				}
				case IDENTIY: {
					if (topicIdentity == null) {
						topicIdentityNameEl.setErrorKey("form.mandatory.hover", null);
						allOk = false;
					}
					break;
				}
				case CURRICULUM: {
					if (topicCurriculum == null) {
						topicCurriculumEl.setErrorKey("form.mandatory.hover", null);
						allOk = false;
					}
					break;
				}
				case CURRICULUM_ELEMENT: {
					if (topicCurriculum == null) {
						topicCurriculumElementEl.setErrorKey("form.mandatory.hover", null);
						allOk = false;
					}
					break;
				}
				case REPOSITORY: {
					if (topicRepository == null) {
						topicRepositoryNameEl.setErrorKey("form.mandatory.hover", null);
						allOk = false;
					}
					break;
				}
				default:
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		dataCollection.setTitle(title);
		
		Date start = startEl.getDate();
		dataCollection.setStart(start);
		Date deadline = deadlineEl.getDate();
		dataCollection.setDeadline(deadline);
		
		// topic
		dataCollection.setTopicType(topicType);
		dataCollection.setTopicCustom(null);
		dataCollection.setTopicIdentity(null);
		dataCollection.setTopicOrganisation(null);
		dataCollection.setTopicCurriculum(null);
		dataCollection.setTopicCurriculumElement(null);
		dataCollection.setTopicRepositoryEntry(null);
		if (topicType != null) {
			switch (topicType) {
			case CUSTOM: 
				String topic = topicCustomTextEl.getValue();
				dataCollection.setTopicCustom(topic);
				break;
			case IDENTIY:
				dataCollection.setTopicIdentity(topicIdentity);
				break;
			case ORGANISATION: 
				if (topicOrganisationEl.isOneSelected()) {
					String organiationKey = topicOrganisationEl.getSelectedKey();
					OrganisationRef organisationRef = QualityUIFactory.getOrganisationRef(organiationKey);
					Organisation organisation = organisationService.getOrganisation(organisationRef);
					dataCollection.setTopicOrganisation(organisation);
				} 
				break;
			case CURRICULUM: 
				if (topicCurriculumEl.isOneSelected()) {
					String curriculumKey = topicCurriculumEl.getSelectedKey();
					CurriculumRef curriculumRef = QualityUIFactory.getCurriculumRef(curriculumKey);
					Curriculum curriculum = curriculumService.getCurriculum(curriculumRef);
					dataCollection.setTopicCurriculum(curriculum);
				}
				break;
			case CURRICULUM_ELEMENT: 
				if (topicCurriculumElementEl.isOneSelected()) {
					String curriculumElementKey = topicCurriculumElementEl.getSelectedKey();
					CurriculumElementRef curriculumElementRef = QualityUIFactory.getCurriculumElementRef(curriculumElementKey);
					CurriculumElement curriculumElement = curriculumService.getCurriculumElement(curriculumElementRef);
					dataCollection.setTopicCurriculumElement(curriculumElement);
				} 
				break;
			case REPOSITORY: 
				dataCollection.setTopicRepositoryEntry(topicRepository);
				break;
			default:
			}
		}
		
		// save
		dataCollection = qualityService.updateDataCollection(dataCollection);

		// form
		if (formEntryChanged) {
			boolean isFormUpdateable = qualityService.isFormEntryUpdateable(dataCollection);
			if (isFormUpdateable) {
				qualityService.updateFormEntry(dataCollection, formEntry);
			} else {
				showError("error.repo.entry.not.replaceable");
			}
		}
		
		// organisations
		if (organisationsEl.isVisible()) {
			currentOrganisations = QualityUIFactory.getSelectedOrganisations(organisationsEl, currentOrganisations);
			qualityService.updateDataCollectionOrganisations(dataCollection, currentOrganisations);
		}
		
		fireEvent(ureq, new DataCollectionEvent(dataCollection, Action.CHANGED));
	}
	
	private void doSelectFormEntry(UserRequest ureq) {
		formSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("data.collection.form.select"));
		listenTo(formSearchCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				formSearchCtrl.getInitialComponent(), true, translate("data.collection.form.select"));
		cmc.activate();
	}

	private void doUpdateFormEntry(RepositoryEntry selectedEntry) {
		if (!selectedEntry.equals(formEntry)) {
			formEntry = selectedEntry;
			formEntryChanged = true;
		}
	}
	
	private void doSelectTopicIdentity(UserRequest ureq) {
		topicIdentitySearchCtrl = new TopicIdentitySearchController(ureq, getWindowControl());
		listenTo(topicIdentitySearchCtrl);
		
		String title = translate("data.collection.topic.identity.select");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), topicIdentitySearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelectTopicRepository(UserRequest ureq) {
		topicRepositorySearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, "CourseModule",
				translate("data.collection.topic.repository.select"));
		this.listenTo(topicRepositorySearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				topicRepositorySearchCtrl.getInitialComponent(), true, translate("data.collection.topic.repository.select"));
		cmc.activate();
	}

	private void doEditEvaluationForm(UserRequest ureq) {
		if (formEntry == null) {
			showError("error.repo.entry.missing");
		} else {
			String bPath = "[RepositoryEntry:" + formEntry.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(formEntry);
		Controller previewCtrl =  new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage);
		stackPanel.pushController(translate("data.collection.form.preview.title"), previewCtrl);
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
