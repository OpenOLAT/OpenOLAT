/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.badge.Badge;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.application.ApplicationEditController;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;
import org.olat.modules.selectus.ui.comparator.IdentityLastnameComparator;
import org.olat.modules.selectus.ui.decision.DecisionToolController;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.events.DeletePositionAnonymousEvent;
import org.olat.modules.selectus.ui.events.DeletePositionPermanentlyEvent;
import org.olat.modules.selectus.ui.events.PushControllerEvent;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.events.UpdateControllerEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.PositionFeedbacksController;
import org.olat.modules.selectus.ui.notifications.NotificationListController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeleteAnonymousController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeleteController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeletePermanentlyController;
import org.olat.modules.selectus.ui.position.PositionEditController;
import org.olat.modules.selectus.ui.reference.PositionReferenceListController;
import org.olat.modules.selectus.ui.rejection.PositionMailCenterController;
import org.olat.modules.selectus.ui.rejection.PositionRejectionEmailPdfDataModel;
import org.olat.modules.selectus.ui.resources.ArchiveMediaResource;
import org.olat.modules.selectus.ui.resources.ExcelFlexiTableResource;
import org.olat.modules.selectus.ui.resources.PDFApplicationsCachedCombinedResource;
import org.olat.modules.selectus.ui.resources.PDFExpertOpinionsCachedCombinedResource;

