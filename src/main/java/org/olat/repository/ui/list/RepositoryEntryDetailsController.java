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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.admin.restapi.RestapiAdminController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.run.InfoCourse;
import org.olat.course.run.RunMainController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.login.LoginModule;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.CatalogEntry;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * 
 */
public class RepositoryEntryDetailsController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryDetailsController.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	protected FormLink markLink, commentsLink, startLink, leaveLink;
	private RatingWithAverageFormItem ratingEl;
	
	private CloseableModalController cmc;
	private DialogBoxController leaveDialogBox;
	private UserCommentsController commentsCtrl;
	
	protected RepositoryEntry entry;
	protected RepositoryEntryRow row;
	private Integer index;
	private final boolean inRuntime;

	@Autowired
	private LoginModule loginModule;
	@Autowired
	protected UserRatingsDAO userRatingsDao;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected ACService acService;
	@Autowired
	protected AccessControlModule acModule;
	@Autowired
	protected MarkManager markManager;
	@Autowired
	protected CatalogManager catalogManager;
	@Autowired
	protected RepositoryModule repositoryModule;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected BusinessGroupService businessGroupService;
	@Autowired
	protected EfficiencyStatementManager effManager;
	@Autowired
	protected UserCourseInformationsManager userCourseInfosManager;
	@Autowired
	protected CoordinatorManager coordinatorManager;
	@Autowired
	protected ReferenceManager referenceManager;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private CourseModule courseModule;

	
	private String baseUrl;
	private final boolean guestOnly;
	
	public RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, RepositoryEntryRow row, boolean inRuntime) {
		this(ureq, wControl, inRuntime);
		this.row = row;
		this.entry = entry;
		initForm(ureq);
	}
	
	public RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean inRuntime) {
		this(ureq, wControl, inRuntime);
		this.entry = entry;
		initForm(ureq);
	}
	
	private RepositoryEntryDetailsController(UserRequest ureq, WindowControl wControl, boolean inRuntime) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RestapiAdminController.class, getLocale(), getTranslator()));
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		this.inRuntime = inRuntime;

		OLATResourceable ores = OresHelper.createOLATResourceableType("MyCoursesSite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
	}
	
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	private void setText(String text, String key, FormLayoutContainer layoutCont) {
		if(!StringHelper.containsNonWhitespace(text)) return;
		text = StringHelper.xssScan(text);
		if(baseUrl != null) {
			text = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(text);
		}
		text = Formatter.formatLatexFormulas(text);
		layoutCont.contextPut(key, text);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int cmpcount = 0;
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("v", entry);
			layoutCont.contextPut("guestOnly", Boolean.valueOf(guestOnly));
			String cssClass = RepositoyUIFactory.getIconCssClass(entry);
			layoutCont.contextPut("cssClass", cssClass);
			boolean closed = entry.getEntryStatus().decommissioned();
			layoutCont.contextPut("closed", Boolean.valueOf(closed));
			
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
			VFSContainer mediaContainer = handler.getMediaContainer(entry);
			if(mediaContainer != null) {
				baseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
			}
			
			setText(entry.getDescription(), "description", layoutCont);
			setText(entry.getRequirements(), "requirements", layoutCont);
			setText(entry.getObjectives(), "objectives", layoutCont);
			setText(entry.getCredits(), "credits", layoutCont);
			if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
				String technicalType = nodeAccessService.getNodeAccessTypeName(NodeAccessType.of(entry.getTechnicalType()), getLocale());
				setText(technicalType, "technicalType", layoutCont);
			}
			if (entry.getEducationalType() != null) {
				String educationalType = translate(RepositoyUIFactory.getI18nKey(entry.getEducationalType()));
				setText(educationalType, "educationalType", layoutCont);
				layoutCont.contextPut("educationalTypeClass", entry.getEducationalType().getCssClass());				
			}
			
			//thumbnail and movie
			VFSLeaf movie = repositoryService.getIntroductionMovie(entry);
			VFSLeaf image = repositoryService.getIntroductionImage(entry);
			if(image != null || movie != null) {
				ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
				if(movie != null) {
					ic.setMedia(movie);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
					// add poster image
					if (image != null) {
						ic.setPoster(image);
					}
				} else {
					ic.setMedia(image);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
				}
				layoutCont.put("thumbnail", ic);
			}
			
			//categories
			if(repositoryModule.isCatalogEnabled()) {
				List<CatalogEntry> categories = catalogManager.getCatalogEntriesReferencing(entry);
				List<String> categoriesLink = new ArrayList<>(categories.size());
				for(CatalogEntry category:categories) {
					String id = "cat_" + ++cmpcount;
					String title = StringHelper.escapeHtml(category.getParent().getName());
					FormLink catLink = uifactory.addFormLink(id, "category", title, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					catLink.setIconLeftCSS("o_icon o_icon-fw o_icon_catalog");
					catLink.setUserObject(category.getKey());
					categoriesLink.add(id);
				}
				layoutCont.contextPut("categories", categoriesLink);
			}
			
			if(!guestOnly) {
				boolean marked;
				if(row == null) {
					marked = markManager.isMarked(entry, getIdentity(), null);
				} else {
					marked = row.isMarked();
				}
				markLink = uifactory.addFormLink("mark", "mark", marked ? "details.bookmark.remove" : "details.bookmark", null, layoutCont, Link.LINK);
				markLink.setElementCssClass("o_bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			}
			
			RepositoryEntryStatistics statistics = entry.getStatistics();
			if(repositoryModule.isRatingEnabled()) {
				Integer myRating;
				if(row == null) {
					myRating = userRatingsDao.getRatingValue(getIdentity(), entry, null);
				} else {
					myRating = row.getMyRating();
				}
				
				Double averageRating = statistics.getRating();
				long numOfRatings = statistics.getNumOfRatings();
				float ratingValue = myRating == null ? 0f : myRating.floatValue();
				float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
				ratingEl = new RatingWithAverageFormItem("rating", ratingValue, averageRatingValue, 5, numOfRatings);
				ratingEl.setEnabled(!guestOnly);
				layoutCont.add("rating", ratingEl);
			}
			
			if(repositoryModule.isCommentEnabled()) {
				long numOfComments = statistics.getNumOfComments();
				String title = "(" + numOfComments + ")";
				commentsLink = uifactory.addFormLink("comments", "comments", title, null, layoutCont, Link.NONTRANSLATED);
				commentsLink.setCustomEnabledLinkCSS("o_comments");
				String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
				commentsLink.setIconLeftCSS(css);
			}
			
			//load memberships
			List<String> memberRoles = repositoryService.getRoles(getIdentity(), entry);
            List<Long> authorKeys = repositoryService.getAuthors(entry);
            boolean isAuthor = false;
            boolean isMember = memberRoles.contains(GroupRoles.owner.name())
            		|| memberRoles.contains(GroupRoles.coach.name())
            		|| memberRoles.contains(GroupRoles.participant.name());
			if (isMember) {
				isAuthor = authorKeys.contains(getIdentity().getKey());
				layoutCont.contextPut("isEntryAuthor", Boolean.valueOf(isAuthor));
			}
			// push roles to velocity as well
            Roles roles = ureq.getUserSession().getRoles();
			layoutCont.contextPut("roles", roles);

			if(!guestOnly && memberRoles.contains(GroupRoles.participant.name())
					&& repositoryService.isParticipantAllowedToLeave(entry)) {
				leaveLink = uifactory.addFormLink("sign.out", "leave", translate("sign.out"), null, formLayout, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
				leaveLink.setElementCssClass("o_sign_out btn-danger");
				leaveLink.setIconLeftCSS("o_icon o_icon_sign_out");
			}

			//access control
			String accessI18n = null;
			List<PriceMethod> types = new ArrayList<>();
			if(entry.isAllUsers() || entry.isGuests()) {
				String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
				startLink = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
				startLink.setElementCssClass("o_start btn-block");
				if(guestOnly) {
					startLink.setVisible(entry.isGuests());
				}
				accessI18n = translate("cif.status.".concat(entry.getEntryStatus().name()));
			} else if(entry.isBookable()) {
				AccessResult acResult = acService.isAccessible(entry, getIdentity(), isMember, false);
				if(acResult.isAccessible()) {
					String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
					startLink = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
					startLink.setElementCssClass("o_start btn-block");
				} else if (!acResult.getAvailableMethods().isEmpty()) {
					for(OfferAccess access:acResult.getAvailableMethods()) {
						AccessMethod method = access.getMethod();
						String type = (method.getMethodCssClass() + "_icon").intern();
						Price p = access.getOffer().getPrice();
						String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
						AccessMethodHandler amh = acModule.getAccessMethodHandler(method.getType());
						String displayName = amh.getMethodName(getLocale());
						types.add(new PriceMethod(price, type, displayName));
					}
					String linkText = translate("book.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
					startLink = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
					startLink.setCustomEnabledLinkCSS("btn btn-success"); // custom style
					startLink.setElementCssClass("o_book btn-block");
					startLink.setVisible(!guestOnly);
				} else {
					// booking not available -> button not visible
					String linkText = translate("book.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
					startLink = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
					startLink.setVisible(false);
				}
				accessI18n = translate("cif.status.".concat(entry.getEntryStatus().name()));
			} else {
				// visible only to members only
				String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
				startLink = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
				startLink.setElementCssClass("o_start btn-block");
				startLink.setVisible(isMember);
				accessI18n = translate("cif.access.membersonly");
			}
			
			startLink.setIconRightCSS("o_icon o_icon_start o_icon-lg");
			startLink.setPrimary(true);
			startLink.setFocus(true);

			layoutCont.contextPut("accessI18n", accessI18n);
			
			if(!types.isEmpty()) {
				layoutCont.contextPut("ac", types);
			}
			
			if(isMember) {
				//show the list of groups
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				List<String> groupLinkNames = new ArrayList<>(groups.size());
				for(BusinessGroup group:groups) {
					String groupLinkName = "grp_" + ++cmpcount;
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
			if(row != null) {
				passed = row.isPassed();
				failed = row.isFailed();
				score = row.getScore();
			} else {
				UserEfficiencyStatement statement = effManager.getUserEfficiencyStatementLightByRepositoryEntry(entry, getIdentity());
				if(statement != null) {
					Boolean p = statement.getPassed();
					if(p != null) {
						passed = p.booleanValue();
						failed = !p.booleanValue();
					}
					
					Float scoreVal = statement.getScore();
					if(scoreVal != null) {
						score = AssessmentHelper.getRoundedScore(scoreVal);
					}
				}
			}
			layoutCont.contextPut("passed", passed);
			layoutCont.contextPut("failed", failed);
			layoutCont.contextPut("score", score);
			
            OLATResource ores = entry.getOlatResource();
			Date recentLaunch = userCourseInfosManager.getRecentLaunchDate(ores, getIdentity());
			layoutCont.contextPut("recentLaunch", recentLaunch);
			
			// show how many users are currently using this resource
            String numUsers;
            int cnt = 0;
			Long courseResId = entry.getOlatResource().getResourceableId();
            OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, courseResId);
            if (ores != null) cnt = coordinatorManager.getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
            numUsers = String.valueOf(cnt);
            layoutCont.contextPut("numUsers", numUsers);
            
            // Where is it in use
            if(isAuthor || roles.isAdministrator() || roles.isLearnResourceManager()) {
				List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());
				if(!refs.isEmpty()) {
					List<String> refLinks = new ArrayList<>(refs.size());
					int count = 0;
					for(RepositoryEntry ref:refs) {
						String name = "ref-" + count++;
						FormLink refLink = uifactory
								.addFormLink(name, "ref", ref.getDisplayname(), null, formLayout, Link.NONTRANSLATED);
						refLink.setUserObject(ref.getKey());
						refLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(ref));
						refLinks.add(name);
					}
	            	layoutCont.contextPut("referenceLinks", refLinks);
				}
            }
            
            if(organisationModule.isEnabled()) {
            	String organisations = getOrganisationsToString();
            	layoutCont.contextPut("organisations", organisations);
            }
            if(curriculumModule.isEnabled()) {
            	List<String> curriculums = getCurriculumsToString();
            	layoutCont.contextPut("curriculums", curriculums);
            }
            
            // Link to bookmark entry
            String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
            layoutCont.contextPut("extlink", url);
            Boolean guestAllowed = Boolean.valueOf(entry.isGuests() && loginModule.isGuestLoginLinksEnabled());
            layoutCont.contextPut("isGuestAllowed", guestAllowed);

			//Owners
			List<String> authorLinkNames = new ArrayList<>(authorKeys.size());
			Map<Long,String> authorNames = userManager.getUserDisplayNamesByKey(authorKeys);
			int counter = 0;
			for(Map.Entry<Long, String> author:authorNames.entrySet()) {
				Long authorKey = author.getKey();
				String authorName = StringHelper.escapeHtml(author.getValue());

				FormLink authorLink = uifactory.addFormLink("owner-" + ++counter, "owner", authorName, null, formLayout, Link.NONTRANSLATED | Link.LINK);
				authorLink.setUserObject(authorKey);
				authorLinkNames.add(authorLink.getComponent().getComponentName());
			}
			layoutCont.contextPut("authorlinknames", authorLinkNames);
			
			// License
			boolean licEnabled = licenseModule.isEnabled(licenseHandler);
			if (licEnabled) {
				License license = licenseService.loadOrCreateLicense(entry.getOlatResource());				
				LicenseType licenseType = license.getLicenseType();
				if (licenseType == null || "no.license".equals(licenseType.getName())) {
					// dont' show the no-license
					layoutCont.contextPut("licSwitch", Boolean.FALSE);					
				} else {
					layoutCont.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
					layoutCont.contextPut("licenseIconCss", LicenseUIFactory.getCssOrDefault(licenseType));
					String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
					layoutCont.contextPut("licensor", licensor);
					layoutCont.contextPut("licenseText", LicenseUIFactory.getFormattedLicenseText(license));					
					layoutCont.contextPut("licSwitch", Boolean.TRUE);					
				}
			} else {
				layoutCont.contextPut("licSwitch", Boolean.FALSE);
			}
			
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
				layoutCont.contextPut("oInfoCourse", oInfoCourse);
			}
		}
	}
	
	private List<String> getCurriculumsToString() {
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(entry);
    	Map<Curriculum, StringBuilder> curriculumToElementsMap = new HashMap<>();
    	for(CurriculumElement curriculumElement:curriculumElements) {
    		Curriculum curriculum = curriculumElement.getCurriculum();
    		StringBuilder sc = curriculumToElementsMap.computeIfAbsent(curriculum, c -> {
    			StringBuilder sb = new StringBuilder(64);
    			sb.append(StringHelper.escapeHtml(c.getDisplayName())).append(" (");
    			return sb;
    		});
    		sc.append(StringHelper.escapeHtml(curriculumElement.getDisplayName())).append(", ");
    	}
    	
    	List<String> curriculumList = new ArrayList<>(curriculumToElementsMap.size());
    	for(StringBuilder sb:curriculumToElementsMap.values()) {
    		String line = sb.toString().substring(0, sb.length() -2).concat(")");
    		curriculumList.add(line);
    	}
    	return curriculumList;
	}
	
	private String getOrganisationsToString() {
		List<Organisation> organisations = repositoryService.getOrganisations(entry);
    	StringBuilder sb = new StringBuilder(64);
    	for(Organisation organisation:organisations) {
    		if(sb.length() > 0) sb.append(", ");
    		sb.append(StringHelper.escapeHtml(organisation.getDisplayName()));
    	}
    	return sb.toString();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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
	
	private void updateComments(int numOfComments) {
		String title = "(" + numOfComments + ")";
		commentsLink.setI18nKey(title);
		String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
		commentsLink.setIconLeftCSS(css);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(cmc);
		commentsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("category".equals(cmd)) {
				Long categoryKey = (Long)link.getUserObject();
				doOpenCategory(ureq, categoryKey);
			} else if("mark".equals(cmd)) {
				boolean marked = doMark();
				markLink.setI18nKey(marked ? "details.bookmark.remove" : "details.bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);

			} else if("comments".equals(cmd)) {
				doOpenComments(ureq);
			} else if("start".equals(cmd)) {
				doStart(ureq);
			} else if("group".equals(cmd)) {
				Long groupKey = (Long)link.getUserObject();
				doOpenGroup(ureq, groupKey);
			} else if("owner".equals(cmd)) {
				Long ownerKey = (Long)link.getUserObject();
				doOpenVisitCard(ureq, ownerKey);
			} else if("leave".equals(cmd)) {
				doConfirmLeave(ureq);
			} else if("ref".equals(cmd)) {
				doOpenReference(ureq, (Long)link.getUserObject());
			}
		} else if(ratingEl == source && event instanceof RatingFormEvent) {
			RatingFormEvent ratingEvent = (RatingFormEvent)event;
			doRating(ratingEvent.getRating());
		}
		super.formInnerEvent(ureq, source, event);
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
	
	protected void doStart(UserRequest ureq) {
		if(inRuntime) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			try {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + entry.getKey() + " (" + entry.getOlatResource().getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		}
	}
	
	protected void doOpenReference(UserRequest ureq, Long entryKey) {
		String businessPath = "[RepositoryEntry:" + entryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenVisitCard(UserRequest ureq, Long ownerKey) {
		String businessPath = "[HomePage:" + ownerKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected boolean doMark() {
		OLATResourceable item = OresHelper.clone(entry);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
	
	private void doRating(float rating) {
		userRatingsDao.updateRating(getIdentity(), entry, null, Math.round(rating));
	}
	
	protected void doOpenComments(UserRequest ureq) {
		if(guardModalController(commentsCtrl)) return;
		
		boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", entry.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, null, secCallback);
		listenTo(commentsCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", commentsCtrl.getInitialComponent(), true, translate("comments"));
		listenTo(cmc);
		cmc.activate();
	}
}
