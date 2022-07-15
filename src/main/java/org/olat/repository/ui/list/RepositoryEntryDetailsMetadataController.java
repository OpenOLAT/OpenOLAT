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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.ui.PriceMethod;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDetailsMetadataController extends FormBasicController {
	
	private FormLink markLink;
	private FormLink commentsLink;
	private FormLink leaveLink;
	private RatingWithAverageFormItem ratingEl;
	
	private CloseableModalController cmc;
	private DialogBoxController leaveDialogBox;
	private UserCommentsController commentsCtrl;
	
	private final RepositoryEntry entry;
	private final boolean isMember;
	private final boolean isParticipant;
	private final boolean guestOnly;
	private final List<PriceMethod> types;
	
	@Autowired
	protected RepositoryModule repositoryModule;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected CatalogManager catalogManager;
	@Autowired
	protected BusinessGroupService businessGroupService;
	@Autowired
	protected EfficiencyStatementManager effManager;
	@Autowired
	protected UserCourseInformationsManager userCourseInfosManager;
	@Autowired
	protected UserRatingsDAO userRatingsDao;
	@Autowired
	protected MarkManager markManager;

	public RepositoryEntryDetailsMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean isParticipant, List<PriceMethod> types) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_metadata.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.isMember = isMember;
		this.isParticipant = isParticipant;
		this.types = types;
		this.guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int counter = 0;
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			layoutCont.contextPut("guestOnly", Boolean.valueOf(guestOnly));
			boolean closed = entry.getEntryStatus().decommissioned();
			layoutCont.contextPut("closed", Boolean.valueOf(closed));
			
			// Catalog V1 categories
			if (repositoryModule.isCatalogEnabled()) {
				List<CatalogEntry> categories = catalogManager.getCatalogEntriesReferencing(entry);
				List<String> categoriesLink = new ArrayList<>(categories.size());
				for(CatalogEntry category:categories) {
					String id = "cat_" + counter++;
					String title = StringHelper.escapeHtml(category.getParent().getName());
					FormLink catLink = uifactory.addFormLink(id, "category", title, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					catLink.setIconLeftCSS("o_icon o_icon-fw o_icon_catalog");
					catLink.setUserObject(category.getKey());
					categoriesLink.add(id);
				}
				layoutCont.contextPut("categories", categoriesLink);
			}
			// taxonomy levels
			List<TaxonomyLevelItem> taxonomyLevels = repositoryService.getTaxonomy(entry).stream()
					.map(level -> new TaxonomyLevelItem(
							TaxonomyUIFactory.translateDisplayName(getTranslator(), level),
							level.getMaterializedPathIdentifiersWithoutSlash()))
					.collect(Collectors.toList());
			layoutCont.contextPut("taxonomyLevels", taxonomyLevels);
			
			if (!guestOnly) {
				boolean marked = markManager.isMarked(entry, getIdentity(), null);
				markLink = uifactory.addFormLink("mark", "mark", marked ? "details.bookmark.remove" : "details.bookmark", null, layoutCont, Link.LINK);
				markLink.setElementCssClass("o_bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			}
			
			RepositoryEntryStatistics statistics = entry.getStatistics();
			if (repositoryModule.isRatingEnabled()) {
				Integer myRating = userRatingsDao.getRatingValue(getIdentity(), entry, null);
				
				Double averageRating = statistics.getRating();
				long numOfRatings = statistics.getNumOfRatings();
				float ratingValue = myRating == null ? 0f : myRating.floatValue();
				float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
				ratingEl = new RatingWithAverageFormItem("rating", ratingValue, averageRatingValue, 5, numOfRatings);
				ratingEl.setEnabled(!guestOnly);
				layoutCont.add("rating", ratingEl);
			}
			
			if (repositoryModule.isCommentEnabled()) {
				long numOfComments = statistics.getNumOfComments();
				String title = "(" + numOfComments + ")";
				commentsLink = uifactory.addFormLink("comments", "comments", title, null, layoutCont, Link.NONTRANSLATED);
				commentsLink.setCustomEnabledLinkCSS("o_comments");
				String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
				commentsLink.setIconLeftCSS(css);
			}
			
			if (!guestOnly && isParticipant && repositoryService.isParticipantAllowedToLeave(entry)) {
				leaveLink = uifactory.addFormLink("sign.out", "leave", translate("sign.out"), null, formLayout, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
				leaveLink.setElementCssClass("o_sign_out btn-danger");
				leaveLink.setIconLeftCSS("o_icon o_icon_sign_out");
			}
			
			if (types != null && !types.isEmpty()) {
				layoutCont.contextPut("ac", types);
			}
			
			if (isMember) {
				//show the list of groups
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				List<String> groupLinkNames = new ArrayList<>(groups.size());
				for (BusinessGroup group:groups) {
					String groupLinkName = "grp_" + counter++;
					String groupName = StringHelper.escapeHtml(group.getName());
					FormLink link = uifactory.addFormLink(groupLinkName, "group", groupName, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
					link.setUserObject(group.getKey());
					groupLinkNames.add(groupLinkName);
				}
				layoutCont.contextPut("groups", groupLinkNames);
			}
			
			boolean passed = false;
			boolean failed = false;
			String score = null;
			UserEfficiencyStatement statement = effManager.getUserEfficiencyStatementLightByRepositoryEntry(entry, getIdentity());
			if (statement != null) {
				Boolean p = statement.getPassed();
				if (p != null) {
					passed = p.booleanValue();
					failed = !p.booleanValue();
				}
				
				Float scoreVal = statement.getScore();
				if (scoreVal != null) {
					score = AssessmentHelper.getRoundedScore(scoreVal);
				}
			}
			layoutCont.contextPut("passed", passed);
			layoutCont.contextPut("failed", failed);
			layoutCont.contextPut("score", score);
			
			Date recentLaunch = userCourseInfosManager.getRecentLaunchDate(entry.getOlatResource(), getIdentity());
			layoutCont.contextPut("recentLaunch", recentLaunch);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if ("category".equals(cmd)) {
				Long categoryKey = (Long)link.getUserObject();
				doOpenCategory(ureq, categoryKey);
			} else if ("mark".equals(cmd)) {
				boolean marked = doMark();
				markLink.setI18nKey(marked ? "details.bookmark.remove" : "details.bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			} else if ("comments".equals(cmd)) {
				doOpenComments(ureq);
			} else if ("group".equals(cmd)) {
				Long groupKey = (Long)link.getUserObject();
				doOpenGroup(ureq, groupKey);
			} else if ("leave".equals(cmd)) {
				doConfirmLeave(ureq);
			}
		} else if (ratingEl == source && event instanceof RatingFormEvent) {
			RatingFormEvent ratingEvent = (RatingFormEvent)event;
			doRating(ratingEvent.getRating());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(commentsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				updateComments(commentsCtrl.getNumOfComments());
				cmc.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			if(commentsCtrl != null) {
				updateComments(commentsCtrl.getNumOfComments());
			}
			cleanUp();
		} else if(leaveDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doLeave();
				fireEvent(ureq, new LeavingEvent());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(cmc);
		commentsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void doConfirmLeave(UserRequest ureq) {
		String reName = StringHelper.escapeHtml(entry.getDisplayname());
		String title = translate("sign.out");
		String text = "<div class='o_warning'>" + translate("sign.out.dialog.text", reName) + "</div>";
		leaveDialogBox = activateYesNoDialog(ureq, title, text, leaveDialogBox);
	}
	
	protected void doLeave() {
		if(guestOnly) return;
		
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), true);
		LeavingStatusList status = new LeavingStatusList();
		//leave course
		repositoryManager.leave(getIdentity(), entry, status, reMailing);
		//leave groups
		businessGroupService.leave(getIdentity(), entry, status, reMailing);
		DBFactory.getInstance().commit();//make sur all changes are committed
		
		if(status.isWarningManagedGroup() || status.isWarningManagedCourse()) {
			showWarning("sign.out.warning.managed");
		} else if(status.isWarningGroupWithMultipleResources()) {
			showWarning("sign.out.warning.mutiple.resources");
		} else {
			showInfo("sign.out.success", new String[]{ entry.getDisplayname() });
		}
	}
	
	protected boolean doMark() {
		OLATResourceable item = OresHelper.clone(entry);
		if (markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		}
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		return true;
	}
	
	private void doRating(float rating) {
		userRatingsDao.updateRating(getIdentity(), entry, null, Math.round(rating));
	}
	
	protected void doOpenComments(UserRequest ureq) {
		if (guardModalController(commentsCtrl)) return;
		
		boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", entry.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, null, secCallback);
		listenTo(commentsCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", commentsCtrl.getInitialComponent(), true, translate("comments"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateComments(int numOfComments) {
		String title = "(" + numOfComments + ")";
		commentsLink.setI18nKey(title);
		String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
		commentsLink.setIconLeftCSS(css);
	}
	
	protected void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	public static final class TaxonomyLevelItem {
		
		private final String displayName;
		private final String materializedPathIdentifiersWithoutSlash;
		
		public TaxonomyLevelItem(String displayName, String materializedPathIdentifiersWithoutSlash) {
			this.displayName = displayName;
			this.materializedPathIdentifiersWithoutSlash = materializedPathIdentifiersWithoutSlash;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getMaterializedPathIdentifiersWithoutSlash() {
			return materializedPathIdentifiersWithoutSlash;
		}
		
	}

}
