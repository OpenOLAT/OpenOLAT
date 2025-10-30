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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.manager.GroupDAO;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
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
import org.olat.modules.coach.security.PendingCourseBookingsRightProvider;
import org.olat.modules.coach.ui.PendingMembershipsTableModel.PendingMembershipsCol;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.ui.member.AcceptDeclineMembershipsController;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.UserResourceReservation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {
	public static final String USER_PROPS_ID = UserListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;

	private static final String ROW_SELECT_ACTION = "select.row";
	private static final String CMD_ACCEPT = "accept";
	private static final String CMD_DECLINE = "decline";
	private static final String CMD_TOOLS = "tools";

	private FlexiTableElement tableEl;
	private PendingMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	private FlexiFiltersTab allTab;
	private AcceptDeclineMembershipsController acceptDeclineCtrl;
	private CloseableModalController cmc;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private int counter = 0;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private ACOrderDAO orderDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BaseSecurity baseSecurity;

	public PendingMembershipsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pending_memberships");

		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		detailsVC = createVelocityContainer("pending_membership_details");

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID, userPropertyHandler);
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", true,
					userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}

		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.title, ROW_SELECT_ACTION));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.extRef, ROW_SELECT_ACTION));
		DateWithDayFlexiCellRenderer dateWithDayFlexiCellRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.begin, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.end, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.type));
		DateFlexiCellRenderer dateFlexiCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCol.confirmationUntil, dateFlexiCellRenderer));

		DefaultFlexiColumnModel acceptColumn = new DefaultFlexiColumnModel(org.olat.resource.accesscontrol.ui.PendingMembershipsTableModel.PendingMembershipCol.accept.i18nHeaderKey(),
				PendingMembershipsCol.accept.ordinal(), "accept",
				new StaticFlexiCellRenderer("", "accept", null, "o_icon o_icon-fw o_icon_accepted", translate("accept")));
		acceptColumn.setIconHeader("o_icon o_icon-fw o_icon_accepted");
		acceptColumn.setHeaderLabel(translate("accept"));
		acceptColumn.setExportable(false);
		acceptColumn.setAlwaysVisible(true);
		columnModel.addFlexiColumnModel(acceptColumn);

		DefaultFlexiColumnModel declineColumn = new DefaultFlexiColumnModel(org.olat.resource.accesscontrol.ui.PendingMembershipsTableModel.PendingMembershipCol.decline.i18nHeaderKey(),
				PendingMembershipsCol.decline.ordinal(), "decline",
				new StaticFlexiCellRenderer("", "decline", null, "o_icon o_icon-fw o_icon_decline", translate("decline")));
		declineColumn.setIconHeader("o_icon o_icon-fw o_icon_decline");
		declineColumn.setHeaderLabel(translate("decline"));
		declineColumn.setExportable(false);
		declineColumn.setAlwaysVisible(true);
		columnModel.addFlexiColumnModel(declineColumn);

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(PendingMembershipsCol.tools);
		toolsColumn.setIconHeader("o_icon o_icon-lg o_icon_actions");
		toolsColumn.setAlwaysVisible(true);
		toolsColumn.setExportable(false);
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new PendingMembershipsTableModel(userManager, userPropertyHandlers, columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list", tableModel, 25, false, getTranslator(), formLayout);
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
			} else if (event instanceof SelectionEvent selectionEvent) {
				int rowIndex = selectionEvent.getIndex();
				PendingMembershipRow row = tableModel.getObject(rowIndex);
				if (tableEl.isDetailsExpended(rowIndex)) {
					tableEl.collapseDetails(rowIndex);
					doCloseDetails(row);
				} else {
					tableEl.expandDetails(rowIndex);
					doOpenDetails(ureq, row);
				}
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
				if (event == Event.CHANGED_EVENT) {
					loadModel();
					checkAcceptDeclineOutcome(row.getIdentityKey(), row.getCurriculumElementKey());
					fireEvent(ureq, event);
				}
			}
		} else if (acceptDeclineCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				Long identityKey = null;
				if (acceptDeclineCtrl.getUserObject() instanceof Long key) {
					identityKey = key;
				}
				Long curriculumElementKey = acceptDeclineCtrl.getSelectedCurriculumElement() != null ? acceptDeclineCtrl.getSelectedCurriculumElement().getKey() : null;
				checkAcceptDeclineOutcome(identityKey, curriculumElementKey);
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, event);
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

	private void checkAcceptDeclineOutcome(Long identityKey, Long curriculumElementKey) {
		if (identityKey == null) {
			return;
		}

		Identity identity = baseSecurity.loadIdentityByKey(identityKey);
		if (identity == null) {
			return;
		}

		if (curriculumElementKey == null) {
			return;
		}

		CurriculumElement curriculumElement = curriculumElementDao.loadByKey(curriculumElementKey);
		if (curriculumElement == null) {
			return;
		}
		
		if (groupDao.hasRole(curriculumElement.getGroup(), identity, CurriculumRoles.participant.name())) {
			showInfo("pending.membership.accepted");
		} else {
			showInfo("pending.membership.declined");
		}
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
		List<UserResourceReservation> userResourceReservations = orderDao.getReservationsWithOrders(getIdentity(),
						PendingCourseBookingsRightProvider.RELATION_RIGHT, userPropertyHandlers).stream()
				.filter(urr -> {
					String type = urr.getResourceReservation().getType();
					return StringHelper.containsNonWhitespace(type) && type.startsWith(CurriculumService.RESERVATION_PREFIX);
				}).toList();
		Set<OLATResource> resources = userResourceReservations.stream()
				.map(UserResourceReservation::getResourceReservation)
				.map(ResourceReservation::getResource).collect(Collectors.toSet());
		List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(resources);
		Map<Long, CurriculumElement> resourceKeyToElement = new HashMap<>();
		for (CurriculumElement curriculumElement : curriculumElements) {
			resourceKeyToElement.put(curriculumElement.getResource().getKey(), curriculumElement);
		}
		String searchString = tableEl.getQuickSearchString() != null ? tableEl.getQuickSearchString().toLowerCase() : null;
		List<PendingMembershipRow> rows = userResourceReservations.stream().map(urr -> toRow(urr, resourceKeyToElement))
				.filter(Objects::nonNull)
				.filter(urr -> { return urr.matchesSearchString(searchString); })
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private PendingMembershipRow toRow(UserResourceReservation urr, Map<Long, CurriculumElement> resourceKeyToElement) {
		CurriculumElement curriculumElement = resourceKeyToElement.get(urr.getResourceReservation().getResource().getKey());
		if (curriculumElement == null) {
			return null;
		}
		if (!curriculumElement.getType().isAllowedAsRootElement()) {
			return null;
		}
		String extRef = curriculumElement.getIdentifier();
		PendingMembershipRow row = new PendingMembershipRow(urr.getResourceReservation().getIdentity(),
				curriculumElement.getDisplayName(), extRef, curriculumElement.getBeginDate(),
				curriculumElement.getEndDate(),
				curriculumElement.getType() != null ? curriculumElement.getType().getDisplayName() : "",
				urr.getResourceReservation().getExpirationDate(), curriculumElement.getKey(),
				urr.getResourceReservation().getKey(), userPropertyHandlers, getLocale());
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
		}
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
		Identity identity = baseSecurity.loadIdentityByKey(row.getIdentityKey());

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
		acceptDeclineCtrl.setUserObject(reservation.getIdentity().getKey());
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
			mainVC = createVelocityContainer("tools_pending_memberships");

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