/**
 * 
 * Description:<br>
 * This controller hold the three controller to overview a specific position:
 * Details, Applications and Committee.
 * 
 * <P>
 * Initial Date:  9 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionController extends BasicController implements TooledController, Activateable2 {
	
	private final Link appsLink;
	private final Link detailsLink;
	private final Link committeeLink;
	private final Link mailCenterLink;
	private final Link ratingPolicyLink;
	
	private Link logLink;
	private Link feedbacksLink;
	private Link referencesLink;
	private Link decisionToolLink;
	private Link committeeListLink;
	private Link exportAllCombinedPdfLink;
	private Link deleteAllCombinedPdfLink;
	private Link exportAllExpertOpinionsPdfLink;
	private Link addApplicationLink;
	private Link editPositionLink;
	private Link deletePositionLink;
	private Link archivePositionLink;
	private final TooledStackedPanel stackPanel;
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;

	private PositionFeedbacksController feedbacksCtrl;
	private PositionDetailsController detailsController;
	private PositionApplicationsController appsController;
	private DecisionToolController decisionToolController;
	private PositionCommitteeController committeeController;
	private PositionMailCenterController rejectionController;
	private PositionRatingPolicyController ratingPolicyController;
	private PositionReferenceListController referencesCtrl;
	private final NotificationListController notificationListCtrl;
	private Controller currentContent;
	
	private CloseableModalController cmc;
	private ApplicationEditController addApplicationCtrl;
	private PositionEditController editPositionController;
	private PositionConfirmDeleteController confirmDeleteController;
	private PositionConfirmDeleteAnonymousController confirmDeleteAnonymousController;
	private PositionConfirmDeletePermanentlyController confirmDeletePermanentlyController;

	private Position position;
	private LockResult lockEntry;
	private boolean settingsDirty = false;
	
	private boolean feedbacksEnabled;
	private boolean referencesEnabled;
	private final String mapperBaseUrl;
	private final String appMapperBaseUrl;
	private final String notesMapperBaseUrl;
	private final NotesMapper notesDownloadMapper;
	private final PositionDocumentMapper documentMapper;
	private final ApplicationsListMapper appDownloadMapper;

	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public PositionController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Position position, Tabs selectedTab, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl);
		
		this.position = position;
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		referencesEnabled =  recruitingModule.isReferenceEnabled()
				&& secCallback.canEditApplicationReferences()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| ( recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()));
		
		feedbacksEnabled = recruitingModule.isMembersFeedbackEnabled()
				&& secCallback.canEditApplicationMembersFeedback()
				&& feedbackService.hasFeedbackConfigurationEnabled(position);

		mainVC = createVelocityContainer("overview_position");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);

		detailsLink = LinkFactory.createLink(Tabs.details.getComponentName(), mainVC, this);
		detailsLink.setElementCssClass("o_sel_position_details");
		detailsLink.setUrl(toUrl("Details"));
		segmentView.addSegment(detailsLink, Tabs.details.equals(selectedTab));
		
		ratingPolicyLink = LinkFactory.createLink(Tabs.ratingPolicy.getComponentName(), mainVC, this);
		ratingPolicyLink.setElementCssClass("o_sel_position_rating_policy");
		ratingPolicyLink.setUrl(toUrl("RatingPolicy"));
		segmentView.addSegment(ratingPolicyLink, Tabs.ratingPolicy.equals(selectedTab));
		
		appsLink = LinkFactory.createLink(Tabs.applications.getComponentName(), mainVC, this);
		appsLink.setElementCssClass("o_sel_position_applications");
		appsLink.setUrl(toUrl("Applications"));
		segmentView.addSegment(appsLink, Tabs.applications.equals(selectedTab));
		
		WindowControl logSwControl = BusinessControlFactory.getInstance()
				.createBusinessWindowControl(ureq, OresHelper.createOLATResourceableType("Activities"), null, getWindowControl(), false);
		notificationListCtrl = new NotificationListController(ureq, logSwControl, position);
		listenTo(notificationListCtrl); 
		
		if(secCallback.canViewPositionListLog()) {
			logLink = LinkFactory.createLink(Tabs.log.getComponentName(), mainVC, this);
			logLink.setElementCssClass("o_sel_position_log");
			logLink.setUrl(toUrl("Activities"));
			segmentView.addSegment(logLink, Tabs.log.equals(selectedTab));
			loadNumOfUnreadNotifications();
		}
		
		referencesLink = LinkFactory.createLink(Tabs.references.getComponentName(), mainVC, this);
		referencesLink.setElementCssClass("o_sel_position_references");
		referencesLink.setVisible(referencesEnabled);
		referencesLink.setUrl(toUrl("References"));
		segmentView.addSegment(referencesLink, Tabs.references.equals(selectedTab));
		
		feedbacksLink = LinkFactory.createLink(Tabs.feedbacks.getComponentName(), mainVC, this);
		feedbacksLink.setElementCssClass("o_sel_position_feedbacks");
		feedbacksLink.setVisible(feedbacksEnabled);
		feedbacksLink.setUrl(toUrl("Feedbacks"));
		segmentView.addSegment(feedbacksLink, Tabs.feedbacks.equals(selectedTab));
		
		decisionToolLink = LinkFactory.createLink(Tabs.decisionTool.getComponentName(), mainVC, this);
		decisionToolLink.setElementCssClass("o_sel_position_decision_tool");
		decisionToolLink.setUrl(toUrl("Decisions"));
		decisionToolLink.setVisible(recruitingModule.isDecisionToolEnabled() && position.isDecisionTool() && secCallback.canDecisionTool());
		segmentView.addSegment(decisionToolLink, Tabs.decisionTool.equals(selectedTab));
	
		committeeLink = LinkFactory.createLink(Tabs.committee.getComponentName(), mainVC, this);
		committeeLink.setElementCssClass("o_sel_position_committee");
		committeeLink.setUrl(toUrl("Committee"));
		segmentView.addSegment(committeeLink, Tabs.committee.equals(selectedTab));
		
		mailCenterLink = LinkFactory.createLink(Tabs.mailCenter.getComponentName(), mainVC, this);
		mailCenterLink.setElementCssClass("o_sel_position_mail_center");
		mailCenterLink.setUrl(toUrl("Emails"));
		if(secCallback.canMailCenter()) {
			segmentView.addSegment(mailCenterLink, Tabs.mailCenter.equals(selectedTab));
		}
		
		documentMapper = new PositionDocumentMapper(position);
		mapperBaseUrl = registerMapper(ureq, documentMapper);
		
		appDownloadMapper = new ApplicationsListMapper(getIdentity(), position, secCallback, getLocale(), getTranslator());
		appMapperBaseUrl = registerMapper(ureq, appDownloadMapper);
		
		notesDownloadMapper = new NotesMapper(getIdentity(), position, getTranslator(), getLocale());
		notesMapperBaseUrl = registerCacheableMapper(ureq, UUID.randomUUID().toString().replace("-", ""), notesDownloadMapper);
		
		appsController = new PositionApplicationsController(ureq, wControl, stackPanel, position, secCallback);
		listenTo(appsController);
		
		if(selectedTab == Tabs.applications) {
			currentContent = appsController;
		} else {
			currentContent = getController(ureq, selectedTab);
		}
	
		mainVC.put("content", currentContent.getInitialComponent());
		mainVC.put("segments", segmentView);
		putInitialPanel(mainVC);
	}
	
	private String toUrl(String activationKey) {
		String path = "[Positions:0][Position:" + this.position.getKey() + "][" + activationKey + ":0]";
		return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
	}
	
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void initTools() {
		initAdminTools();
		initDownloadTools();
	
		if(currentContent instanceof TooledController && stackPanel != null) {
			((TooledController)currentContent).initTools();
		}
	}

	private void initAdminTools() {
		Dropdown adminDropdown = new Dropdown("admin-actions", "admin.actions", false, getTranslator());
		adminDropdown.setIconCSS("o_icon o_icon-lg o_icon_actions");
		adminDropdown.setElementCssClass("o_sel_position_admin_menu");

		if(secCallback.canEditPosition()) {
			editPositionLink = LinkFactory.createToolLink("edit", translate("edit"), this);
			editPositionLink.setIconLeftCSS("o_icon o_icon_edit");
			editPositionLink.setElementCssClass("o_sel_edit_position");
			adminDropdown.addComponent(editPositionLink);
			adminDropdown.addComponent(new Spacer("-"));
		}
			
		if(secCallback.canAddApplication()) {
			addApplicationLink = LinkFactory.createToolLink("add_application", translate("add_application"), this);
			addApplicationLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addApplicationLink.setElementCssClass("o_sel_add_application");
			stackPanel.addTool(addApplicationLink, Align.right);
		}
		
		if(secCallback.canDeleteCache()) {
			deleteAllCombinedPdfLink = LinkFactory.createToolLink("edit.export_apps.delete.zip", translate("edit.export_apps.delete.zip"), this);
			deleteAllCombinedPdfLink.setIconLeftCSS("o_icon o_icon_refresh");
			adminDropdown.addComponent(deleteAllCombinedPdfLink);
			adminDropdown.addComponent(new Spacer("-"));
		}
		
		if(secCallback.canArchivePosition()) {
			archivePositionLink = LinkFactory.createToolLink("archive", translate("archive"), this);
			archivePositionLink.setIconLeftCSS("o_icon o_icon_download");
			archivePositionLink.setTarget("export_log");
			adminDropdown.addComponent(archivePositionLink);
		}
		
		if(secCallback.canDeletePosition()) {
			deletePositionLink = LinkFactory.createToolLink("delete", translate("delete"), this);
			deletePositionLink.setIconLeftCSS("o_icon o_icon_delete_item");
			adminDropdown.addComponent(deletePositionLink);
		}
		
		if(adminDropdown.size() > 0) {
			stackPanel.addTool(adminDropdown, Align.left);
		}
	}
	
	private void initDownloadTools() {
		Dropdown downloadDropdown = new Dropdown("download-actions", "download.actions", false, getTranslator());
		downloadDropdown.setIconCSS("o_icon o_icon-lg o_icon_download");
		stackPanel.addTool(downloadDropdown, Align.left);
		
		// applications download
		Long size = erFrontendManager.streamSize(position);
		String sizeStr;
		if(size == null || size.longValue() <= 0) {
			sizeStr = "";
		} else {
			sizeStr = Formatter.formatBytes(size.longValue()) + ", ";
		}
		String text = translate("edit.export_apps.zip.desc", new String[]{sizeStr});
		exportAllCombinedPdfLink = LinkFactory.createToolLink("edit.export_apps.zip", text, this);
		exportAllCombinedPdfLink.setIconLeftCSS("o_icon o_filetype_zip");
		exportAllCombinedPdfLink.setTooltip("edit.export_apps.zip");
		exportAllCombinedPdfLink.setEnabled(!appsController.getApplications().isEmpty());
		downloadDropdown.addComponent(exportAllCombinedPdfLink);
		
		boolean hasExpertOpinions = recruitingModule.isReferenceEnabled()
				&& position.isExpertRecommendationEnabled()
				&& erFrontendManager.hasApplicationReferencePDFs(position, ReferenceType.expert);
		if( hasExpertOpinions) {
			Long expertOpinionsSize = erFrontendManager.streamExpertOpinionsSize(position);
			String expertOpinionsSizeStr;
			if(expertOpinionsSize == null || expertOpinionsSize.longValue() <= 0) {
				expertOpinionsSizeStr = "";
			} else {
				expertOpinionsSizeStr = Formatter.formatBytes(expertOpinionsSize.longValue()) + ", ";;
			}

			String expertOpinionsText = translate("edit.export_expert_opinions_apps.zip.desc", new String[]{ expertOpinionsSizeStr });
			exportAllExpertOpinionsPdfLink = LinkFactory.createToolLink("edit.export_expert_opinions_apps.zip", expertOpinionsText, this);
			exportAllExpertOpinionsPdfLink.setIconLeftCSS("o_icon o_filetype_zip");
			exportAllExpertOpinionsPdfLink.setTooltip("edit.export_expert_opinions_apps.zip");
			exportAllExpertOpinionsPdfLink.setEnabled(!appsController.getApplications().isEmpty());
			downloadDropdown.addComponent(exportAllExpertOpinionsPdfLink);
		}
		
		downloadDropdown.addComponent(new Spacer("all-apps"));

		String derivedFilename = RecruitingHelper
				.getPositionDerivedFilename(position, getLocale());
		if(derivedFilename == null) {
			derivedFilename = position.getKey().toString();
		}
		String filename = "/" + position.getKey() + "/" + normalizeFilename(derivedFilename);
		if(secCallback.canPDFApplicationList()) {
			// Application list (PDF)
			VelocityContainer exportRatingsContainer = createVelocityContainer("download_link");
			exportRatingsContainer.setDomReplacementWrapperRequired(false);
			String exportPDFLabel = translate("edit.export_apps.staff.pdf");
			exportRatingsContainer.contextPut("document", new ApplicationDocument(exportPDFLabel, filename + "_staff.pdf"));
			exportRatingsContainer.contextPut("mapperBaseURL", appMapperBaseUrl);
			downloadDropdown.addComponent(exportRatingsContainer);
		} 
		
		if(secCallback.canDownloadMyRatings()) {
			// List / My rating (PDF)
			VelocityContainer exportRatingsContainer = createVelocityContainer("download_link");
			exportRatingsContainer.setDomReplacementWrapperRequired(false);
			String exportPDFLabel = translate("edit.export_apps.pdf");
			exportRatingsContainer.contextPut("document", new ApplicationDocument(exportPDFLabel, filename + "_apps.pdf"));
			exportRatingsContainer.contextPut("mapperBaseURL", appMapperBaseUrl);
			exportRatingsContainer.contextPut("tooltip", translate("edit.export_apps.pdf.explanation"));
			downloadDropdown.addComponent(exportRatingsContainer);
		}
		
		if(secCallback.canPDFRatings()) {
			// Ratings (PDF) -> for all which are not in committee
			VelocityContainer exportRatingsContainer = createVelocityContainer("download_link");
			exportRatingsContainer.setDomReplacementWrapperRequired(false);
			String exportPDFLabel = translate("edit.export_ratings.pdf");
			exportRatingsContainer.contextPut("document", new ApplicationDocument(exportPDFLabel, filename + "_ratings.pdf"));
			exportRatingsContainer.contextPut("mapperBaseURL", appMapperBaseUrl);
			exportRatingsContainer.contextPut("tooltip", translate("edit.export_ratings.pdf.explanation"));
			downloadDropdown.addComponent(exportRatingsContainer);
		}
		
		// My notes (PDF) -> only committee members
		if(secCallback.canNotes()) {
			VelocityContainer exportNotesContainer = createVelocityContainer("download_link");
			exportNotesContainer.setDomReplacementWrapperRequired(false);
			String exportNotesPDFLabel = translate("edit.export_notes.pdf");
			exportNotesContainer.contextPut("document", new ApplicationDocument(exportNotesPDFLabel, filename + "_notes.pdf"));
			exportNotesContainer.contextPut("mapperBaseURL", notesMapperBaseUrl);
			exportNotesContainer.contextPut("tooltip", translate("edit.export_notes.pdf.explanation"));
			downloadDropdown.addComponent(exportNotesContainer);
		}
		
		downloadDropdown.addComponent(new Spacer("pdf-downloads"));
		
		// Applications list Excel
		boolean canApplicationsExcel = secCallback.canExcelApplicationList();
		if(canApplicationsExcel) {
			VelocityContainer exportAppsContainer = createVelocityContainer("download_link");
			exportAppsContainer.setDomReplacementWrapperRequired(false);
			String exportXLSLabel = translate("edit.export_apps.excel");
			exportAppsContainer.contextPut("document", new ApplicationDocument(exportXLSLabel, "o_filetype_xls", filename + "_applications.xlsx"));
			exportAppsContainer.contextPut("mapperBaseURL", appMapperBaseUrl);
			downloadDropdown.addComponent(exportAppsContainer);
		}
		
		boolean canViewReviews = secCallback.canExcelReviewStatistics();
		if(canViewReviews) {
			VelocityContainer exportAppsContainer = createVelocityContainer("download_link");
			exportAppsContainer.setDomReplacementWrapperRequired(false);
			String exportXLSLabel = translate("edit.export_reviews_stats.excel");
			exportAppsContainer.contextPut("document", new ApplicationDocument(exportXLSLabel, "o_filetype_xls", filename + "_reviews_statistics.xlsx"));
			exportAppsContainer.contextPut("mapperBaseURL", appMapperBaseUrl);
			downloadDropdown.addComponent(exportAppsContainer);
		}
		
		if(canViewReviews || canApplicationsExcel) {
			downloadDropdown.addComponent(new Spacer("excel-1-downloads"));
		}

		//Committee list Excel
		if(secCallback.canExcelListCommittee()) {
			committeeListLink= LinkFactory.createToolLink("export.committee.excel", translate("export.committee.excel"), this);
			committeeListLink.setIconLeftCSS("o_icon o_filetype_xls");
			downloadDropdown.addComponent(committeeListLink);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Emails".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.mailCenter, mailCenterLink);
		} else if("Committee".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.committee, committeeLink);
		} else if("References".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.references, referencesLink);
		} else if("Feedbacks".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.feedbacks, feedbacksLink);
		} else if("RatingPolicy".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.ratingPolicy, ratingPolicyLink);
		} else if("Details".equalsIgnoreCase(type) || "Profile".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.details, detailsLink);
		} else if("Decisions".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.decisionTool, decisionToolLink);
		} else if("Activities".equalsIgnoreCase(type)) {
			activate(ureq, Tabs.log, logLink);
		} else if("Applications".equalsIgnoreCase(type) || "Application".equalsIgnoreCase(type)) {
			Long applicationKey = entries.get(0).getOLATResourceable().getResourceableId();
			if(applicationKey.longValue() == 0) {
				Controller appController = activate(ureq, Tabs.applications);
				segmentView.select(appsLink);
				addToHistory(ureq, OresHelper.createOLATResourceableType("Applications"), null);
				if(appController instanceof Activateable2) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					((Activateable2)appController).activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			} else {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				Application appToSelect = erFrontendManager.getApplicationByKey(applicationKey);
				if(appToSelect != null && appToSelect.getPosition().equals(position)) {
					selectApplication(ureq, appToSelect, subEntries);
					segmentView.select(appsLink);
					addToHistory(ureq, OresHelper.createOLATResourceableInstance("Applications", applicationKey), null);
				} else {
					logAudit("Try to load an application of an other position: " + applicationKey, null);
				}
			}
		} else if("Edit".equalsIgnoreCase(type)) {
			if(secCallback.canEditPosition()) {
				PositionEditController editCtrl = doEditPosition(ureq);
				if(editCtrl != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					editCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Tabs selectedTab = Tabs.get(segmentCName);
				Controller ctrl = activate(ureq, selectedTab);
				if(ctrl == appsController) {
					addToHistory(ureq, OresHelper.createOLATResourceableType("Applications"), null);
				} else if(ctrl != null) {
					addToHistory(ureq, ctrl);
				}
			}	
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == editPositionController) {
					popEditPosition(ureq);
				}
			}
		} else if (source == deleteAllCombinedPdfLink) {
			doDeleteAllCombinedPDFs();
		} else if (source == addApplicationLink) {
			doAddApplication(ureq);
		} else if (source == editPositionLink) {
			doEditPosition(ureq);
		} else if (source ==  exportAllCombinedPdfLink) {
			doExportAllCombinedPDFs(ureq);
		} else if (source ==  exportAllExpertOpinionsPdfLink) {
			doExportExpertOpinionsCombinedPDFs(ureq);
		} else if (source == archivePositionLink) {
			doArchivePosition(ureq);
		} else if (source == deletePositionLink) {
			doConfirmDelete(ureq);
		} else if (source == committeeListLink) {
			doExportCommitteeList(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof PushControllerEvent) {
			fireEvent(ureq, event);
		} else if (event instanceof UpdateControllerEvent) {
			fireEvent(ureq, event);
		} else if (source == committeeController) {
			if(event == Event.CHANGED_EVENT) {
				if(appsController != null) {
					appsController.loadModel(position);
				}
				loadNumOfUnreadNotifications();
			}
		} else if(source == rejectionController) {
			if(event instanceof DecisionEvent || event == Event.CHANGED_EVENT) {
				appsController.loadModel(position);
				loadNumOfUnreadNotifications();
			}
		} else if(source == addApplicationCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				//do nothing
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT){
				reloadApplications();
				loadNumOfUnreadNotifications();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == editPositionController) {
			if(event == Event.CANCELLED_EVENT) {
				position = editPositionController.getPosition();
				doUpdatePosition(ureq);
			} else if(event == Event.CHANGED_EVENT) {
				settingsDirty = true;
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
			if(event == Event.CANCELLED_EVENT) {
				if(lockEntry != null) {
					CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
					lockEntry = null;
				}
				stackPanel.popUpToController(this);
			}
		} else if(source == decisionToolController) {
			if(event == Event.CHANGED_EVENT) {
				position = decisionToolController.getPosition();
				doUpdatePosition(ureq);
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(source == notificationListCtrl) {
			if(event instanceof SelectApplicationEvent) {
				SelectApplicationEvent sae = (SelectApplicationEvent)event;
				if(sae.getApplication() == null) {
					showWarning("warning.application.deleted");
				} else {
					Application app = erFrontendManager.getApplicationByKey(sae.getApplication().getKey());
					selectApplication(ureq, app, sae.getActivation());
					segmentView.select(appsLink);
				}
			} else if(event == Event.CHANGED_EVENT) {
				loadNumOfUnreadNotifications();
			} else if(event instanceof SelectPositionLightEvent) {
				SelectPositionLightEvent sple = (SelectPositionLightEvent)event;
				activate(ureq, sple.getActivation(), null);
			}
		} else if(source == confirmDeleteController) {
			Position position = confirmDeleteController.getPosition();
			cmc.deactivate();
			cleanUp();
			if (event instanceof DeletePositionPermanentlyEvent) {
				confirmDeletePermanentely(ureq, position);
			} else if(event instanceof DeletePositionAnonymousEvent) {
				confirmDeleteAnonymous(ureq, position);
			} else if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		} else if(source == confirmDeletePermanentlyController || source == confirmDeleteAnonymousController) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editPositionController);
		removeAsListenerAndDispose(addApplicationCtrl);
		removeAsListenerAndDispose(cmc);
		editPositionController = null;
		addApplicationCtrl = null;
		cmc = null;
	}
	
	private void reloadApplications() {
		if(appsController != null) {
			appsController.loadModel(position);
		}
	}

	@Override
	protected void doDispose() {
		if(lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
		stackPanel.removeListener(this);
	}

	private void activate(UserRequest ureq, Tabs selectedTab, Link segment) {
		Controller ctrl = activate(ureq, selectedTab);
		if(ctrl != null && segment != null) {
			segmentView.select(segment);
			addToHistory(ureq, ctrl);
		}
	}
	
	private Controller activate(UserRequest ureq, Tabs selectedTab) {
		if(currentContent instanceof PositionCommitteeController pcc) {
			pcc.removeTools();
		} else if(currentContent instanceof PositionEditController pec) {
			pec.removeTools();
		}
		
		
		Controller content = getController(ureq, selectedTab);
		if(content instanceof TooledController) {
			((TooledController)content).initTools();
		}
		currentContent = content;
		mainVC.put("content", currentContent.getInitialComponent());
		return content;
	}
	
	private void doDeleteAllCombinedPDFs() {
		erFrontendManager.deleteCachedStream(position);
	}
	
	protected void selectApplication(UserRequest ureq, ApplicationRef appToSelect, List<ContextEntry> entries) {
		Controller content = getController(ureq, Tabs.applications);
		if(content instanceof TooledController) {
			((TooledController)content).initTools();
		}
		currentContent = content;
		mainVC.put("content", currentContent.getInitialComponent());
		
		if(content == appsController) {
			appsController.selectApplication(ureq, appToSelect, false, entries);
		}
	}
	
	private void doAddApplication(UserRequest ureq) {
		removeAsListenerAndDispose(addApplicationCtrl);
		removeAsListenerAndDispose(cmc);
		
		Application app = erFrontendManager.createTempApplication(position, true);
		addApplicationCtrl = new ApplicationEditController(ureq, getWindowControl(), app, position, secCallback);
		listenTo(addApplicationCtrl);
		
		String title = translate("add_application");
		cmc = new CloseableModalController(getWindowControl(), "c", addApplicationCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void loadNumOfUnreadNotifications() {
		if(logLink == null) return;
		
		int numOfUnread = notificationListCtrl.getNumOfUnreadNotifications();
		if(numOfUnread > 0) {
			logLink.setBadge(Integer.toString(numOfUnread), Badge.Level.none);
		} else {
			logLink.removeBadge();
		}
	}
	
	private void doUpdatePosition(UserRequest ureq) {
		secCallback.updatePosition(position);
		if(appsController != null) {
			appsController.loadMailTemplatesAndCo();
			appsController.loadModel(position);
		}
		if(detailsController != null) {
			detailsController.updatePosition(position);
		}
		if(ratingPolicyController != null) {
			ratingPolicyController.updatePosition(position);
		}
		if(referencesCtrl != null) {
			referencesCtrl.updatePosition(position);
		}
		if(decisionToolController != null) {
			decisionToolController.updatePosition(position);
		}
		if(rejectionController != null) {
			rejectionController.updatePosition(position);
		}

		boolean decision = recruitingModule.isDecisionToolEnabled() && position.isDecisionTool()
				&& (secCallback.canEditDecisionRubrics() || secCallback.canDecisionTool());
		if(decision != decisionToolLink.isVisible()) {
			decisionToolLink.setVisible(decision);
		}

		if(documentMapper != null) {
			documentMapper.setPosition(position);
		}
		
		referencesEnabled = recruitingModule.isReferenceEnabled()
				&& secCallback.canEditApplicationReferences()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| (recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()));
		referencesLink.setVisible(referencesEnabled);
		if(!referencesEnabled && referencesCtrl != null) {
			if(segmentView.isSelected(referencesLink)) {
				currentContent = getController(ureq, Tabs.applications);
				mainVC.put("content", currentContent.getInitialComponent());
				addToHistory(ureq, currentContent);
			}
			removeAsListenerAndDispose(referencesCtrl);
			referencesCtrl = null;
		}
		
		feedbacksEnabled = recruitingModule.isMembersFeedbackEnabled()
				&& secCallback.canEditApplicationMembersFeedback()
				&& feedbackService.hasFeedbackConfigurationEnabled(position);
		feedbacksLink.setVisible(feedbacksEnabled);
		if(!feedbacksEnabled && feedbacksCtrl != null) {
			if(segmentView.isSelected(feedbacksLink)) {
				currentContent = getController(ureq, Tabs.applications);
				mainVC.put("content", currentContent.getInitialComponent());
				addToHistory(ureq, currentContent);
			}
			removeAsListenerAndDispose(feedbacksCtrl);
			feedbacksCtrl = null;
		}
	}
	
	public PositionEditController doEditPosition(UserRequest ureq) {
		if(editPositionController != null) {
			removeAsListenerAndDispose(editPositionController);
			stackPanel.popController(editPositionController);
			editPositionController = null;
		}
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(position, ureq.getIdentity(), "mmp", getWindow());
		
		PositionEditController controller = null;
		if(lockEntry.isSuccess()) {
			position = erFrontendManager.getPosition(position.getKey());
			
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Edit"), null);
			controller = new PositionEditController(ureq, swControl, stackPanel, position, false, secCallback);
			listenTo(controller);
			stackPanel.pushController(translate("edit"), controller);
			editPositionController = controller;
		} else {
			Identity owner = lockEntry.getOwner();
			String fullName = userManager.getUserDisplayName(owner);
			String[] args = new String[] { fullName,
					(owner == null ? "" : owner.getUser().getProperty(UserConstants.EMAIL, getLocale())),
					Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime()))
				};
			showWarning("position.already.edited", args);
		}
		return controller;
	}
	
	private void popEditPosition(UserRequest ureq) {
		if(lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
		position = editPositionController.getPosition();
		secCallback.updatePosition(position);
		
		// remove the applications list if the settings changed
		if(settingsDirty) {
			removeAsListenerAndDispose(appsController);
			appsController = new PositionApplicationsController(ureq, getWindowControl(), stackPanel, position, secCallback);
			listenTo(appsController);
			if(currentContent instanceof PositionApplicationsController) {
				currentContent = appsController;
				mainVC.put("content", currentContent.getInitialComponent());
			}
			settingsDirty = false;
		}

		doUpdatePosition(ureq);
		removeAsListenerAndDispose(editPositionController);
		editPositionController = null;
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if(PositionStatus.reporting.name().equals(position.getStatus())) {
			confirmDeletePermanentely(ureq, position);
		} else {

			confirmDeleteController = new PositionConfirmDeleteController(ureq, getWindowControl(), position, secCallback);
			listenTo(confirmDeleteController);

			String title = translate("confirm.delete.title");
			cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteController.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void confirmDeletePermanentely(UserRequest ureq, Position position) {
		confirmDeletePermanentlyController = new PositionConfirmDeletePermanentlyController(ureq, getWindowControl(), position);
		listenTo(confirmDeletePermanentlyController);
		
		String title = translate("confirm.delete.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeletePermanentlyController.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void confirmDeleteAnonymous(UserRequest ureq, Position position) {
		confirmDeleteAnonymousController = new PositionConfirmDeleteAnonymousController(ureq, getWindowControl(), position);
		listenTo(confirmDeleteAnonymousController);

		String title = translate("confirm.delete.anonymous.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteAnonymousController.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	

	private void doExportAllCombinedPDFs(UserRequest ureq) {
		List<Application> apps = erFrontendManager.getApplicationsWithAttachment(position);
		String derivedFilename = RecruitingHelper
				.getPositionDerivedFilename(position, getLocale());
		String name = normalizeFilename(StringHelper.containsNoneOfCoDouSemi(derivedFilename) ? derivedFilename : "batchcombined") + "_applications.zip";
		PDFApplicationsCachedCombinedResource resource = new PDFApplicationsCachedCombinedResource(position, apps, secCallback, name, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doExportExpertOpinionsCombinedPDFs(UserRequest ureq) {
		List<Reference> references = erFrontendManager.getPositionReferences(position, ReferenceType.expert, true);
		String derivedFilename = RecruitingHelper
				.getPositionDerivedFilename(position, getLocale());
		String name = normalizeFilename(StringHelper.containsNoneOfCoDouSemi(derivedFilename) ? derivedFilename : "batchcombined") + "_assessments.zip";
		String desc = position.getMLDescription(getLocale());
		PDFExpertOpinionsCachedCombinedResource resource = new PDFExpertOpinionsCachedCombinedResource(position, references,
				secCallback, name, desc, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doArchivePosition(UserRequest ureq) {
		List<MailLogInfos> rejectionLog = erFrontendManager.getMailLog(position);
		PositionRejectionEmailPdfDataModel rejectionLogModel = new PositionRejectionEmailPdfDataModel(rejectionLog, getTranslator());
		
		List<ApplicationLight> applications = erFrontendManager.getApplications(position);

		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<? extends IdentityRef> committee = erFrontendManager.getCommitteeRefs(position, ratingRoles);
		List<UserRating> ratings = erFrontendManager.getRatings(position, committee);
		int committeeSize = committee.size();

		Map<Long,ApplicationRefereeStats> appKeyToRefereeStats = new HashMap<>();
		if(recruitingModule.isReferenceEnabled()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| (recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()))) {
			List<ApplicationRefereeStats> refereeStats = erFrontendManager.getApplicationReviewerStats(position);
			for(ApplicationRefereeStats stats:refereeStats) {
				appKeyToRefereeStats.put(stats.getKey(), stats);
			}
		}
		
		Map<Long,List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(position, true);
		
		PositionApplicationsPDFDataModel applicationPdfModel
			= new PositionApplicationsPDFDataModel(getIdentity(), position, applications, ratings, getTranslator());
		PositionApplicationsExcelDataModel excelDataModel
			= new PositionApplicationsExcelDataModel(committeeSize, position, applications, ratings, appKeyToRefereeStats,
					null, null, appToCategories, secCallback, getTranslator());

		ArchiveMediaResource resource = new ArchiveMediaResource(getIdentity(), position, rejectionLogModel,
				applicationPdfModel, excelDataModel, getTranslator(), getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doExportCommitteeList(UserRequest ureq) {
		try {
			List<Identity> allMembers = erFrontendManager.getCommitteeMembers(position);

			List<Identity> heads = erFrontendManager.getHeads(position);
			allMembers.addAll(heads);
			List<Identity> secretaries = erFrontendManager.getSecretaries(position);
			allMembers.addAll(secretaries);
			List<Identity> exOfficios = erFrontendManager.getExOfficios(position);
			allMembers.addAll(exOfficios);
			
			Collections.sort(allMembers, new IdentityLastnameComparator());
			
			Translator userPropTranslator = userManager.getPropertyHandlerTranslator(getTranslator());
			CommitteeExcelExport export = new CommitteeExcelExport(allMembers, secretaries, heads, exOfficios, userPropTranslator);

			String derivedFilename = RecruitingHelper.getPositionDerivedFilename(position, getLocale());
			String filename = normalizeFilename(derivedFilename) + "_committee.xlsx";
			MediaResource resource = new ExcelFlexiTableResource(filename, export, getTranslator());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} catch (Exception e) {
			logError("Cannot export committee list", e);
		}
	}
	
	private Controller getController(UserRequest ureq, Tabs selectedTab) {
		switch(selectedTab) {
			case details: {
				if(detailsController == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Details"), null);
					detailsController = new PositionDetailsController(ureq, swControl, position, mapperBaseUrl, secCallback);
					listenTo(detailsController);
				}
				return detailsController;
			}
			case applications: {
				reloadApplications();
				return appsController;
			}
			case references: {
				if(referencesCtrl == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("References"), null);
					referencesCtrl = new PositionReferenceListController(ureq, swControl, position, secCallback);
					listenTo(referencesCtrl);
				} else {
					referencesCtrl.reloadModel();
				}
				stackPanel.popUpToController(this);
				return referencesCtrl;
			}
			case feedbacks: {
				if(feedbacksCtrl == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Feedbacks"), null);
					feedbacksCtrl = new PositionFeedbacksController(ureq, swControl, position, secCallback);
					listenTo(feedbacksCtrl);
				} else {
					feedbacksCtrl.reloadModel();
				}
				stackPanel.popUpToController(this);
				return feedbacksCtrl;
			}
			case decisionTool: {
				if(decisionToolController == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Decisions"), null);
					decisionToolController = new DecisionToolController(ureq, swControl, position, secCallback);
					listenTo(decisionToolController);
				} else {
					decisionToolController.updateApplications(position);
				}
				return decisionToolController;
			}
			case committee: {
				if(committeeController == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Committee"), null);
					committeeController = new PositionCommitteeController(ureq, swControl, stackPanel, position, secCallback);
					listenTo(committeeController);
				} else {
					committeeController.updateCommitteeStatistics();
				}
				return committeeController;
			}
			case ratingPolicy: {
				if(ratingPolicyController == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("RatingPolicy"), null);
					ratingPolicyController = new PositionRatingPolicyController(ureq, swControl, position);
					listenTo(ratingPolicyController);
				}
				return ratingPolicyController;
			}
			case mailCenter: {
				if(rejectionController == null) {
					WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Emails"), null);
					rejectionController = new PositionMailCenterController(ureq, swControl, position, secCallback);
					listenTo(rejectionController);
				}
				return rejectionController;
			}
			case log: {
				notificationListCtrl.loadModel();
				loadNumOfUnreadNotifications();
				return notificationListCtrl;
			}
		}
		return null;
	}
	
	public enum Tabs {
		details("edit.details"),
		applications("edit.applications"),
		references("edit.references"),
		feedbacks("edit.feedbacks"),
		decisionTool("edit.decisionTool"),
		committee("edit.committee"),
		ratingPolicy("edit.rating.policy"),
		mailCenter("edit.rejection"),
		log("view.position.log");
		
		private final String componentName;
		
		private Tabs(String componentName) {
			this.componentName = componentName;
		}

		public String getComponentName() {
			return componentName;
		}
		
		public static Tabs get(String componentName) {
			Tabs selected = null;
			for(Tabs t:values()) {
				if(t.componentName.equals(componentName)) {
					selected = t;
				}
			}
			return selected;
		}
	}
}
