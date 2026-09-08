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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.member.CurriculumElementMemberUsersController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipationCounts;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.resource.accesscontrol.ui.OfferSurveyExportFactory.SurveyExportInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRowSelectionEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.ui.OfferSurveyListTableModel.OfferSurveyCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lists and manages the forms attached to an offer's resource (the booking
 * order form definitions), independent of which offers actually use them.
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyListController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String CMD_STEP_NAME = "stepName";
	private static final String CMD_TOOLS = "tools";
	private static final String CMD_OPEN = "open";
	private static final String CMD_REMOVE = "remove";
	private static final String FILTER_USED = "used";
	private static final String FILTER_NOT_USED = "not.used";

	private FormLink exportLink;
	private FormLink addFormLink;
	private OfferSurveyListTableModel tableModel;
	private FlexiTableElement tableEl;
	private final VelocityContainer detailsVC;
	private List<EvaluationFormSurvey> surveys;
	private List<UserPropertyHandler> userPropertyHandlers;

	private CloseableModalController cmc;
	private AuthorListController formSearchCtrl;
	private OfferSurveyStepNameEditController stepNameEditCtrl;
	private CloseableCalloutWindowController stepNameCalloutCtrl;
	private ConfirmationController removeConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final OLATResource resource;
	private final boolean readOnly;
	private List<Offer> offers;

	@Autowired
	private ACService acService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	public OfferSurveyListController(UserRequest ureq, WindowControl wControl, Form mainForm, OLATResource resource, boolean readOnly) {
		super(ureq, wControl, LAYOUT_CUSTOM, "offer_survey_list", mainForm);
		this.resource = resource;
		this.readOnly = readOnly;
		detailsVC = createVelocityContainer("offer_survey_details");

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(
				CurriculumElementMemberUsersController.class.getCanonicalName(), isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("offer.survey.list.title");
		setFormInfo("offer.survey.list.hint");

		exportLink = uifactory.addFormLink("offer.survey.export", formLayout, Link.BUTTON);
		exportLink.setIconLeftCSS("o_icon o_icon-lg o_icon_download");

		if (!readOnly) {
			addFormLink = uifactory.addFormLink("offer.survey.add", formLayout, Link.BUTTON);
			addFormLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.reference));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.stepName));

		offers = acService.findOfferByResource(resource, true, null, null);
		OfferSurveyPositionCellRenderer positionRenderer = new OfferSurveyPositionCellRenderer();
		int offerColumnIndex = OfferSurveyCols.values().length;
		for (Offer offer : offers) {
			DefaultFlexiColumnModel offerColumn = new DefaultFlexiColumnModel(
					"offer.survey.offer.column", offerColumnIndex++, positionRenderer);
			offerColumn.setHeaderLabel(OfferSurveyUIFactory.getOfferLabel(offer, getTranslator()));
			columnsModel.addFlexiColumnModel(offerColumn);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.open));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.completed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyCols.canceled));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(OfferSurveyCols.tools));

		tableModel = new OfferSurveyListTableModel(columnsModel, getLocale(), offers);
		tableEl = uifactory.addTableElement(getWindowControl(), "offerSurveyTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(OfferSurveyCols.title.name(), true)));
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);

		initFilterTabs(ureq);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);

		FlexiFiltersTab usedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FILTER_USED, translate("offer.survey.filter.used"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(usedTab);

		FlexiFiltersTab notUsedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FILTER_NOT_USED, translate("offer.survey.filter.not.used"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(notUsedTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void loadModel() {
		List<EvaluationFormSurvey> allSurveys = acService.loadOfferSurveys(resource);
		Map<Long, EvaluationFormParticipationCounts> countsBySurveyKey = evaluationFormManager.loadParticipationCounts(allSurveys);

		List<OfferSurveyRow> rows = new ArrayList<>(allSurveys.size());
		List<EvaluationFormSurvey> visibleSurveys = new ArrayList<>(allSurveys.size());
		for (EvaluationFormSurvey survey : allSurveys) {
			boolean used = acService.isOfferSurveyUsed(survey);
			if (isTabMatch(used)) {
				OfferSurveyRow row = new OfferSurveyRow(survey, used);
				EvaluationFormParticipationCounts counts = countsBySurveyKey.getOrDefault(survey.getKey(), EvaluationFormParticipationCounts.EMPTY);
				row.setOpenCount(counts.prepared());
				row.setCompletedCount(counts.done());
				row.setCanceledCount(counts.canceled());
				row.setPositionByOfferKey(loadPositionByOfferKey(survey));
				if (!readOnly) {
					forgeStepNameLink(row);
				}
				forgeToolsLink(row);
				rows.add(row);
				visibleSurveys.add(survey);
			}
		}
		surveys = visibleSurveys;
		exportLink.setVisible(!surveys.isEmpty());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private Map<Long, Integer> loadPositionByOfferKey(EvaluationFormSurvey survey) {
		Map<Long, Integer> positionByOfferKey = new HashMap<>();
		for (OfferToSurvey offerToSurvey : acService.loadOfferToSurveys(survey)) {
			positionByOfferKey.put(offerToSurvey.getOffer().getKey(), offerToSurvey.getPos());
		}
		return positionByOfferKey;
	}

	private boolean isTabMatch(boolean used) {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if (selectedTab == null) {
			return true;
		}
		if (FILTER_USED.equals(selectedTab.getId())) {
			return used;
		}
		if (FILTER_NOT_USED.equals(selectedTab.getId())) {
			return !used;
		}
		return true;
	}

	private void forgeStepNameLink(OfferSurveyRow row) {
		FormLink stepNameLink = uifactory.addFormLink("stepName_" + row.getKey(), CMD_STEP_NAME, row.getStepName(), null, flc, Link.NONTRANSLATED + Link.LINK);
		stepNameLink.setEscapeMode(EscapeMode.html);
		stepNameLink.setUserObject(row);
		stepNameLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		row.setStepNameLink(stepNameLink);
	}

	private void forgeToolsLink(OfferSurveyRow row) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if (rowObject instanceof OfferSurveyRow surveyRow && surveyRow.getDetailsController() != null) {
			return List.of(surveyRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return List.of();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == exportLink) {
			doExport(ureq);
		} else if (source == addFormLink) {
			doChooseForm(ureq);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			} else if (event instanceof DetailsToggleEvent toggleEvent) {
				OfferSurveyRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if (toggleEvent.isVisible()) {
					doOpenSurveyDetails(ureq, row);
				} else {
					doCloseSurveyDetails(row);
				}
			}
		} else if (source instanceof FormLink link) {
			if (CMD_STEP_NAME.equals(link.getCmd()) && link.getUserObject() instanceof OfferSurveyRow row) {
				doEditStepName(ureq, row, link);
			} else if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof OfferSurveyRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (formSearchCtrl == source) {
			if (event instanceof AuthoringEntryRowSelectionEvent se) {
				doAddForm(se.getRow());
			}
			cmc.deactivate();
			cleanUp();
		} else if (stepNameEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSaveStepName(stepNameEditCtrl.getSurvey(), stepNameEditCtrl.getStepName());
			}
			stepNameCalloutCtrl.deactivate();
			cleanUp();
		} else if (removeConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doRemove((OfferSurveyRow) removeConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				removeAsListenerAndDispose(toolsCalloutCtrl);
				removeAsListenerAndDispose(toolsCtrl);
				toolsCalloutCtrl = null;
				toolsCtrl = null;
			}
		} else if (cmc == source || stepNameCalloutCtrl == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(stepNameEditCtrl);
		removeAsListenerAndDispose(stepNameCalloutCtrl);
		removeAsListenerAndDispose(removeConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		formSearchCtrl = null;
		stepNameEditCtrl = null;
		stepNameCalloutCtrl = null;
		removeConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	private void doChooseForm(UserRequest ureq) {
		if (guardModalController(formSearchCtrl)) return;

		Roles roles = ureq.getUserSession().getRoles();
		AuthorListConfiguration tableConfig = AuthorListConfiguration.selectRessource("offer-survey-form-v1", EvaluationFormResource.TYPE_NAME);
		tableConfig.setSelectRepositoryEntry(SelectionMode.single);
		tableConfig.setImportRessources(false);
		tableConfig.setCreateRessources(false);
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.addResourceTypes(EvaluationFormResource.TYPE_NAME);

		formSearchCtrl = new AuthorListController(ureq, getWindowControl(), searchParams, tableConfig);
		listenTo(formSearchCtrl);
		formSearchCtrl.selectFilterTab(ureq, formSearchCtrl.getFavoritTab());

		cmc = new CloseableModalController(getWindowControl(), translate("close"), formSearchCtrl.getInitialComponent(),
				true, translate("offer.survey.add"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddForm(RepositoryEntryRef formEntryRef) {
		RepositoryEntry formEntry = repositoryService.loadBy(formEntryRef);
		if (formEntry != null) {
			acService.createOfferSurvey(resource, formEntry, formEntry.getDisplayname());
			loadModel();
		}
	}

	private void doOpenForm(UserRequest ureq, OfferSurveyRow row) {
		String businessPath = "[RepositoryEntry:" + row.getFormEntry().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doExport(UserRequest ureq) {
		if (surveys.isEmpty()) return;

		boolean withPath = surveys.size() > 1;
		List<SurveyExportInfos> surveyExportInfos = surveys.stream().map(survey -> createExport(survey, withPath)).toList();
		MediaResource mediaResource = OfferSurveyExportFactory.createExport(getIdentity(), getWindowControl(),
				getTranslator(), translate("offer.survey.list.title"), surveyExportInfos);
		ureq.getDispatchResult().setResultingMediaResource(mediaResource);
	}

	private SurveyExportInfos createExport(EvaluationFormSurvey survey, boolean withPath) {
		org.olat.modules.forms.model.xml.Form form = evaluationFormManager.loadForm(survey.getFormEntry());
		SessionFilter filter = SessionFilterFactory.create(survey, true);
		UserPropertiesColumns userColumns = new UserPropertiesColumns(userPropertyHandlers, getTranslator());
		String stepName = survey.getDisplayName();
		String fileName = StringHelper.containsNonWhitespace(stepName) ? stepName : survey.getFormEntry().getDisplayname();
		EvaluationFormExcelExport excelExport = new EvaluationFormExcelExport(getLocale(), survey.getFormEntry(), form,
				filter, null, userColumns, fileName);
		String path = withPath ? StringHelper.transformDisplayNameToFileSystemName(fileName) : "";
		return new SurveyExportInfos(survey, form, filter, excelExport, path);
	}

	private void doEditStepName(UserRequest ureq, OfferSurveyRow row, FormLink link) {
		if (guardModalController(stepNameEditCtrl)) return;

		stepNameEditCtrl = new OfferSurveyStepNameEditController(ureq, getWindowControl(), row.getSurvey());
		listenTo(stepNameEditCtrl);

		stepNameCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				stepNameEditCtrl.getInitialComponent(), link, translate("offer.survey.step.name.edit"), true, "");
		listenTo(stepNameCalloutCtrl);
		stepNameCalloutCtrl.activate();
	}

	private void doSaveStepName(EvaluationFormSurvey survey, String stepName) {
		evaluationFormManager.updateSurveyDisplayName(survey, stepName);
		loadModel();
	}

	private void doConfirmRemove(UserRequest ureq, OfferSurveyRow row) {
		if (guardModalController(removeConfirmationCtrl)) return;

		String message;
		if (row.getCompletedCount() > 0) {
			message = translate("offer.survey.remove.confirmation.message.completed", String.valueOf(row.getCompletedCount()));
		} else if (row.getOpenCount() + row.getCanceledCount() > 0) {
			message = translate("offer.survey.remove.confirmation.message.used", StringHelper.escapeHtml(row.getTitle()));
		} else {
			message = translate("offer.survey.remove.confirmation.message", StringHelper.escapeHtml(row.getTitle()));
		}
		removeConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				message,
				"",
				translate("offer.survey.remove.confirmation.button"), ButtonType.danger);
		removeConfirmationCtrl.setUserObject(row);
		listenTo(removeConfirmationCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeConfirmationCtrl.getInitialComponent(),
				true, translate("offer.survey.remove"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doRemove(OfferSurveyRow row) {
		acService.deleteOfferSurvey(row.getSurvey());
		loadModel();
	}

	private void doOpenSurveyDetails(UserRequest ureq, OfferSurveyRow row) {
		doCloseSurveyDetails(row);

		OfferSurveyParticipationListController detailsCtrl = new OfferSurveyParticipationListController(ureq, getWindowControl(), mainForm, row.getSurvey());
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doCloseSurveyDetails(OfferSurveyRow row) {
		if (row.getDetailsController() == null) return;

		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}

	private void doOpenTools(UserRequest ureq, OfferSurveyRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final List<String> names = new ArrayList<>(2);

		private final OfferSurveyRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, OfferSurveyRow row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("offer_survey_tools");
			putInitialPanel(mainVC);

			Link openLink = addLink(CMD_OPEN, "offer.survey.open", "o_icon o_icon-fw o_icon_quickview");
			String businessPath = "[RepositoryEntry:" + row.getFormEntry().getKey() + "]";
			openLink.setUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath));
			if (!readOnly) {
				names.add("divider");
				addLink(CMD_REMOVE, "offer.survey.remove", "o_icon o_icon-fw o_icon_delete_item");
			}

			mainVC.contextPut("names", names);
		}

		private Link addLink(String cmd, String i18nKey, String iconCSS) {
			Link link = LinkFactory.createLink(cmd, cmd, cmd, i18nKey, getTranslator(), mainVC, this, Link.LINK);
			if (iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(cmd, link);
			names.add(cmd);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source instanceof Link link) {
				String cmd = link.getCommand();
				fireEvent(ureq, Event.DONE_EVENT);
				if (CMD_OPEN.equals(cmd)) {
					doOpenForm(ureq, row);
				} else if (CMD_REMOVE.equals(cmd)) {
					doConfirmRemove(ureq, row);
				}
			}
		}

	}

}
