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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.forms.CoachCandidates;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.ui.OrderFormListTableModel.OrderFormCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lists the booking order forms of one or several orders, for the order
 * detail view (one order) and the membership detail view (all orders of one
 * identity).
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrderFormListController extends FormBasicController {

	private static final String CMD_VIEW = "view";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_TOOLS = "tools";

	private OrderFormListTableModel tableModel;
	private FlexiTableElement tableEl;

	private CloseableModalController cmc;
	private EvaluationFormExecutionController executionCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final List<Order> orders;
	private final boolean showOrderColumn;

	@Autowired
	private ACService acService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public OrderFormListController(UserRequest ureq, WindowControl wControl, Form rootForm, List<Order> orders,
			boolean showOrderColumn) {
		super(ureq, wControl, LAYOUT_CUSTOM, "order_form_list", rootForm);
		this.orders = orders;
		this.showOrderColumn = showOrderColumn;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.reference));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.stepName));
		if (showOrderColumn) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.order));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.status, new OfferSurveyParticipationStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderFormCols.submissionDate, new DateTimeFlexiCellRenderer(getLocale())));
		DefaultFlexiColumnModel viewColumn = new DefaultFlexiColumnModel(OrderFormCols.view);
		viewColumn.setIconHeader("o_icon o_icon_quickview");
		columnsModel.addFlexiColumnModel(viewColumn);
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(OrderFormCols.tools));

		tableModel = new OrderFormListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderFormTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
	}

	private void loadModel() {
		List<OrderFormRow> rows = new ArrayList<>();
		Map<Long, List<OfferToSurvey>> offerToSurveysByOfferKey = new HashMap<>();

		for (Order order : orders) {
			for (Offer offer : getOffers(order)) {
				List<OfferToSurvey> offerToSurveys = offerToSurveysByOfferKey.computeIfAbsent(offer.getKey(),
						key -> acService.loadOfferToSurveys(offer));
				for (OfferToSurvey offerToSurvey : offerToSurveys) {
					EvaluationFormSurvey survey = offerToSurvey.getSurvey();
					for (EvaluationFormParticipation participation : acService.loadOfferSurveyParticipations(survey, order)) {
						rows.add(new OrderFormRow(survey, participation, order));
					}
				}
			}
		}

		List<EvaluationFormParticipation> participations = rows.stream().map(OrderFormRow::getParticipation).toList();
		Map<Long, EvaluationFormSession> sessionByParticipationKey = loadSessionsByParticipationKey(participations);
		for (OrderFormRow row : rows) {
			row.setSession(sessionByParticipationKey.get(row.getParticipation().getKey()));
			forgeLinks(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.setVisible(!rows.isEmpty());
	}

	private Map<Long, EvaluationFormSession> loadSessionsByParticipationKey(List<EvaluationFormParticipation> participations) {
		if (participations.isEmpty()) {
			return Map.of();
		}
		SessionFilter filter = SessionFilterFactory.createOfParticipations(participations);
		Map<Long, EvaluationFormSession> sessionByParticipationKey = new HashMap<>();
		for (EvaluationFormSession session : evaluationFormManager.loadSessionsFiltered(filter, 0, -1)) {
			if (session.getParticipation() != null) {
				sessionByParticipationKey.put(session.getParticipation().getKey(), session);
			}
		}
		return sessionByParticipationKey;
	}

	private List<Offer> getOffers(Order order) {
		Set<Long> seenOfferKeys = new LinkedHashSet<>();
		List<Offer> offers = new ArrayList<>();
		order.getParts().stream()
				.flatMap(part -> part.getOrderLines().stream())
				.map(orderLine -> orderLine.getOffer())
				.forEach(offer -> {
					if (seenOfferKeys.add(offer.getKey())) {
						offers.add(offer);
					}
				});
		return offers;
	}

	private void forgeLinks(OrderFormRow row) {
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
		if (source instanceof FormLink link && link.getUserObject() instanceof OrderFormRow row) {
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

	private void doOpenForm(UserRequest ureq, OrderFormRow row, boolean readOnly) {
		if (guardModalController(executionCtrl)) return;

		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(row.getParticipation());
		if (session == null) {
			session = evaluationFormManager.createSession(row.getParticipation());
		}

		String titleKey = readOnly ? "offer.survey.participation.view.form" : "offer.survey.participation.edit.form";
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, CoachCandidates.NONE, readOnly, !readOnly, !readOnly, false, null);
		listenTo(executionCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), executionCtrl.getInitialComponent(),
				true, translate(titleKey));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenTools(UserRequest ureq, OrderFormRow row, FormLink link) {
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

		private final OrderFormRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, OrderFormRow row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("order_form_tools");
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
