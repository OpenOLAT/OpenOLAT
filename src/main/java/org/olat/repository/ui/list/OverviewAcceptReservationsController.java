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
package org.olat.repository.ui.list;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-03-09<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OverviewAcceptReservationsController extends BasicController {

	public static final Event RESERVATION_CHANGED_EVENT = new Event("reservation.changed");

	private static final String CMD_DETAILS = "details";
	private static final String CMD_ACCEPT = "accept";
	private static final String CMD_DECLINE = "decline";

	private final VelocityContainer mainVC;
	private final InfoPanel infoPanel;
	private LightboxController lightboxCtrl;
	private OverviewReservationDetailController detailCtrl;

	private final List<OverviewReservationRow> rows;
	private final MapperKey mapperThumbnailKey;
	private final MapperKey ceMapperKey;

	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private MapperService mapperService;

	public OverviewAcceptReservationsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		mainVC = createVelocityContainer("overview_accept_reservations");

		infoPanel = new InfoPanel("infoPanel");
		infoPanel.setElementCssClass("o_warning_with_icon");
		infoPanel.setTitle(translate("overview.reservation.title"));
		infoPanel.setInformations(mainVC);
		infoPanel.setPersistedStatusId(ureq, "overview-accept-reservations-v1");

		RepositoryEntryImageMapper reImageMapper = RepositoryEntryImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, reImageMapper);

		CurriculumElementImageMapper ceImageMapper = CurriculumElementImageMapper.mapper210x140();
		ceMapperKey = mapperService.register(null, CurriculumElementImageMapper.MAPPER_ID_210_140, ceImageMapper);

		rows = new ArrayList<>();
		loadReservations(reImageMapper, ceImageMapper);
		updateUI();

		putInitialPanel(infoPanel);
	}

	public boolean hasReservations() {
		return !rows.isEmpty();
	}

	private void loadReservations(RepositoryEntryImageMapper reImageMapper, CurriculumElementImageMapper ceImageMapper) {
		List<ResourceReservation> reservations = acService.getReservations(getIdentity());

		List<ResourceReservation> participantReservations = reservations.stream()
				.filter(r -> r.getConfirmableBy() == ConfirmationByEnum.PARTICIPANT)
				.toList();
		if (participantReservations.isEmpty()) {
			return;
		}

		List<ResourceReservation> ceReservations = participantReservations.stream()
				.filter(r -> "CurriculumElement".equals(r.getResource().getResourceableTypeName()))
				.toList();
		List<ResourceReservation> reReservations = participantReservations.stream()
				.filter(r -> !"CurriculumElement".equals(r.getResource().getResourceableTypeName()))
				.toList();

		if (!reReservations.isEmpty()) {
			List<Long> resourceKeys = reReservations.stream()
					.map(r -> r.getResource().getKey())
					.toList();
			Map<Long, RepositoryEntry> resourceKeyToEntry = repositoryService.loadByResourceKeys(resourceKeys).stream()
					.collect(Collectors.toMap(e -> e.getOlatResource().getKey(), Function.identity(), (a, b) -> a));

			List<RepositoryEntry> repoEntries = new ArrayList<>();
			for (ResourceReservation reservation : reReservations) {
				RepositoryEntry entry = resourceKeyToEntry.get(reservation.getResource().getKey());
				if (entry != null) {
					repoEntries.add(entry);
				}
			}

			Map<Long, VFSThumbnailInfos> repoThumbnails = reImageMapper.getRepositoryThumbnails(repoEntries);

			for (ResourceReservation reservation : reReservations) {
				RepositoryEntry entry = resourceKeyToEntry.get(reservation.getResource().getKey());
				if (entry != null) {
					addRepositoryEntryRow(reservation, entry, repoThumbnails);
				}
			}
		}

		if (!ceReservations.isEmpty()) {
			List<CurriculumElementRefImpl> elementRefs = ceReservations.stream()
					.map(r -> new CurriculumElementRefImpl(r.getResource().getResourceableId()))
					.toList();
			Map<Long, CurriculumElement> resourceKeyToElement = curriculumService.getCurriculumElements(elementRefs).stream()
					.collect(Collectors.toMap(e -> e.getResource().getKey(), Function.identity(), (a, b) -> a));

			List<CurriculumElement> elements = new ArrayList<>(resourceKeyToElement.values());
			Map<Long, VFSThumbnailInfos> ceThumbnails = ceImageMapper.getThumbnails(elements);
			Map<Long, Set<RepositoryEntry>> entriesByElementKey = curriculumService.getCurriculumElementKeyToRepositoryEntries(elements);
			Set<Long> elementKeysWithChildren = elements.stream()
					.filter(e -> curriculumService.hasCurriculumElementChildren(e))
					.map(CurriculumElement::getKey)
					.collect(Collectors.toSet());

			for (ResourceReservation reservation : ceReservations) {
				CurriculumElement element = resourceKeyToElement.get(reservation.getResource().getKey());
				if (element != null) {
					addCurriculumElementRow(reservation, element, ceThumbnails, entriesByElementKey, elementKeysWithChildren);
				}
			}
		}

		Collator collator = Collator.getInstance(getLocale());
		rows.sort((a, b) -> collator.compare(a.getDisplayName(), b.getDisplayName()));
	}

	private void addRepositoryEntryRow(ResourceReservation reservation, RepositoryEntry entry,
			Map<Long, VFSThumbnailInfos> thumbnails) {
		String translatedType = translate(entry.getOlatResource().getResourceableTypeName());

		VFSThumbnailInfos thumb = thumbnails.get(entry.getKey());
		String thumbUrl = thumb != null
				? RepositoryEntryImageMapper.getImageURL(mapperThumbnailKey.getUrl(), thumb.metadata(), thumb.thumbnailMetadata())
				: null;

		boolean detailsAvailable = StringHelper.containsNonWhitespace(entry.getDescription());

		OverviewReservationRow row = new OverviewReservationRow(reservation, entry.getDisplayname(),
				entry.getExternalRef(), translatedType, entry.getDescription(), thumbUrl,
				detailsAvailable, entry.getKey(), null);
		addRowLinks(row);
		rows.add(row);
	}

	private void addCurriculumElementRow(ResourceReservation reservation, CurriculumElement element,
			Map<Long, VFSThumbnailInfos> thumbnails, Map<Long, Set<RepositoryEntry>> entriesByElementKey,
			Set<Long> elementKeysWithChildren) {
		String translatedType = element.getType() != null ? element.getType().getDisplayName() : null;

		VFSThumbnailInfos thumb = thumbnails.get(element.getKey());
		String thumbUrl = thumb != null
				? CurriculumElementImageMapper.getImageURL(ceMapperKey.getUrl(), thumb.metadata(), thumb.thumbnailMetadata())
				: null;

		boolean detailsAvailable = StringHelper.containsNonWhitespace(element.getDescription())
				|| elementKeysWithChildren.contains(element.getKey())
				|| !entriesByElementKey.getOrDefault(element.getKey(), Set.of()).isEmpty();

		OverviewReservationRow row = new OverviewReservationRow(reservation, element.getDisplayName(),
				element.getIdentifier(), translatedType, element.getDescription(), thumbUrl,
				detailsAvailable, null, element.getKey());
		addRowLinks(row);
		rows.add(row);
	}

	private void addRowLinks(OverviewReservationRow row) {
		Long key = row.getKey();

		if (row.isDetailsAvailable()) {
			Link detailsLink = LinkFactory.createCustomLink("details_" + key, CMD_DETAILS, "details_" + key, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			detailsLink.setCustomDisplayText(translate("overview.reservation.details"));
			detailsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_circle_info");
			detailsLink.setLabelCSS("o_label_text");
			detailsLink.setGhost(true);
			detailsLink.setUserObject(row);
			row.setDetailsLink(detailsLink);
		}

		String acceptText = translate("accept.pending.reservation.accept");
		Link acceptLink = LinkFactory.createCustomLink("accept_" + key, CMD_ACCEPT, "accept_" + key, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		acceptLink.setCustomDisplayText(acceptText);
		acceptLink.setIconLeftCSS("o_icon o_icon-fw o_icon_accepted");
		acceptLink.setLabelCSS("o_label_text");
		acceptLink.setTitle(acceptText);
		acceptLink.setUserObject(row);
		row.setAcceptLink(acceptLink);

		String declineText = translate("accept.pending.reservation.decline");
		Link declineLink = LinkFactory.createCustomLink("decline_" + key, CMD_DECLINE, "decline_" + key, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
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
			text = translate("overview.reservation.text.singular");
		} else {
			text = translate("overview.reservation.text.plural", String.valueOf(rows.size()));
		}
		mainVC.contextPut("text", text);
		infoPanel.setVisible(!rows.isEmpty());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && link.getUserObject() instanceof OverviewReservationRow row) {
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

	private void doOpenDetails(UserRequest ureq, OverviewReservationRow row) {
		detailCtrl = new OverviewReservationDetailController(ureq, getWindowControl(), row);
		listenTo(detailCtrl);

		lightboxCtrl = new LightboxController(ureq, getWindowControl(), detailCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	private void doAccept(UserRequest ureq, OverviewReservationRow row) {
		ResourceReservation reservation = acService.getReservation(getIdentity(),
				row.getReservation().getResource());
		if (reservation != null) {
			acService.acceptReservationToResource(getIdentity(), reservation);
		}
		rows.remove(row);
		updateUI();
		fireEvent(ureq, RESERVATION_CHANGED_EVENT);
	}

	private void doDecline(UserRequest ureq, OverviewReservationRow row) {
		ResourceReservation reservation = acService.getReservation(getIdentity(),
				row.getReservation().getResource());
		if (reservation != null) {
			acService.removeReservation(getIdentity(), getIdentity(), reservation, null);
		}
		rows.remove(row);
		updateUI();
		fireEvent(ureq, RESERVATION_CHANGED_EVENT);
	}
}
