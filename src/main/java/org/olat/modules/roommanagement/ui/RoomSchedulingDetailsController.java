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
package org.olat.modules.roommanagement.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingDetailsController extends FormBasicController {

	private static final String EVENTS_BUSINESS_PATH = "[CurriculumAdmin:0][Events:0][All:0]";

	private FormLink openInCoursePlannerLink;
	private FormLink openEntryLink;
	private CloseableModalController cmc;
	private RoomDetailViewController roomDetailViewCtrl;

	private final RoomSchedulingRow row;
	private final UserInfoProfileConfig profileConfig;
	private RepositoryEntry repositoryEntry;
	private RepositoryEntryImageMapper mapperThumbnail;
	private MapperKey mapperThumbnailKey;

	@Autowired
	private LectureService lectureService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private RoomManagementService roomManagementService;

	public RoomSchedulingDetailsController(UserRequest ureq, WindowControl wControl, RoomSchedulingRow row, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "room_scheduling_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		this.row = row;
		profileConfig = userPortraitService.createProfileConfig();
		LectureBlock lb = row.getBooking().getLectureBlock();
		if (lb != null && lb.getEntry() != null) {
			repositoryEntry = repositoryService.loadByKey(lb.getEntry().getKey());
			if (repositoryEntry != null) {
				mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
				mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);
			}
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!(formLayout instanceof FormLayoutContainer layoutCont)) return;

		LectureBlock lb = row.getBooking().getLectureBlock();
		if (lb == null) return;

		layoutCont.contextPut("title", lb.getTitle());
		layoutCont.contextPut("externalRef", lb.getExternalRef());
		layoutCont.contextPut("warnings", row.getWarnings());
		String statusBadge = LectureBlockStatusCellRenderer.getStatusLabelSolidWithIcon(lb, false, getTranslator());
		layoutCont.contextPut("lectureBlockStatusBadge", statusBadge);

		openInCoursePlannerLink = uifactory.addFormLink("openInCoursePlanner", "openInCoursePlanner",
				"room.scheduling.details.open.in.course.planner", null, formLayout, Link.BUTTON);
		openInCoursePlannerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
		openInCoursePlannerLink.setUrl(BusinessControlFactory.getInstance()
				.getRelativeURLFromBusinessPathString(EVENTS_BUSINESS_PATH));

		initSubjects(formLayout, lb);
		initMetadata(formLayout, lb);
		initTeachers(layoutCont, ureq, lb);
		initCourse(layoutCont);
		initRooms(layoutCont, lb);
	}

	private void initSubjects(FormItemContainer formLayout, LectureBlock lb) {
		if (!taxonomyModule.isEnabled() || curriculumModule.getTaxonomyRefs().isEmpty()) return;

		Collection<TaxonomyRef> taxonomyRefs = new HashSet<>(curriculumModule.getTaxonomyRefs());
		taxonomyRefs.addAll(repositoryModule.getTaxonomyRefs());
		if (taxonomyRefs.isEmpty()) return;

		List<TaxonomyLevel> subjects = lectureService.getTaxonomy(lb);
		if (subjects == null || subjects.isEmpty()) return;

		ObjectSelectionElement taxonomyLevelEl = uifactory.addObjectSelectionElement("lecture.subjects",
				"lecture.subjects", formLayout, getWindowControl(), true,
				new TaxonomyLevelSelectionSource(getLocale(), subjects, List::of, null));
		taxonomyLevelEl.setEnabled(false);
	}

	private void initMetadata(FormItemContainer formLayout, LectureBlock lb) {
		Formatter formatter = Formatter.getInstance(getLocale());

		Date startDate = lb.getStartDate();
		if (startDate != null) {
			uifactory.addStaticTextElement("lecture.date", "lecture.date",
					formatter.formatDateWithDay(startDate), formLayout);
		}

		Date endDate = lb.getEndDate();
		if (startDate != null && endDate != null) {
			String time = translate("lecture.from.to.format.short",
					formatter.formatTimeShort(startDate), formatter.formatTimeShort(endDate));
			uifactory.addStaticTextElement("lecture.time", "lecture.time", time, formLayout);
		}

		String location = lb.getLocation();
		if (StringHelper.containsNonWhitespace(location)) {
			uifactory.addStaticTextElement("lecture.location", "lecture.location",
					"<i class=\"o_icon o_icon-fw o_icon_location\"> </i> " + StringHelper.escapeHtml(location), formLayout);
		}

		String participants = "<i class=\"o_icon o_icon-fw o_icon_user\"> </i> " + row.getNumParticipants();
		uifactory.addStaticTextElement("lecture.participants", "lecture.participants", participants, formLayout);

		LecturesBlockSearchParameters statsParams = new LecturesBlockSearchParameters();
		statsParams.setLectureBlocks(List.of(lb));
		List<LectureBlockBlockStatistics> statsList = lectureService.getLectureBlocksStatistics(statsParams);
		if (!statsList.isEmpty()) {
			int openAbsences = statsList.get(0).getNumOfAbsenceUnauthorized();
			uifactory.addStaticTextElement("lecture.absences", "lecture.absences",
					openAbsences + " " + translate("open"), formLayout);
		}

		String compulsory = lb.isCompulsory() ? translate("yes") : translate("no");
		uifactory.addStaticTextElement("lecture.compulsory", "lecture.compulsory", compulsory, formLayout);
	}

	private void initTeachers(FormLayoutContainer formLayout, UserRequest ureq, LectureBlock lb) {
		List<String> profilesIds = new ArrayList<>();
		List<Identity> teachers = lectureService.getTeachers(lb);
		if (teachers == null || teachers.isEmpty()) {
			EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("teacher.empty", "lecture.teacher", formLayout);
			emptyTeachersList.setTitle(translate("lecture.no.teacher.assigned.title"));
			emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_icon_user");
			profilesIds.add(emptyTeachersList.getName());
		} else {
			for (Identity teacher : teachers) {
				PortraitUser teacherPortraitUser = userPortraitService.createPortraitUser(getLocale(), teacher);
				UserInfoProfileController profile = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, teacherPortraitUser);
				listenTo(profile);
				String profileId = "profile_" + teacher.getKey();
				profilesIds.add(profileId);
				formLayout.put(profileId, profile.getInitialComponent());
			}
		}
		formLayout.contextPut("profilesIds", profilesIds);
	}

	private void initCourse(FormLayoutContainer formLayout) {
		if (repositoryEntry == null) return;

		String entryPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(entryPath);

		openEntryLink = uifactory.addFormLink("entry.open", repositoryEntry.getDisplayname(), null, formLayout, Link.LINK | Link.NONTRANSLATED);
		openEntryLink.setEscapeMode(EscapeMode.html);
		openEntryLink.setUrl(url);

		VFSThumbnailInfos thumbnail = mapperThumbnail.getRepositoryThumbnail(repositoryEntry);
		if (thumbnail != null) {
			String thumbnailUrl = RepositoryEntryImageMapper.getImageURL(mapperThumbnailKey.getUrl(), thumbnail.metadata(), thumbnail.thumbnailMetadata());
			formLayout.contextPut("thumbnailUrl", thumbnailUrl);
		}

		formLayout.contextPut("entryKey", repositoryEntry.getKey());
		formLayout.contextPut("entryUrl", url);
		formLayout.contextPut("entryDisplayName", repositoryEntry.getDisplayname());
		if (StringHelper.containsNonWhitespace(repositoryEntry.getExternalRef())) {
			formLayout.contextPut("entryExternalRef", repositoryEntry.getExternalRef());
		}
	}

	private void initRooms(FormLayoutContainer formLayout, LectureBlock lb) {
		List<RoomBooking> bookings = roomManagementService.getBookings(lb);
		List<String> roomCardIds = new ArrayList<>();

		for (RoomBooking booking : bookings) {
			Room room = booking.getRoom();
			if (room == null) continue;

			String cardId = RoomUIHelper.forgeRoomCard(formLayout, uifactory, room, velocity_root, getTranslator());
			roomCardIds.add(cardId);
		}

		formLayout.contextPut("roomCardIds", roomCardIds);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			cmc = null;
			removeAsListenerAndDispose(roomDetailViewCtrl);
			roomDetailViewCtrl = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == openInCoursePlannerLink) {
			NewControllerFactory.getInstance().launch(EVENTS_BUSINESS_PATH, ureq, getWindowControl());
		} else if (source == openEntryLink) {
			doOpenRepositoryEntry(ureq);
		} else if (source instanceof FormLink link && link.getUserObject() instanceof Room room) {
			doOpenDetails(ureq, room);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenDetails(UserRequest ureq, Room room) {
		removeAsListenerAndDispose(roomDetailViewCtrl);
		removeAsListenerAndDispose(cmc);
		roomDetailViewCtrl = new RoomDetailViewController(ureq, getWindowControl(), room);
		listenTo(roomDetailViewCtrl);
		String title = room.getExternalRef() != null ? room.getExternalRef() : "";
		cmc = new CloseableModalController(getWindowControl(), translate("close"), roomDetailViewCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenRepositoryEntry(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read-only
	}
}
