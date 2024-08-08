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

package org.olat.course.nodes.portfolio;

import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.formEvaluation;
import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.gradeSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.AssessmentDocumentsSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.PanelInfo;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.ceditor.ContentElement;
import org.olat.modules.ceditor.ContentElementType;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.Page;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.ui.AccessRightsEditController;
import org.olat.modules.portfolio.ui.ImageMapper;
import org.olat.modules.portfolio.ui.InvitationEditRightsController;
import org.olat.modules.portfolio.ui.InvitationEmailController;
import org.olat.modules.portfolio.ui.event.AccessRightsEvent;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.modules.portfolio.ui.shared.SharedBindersCourseNodeController;
import org.olat.modules.portfolio.ui.wizard.AccessRightsContext;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_ChooseMemberStep;
import org.olat.modules.portfolio.ui.wizard.AddMember_1_CourseMemberChoiceStep;
import org.olat.modules.portfolio.ui.wizard.AddMember_3_ChoosePermissionStep;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Initial Date:  6 oct. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PortfolioCourseNodeRunController extends FormBasicController implements AssessmentDocumentsSupplier {

	private Binder copyBinder;
	private Binder templateBinder;

	private final PortfolioCourseNode courseNode;

	private FormLink newMapLink;
	private FormLink selectMapLink;
	private FormLayoutContainer assessmentInfosContainer;

	private DialogBoxController restoreBinderCtrl;
	private AssessmentParticipantViewController assessmentParticipantViewCtrl;
	private InvitationEditRightsController addInvitationCtrl;
	private StepsMainRunController addMembersWizardCtrl;
	private AccessRightsEditController editAccessRightsCtrl;
	private InvitationEmailController addInvitationEmailCtrl;
	private CloseableModalController cmc;

	private Boolean sharedMeOpen = Boolean.TRUE;
	private Boolean sharedByOpen = Boolean.TRUE;

	private final UserCourseEnvironment userCourseEnv;
	private final AssessmentConfig assessmentConfig;
	private AssessmentEvaluation assessmentEval;
	private PanelInfo panelInfo;
	private BinderSecurityCallback binderSecCallback;

	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public PortfolioCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
											PortfolioCourseNode courseNode) {
		super(ureq, wControl, "run");
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));

		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), courseNode);

		RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
		if (mapEntry != null) {
			if (RepositoryEntryStatusEnum.deleted == mapEntry.getEntryStatus()
					|| RepositoryEntryStatusEnum.trash == mapEntry.getEntryStatus()) {
				EmptyStateConfig emptyState = EmptyStateConfig.builder()
						.withIconCss("o_ep_icon")
						.withIndicatorIconCss("o_icon_deleted")
						.withMessageI18nKey("error.portfolio.deleted.node")
						.build();
				EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
				emptyStateCmp.setTranslator(getTranslator());
				initialPanel.setContent(emptyStateCmp);
				return;
			}
			if (BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				templateBinder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
			}
		}

		initForm(ureq);
	}

	@Override
	public List<VFSLeaf> getIndividualAssessmentDocuments() {
		return courseAssessmentService.getIndividualAssessmentVFSDocuments(courseNode, userCourseEnv);
	}

	@Override
	public boolean isDownloadEnabled() {
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String assessmentPage = velocity_root + "/assessment_infos.html";
		assessmentInfosContainer = FormLayoutContainer.createCustomFormLayout("assessmentInfos", getTranslator(), assessmentPage);
		assessmentInfosContainer.setVisible(false);
		formLayout.add(assessmentInfosContainer);

		VelocityContainer mainVC = ((FormLayoutContainer) formLayout).getFormItemComponent();

		if (courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, false)) {
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(), userCourseEnv,
					courseNode, this.mainForm);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				mainVC.put("highScore", highScoreComponent);
			}
		}

		if (templateBinder != null) {
			updateUI(ureq);
		}
	}

	protected void updateUI(UserRequest ureq) {
		if (templateBinder != null) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			copyBinder = portfolioService.getBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent());
		}

		assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);

		if (assessmentEval != null && assessmentEval.getMaxScore() != null) {
			String maxScoreLabel = translate("assessment.minmax.value.plural", "0", AssessmentHelper.getRoundedScore(assessmentEval.getMaxScore()));
			flc.contextPut("maxScore", maxScoreLabel);
		}

		if (copyBinder == null || copyBinder.getBinderStatus() == BinderStatus.deleted) {
			updateEmptyUI(ureq);
		} else {
			updateSelectedUI(ureq);
		}

		if (selectMapLink != null) {
			selectMapLink.setVisible(copyBinder != null && copyBinder.getBinderStatus() != BinderStatus.deleted);
		}
		if (newMapLink != null) {
			newMapLink.setVisible(copyBinder == null || copyBinder.getBinderStatus() == BinderStatus.deleted);
		}
	}

	private void updateEmptyUI(UserRequest ureq) {
		if (templateBinder != null) {
			updateBinderData(ureq, templateBinder);
			String portfolioDesc = StringHelper.containsNonWhitespace(templateBinder.getSummary()) ? Formatter.truncate(FilterFactory.getHtmlTagsFilter().filter(templateBinder.getSummary()), 255) : " ";
			flc.contextPut("portfolioDesc", portfolioDesc);
		}

		if (newMapLink == null) {
			newMapLink = uifactory.addFormLink("map.new", flc, Link.BUTTON);
			newMapLink.setElementCssClass("o_sel_ep_new_map_template o_button_call_to_action");
			newMapLink.setPrimary(true);
		}
	}

	private void updateBinderData(UserRequest ureq, Binder binder) {
		String title = StringHelper.escapeHtml(binder.getTitle());
		if (portfolioService.getPosterImageLeaf(binder) != null) {
			// put image information into context
			flc.contextPut("image", registerCacheableMapper(ureq, "binder-cn", new ImageMapper(portfolioService.getPosterImageLeaf(binder))));
			flc.contextPut("imageName", portfolioService.getPosterImageLeaf(binder).getName());
		}
		// put binder information into context
		flc.contextPut("instructions", courseNode.getInstruction());
		flc.contextPut("portfolioTitle", title);
	}

	private void updateSelectedUI(UserRequest ureq) {
		if (selectMapLink == null) {
			selectMapLink = uifactory.addFormLink("select", "open.portfolio", "open.portfolio", flc, Link.BUTTON);
			selectMapLink.setElementCssClass("o_sel_ep_select_map o_button_call_to_action");
			selectMapLink.setPrimary(true);
		} else {
			selectMapLink.setVisible(true);
		}

		if (copyBinder != null) {
			flc.contextRemove("portfolioDesc");
			flc.remove("map.new");
			updateSelectedBinderUI(ureq);
		}
	}

	private void updateSelectedBinderUI(UserRequest ureq) {
		BinderStatistics binderStats = portfolioService.getBinderStatistics(copyBinder);
		binderSecCallback = BinderSecurityCallbackFactory.getCallbackForOwnedBinder(copyBinder);

		updateBinderData(ureq, copyBinder);
		flc.contextPut("binderLastUpdate", binderStats.getLastModified());
		String[] numOfSectionsAndPages = {
				Integer.toString(binderStats.getNumOfSections()),
				Integer.toString(binderStats.getNumOfPages())
		};
		flc.contextPut("numSections", numOfSectionsAndPages);
		flc.contextPut("numComments", binderStats.getNumOfComments());

		List<AccessRights> accessRights = portfolioService.getAccessRights(copyBinder);
		flc.contextPut("accessRights", accessRights);

		PortfolioElementRow binderRow = new PortfolioElementRow(copyBinder, null);
		flc.contextPut("binderRow", binderRow);

		boolean canEditBinderAccessRights = binderSecCallback.canEditAccessRights(copyBinder);
		for (AccessRights right : accessRights) {
			if (right.getSectionKey() == null && right.getPageKey() == null) {
				if (ContentRoles.invitee.equals(right.getRole())) {
					continue; //only access
				}

				FormLink editLink = null;
				if (canEditBinderAccessRights
						&& !ContentRoles.owner.equals(right.getRole())) {
					String id = "edit_" + CodeHelper.getUniqueID();

					editLink = uifactory.addFormLink(id, "edit_access", "edit", "edit", flc, Link.LINK);
				}
				binderRow.getAccessRights().add(new AccessRightsRow(copyBinder, right, editLink));
			}
		}
		flc.contextPut("isSharedWithCoach", accessRights.stream().anyMatch(ar -> ar.getRole().equals(ContentRoles.coach)));
		flc.contextPut("sharedByOpen", sharedByOpen);

		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(copyBinder, getIdentity());
		Map<Section, AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for (AssessmentSection assessmentSection : assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}

		//sections
		List<Section> sections = portfolioService.getSections(copyBinder);
		Map<Long, PortfolioElementRow> sectionMap = new HashMap<>();
		for (Section section : sections) {
			boolean canEditSectionAccessRights = binderSecCallback.canEditAccessRights(section);
			boolean canViewSectionAccessRights = binderSecCallback.canViewAccessRights(section);
			if (canEditSectionAccessRights || canViewSectionAccessRights) {
				PortfolioElementRow sectionRow = new PortfolioElementRow(section, sectionToAssessmentSectionMap.get(section));
				binderRow.getChildren().add(sectionRow);
				sectionMap.put(section.getKey(), sectionRow);

				for (AccessRights right : accessRights) {
					if (section.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
						FormLink editLink = null;
						if (!ContentRoles.owner.equals(right.getRole())) {
							String id = "edit_" + CodeHelper.getUniqueID();
							editLink = uifactory.addFormLink(id, "edit_access", "edit", "edit", flc, Link.LINK);
							sectionRow.getAccessRights().add(new AccessRightsRow(section, right, editLink));
						}
					}
				}
			}
		}

		//pages
		List<Page> pages = portfolioService.getPages(copyBinder);
		for (Page page : pages) {
			boolean canEditPageAccessRights = binderSecCallback.canEditAccessRights(page);
			boolean canViewPageAccessRights = binderSecCallback.canViewAccessRights(page);
			if (canEditPageAccessRights || canViewPageAccessRights) {
				Section section = page.getSection();
				PortfolioElementRow sectionRow = sectionMap.get(section.getKey());
				if (sectionRow == null) {
					logError("Section not found: " + section.getKey() + " of page: " + page.getKey(), null);
					continue;
				}

				PortfolioElementRow pageRow = new PortfolioElementRow(page, null);
				sectionRow.getChildren().add(pageRow);

				for (AccessRights right : accessRights) {
					if (page.getKey().equals(right.getPageKey())) {
						FormLink editLink = null;
						if (!ContentRoles.owner.equals(right.getRole())) {
							String id = "edit_" + CodeHelper.getUniqueID();
							editLink = uifactory.addFormLink(id, "edit_access", "edit", "edit", flc, Link.LINK);
							pageRow.getAccessRights().add(new AccessRightsRow(page, right, editLink));
						}
					}
				}
			}
		}

		initAddAccessRightsTools();
		updateAssessmentInfos(ureq, copyBinder.getReturnDate());

		doOpenBinders(ureq);
	}

	public void initAddAccessRightsTools() {
		if (binderSecCallback.canEditAccessRights(copyBinder)) {
			DropdownItem accessDropdown = uifactory.addDropdownMenu("access.rights", "access.rights", flc, getTranslator());
			accessDropdown.setIconCSS("o_icon o_icon-fw o_icon_new_portfolio");
			accessDropdown.setElementCssClass("o_sel_pf_access");
			accessDropdown.setOrientation(DropdownOrientation.right);

			FormLink addOwnerAccessRightsLink = uifactory.addFormLink("add.course.owner", flc, Link.LINK);
			addOwnerAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user_vip");
			addOwnerAccessRightsLink.setElementCssClass("o_sel_pf_access_course_owner");
			accessDropdown.addElement(addOwnerAccessRightsLink);

			FormLink addCoachAccessRightsLink = uifactory.addFormLink("add.course.coach", flc, Link.LINK);
			addCoachAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user_vip");
			addCoachAccessRightsLink.setElementCssClass("o_sel_pf_access_course_coach");
			accessDropdown.addElement(addCoachAccessRightsLink);

			FormLink addParticipantAccessRightsLink = uifactory.addFormLink("add.course.participant", flc, Link.LINK);
			addParticipantAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_group");
			addParticipantAccessRightsLink.setElementCssClass("o_sel_pf_access_course_participant");
			accessDropdown.addElement(addParticipantAccessRightsLink);

			FormLink addAccessRightsLink = uifactory.addFormLink("add.member", flc, Link.LINK);
			addAccessRightsLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user");
			addAccessRightsLink.setElementCssClass("o_sel_pf_access_member");
			accessDropdown.addElement(addAccessRightsLink);

			if (invitationModule.isPortfolioInvitationEnabled()) {
				FormLink addInvitationLink = uifactory.addFormLink("add.invitation", flc, Link.LINK);
				addInvitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_user_anonymous");
				addInvitationLink.setElementCssClass("o_sel_pf_access_invitation");
				accessDropdown.addElement(addInvitationLink);
			}
		}
	}

	private void updateAssessmentInfos(UserRequest ureq, Date returnDate) {
		if (userCourseEnv.isParticipant() && (returnDate != null || copyBinder != null)) {
			if (panelInfo == null) {
				panelInfo = new PanelInfo(PortfolioCourseNodeRunController.class,
						"::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent());
			}

			removeAsListenerAndDispose(assessmentParticipantViewCtrl);
			assessmentParticipantViewCtrl = null;
			if (Mode.none != assessmentConfig.getScoreMode() || Mode.none != assessmentConfig.getPassedMode()) {
				assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(),
						assessmentEval, assessmentConfig, this, gradeSystem(userCourseEnv, courseNode),
						formEvaluation(userCourseEnv, courseNode, assessmentConfig), panelInfo, false, false);
				listenTo(assessmentParticipantViewCtrl);
				assessmentInfosContainer.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());
				
				PortfolioWidget widget = new PortfolioWidget("binder", getTranslator());
				widget.setTitle(translate("portfolio.title"));
				widget.setIconCss("o_icon_pf_binder");
				widget.setBinder(copyBinder);
				assessmentParticipantViewCtrl.addCustomWidget(widget);
			}

			assessmentInfosContainer.setVisible(true);
		} else {
			assessmentInfosContainer.setVisible(false);
		}
	}

	private void doOpenBinders(UserRequest ureq) {
		flc.contextPut("sharedMeOpen", sharedMeOpen);
		SharedBindersCourseNodeController sharedBindersCourseNodeController = new SharedBindersCourseNodeController(ureq, getWindowControl(), mainForm, courseNode.getIdent());
		listenTo(sharedBindersCourseNodeController);
		flc.contextPut("shareBinderSize", sharedBindersCourseNodeController.getModel().getObjects());
		flc.add("shareBinder", sharedBindersCourseNodeController.getInitialFormItem());
	}

	private void cleanUp() {
		removeAsListenerAndDispose(addInvitationEmailCtrl);
		removeAsListenerAndDispose(editAccessRightsCtrl);
		removeAsListenerAndDispose(addInvitationCtrl);
		removeAsListenerAndDispose(addMembersWizardCtrl);
		removeAsListenerAndDispose(cmc);
		addInvitationEmailCtrl = null;
		editAccessRightsCtrl = null;
		addInvitationCtrl = null;
		addMembersWizardCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (restoreBinderCtrl == source && (DialogBoxUIFactory.isYesEvent(event))) {
			doRestore();
			updateUI(ureq);
		} else if (addMembersWizardCtrl == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					updateSelectedBinderUI(ureq);
				}
				cleanUp();
			}
		} else if (addInvitationCtrl == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					updateSelectedBinderUI(ureq);
				}
				cleanUp();
			}
		} else if (source == editAccessRightsCtrl && editAccessRightsCtrl != null) {
			if (event == Event.DONE_EVENT) {
				List<AccessRightChange> changes = editAccessRightsCtrl.getChanges();
				List<Identity> identities = Collections.singletonList(editAccessRightsCtrl.getMember());
				portfolioService.changeAccessRights(identities, changes);
				updateSelectedBinderUI(ureq);
			} else if (AccessRightsEvent.REMOVE_ALL_RIGHTS.equals(event.getCommand())) {
				portfolioService.removeAccessRights(copyBinder, editAccessRightsCtrl.getMember(),
						ContentRoles.coach, ContentRoles.reviewer, ContentRoles.invitee, ContentRoles.readInvitee);
				updateSelectedBinderUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == addInvitationEmailCtrl
				&& addInvitationEmailCtrl != null
				&& (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT)) {
			String email = addInvitationEmailCtrl.getEmail();
			Identity invitee = addInvitationEmailCtrl.getInvitee();
			cmc.deactivate();
			cleanUp();

			if (event == Event.DONE_EVENT) {
				if (invitee != null) {
					doAddInvitation(ureq, invitee);
				} else {
					doAddInvitation(ureq, email);
				}
			}

		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String sharedMeVal = ureq.getParameter("sharedMeOpen");
			if (StringHelper.containsNonWhitespace(sharedMeVal)) {
				sharedMeOpen = Boolean.valueOf(sharedMeVal);
				flc.contextPut("sharedMeOpen", sharedMeOpen);
			}
			String sharedByVal = ureq.getParameter("sharedByOpen");
			if (StringHelper.containsNonWhitespace(sharedByVal)) {
				sharedByOpen = Boolean.valueOf(sharedByVal);
				flc.contextPut("sharedByOpen", sharedByOpen);
			}
		} else if (source == newMapLink) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			if (templateBinder != null) {
				if (copyBinder == null) {
					copyBinder = portfolioService.assignBinder(getIdentity(), templateBinder, courseEntry, courseNode.getIdent());
					if (copyBinder != null) {
						// after user collects binder, set assessmentEntryStatus inProgress
						AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(getIdentity(), courseEntry, courseNode.getIdent());
						assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
						showInfo("map.copied", StringHelper.escapeHtml(templateBinder.getTitle()));
						ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(copyBinder));
						ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_TASK_STARTED, getClass());
					}
				} else if (copyBinder.getBinderStatus() == BinderStatus.deleted) {
					String title = translate("trashed.binder.confirm.title");
					String text = translate("trashed.binder.confirm.descr", StringHelper.escapeHtml(copyBinder.getTitle()));
					restoreBinderCtrl = activateYesNoDialog(ureq, title, text, restoreBinderCtrl);
					restoreBinderCtrl.setUserObject(copyBinder);
					return;
				}
			}

			updateUI(ureq);
		} else if (source == selectMapLink) {
			String resourceUrl;
			if (copyBinder != null) {
				resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MyBinders:0][Binder:" + copyBinder.getKey() + "]";
			} else {
				return;
			}
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		} else if (source instanceof FormLink editLink) {
			String cmd = editLink.getCmd();
			if ("edit_access".equals(cmd)) {
				AccessRightsRow row = (AccessRightsRow) editLink.getUserObject();
				if (ContentRoles.invitee.name().equals(row.getRole())
						|| ContentRoles.readInvitee.name().equals(row.getRole())) {
					doEditInvitation(ureq, row.getIdentity());
				} else {
					doEditAccessRights(ureq, row.getElement(), row.getIdentity());
				}
			} else if ("add.member".equals(cmd)) {
				doAddAccessRights(ureq);
			} else if ("add.course.owner".equals(cmd)) {
				doAddAccessRights(ureq, GroupRoles.owner, "add.course.owner");
			} else if ("add.course.coach".equals(cmd)) {
				doAddAccessRights(ureq, GroupRoles.coach, "add.course.coach");
			} else if ("add.course.participant".equals(cmd)) {
				doAddAccessRights(ureq, GroupRoles.participant, "add.course.participant");
			} else if ("add.invitation".equals(cmd)) {
				doAddInvitationEmail(ureq);
			}
		}
	}

	private void doAddInvitation(UserRequest ureq, String email) {
		if (guardModalController(addInvitationCtrl)) return;

		addInvitationCtrl = new InvitationEditRightsController(ureq, getWindowControl(), copyBinder, email, null);
		listenTo(addInvitationCtrl);

		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddInvitation(UserRequest ureq, Identity invitee) {
		removeAsListenerAndDispose(addMembersWizardCtrl);

		Roles inviteeRoles = securityManager.getRoles(invitee);
		if (inviteeRoles.isInvitee()) {
			doAddInvitation(ureq, invitee.getUser().getEmail());
		} else {

			Step start = new AddMember_3_ChoosePermissionStep(ureq, copyBinder, invitee);
			StepRunnerCallback finish = (uureq, wControl, runContext) -> {
				AccessRightsContext rightsContext = (AccessRightsContext) runContext.get("rightsContext");
				MailTemplate mailTemplate = (MailTemplate) runContext.get("mailTemplate");
				addMembers(rightsContext, mailTemplate);
				return StepsMainRunController.DONE_MODIFIED;
			};

			addMembersWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
					translate("add.member"), "o_sel_course_member_import_1_wizard");
			listenTo(addMembersWizardCtrl);
			getWindowControl().pushAsModalDialog(addMembersWizardCtrl.getInitialComponent());
		}
	}

	private void doAddInvitationEmail(UserRequest ureq) {
		if (guardModalController(addInvitationEmailCtrl)) return;

		addInvitationEmailCtrl = new InvitationEmailController(ureq, getWindowControl(), copyBinder);
		listenTo(addInvitationEmailCtrl);

		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationEmailCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddAccessRights(UserRequest ureq, GroupRoles role, String titleKey) {
		removeAsListenerAndDispose(addMembersWizardCtrl);

		Step start = new AddMember_1_CourseMemberChoiceStep(ureq, copyBinder, userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), role);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			AccessRightsContext rightsContext = (AccessRightsContext) runContext.get("rightsContext");
			MailTemplate mailTemplate = (MailTemplate) runContext.get("mailTemplate");
			addMembers(rightsContext, mailTemplate);
			return StepsMainRunController.DONE_MODIFIED;
		};

		addMembersWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(titleKey), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizardCtrl);
		getWindowControl().pushAsModalDialog(addMembersWizardCtrl.getInitialComponent());
	}

	private void doAddAccessRights(UserRequest ureq) {
		removeAsListenerAndDispose(addMembersWizardCtrl);

		Step start = new AddMember_1_ChooseMemberStep(ureq, copyBinder);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			AccessRightsContext rightsContext = (AccessRightsContext) runContext.get("rightsContext");
			MailTemplate mailTemplate = (MailTemplate) runContext.get("mailTemplate");
			addMembers(rightsContext, mailTemplate);
			updateSelectedBinderUI(uureq);
			return StepsMainRunController.DONE_MODIFIED;
		};

		addMembersWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_course_member_import_1_wizard");
		listenTo(addMembersWizardCtrl);
		getWindowControl().pushAsModalDialog(addMembersWizardCtrl.getInitialComponent());
	}

	private void doEditInvitation(UserRequest ureq, Identity invitee) {
		if (guardModalController(addInvitationCtrl)) return;

		addInvitationCtrl = new InvitationEditRightsController(ureq, getWindowControl(), copyBinder, invitee);
		listenTo(addInvitationCtrl);

		String title = translate("add.invitation");
		cmc = new CloseableModalController(getWindowControl(), null, addInvitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditAccessRights(UserRequest ureq, ContentElement element, Identity member) {
		if (guardModalController(editAccessRightsCtrl)) return;

		boolean canEdit = binderSecCallback.canEditAccessRights(element);
		editAccessRightsCtrl = new AccessRightsEditController(ureq, getWindowControl(), copyBinder, member, canEdit);
		listenTo(editAccessRightsCtrl);

		String title = translate("edit.access.rights");
		cmc = new CloseableModalController(getWindowControl(), null, editAccessRightsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void addMembers(AccessRightsContext rightsContext, MailTemplate mailTemplate) {
		List<Identity> identities = rightsContext.getIdentities();
		List<AccessRightChange> changes = rightsContext.getAccessRightChanges();
		portfolioService.changeAccessRights(identities, changes);

		if (mailTemplate != null) {
			sendInvitation(identities, mailTemplate);
		}
	}

	private void sendInvitation(List<Identity> identities, MailTemplate mailTemplate) {
		ContactList contactList = new ContactList("Invitation");
		contactList.addAllIdentites(identities);

		boolean success = false;
		try {
			MailContext context = new MailContextImpl(copyBinder, null, getWindowControl().getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());
			bundle.setContactList(contactList);
			bundle.setContent(mailTemplate.getSubjectTemplate(), mailTemplate.getBodyTemplate());
			MailerResult result = mailManager.sendMessage(bundle);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("invitation.mail.success");
		} else {
			showError("invitation.mail.failure");
		}
	}

	private void doRestore() {
		copyBinder = portfolioService.getBinderByKey(copyBinder.getKey());
		copyBinder.setBinderStatus(BinderStatus.open);
		copyBinder = portfolioService.updateBinder(copyBinder);
		showInfo("restore.binder.success");
	}

	public class PortfolioElementRow {

		private final ContentElement element;
		private List<PortfolioElementRow> children;
		private final List<AccessRightsRow> accessRights = new ArrayList<>();
		private final BinderConfiguration binderConfig;

		private final AssessmentSection assessmentSection;

		public PortfolioElementRow(ContentElement element, AssessmentSection assessmentSection) {
			this.element = element;
			this.assessmentSection = assessmentSection;
			this.binderConfig = BinderConfiguration.createConfig(copyBinder);
		}

		public boolean isAssessable() {
			return binderConfig.isAssessable();
		}


		public String getTitle() {
			return element.getTitle();
		}

		public String getCssClassStatus() {
			if (element.getType() == ContentElementType.section) {
				Section section = (Section) element;
				return section.getSectionStatus() == null
						? SectionStatus.notStarted.iconClass() : section.getSectionStatus().iconClass();
			}
			return "";
		}

		public String getFormattedResult() {
			if (element.getType() == ContentElementType.section) {
				return PortfolioRendererHelper.getFormattedResult(assessmentSection, getTranslator());
			}
			return "";
		}

		public List<AccessRightsRow> getAccessRights() {
			return accessRights;
		}

		public List<PortfolioElementRow> getChildren() {
			if (children == null) {
				children = new ArrayList<>();
			}
			return children;
		}
	}

	public class AccessRightsRow {

		private final AccessRights rights;
		private final ContentElement element;
		private final String fullName;
		private final FormLink editLink;

		public AccessRightsRow(ContentElement element, AccessRights rights, FormLink editLink) {
			this.rights = rights;
			this.editLink = editLink;
			this.element = element;

			if (rights.getInvitation() == null) {
				fullName = userManager.getUserDisplayName(rights.getIdentity());
			} else {
				Invitation invitation = rights.getInvitation();
				fullName = userManager.getUserDisplayName(invitation.getFirstName(), invitation.getLastName());
			}

			if (editLink != null) {
				editLink.setUserObject(this);
			}
		}

		public String getRole() {
			return rights.getRole().name();
		}

		public Identity getIdentity() {
			return rights.getIdentity();
		}

		public ContentElement getElement() {
			return element;
		}

		public String getFullName() {
			return fullName;
		}

		public String getCssClass() {
			if (ContentRoles.reviewer.equals(rights.getRole())) {
				return "o_icon o_icon_reviewer o_icon-fw";
			}
			return "o_icon o_icon_user o_icon-fw";
		}

		public FormLink getEditLink() {
			return editLink;
		}

		public String getExplanation() {
			String explanation = null;
			if (ContentRoles.owner.equals(rights.getRole())) {
				explanation = translate("access.rights.owner.long");
			} else if (ContentRoles.coach.equals(rights.getRole())) {
				explanation = translate("access.rights.coach.long");
			} else if (ContentRoles.reviewer.equals(rights.getRole())) {
				explanation = translate("access.rights.reviewer.long");
			} else if (ContentRoles.readInvitee.equals(rights.getRole())) {
				explanation = translate("access.rights.invitee.long");
			}
			return explanation;
		}
	}

}
