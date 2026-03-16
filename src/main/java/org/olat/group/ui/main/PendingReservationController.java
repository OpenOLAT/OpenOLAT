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
package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.repository.ui.list.OverviewAcceptReservationsController;

/**
 * Initial date: 2026-03-11<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class PendingReservationController extends BasicController implements SupportsAfterLoginInterceptor {

	private final VelocityContainer mainVC;
	private final Link laterLink;

	private final OverviewAcceptReservationsController overviewReservationsCtrl;
	private final GroupAcceptReservationsController groupReservationsCtrl;

	public PendingReservationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("pending_reservation");

		overviewReservationsCtrl = new OverviewAcceptReservationsController(ureq, wControl, false);
		listenTo(overviewReservationsCtrl);

		groupReservationsCtrl = new GroupAcceptReservationsController(ureq, wControl, false);
		listenTo(groupReservationsCtrl);

		if (overviewReservationsCtrl.hasReservations()) {
			mainVC.put("overviewReservations", overviewReservationsCtrl.getInitialComponent());
		}
		if (groupReservationsCtrl.hasReservations()) {
			mainVC.put("groupReservations", groupReservationsCtrl.getInitialComponent());
		}

		laterLink = LinkFactory.createButton("later", mainVC, this);
		laterLink.setCustomDisplayText(translate("pending.reservation.later"));

		putInitialPanel(mainVC);
	}

	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		return overviewReservationsCtrl.hasReservations() || groupReservationsCtrl.hasReservations();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == laterLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == overviewReservationsCtrl
				&& event == OverviewAcceptReservationsController.RESERVATION_CHANGED_EVENT) {
			if (!overviewReservationsCtrl.hasReservations()) {
				mainVC.remove(overviewReservationsCtrl.getInitialComponent());
			}
			checkAllDone(ureq);
		} else if (source == groupReservationsCtrl
				&& event == GroupAcceptReservationsController.RESERVATION_CHANGED_EVENT) {
			if (!groupReservationsCtrl.hasReservations()) {
				mainVC.remove(groupReservationsCtrl.getInitialComponent());
			}
			checkAllDone(ureq);
		}
	}

	private void checkAllDone(UserRequest ureq) {
		if (!overviewReservationsCtrl.hasReservations() && !groupReservationsCtrl.hasReservations()) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
