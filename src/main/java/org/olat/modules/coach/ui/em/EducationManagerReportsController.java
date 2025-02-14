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
package org.olat.modules.coach.ui.em;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.ui.coach.LecturesListController;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.model.UserOrder;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-12-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EducationManagerReportsController extends BasicController implements Activateable2 {

	private static final String PROPS_IDENTIFIER = EducationManagerReportsController.class.getName();
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	
	private final Link attendanceReportsLink;
	private final Link ordersLink;

	private LecturesListController lecturesListCtrl;
	
	private OrdersController ordersCtrl;

	private List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ACOrderDAO acOrderDAO;

	public EducationManagerReportsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);

		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);

		attendanceReportsLink = LinkFactory.createLink("link.attendance.report", mainVC, this);
		attendanceReportsLink.setVisible(true);
		segmentView.addSegment(attendanceReportsLink, true);
		doOpenAttendanceReports(ureq);

		ordersLink = LinkFactory.createLink("link.orders", mainVC, this);
		ordersLink.setVisible(true);
		segmentView.addSegment(ordersLink, false);
		
		if (mainVC.contextGet("segmentCmp") == null) {
			EmptyStateFactory.create("emptyStateCmp", mainVC, this);
		}

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			return;
		}

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		StateEntry nextState = entries.get(0).getTransientState();

		if ("AttendanceReports".equals(type)) {
			doOpenAttendanceReports(ureq);
			segmentView.select(attendanceReportsLink);
		} else if ("Bookings".equals(type)) {
			doOpenOrders(ureq);
			segmentView.select(ordersLink);
		}
	}

	private void doOpenAttendanceReports(UserRequest ureq) {
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		List<LectureBlockIdentityStatistics> statistics = lectureService.getLecturesStatistics(params, userPropertyHandlers, getIdentity());
		if (lecturesListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("AttendanceReports"), null);
			lecturesListCtrl = new LecturesListController(ureq, swControl, statistics, userPropertyHandlers, 
					PROPS_IDENTIFIER, true, true);
			listenTo(lecturesListCtrl);
		} else {
			lecturesListCtrl.reloadModel(statistics);
		}
		addToHistory(ureq, lecturesListCtrl);
		mainVC.put("segmentCmp", lecturesListCtrl.getInitialComponent());
	}

	private void doOpenOrders(UserRequest ureq) {
		List<UserOrder> orders = acOrderDAO.getUserBookingsForOrganizations(getIdentity(), OrganisationRoles.educationmanager, userPropertyHandlers);
		if (ordersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Bookings"), null);
			ordersCtrl = new OrdersController(ureq, swControl, orders, userPropertyHandlers, PROPS_IDENTIFIER);
			listenTo(ordersCtrl);
		} else {
			ordersCtrl.reload(orders);
		}
		addToHistory(ureq, ordersCtrl);
		mainVC.put("segmentCmp", ordersCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent segmentViewEvent) {
				String segmentComponentName = segmentViewEvent.getComponentName();
				Component segmentComponent = mainVC.getComponent(segmentComponentName);
				if (segmentComponent == attendanceReportsLink) {
					doOpenAttendanceReports(ureq);
				} else if (segmentComponent == ordersLink) {
					doOpenOrders(ureq);
				}
			}
		}
	}
}
