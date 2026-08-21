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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfOutputOptions.MediaType;
import org.olat.core.commons.services.pdf.PdfOutputOptions.Margin;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.InfoCourse;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureBlocksTimelineController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public abstract class RepositoryEntryDetailsController extends BasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryDetailsController.class);

	private final InfoPageHeaderController headerCtrl;
	private final InfoPageTeaserImageController teaserImageCtrl;
	private RepositoryEntryInfoPageGetStartedController getStartedCtrl;
	private InfoPagePublicGetStartedController publicGetStartedCtrl;
	private final InfoPageFactsController factsCtrl;
	private final RepositoryEntryInfoPageSectionsController sectionsCtrl;
	private LectureBlocksTimelineController lectureBlocksCtrl;
	private final InfoPageLicenceController licenceCtrl;
	private final InfoPageMyCourseController myCourseCtrl;
	private InfoPageRatingController ratingCtrl;
	private UserCommentsAndRatingsController userCommentsCtrl;
	private RepositoryEntryDetailsTechnicalController technicalDetailsCtrl;

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final RepositoryEntry entry;
	private final DetailsHeaderConfig config;
	private final String shareUrl;
	private final boolean isResourceInfoView;
	private final boolean guestOnly;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private PdfService pdfService;

	public RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			DetailsHeaderConfig config, String shareUrl, boolean isResourceInfoView, boolean closeTabOnLeave) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.config = config;
		this.shareUrl = shareUrl;
		this.isResourceInfoView = isResourceInfoView;
		UserSession usess = ureq.getUserSession();
		guestOnly = usess.getRoles() == null || usess.getRoles().isGuestOnly();

		List<String> memberRoles = getIdentity() != null? repositoryService.getRoles(getIdentity(), entry): List.of();
		boolean isOwner = memberRoles.contains(GroupRoles.owner.name());
		boolean isParticipant = memberRoles.contains(GroupRoles.participant.name());
		boolean isMember = isOwner || isParticipant || memberRoles.contains(GroupRoles.coach.name());

		velocity_root = Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class);
		VelocityContainer mainVC = createVelocityContainer("info_page");

		InfoPageData data = new RepositoryEntryInfoPageData(entry, getTranslator());

		headerCtrl = new InfoPageHeaderController(ureq, wControl, data, shareUrl);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());

		teaserImageCtrl = new InfoPageTeaserImageController(ureq, wControl, data);
		listenTo(teaserImageCtrl);
		if (teaserImageCtrl.hasContent()) {
			mainVC.put("thumbnail", teaserImageCtrl.getInitialComponent());
		}

		if (isResourceInfoView) {
			publicGetStartedCtrl = new InfoPagePublicGetStartedController(ureq, wControl, entry);
			listenTo(publicGetStartedCtrl);
			if (publicGetStartedCtrl.hasContent()) {
				mainVC.put("getStarted", publicGetStartedCtrl.getInitialComponent());
			}
		} else {
			getStartedCtrl = new RepositoryEntryInfoPageGetStartedController(ureq, wControl, entry, closeTabOnLeave, config);
			listenTo(getStartedCtrl);
			if (getStartedCtrl.hasContent()) {
				mainVC.put("getStarted", getStartedCtrl.getInitialComponent());
			}
		}

		myCourseCtrl = new InfoPageMyCourseController(ureq, wControl, entry, isMember, guestOnly);
		listenTo(myCourseCtrl);
		if (myCourseCtrl.hasContent()) {
			mainVC.put("myCourse", myCourseCtrl.getInitialComponent());
		}

		List<LectureBlock> lectureBlocks = lectureService.isRepositoryEntryLectureEnabled(entry)
				? lectureService.getLectureBlocks(entry)
				: List.of();
		factsCtrl = new InfoPageFactsController(ureq, wControl, entry, lectureBlocks.size());
		listenTo(factsCtrl);
		if (factsCtrl.hasContent()) {
			mainVC.put("facts", factsCtrl.getInitialComponent());
		}

		sectionsCtrl = new RepositoryEntryInfoPageSectionsController(ureq, wControl, entry);
		listenTo(sectionsCtrl);
		if (sectionsCtrl.hasContent()) {
			mainVC.put("sections", sectionsCtrl.getInitialComponent());
		}

		if (!lectureBlocks.isEmpty()) {
			lectureBlocksCtrl = new LectureBlocksTimelineController(ureq, wControl, lectureBlocks, true);
			listenTo(lectureBlocksCtrl);
			mainVC.put("events", lectureBlocksCtrl.getInitialComponent());
		}

		licenceCtrl = new InfoPageLicenceController(ureq, wControl, entry);
		listenTo(licenceCtrl);
		if (licenceCtrl.hasContent()) {
			mainVC.put("licence", licenceCtrl.getInitialComponent());
		}

		if (repositoryModule.isRatingEnabled()) {
			ratingCtrl = new InfoPageRatingController(ureq, wControl, entry, guestOnly);
			listenTo(ratingCtrl);
			mainVC.put("rating", ratingCtrl.getInitialComponent());
		}

		if (repositoryModule.isCommentEnabled()) {
			userCommentsCtrl = initCommentsCtrl(ureq);
			mainVC.put("comments", userCommentsCtrl.getInitialComponent());
		}

		// show technical data only for administrative users or owners, hide from normal users
		if (ureq.getUserSession().getRoles() != null) {
			Roles roles = ureq.getUserSession().getRoles();
			if (isOwner || roles.isAdministrator() || roles.isManager()) {
				technicalDetailsCtrl = new RepositoryEntryDetailsTechnicalController(ureq, wControl, entry, isOwner);
				listenTo(technicalDetailsCtrl);
				mainVC.put("technical", technicalDetailsCtrl.getInitialComponent());
			}
		}

		if (entry.getEducationalType() != null) {
			mainVC.contextPut("educationalTypeClass", entry.getEducationalType().getCssClass());
		}
		mainVC.contextPut("isMember", isMember);

		if (courseModule.isInfoDetailsEnabled()) {
			String oInfoCourse = null;
			try {
				InfoCourse infoCourse = InfoCourse.of(entry);
				if (infoCourse != null) {
					oInfoCourse = objectMapper.writeValueAsString(infoCourse);
				}
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			mainVC.contextPut("oInfoCourse", oInfoCourse);
		}

		putInitialPanel(mainVC);
	}

	public static String getShareUrl(RepositoryEntry entry) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][Infos:0]";
		List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		return BusinessControlFactory.getInstance().getAsURIString(ces, true);
	}

	private UserCommentsAndRatingsController initCommentsCtrl(UserRequest ureq) {
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, guestOnly);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", entry.getKey());
		UserCommentsAndRatingsController commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), ores, null, secCallback, null, secCallback.canViewComments(), true, true);
		listenTo(commentsCtrl);
		return commentsCtrl;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Comments".equalsIgnoreCase(type) && userCommentsCtrl != null) {
			userCommentsCtrl.scrollToCommentsArea();
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == getStartedCtrl) {
			if (event == AbstractInfoPageGetStartedController.START_EVENT) {
				doStart(ureq);
			} else if (event == AbstractInfoPageGetStartedController.RESERVATION_CONFIRMATION_EVENT) {
				doBooked(ureq);
				fireEvent(ureq, new BookedEvent(entry));
			} else if (event instanceof BookEvent) {
				fireEvent(ureq, event);
			} else if (event instanceof LeavingEvent) {
				fireEvent(ureq, event);
			} else if (event == AccessEvent.ACCESS_OK_EVENT) {
				doBooked(ureq);
				fireEvent(ureq, new BookedEvent(entry));
			} else if (event == OffersController.LOGIN_EVENT) {
				fireEvent(ureq, new BookEvent(entry.getOlatResource().getKey()));
			}
		} else if (source == publicGetStartedCtrl) {
			if (event == InfoPagePublicGetStartedController.START_EVENT) {
				doStart(ureq);
			}
		} else if (source == headerCtrl) {
			if (event == InfoPageHeaderController.PDF_EVENT) {
				doExportPdf(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doExportPdf(UserRequest ureq) {
		// The rendered snapshot never triggers doStart()/doBooked(), so any
		// concrete subclass reproduces the same page for the PDF.
		ControllerCreator printControllerCreator = isResourceInfoView
				? (lureq, lwControl) -> new RepositoryEntryPublicInfosController(lureq, lwControl, entry)
				: (lureq, lwControl) -> new RepositoryEntryInfosController(lureq, lwControl, entry, config, true);
		String filename = StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname());
		PdfOutputOptions options = PdfOutputOptions.valueOf(MediaType.print, Margin.ONE_CM, null);
		MediaResource resource = pdfService.convert(filename, getIdentity(), printControllerCreator, getWindowControl(), options);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	protected RepositoryEntry getEntry() {
		return entry;
	}

	protected abstract void doStart(UserRequest ureq);

	protected abstract void doBooked(UserRequest ureq);

}
