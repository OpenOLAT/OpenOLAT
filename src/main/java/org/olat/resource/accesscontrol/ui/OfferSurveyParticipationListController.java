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
import java.util.Objects;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.member.CurriculumElementMemberUsersController;
import org.olat.modules.forms.CoachCandidates;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferSurveyParticipationIdentifiers;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.ui.OfferSurveyParticipationListTableModel.OfferSurveyParticipationCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lists the participations of one booking order form survey, across all
 * offers and orders that use it.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyParticipationListController extends FormBasicController {

	public static final int USER_PROPS_OFFSET = 500;

	private static final String CMD_VIEW = "view";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_TOOLS = "tools";
	private static final String FILTER_STATUS = "status";

	private FormLink openFormLink;
	private FormLink exportLink;
	private OfferSurveyParticipationListTableModel tableModel;
	private FlexiTableElement tableEl;
	private List<UserPropertyHandler> userPropertyHandlers;

	private CloseableModalController cmc;
	private EvaluationFormExecutionController executionCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final EvaluationFormSurvey survey;
	private List<EvaluationFormParticipation> participations;

	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private UserManager userManager;

	public OfferSurveyParticipationListController(UserRequest ureq, WindowControl wControl, Form mainForm, EvaluationFormSurvey survey) {
		super(ureq, wControl, LAYOUT_CUSTOM, "offer_survey_participation_list", mainForm);
		this.survey = survey;

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(
				CurriculumElementMemberUsersController.class.getCanonicalName(), isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("title", survey.getFormEntry().getDisplayname());
			layoutCont.contextPut("externalRef", survey.getFormEntry().getExternalRef());
		}

		openFormLink = uifactory.addFormLink("offer.survey.open", formLayout, Link.BUTTON);
		openFormLink.setIconLeftCSS("o_icon o_icon-lg o_icon_external_link");
		openFormLink.setNewWindow(true, true, false);

		exportLink = uifactory.addFormLink("offer.survey.participation.export", formLayout, Link.BUTTON);
		exportLink.setIconLeftCSS("o_icon o_icon-lg o_icon_download");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colIndex = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, null, false, null));
			colIndex++;
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyParticipationCols.offer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyParticipationCols.order));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyParticipationCols.status, new OfferSurveyParticipationStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OfferSurveyParticipationCols.submissionDate, new DateTimeFlexiCellRenderer(getLocale())));
		DefaultFlexiColumnModel viewColumn = new DefaultFlexiColumnModel(OfferSurveyParticipationCols.view);
		viewColumn.setIconHeader("o_icon o_icon_quickview");
		columnsModel.addFlexiColumnModel(viewColumn);
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(OfferSurveyParticipationCols.tools));

		tableModel = new OfferSurveyParticipationListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "offerSurveyParticipationTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);

		initFilters();
		initFilterTabs(ureq);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(EvaluationFormParticipationStatus.prepared.name(), translate("offer.survey.participation.status.prepared")));
		statusValues.add(SelectionValues.entry(EvaluationFormParticipationStatus.done.name(), translate("offer.survey.participation.status.done")));
		statusValues.add(SelectionValues.entry(EvaluationFormParticipationStatus.canceled.name(), translate("offer.survey.participation.status.canceled")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("offer.survey.participation.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);

		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);

		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EvaluationFormParticipationStatus.prepared.name(),
				translate("offer.survey.participation.status.prepared"), TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(EvaluationFormParticipationStatus.prepared.name()))));
		tabs.add(openTab);

		FlexiFiltersTab completedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EvaluationFormParticipationStatus.done.name(),
				translate("offer.survey.participation.status.done"), TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(EvaluationFormParticipationStatus.done.name()))));
		tabs.add(completedTab);

		FlexiFiltersTab canceledTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EvaluationFormParticipationStatus.canceled.name(),
				translate("offer.survey.participation.status.canceled"), TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(EvaluationFormParticipationStatus.canceled.name()))));
		tabs.add(canceledTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void loadModel() {
		Map<Long, Offer> offerByKey = new HashMap<>();
		for (OfferToSurvey offerToSurvey : acService.loadOfferToSurveys(survey)) {
			offerByKey.put(offerToSurvey.getOffer().getKey(), offerToSurvey.getOffer());
		}

		participations = acService.loadOfferSurveyParticipations(survey);
		List<String> statusFilterValues = getStatusFilterValues();

		SessionFilter filter = SessionFilterFactory.createOfParticipations(participations);
		Map<Long, EvaluationFormSession> sessionByParticipationKey = new HashMap<>();
		for (EvaluationFormSession session : evaluationFormManager.loadSessionsFiltered(filter, 0, -1)) {
			if (session.getParticipation() != null) {
				sessionByParticipationKey.put(session.getParticipation().getKey(), session);
			}
		}

		List<OfferSurveyParticipationRow> rows = new ArrayList<>(participations.size());
		for (EvaluationFormParticipation participation : participations) {
			if (statusFilterValues != null && !statusFilterValues.isEmpty()
					&& !statusFilterValues.contains(participation.getStatus().name())) {
				continue;
			}

			Long orderKey = OfferSurveyParticipationIdentifiers.getOrderKey(participation.getIdentifier());
			Order order = acService.loadOrderByKey(orderKey);
			Offer offer = resolveOffer(order, offerByKey);
			String offerLabel = offer != null ? getOfferLabel(offer) : null;
			EvaluationFormSession session = sessionByParticipationKey.get(participation.getKey());

			OfferSurveyParticipationRow row = new OfferSurveyParticipationRow(participation, order, offer, offerLabel,
					session, userPropertyHandlers, getLocale());
			forgeLinks(row);
			rows.add(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<String> getStatusFilterValues() {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_STATUS);
		if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			return extendedFilter.getValues();
		}
		return null;
	}

	private Offer resolveOffer(Order order, Map<Long, Offer> offerByKey) {
		if (order == null) {
			return null;
		}
		return order.getParts().stream()
				.flatMap(part -> part.getOrderLines().stream())
				.map(line -> offerByKey.get(line.getOffer().getKey()))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private String getOfferLabel(Offer offer) {
		String label = offer.getLabel();
		if (StringHelper.containsNonWhitespace(label)) {
			return label;
		}
		List<OfferAccess> offerAccesses = acService.getOfferAccess(offer, true);
		if (!offerAccesses.isEmpty()) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(offerAccesses.get(0).getMethod().getType());
			if (handler != null) {
				return handler.getMethodName(getLocale());
			}
		}
		return translate("offer.survey.offer.column");
	}

	private void forgeLinks(OfferSurveyParticipationRow row) {
		FormLink viewLink = uifactory.addFormLink("view_" + row.getKey(), CMD_VIEW, "", null, flc, Link.NONTRANSLATED + Link.LINK);
		viewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quickview");
		viewLink.setTitle(translate("offer.survey.participation.view.form"));
		viewLink.setUserObject(row);
		row.setViewLink(viewLink);

		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == openFormLink) {
			doOpenSurveyForm();
		} else if (source == exportLink) {
			doExport(ureq);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if (source instanceof FormLink link && link.getUserObject() instanceof OfferSurveyParticipationRow row) {
			if (CMD_VIEW.equals(link.getCmd())) {
				doOpenForm(ureq, row, true);
			} else if (CMD_TOOLS.equals(link.getCmd())) {
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
		if (executionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
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
		} else if (cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(executionCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		executionCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	private void doOpenForm(UserRequest ureq, OfferSurveyParticipationRow row, boolean readOnly) {
		if (guardModalController(executionCtrl)) return;

		EvaluationFormSession session = row.getSession();
		if (session == null) {
			session = evaluationFormManager.createSession(row.getParticipation());
		} else {
			session = evaluationFormManager.loadSessionByKey(session);
		}

		String titleKey = readOnly ? "offer.survey.participation.view.form" : "offer.survey.participation.edit.form";
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, CoachCandidates.NONE, readOnly, !readOnly, false, null);
		listenTo(executionCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), executionCtrl.getInitialComponent(),
				true, translate(titleKey));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenTools(UserRequest ureq, OfferSurveyParticipationRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doOpenSurveyForm() {
		String businessPath = "[RepositoryEntry:" + survey.getFormEntry().getKey() + "]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	private void doExport(UserRequest ureq) {
		SessionFilter filter = SessionFilterFactory.createOfParticipations(participations, true);
		org.olat.modules.forms.model.xml.Form form = evaluationFormManager.loadForm(survey.getFormEntry());

		UserPropertiesColumns userColumns = new UserPropertiesColumns(userPropertyHandlers, getTranslator());
		EvaluationFormExcelExport export = new EvaluationFormExcelExport(getLocale(), survey.getFormEntry(), form,
				filter, null, userColumns, survey.getFormEntry().getDisplayname());
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final List<String> names = new ArrayList<>(2);

		private final OfferSurveyParticipationRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, OfferSurveyParticipationRow row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("offer_survey_participation_tools");
			putInitialPanel(mainVC);

			addLink(CMD_VIEW, "offer.survey.participation.view.form", "o_icon o_icon-fw o_icon_quickview");
			if (row.isEditable()) {
				addLink(CMD_EDIT, "offer.survey.participation.edit.form", "o_icon o_icon-fw o_icon_edit");
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
				if (CMD_VIEW.equals(cmd)) {
					doOpenForm(ureq, row, true);
				} else if (CMD_EDIT.equals(cmd)) {
					doOpenForm(ureq, row, false);
				}
			}
		}

	}

}
