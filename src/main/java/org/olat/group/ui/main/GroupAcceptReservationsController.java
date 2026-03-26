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

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-03-11<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GroupAcceptReservationsController extends BasicController {

	public static final Event RESERVATION_CHANGED_EVENT = new Event("reservation.changed");

	private static final String CMD_DETAILS = "details";
	private static final String CMD_ACCEPT = "accept";
	private static final String CMD_DECLINE = "decline";

	private final VelocityContainer mainVC;
	private final InfoPanel infoPanel;
	private LightboxController lightboxCtrl;
	private GroupReservationDetailController detailCtrl;

	private final List<GroupReservationRow> rows = new ArrayList<>(1);

	@Autowired
	private ACService acService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public GroupAcceptReservationsController(UserRequest ureq, WindowControl wControl, boolean collapsible) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("group_accept_reservations");

		infoPanel = new InfoPanel("infoPanel");
		infoPanel.setCollapsible(collapsible);
		infoPanel.setElementCssClass("o_warning_with_icon");
		infoPanel.setTitle(translate("group.reservation.title"));
		infoPanel.setInformations(mainVC);
		infoPanel.setPersistedStatusId(ureq, "group-accept-reservations-v1");
		putInitialPanel(infoPanel);

		reload();
	}
	
	public void reload() {
		rows.clear();
		loadReservations();
		updateUI();
	}

	public boolean hasReservations() {
		return !rows.isEmpty();
	}

	private void loadReservations() {
		List<ResourceReservation> reservations = acService.getReservations(getIdentity());

		List<ResourceReservation> groupReservations = reservations.stream()
				.filter(r -> r.getConfirmableBy() == ConfirmationByEnum.PARTICIPANT)
				.filter(r -> "BusinessGroup".equals(r.getResource().getResourceableTypeName()))
				.toList();
		if (groupReservations.isEmpty()) {
			return;
		}

		List<Long> groupKeys = groupReservations.stream()
				.map(r -> r.getResource().getResourceableId())
				.toList();
		Map<Long, BusinessGroup> resourceKeyToGroup = businessGroupService.loadBusinessGroups(groupKeys).stream()
				.collect(Collectors.toMap(BusinessGroup::getKey, Function.identity(), (a, b) -> a));

		for (ResourceReservation reservation : groupReservations) {
			BusinessGroup group = resourceKeyToGroup.get(reservation.getResource().getResourceableId());
			if (group != null) {
				addRow(reservation, group);
			}
		}

		Collator collator = Collator.getInstance(getLocale());
		rows.sort((a, b) -> collator.compare(a.getDisplayName(), b.getDisplayName()));
	}

	private void addRow(ResourceReservation reservation, BusinessGroup group) {
		boolean detailsAvailable = StringHelper.containsNonWhitespace(group.getDescription());
		GroupReservationRow row = new GroupReservationRow(reservation, group.getName(), group.getDescription(),
				detailsAvailable);
		addRowLinks(row);
		rows.add(row);
	}

	private void addRowLinks(GroupReservationRow row) {
		Long key = row.getKey();

		if (row.isDetailsAvailable()) {
			Link detailsLink = LinkFactory.createCustomLink("details_" + key, CMD_DETAILS, "details_" + key,
					Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			detailsLink.setCustomDisplayText(translate("group.reservation.details"));
			detailsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_circle_info");
			detailsLink.setLabelCSS("o_label_text");
			detailsLink.setGhost(true);
			detailsLink.setUserObject(row);
			row.setDetailsLink(detailsLink);
		}

		String acceptText = translate("accept.pending.reservation.accept");
		Link acceptLink = LinkFactory.createCustomLink("accept_" + key, CMD_ACCEPT, "accept_" + key,
				Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		acceptLink.setCustomDisplayText(acceptText);
		acceptLink.setIconLeftCSS("o_icon o_icon-fw o_icon_accepted");
		acceptLink.setLabelCSS("o_label_text");
		acceptLink.setTitle(acceptText);
		acceptLink.setUserObject(row);
		row.setAcceptLink(acceptLink);

		String declineText = translate("accept.pending.reservation.decline");
		Link declineLink = LinkFactory.createCustomLink("decline_" + key, CMD_DECLINE, "decline_" + key,
				Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		declineLink.setCustomDisplayText(declineText);
		declineLink.setIconLeftCSS("o_icon o_icon-fw o_icon_decline");
		declineLink.setLabelCSS("o_label_text");
		declineLink.setTitle(declineText);
		declineLink.setPrimary(true);
		declineLink.setElementCssClass("btn-danger");
		declineLink.setUserObject(row);
		row.setDeclineLink(declineLink);
	}

	private void updateUI() {
		mainVC.contextPut("rows", rows);
		String text;
		if (rows.size() == 1) {
			text = translate("group.reservation.text.singular");
		} else {
			text = translate("group.reservation.text.plural", String.valueOf(rows.size()));
		}
		mainVC.contextPut("text", text);
		infoPanel.setVisible(!rows.isEmpty());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && link.getUserObject() instanceof GroupReservationRow row) {
			String cmd = link.getCommand();
			if (CMD_DETAILS.equals(cmd)) {
				doOpenDetails(ureq, row);
			} else if (CMD_ACCEPT.equals(cmd)) {
				doAccept(ureq, row);
			} else if (CMD_DECLINE.equals(cmd)) {
				doDecline(ureq, row);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == lightboxCtrl) {
			cleanUp();
		} else if (source == detailCtrl) {
			lightboxCtrl.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(detailCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		detailCtrl = null;
		lightboxCtrl = null;
	}

	private void doOpenDetails(UserRequest ureq, GroupReservationRow row) {
		detailCtrl = new GroupReservationDetailController(ureq, getWindowControl(), row);
		listenTo(detailCtrl);

		lightboxCtrl = new LightboxController(ureq, getWindowControl(), detailCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	private void doAccept(UserRequest ureq, GroupReservationRow row) {
		ResourceReservation reservation = acService.getReservation(getIdentity(), row.getReservation().getResource());
		if (reservation != null) {
			acService.acceptReservationToResource(getIdentity(), reservation);
		}
		rows.remove(row);
		updateUI();
		fireEvent(ureq, RESERVATION_CHANGED_EVENT);
	}

	private void doDecline(UserRequest ureq, GroupReservationRow row) {
		ResourceReservation reservation = acService.getReservation(getIdentity(), row.getReservation().getResource());
		if (reservation != null) {
			acService.removeReservation(getIdentity(), getIdentity(), reservation, null);
		}
		rows.remove(row);
		updateUI();
		fireEvent(ureq, RESERVATION_CHANGED_EVENT);
	}
}
