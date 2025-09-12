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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
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
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.ui.member.AcceptDeclineMembershipsController;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.repository.ui.list.ImplementationEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.ui.PendingMembershipsTableModel.PendingMembershipCol;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-09-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String ROW_SELECT_ACTION = "select.row";
	private static final String CMD_ACCEPT = "accept";
	private static final String CMD_DECLINE = "decline";
	private static final String CMD_TOOLS = "tools";

	private final Identity identity;
	private FlexiTableElement tableEl;
	private PendingMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	private FlexiFiltersTab allTab;
	private AcceptDeclineMembershipsController acceptDeclineCtrl;
	private CloseableModalController cmc;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private int counter = 0;

	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private ACReservationDAO reservationDao;

	public PendingMembershipsController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, "pending_memberships");
		this.identity = identity;

		detailsVC = createVelocityContainer("pending_membership_details");

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.title, ROW_SELECT_ACTION));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.extRef, ROW_SELECT_ACTION));
		DateWithDayFlexiCellRenderer dateWithDayFlexiCellRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.begin, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.end, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.type));
		DateFlexiCellRenderer dateCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.confirmationUntil, dateCellRenderer));

		DefaultFlexiColumnModel acceptColumn = new DefaultFlexiColumnModel(PendingMembershipCol.accept.i18nHeaderKey(), 
				PendingMembershipCol.accept.ordinal(), "accept",
				new StaticFlexiCellRenderer("", "accept", null, "o_icon o_icon-fw o_icon_accepted", translate("accept")));
		acceptColumn.setIconHeader("o_icon o_icon-fw o_icon_accepted");
		acceptColumn.setHeaderLabel(translate("accept"));
		acceptColumn.setExportable(false);
		acceptColumn.setAlwaysVisible(true);
		columnModel.addFlexiColumnModel(acceptColumn);

		DefaultFlexiColumnModel declineColumn = new DefaultFlexiColumnModel(PendingMembershipCol.decline.i18nHeaderKey(),
				PendingMembershipCol.decline.ordinal(), "decline",
				new StaticFlexiCellRenderer("", "decline", null, "o_icon o_icon-fw o_icon_decline", translate("decline")));
		declineColumn.setIconHeader("o_icon o_icon-fw o_icon_decline");
		declineColumn.setHeaderLabel(translate("decline"));
		declineColumn.setExportable(false);
		declineColumn.setAlwaysVisible(true);
		columnModel.addFlexiColumnModel(declineColumn);

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(PendingMembershipCol.tools);
		toolsColumn.setIconHeader("o_icon o_icon-lg o_icon_actions");
		toolsColumn.setAlwaysVisible(true);
		toolsColumn.setExportable(false);
		columnModel.addFlexiColumnModel(toolsColumn);
		
		tableModel = new  PendingMembershipsTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		//tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		initFiltersPreset(ureq);
	}

	private void initFiltersPreset(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"), 
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof DetailsToggleEvent detailsToggleEvent) {
				PendingMembershipRow row = tableModel.getObject(detailsToggleEvent.getRowIndex());
				if (detailsToggleEvent.isVisible()) {
					doOpenDetails(ureq, row);
				} else {
					doCloseDetails(row);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if (source instanceof FormLink formLink) {
			if (formLink.getUserObject() instanceof PendingMembershipRow row) {
				if (CMD_TOOLS.equals(formLink.getCmd())) {
					doOpenTools(ureq, row, formLink);
				} else if (CMD_ACCEPT.equals(formLink.getCmd())) {
					doAcceptDeclineOne(ureq, row, true);
				} else if (CMD_DECLINE.equals(formLink.getCmd())) {
					doAcceptDeclineOne(ureq, row, false);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof MemberDetailsController detailsCtrl) {
			if (detailsCtrl.getUserObject() instanceof PendingMembershipRow row) {
				if (event instanceof ImplementationEvent) {
					doLearnMoreAboutImplementation(ureq, row);
				}
			}
		} else if (acceptDeclineCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				cmc.deactivate();
				cleanUp();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}		
		} else if (cmc == source) {
			cleanUp();
		} else if (calloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (calloutCtrl != null) {
					calloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(acceptDeclineCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(calloutCtrl);
		acceptDeclineCtrl = null;
		toolsCtrl = null;
		cmc = null;
		calloutCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		List<ResourceReservation> reservations = reservationDao.loadReservations(identity).stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getType()))
				.filter(r -> r.getType().startsWith(CurriculumService.RESERVATION_PREFIX))
				.toList();
		Set<OLATResource> resources = reservations.stream().map(ResourceReservation::getResource).collect(Collectors.toSet());
		List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(resources);
		Map<Long, CurriculumElement> resourceKeyToElement = new HashMap<>();
		for (CurriculumElement curriculumElement : curriculumElements) {
			resourceKeyToElement.put(curriculumElement.getResource().getKey(), curriculumElement);
		}
		String searchString = tableEl.getQuickSearchString() != null ? tableEl.getQuickSearchString().toLowerCase() : null;
		List<PendingMembershipRow> rows = reservations.stream().map(r -> toRow(r, resourceKeyToElement))
				.filter(r -> {
					if (!StringHelper.containsNonWhitespace(searchString)) {
						return true;
					}
					if (StringHelper.containsNonWhitespace(r.getTitle()) && r.getTitle().toLowerCase().contains(searchString)) {
						return true;
					}
					if (StringHelper.containsNonWhitespace(r.getExtRef()) && r.getExtRef().toLowerCase().contains(searchString)) {
						return true;
					}
					return false;
				})
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private PendingMembershipRow toRow(ResourceReservation reservation, Map<Long, CurriculumElement> resourceKeyToElement) {
		CurriculumElement curriculumElement = resourceKeyToElement.get(reservation.getResource().getKey());
		PendingMembershipRow row = new PendingMembershipRow(curriculumElement.getDisplayName(), curriculumElement.getIdentifier(),
				curriculumElement.getBeginDate(), curriculumElement.getEndDate(),
				curriculumElement.getType() != null ? curriculumElement.getType().getDisplayName() : "",
				reservation.getExpirationDate(), curriculumElement.getKey(), reservation.getKey());
		
		forgeLinks(row);
		return row;
	}

	private void forgeLinks(PendingMembershipRow row) {
		String id = Integer.toString(++counter);

		FormLink acceptLink = uifactory.addFormLink("accept_".concat(id), CMD_ACCEPT, "", null, null, Link.NONTRANSLATED);
		acceptLink.setIconLeftCSS("o_icon o_icon-fw o_icon_accepted");
		acceptLink.setTitle(translate("accept"));
		acceptLink.setUserObject(row);
		row.setAcceptLink(acceptLink);

		FormLink declineLink = uifactory.addFormLink("decline_".concat(id), CMD_DECLINE, "", null, null, Link.NONTRANSLATED);
		declineLink.setIconLeftCSS("o_icon o_icon-fw o_icon_decline");
		declineLink.setTitle(translate("decline"));
		declineLink.setUserObject(row);
		row.setDeclineLink(declineLink);

		FormLink toolsLink = uifactory.addFormLink("tools_".concat(id), CMD_TOOLS, "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof PendingMembershipRow pendingMembershipRow && 
				pendingMembershipRow.getDetailsController() != null) {
			components.add(pendingMembershipRow.getDetailsController().getInitialFormItem().getComponent());
		};
		return components;
	}
	
	private void doOpenDetails(UserRequest ureq, PendingMembershipRow row) {
		if (row == null) {
			return;
		}
		
		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}

		CurriculumElement curriculumElement = curriculumElementDao.loadByKey(row.getCurriculumElementKey());
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(curriculumElement);
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		MemberDetailsConfig detailsConfig = new MemberDetailsConfig(null, null, false, true, false, false, true, 
				true, true, true, true, true);
		
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculumElement.getCurriculum(), curriculumElement, elements, identity, detailsConfig);
		detailsCtrl.setUserObject(row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private void doCloseDetails(PendingMembershipRow row) {
		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
			row.setDetailsController(null);
		}
	}
	
	private void doLearnMoreAboutImplementation(UserRequest ureq, PendingMembershipRow row) {
		//
	}
	
	private void doAcceptDeclineOne(UserRequest ureq, PendingMembershipRow row, boolean accept) {
		CurriculumElement curriculumElement = curriculumElementDao.loadByKey(row.getCurriculumElementKey());
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(curriculumElement);
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		ResourceReservation reservation = reservationDao.loadReservation(row.getReservationKey());
		List<ResourceReservation> reservations = List.of(reservation);
		acceptDeclineCtrl = new AcceptDeclineMembershipsController(ureq, getWindowControl(), 
				curriculumElement.getCurriculum(), curriculumElement, elements, reservations, 
				accept ? GroupMembershipStatus.active : GroupMembershipStatus.declined);
		listenTo(acceptDeclineCtrl);
		
		String title = translate(accept ? "accept.membership" : "decline.membership");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				acceptDeclineCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenTools(UserRequest ureq, PendingMembershipRow row, FormLink formLink) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsCtrl.getInitialComponent(),
				formLink.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		private final PendingMembershipRow row;
		private final List<String> names = new ArrayList<>();
		private final VelocityContainer mainVC;

		protected ToolsController(UserRequest ureq, WindowControl wControl, PendingMembershipRow row) {
			super(ureq, wControl);

			this.row = row;
			mainVC = createVelocityContainer("tools");
			
			addLink("accept", CMD_ACCEPT, "o_icon o_icon-fw o_icon_accepted");
			addLink("decline", CMD_DECLINE, "o_icon o_icon-fw o_icon_decline");
			
			mainVC.contextPut("links", names);
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if (StringHelper.containsNonWhitespace(iconCSS)) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			
			if (source instanceof Link link) {
				String cmd = link.getCommand();
				if (CMD_ACCEPT.equals(cmd)) {
					doAcceptDeclineOne(ureq, row, true);
				} else if (CMD_DECLINE.equals(cmd)) {
					doAcceptDeclineOne(ureq, row, false);
				}
			}
		}
	}
}
