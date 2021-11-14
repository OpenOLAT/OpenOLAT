/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import java.util.Collections;
import java.util.List;

import org.olat.admin.user.course.CourseOverviewController;
import org.olat.admin.user.groups.GroupOverviewController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.notifications.ui.NotificationSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListController;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.ui.CurriculumListController;
import org.olat.modules.dcompensation.ui.UserDisadvantageCompensationListController;
import org.olat.modules.grading.GradingModule;
import org.olat.modules.grading.ui.GraderUserOverviewController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.ui.CompetencesOverviewController;
import org.olat.properties.Property;
import org.olat.resource.accesscontrol.ui.UserOrderController;
import org.olat.user.ChangePrefsController;
import org.olat.user.DisplayPortraitController;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.ProfileFormController;
import org.olat.user.PropFoundEvent;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesController;
import org.olat.user.ui.admin.authentication.UserAuthenticationsEditorController;
import org.olat.user.ui.admin.lifecycle.ConfirmDeleteUserController;
import org.olat.user.ui.admin.lifecycle.IdentityDeletedEvent;
import org.olat.user.ui.data.UserDataExportController;
import org.olat.user.ui.identity.UserRelationsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Initial Date:  Jul 29, 2003
 *  @author Sabina Jeger
 *  <pre>
 *  Complete rebuild on 17. jan 2006 by Florian Gnaegi
 *
 *  Functionality to change or view all kind of things for this user
 *  based on the configuration for the user manager.
 *  This controller should only be used by the UserAdminMainController.
 *
 * </pre>
 */
public class UserAdminController extends BasicController implements Activateable2, TooledController {

	// NLS support
	private static final String NLS_ERROR_NOACCESS_TO_USER = "error.noaccess.to.user";
	private static final String NLS_FOUND_PROPERTY		= "found.property";
	private static final String NLS_EDIT_UPROFILE		= "edit.uprofile";
	private static final String NLS_EDIT_UPREFS			= "edit.uprefs";
	private static final String NLS_EDIT_UPCRED 		= "edit.upwd";
	private static final String NLS_EDIT_UAUTH 			= "edit.uauth";
	private static final String NLS_EDIT_UPROP			= "edit.uprop";
	private static final String NLS_EDIT_UROLES			= "edit.uroles";
	private static final String NLS_EDIT_RELATIONS		= "edit.urelations";
	private static final String NLS_EDIT_UQUOTA			= "edit.uquota";
	private static final String NLS_EDIT_DISADVANTAGE	= "edit.udisadvantage";
	private static final String NLS_VIEW_GROUPS			= "view.groups";
	private static final String NLS_VIEW_COURSES		= "view.courses";
	private static final String NLS_VIEW_ACCESS			= "view.access";
	private static final String NLS_VIEW_EFF_STATEMENTS	= "view.effStatements";
	private static final String NLS_VIEW_SUBSCRIPTIONS 	= "view.subscriptions";
	private static final String NLS_VIEW_LECTURES		= "view.lectures";
	private static final String NLS_VIEW_COMPETENCES	= "view.competences";
	private static final String NLS_VIEW_CURRICULUM		= "view.curriculum";
	private static final String NLS_VIEW_GRADER			= "view.grader";
	

	private VelocityContainer myContent;
	private final TooledStackedPanel stackPanel;

	private final Roles managerRoles;
	private Identity editedIdentity;
	private final Roles editedRoles;
	private final boolean allowedToManage;
	private int rolesTab;

	private Link deleteLink;
	
	// controllers used in tabbed pane
	private TabbedPane userTabP;
	private Controller prefsCtr;
	private Controller pwdCtr;
	private Controller quotaCtr;
	private Controller rolesCtr;
	private Controller propertiesCtr;
	private Controller userShortDescrCtr;
	private CurriculumListController curriculumCtr;
	private UserRelationsController relationsCtrl;
	private DisplayPortraitController portraitCtr;
	private GraderUserOverviewController graderOverviewCtrl;
	private UserAuthenticationsEditorController authenticationsCtr;
	private Link backLink;
	private Link exportDataButton;
	private ProfileFormController profileCtr;
	private ProfileAndHomePageEditController userProfileCtr;
	private CourseOverviewController courseCtr;
	private GroupOverviewController grpCtr;
	private CloseableModalController cmc;
	private UserDataExportController exportDataCtrl;
	private CompetencesOverviewController competencesCtrl;
	private ConfirmDeleteUserController confirmDeleteUserCtlr;
	private ParticipantLecturesOverviewController lecturesCtrl;
	private CertificateAndEfficiencyStatementListController efficicencyCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private GradingModule gradingModule;
	@Autowired
	private OrganisationService organisationService;

	/**
	 * Constructor that creates a back - link as default
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public UserAdminController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Identity identity) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		managerRoles = ureq.getUserSession().getRoles();
		editedIdentity = identity;
		editedRoles = securityManager.getRoles(editedIdentity);

		allowedToManage = allowedToManageUser();
		if (allowedToManage) {
			myContent = createVelocityContainer("udispatcher");
			backLink = LinkFactory.createLinkBack(myContent, this);
			if(stackPanel == null) {
				exportDataButton = LinkFactory.createButton("export.user.data", myContent, this);
				exportDataButton.setIconLeftCSS("o_icon o_icon_download");
			}
			
			userShortDescrCtr = new UserShortDescription(ureq, wControl, identity);
			listenTo(userShortDescrCtr);
			myContent.put("userShortDescription", userShortDescrCtr.getInitialComponent());

			setBackButtonEnabled(true); // default
			setShowTitle(true);
			initTabbedPane(editedIdentity, ureq);
			exposeUserDataToVC(ureq, editedIdentity);
			putInitialPanel(myContent);
		} else {
			String supportAddr = WebappHelper.getMailConfig("mailSupport");
			showWarning(NLS_ERROR_NOACCESS_TO_USER, supportAddr);
			putInitialPanel(new Panel("empty"));
		}
	}
	
	public Identity getEditedIdentity() {
		return editedIdentity;
	}

	@Override
	public void initTools() {
		if(allowedToManage && stackPanel != null) {
			exportDataButton = LinkFactory.createToolLink("exportUserData", translate("export.user.data"), this, "o_icon_download");
			stackPanel.addTool(exportDataButton, Align.left);
		}
		if(stackPanel != null && managerRoles.isAdministrator()) {
			deleteLink = LinkFactory.createToolLink("delete", translate("delete"), this, "o_icon o_icon_delete_item");
			deleteLink.setElementCssClass("o_sel_user_delete");
			stackPanel.addTool(deleteLink, Align.left);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String entryPoint = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("tab".equalsIgnoreCase(entryPoint)) {
			userTabP.activate(ureq, entries, state);
		} else if("roles".equalsIgnoreCase(entryPoint) && rolesTab >= 0) {
			List<ContextEntry> tabEntries = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableInstance("tab", Long.valueOf(rolesTab)));
			userTabP.activate(ureq, tabEntries, state);
		} else if("table".equalsIgnoreCase(entryPoint)) {
			if(entries.size() > 2) {
				List<ContextEntry> subEntries = entries.subList(2, entries.size());
				userTabP.activate(ureq, subEntries, state);
			}
		}
	}

	/**
	 * @param backButtonEnabled
	 */
	public void setBackButtonEnabled(boolean backButtonEnabled) {
		if (myContent != null) {
			myContent.contextPut("showButton", Boolean.valueOf(backButtonEnabled));
		}
	}
	
	public void setShowTitle(boolean titleEnabled) {
		if(myContent != null) {
			myContent.contextPut("showTitle", Boolean.valueOf(titleEnabled));
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(exportDataButton == source) {
			doExportData(ureq);
		} else if (source == userTabP) {
			userTabP.addToHistory(ureq, getWindowControl());
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == propertiesCtr) {
			if (event.getCommand().equals("PropFound")){
				PropFoundEvent foundEvent = (PropFoundEvent) event;
				Property myfoundProperty = foundEvent.getProperty();
				showInfo(NLS_FOUND_PROPERTY, myfoundProperty.getKey().toString());
			}
		} else if (source == pwdCtr) {
			if (event == Event.DONE_EVENT) {
				// rebuild authentication tab, could be wrong now
				if (authenticationsCtr != null) {
					authenticationsCtr.reloadModel();
				}
			}
		} else if (userProfileCtr == source || authenticationsCtr == source) {
			if (event == Event.DONE_EVENT) {
				//reload profile data on top
				editedIdentity = securityManager.loadIdentityByKey(editedIdentity.getKey());
				exposeUserDataToVC(ureq, editedIdentity);
				userProfileCtr.resetForm(ureq, editedIdentity);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(source == exportDataCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteUserCtlr) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new IdentityDeletedEvent());
			}
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteUserCtlr);
		removeAsListenerAndDispose(exportDataCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteUserCtlr = null;
		exportDataCtrl = null;
		cmc = null;
	}
	
	private void doExportData(UserRequest ureq) {
		if(guardModalController(exportDataCtrl)) return;
		
		exportDataCtrl = new UserDataExportController(ureq, getWindowControl(), editedIdentity);
		listenTo(exportDataCtrl);
		
		String fullname = userManager.getUserDisplayName(editedIdentity);
		String title = translate("export.user.data.title", new String[] { fullname });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), exportDataCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if(guardModalController(confirmDeleteUserCtlr)) return;
		
		confirmDeleteUserCtlr = new ConfirmDeleteUserController(ureq, getWindowControl(),
				Collections.singletonList(editedIdentity));
		listenTo(confirmDeleteUserCtlr);
		
		String fullname = userManager.getUserDisplayName(editedIdentity);
		String title = translate("delete.user.data.title", new String[] { fullname });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteUserCtlr.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}

	/**
	 * Check if user allowed to modify this identity. Only modification of user
	 * that have lower rights is allowed. No one exept admins can manage usermanager
	 * and admins
	 * @return boolean
	 */
	private boolean allowedToManageUser() {
		// prevent editing of users that are in sysadmin / superadmin group
		Roles identityRoles = securityManager.getRoles(editedIdentity);
		if(identityRoles.hasRole(OrganisationRoles.sysadmin)) {
			return getIdentity().equals(editedIdentity)
					|| organisationService.hasRole(getIdentity(), OrganisationRoles.sysadmin)
					|| managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles)
					|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles);
		}

		// if user is guest only allowed to edit if configured
		if(identityRoles.isGuestOnly()) {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			return organisationService.hasRole(getIdentity(), defOrganisation,
					OrganisationRoles.administrator, OrganisationRoles.rolesmanager);
		}
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.principal, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isMyInvitee(OrganisationRoles.administrator, identityRoles)
				|| managerRoles.isMyInvitee(OrganisationRoles.principal, identityRoles)
				|| managerRoles.isMyInvitee(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isMyInvitee(OrganisationRoles.usermanager, identityRoles);
	}

	/**
	 * Initialize the tabbed pane according to the users rights and the system
	 * configuration
	 * @param identity
	 * @param ureq
	 */
	private void initTabbedPane(Identity identity, UserRequest ureq) {
		// first Initialize the user details tabbed pane
		userTabP = new TabbedPane("userTabP", ureq.getLocale());
		userTabP.addListener(this);
	
		boolean isAdminOf = managerRoles.isManagerOf(OrganisationRoles.administrator, editedRoles);
		boolean isPrincipalOf = managerRoles.isManagerOf(OrganisationRoles.principal, editedRoles);
		boolean isUserManagerOf = managerRoles.isManagerOf(OrganisationRoles.usermanager, editedRoles);
		boolean isRolesManagerOf = managerRoles.isManagerOf(OrganisationRoles.rolesmanager, editedRoles);

		if(isAdminOf || isUserManagerOf || isRolesManagerOf) {
			userProfileCtr = new ProfileAndHomePageEditController(ureq, getWindowControl(), identity, true);
			listenTo(userProfileCtr);
			userTabP.addTab(translate(NLS_EDIT_UPROFILE), userProfileCtr.getInitialComponent());
		} else {
			profileCtr = new ProfileFormController(ureq, getWindowControl(), identity, true, false);
			listenTo(profileCtr);
			userTabP.addTab(translate(NLS_EDIT_UPROFILE), profileCtr.getInitialComponent());
		}

		if(isAdminOf || isUserManagerOf || isRolesManagerOf) {
			userTabP.addTab(ureq, translate(NLS_EDIT_UPREFS), uureq -> {
				prefsCtr = new ChangePrefsController(uureq, getWindowControl(), identity);
				listenTo(prefsCtr);
				return prefsCtr.getInitialComponent();
			});
		}

		if (isPasswordChangesAllowed(identity)) {
			userTabP.addTab(ureq, translate(NLS_EDIT_UPCRED), uureq -> {
				pwdCtr =  new UserChangePasswordController(uureq, getWindowControl(), identity);
				listenTo(pwdCtr);
				return pwdCtr.getInitialComponent();
			});
		}

		if (isAdminOf) {
			userTabP.addTab(ureq, translate(NLS_EDIT_UAUTH),  uureq -> {
				authenticationsCtr =  new UserAuthenticationsEditorController(uureq, getWindowControl(), identity);
				listenTo(authenticationsCtr);
				return authenticationsCtr.getInitialComponent();
			});

			userTabP.addTab(ureq, translate(NLS_EDIT_UPROP), uureq -> {
				propertiesCtr = new UserPropertiesController(uureq, getWindowControl(), identity, editedRoles);
				listenTo(propertiesCtr);
				return propertiesCtr.getInitialComponent();
			});
		}
		
		if(isAdminOf || isPrincipalOf || isUserManagerOf || isRolesManagerOf) {
			userTabP.addTab(ureq, translate(NLS_VIEW_GROUPS),  uureq -> {
				boolean canModify = isAdminOf || isUserManagerOf || isRolesManagerOf;
				grpCtr = new GroupOverviewController(uureq, getWindowControl(), identity, canModify, true);
				listenTo(grpCtr);
				return grpCtr.getInitialComponent();
			});
	
			userTabP.addTab(ureq, translate(NLS_VIEW_COURSES), uureq -> {
				boolean canModify = isAdminOf || isUserManagerOf || isRolesManagerOf;
				courseCtr = new CourseOverviewController(uureq, getWindowControl(), identity, canModify);
				listenTo(courseCtr);
				return courseCtr.getInitialComponent();
			});
		}

		if (isAdminOf || isPrincipalOf || isRolesManagerOf) {
			userTabP.addTab(ureq, translate(NLS_VIEW_ACCESS), uureq -> {
				Controller accessCtr = new UserOrderController(uureq, getWindowControl(), identity);
				listenTo(accessCtr);
				return accessCtr.getInitialComponent();
			});
		}
		
		if (isAdminOf || isPrincipalOf ||  isUserManagerOf || isRolesManagerOf) {
			userTabP.addTab(ureq, translate(NLS_VIEW_EFF_STATEMENTS),  uureq -> {
				boolean canModify = isAdminOf || isRolesManagerOf;
				efficicencyCtrl = new CertificateAndEfficiencyStatementListController(uureq, getWindowControl(),
						identity, true, canModify, true);
				listenTo(efficicencyCtrl);
				BreadcrumbedStackedPanel efficiencyPanel = new BreadcrumbedStackedPanel("statements", getTranslator(), efficicencyCtrl);
				efficiencyPanel.pushController(translate(NLS_VIEW_EFF_STATEMENTS), efficicencyCtrl);
				efficicencyCtrl.setBreadcrumbPanel(efficiencyPanel);
				efficiencyPanel.setInvisibleCrumb(1);
				return efficiencyPanel;
			});
			
			userTabP.addTab(ureq, translate(NLS_EDIT_DISADVANTAGE), uureq -> {
				boolean canModify = isAdminOf || isRolesManagerOf;
				Controller compensationCtrl = new UserDisadvantageCompensationListController(uureq, getWindowControl(), identity, canModify);
				listenTo(compensationCtrl);
				return compensationCtrl.getInitialComponent();
			});
		}

		if (isUserManagerOf || isRolesManagerOf || isAdminOf) {
			userTabP.addTab(ureq, translate(NLS_VIEW_SUBSCRIPTIONS),  uureq -> {
				Controller subscriptionsCtr = new NotificationSubscriptionController(uureq, getWindowControl(), identity, true, true);
				listenTo(subscriptionsCtr);
				return subscriptionsCtr.getInitialComponent();
			});
		}

		// the controller manager is read-write permissions
		rolesTab = userTabP.addTab(ureq, translate(NLS_EDIT_UROLES), uureq -> {
			rolesCtr = new SystemRolesAndRightsController(getWindowControl(), uureq, identity);
			listenTo(rolesCtr);
			return rolesCtr.getInitialComponent();
		});
		
		if (securityModule.isRelationRoleEnabled() && (isUserManagerOf || isRolesManagerOf || isAdminOf || isPrincipalOf)) {
			userTabP.addTab(ureq, translate(NLS_EDIT_RELATIONS),  uureq -> {
				boolean canModify = isUserManagerOf || isRolesManagerOf || isAdminOf;
				relationsCtrl = new UserRelationsController(uureq, getWindowControl(), identity, canModify);
				listenTo(relationsCtrl);
				return relationsCtrl.getInitialComponent();
			});
		}

		if (isUserManagerOf || isRolesManagerOf || isAdminOf) {
			userTabP.addTab(ureq, translate(NLS_EDIT_UQUOTA),  uureq -> {
				String relPath = FolderConfig.getUserHomes() + "/" + identity.getName();
				quotaCtr = quotaManager.getQuotaEditorInstance(uureq, getWindowControl(), relPath, true, false);
				return quotaCtr.getInitialComponent();
			});
		}

		if(lectureModule.isEnabled() && (isUserManagerOf || isRolesManagerOf || isAdminOf || isPrincipalOf)) {
			userTabP.addTab(ureq, translate(NLS_VIEW_LECTURES),  uureq -> {
				lecturesCtrl = new ParticipantLecturesOverviewController(uureq, getWindowControl(), identity, null,
						true, true, true, true, true, false);
				listenTo(lecturesCtrl);
				BreadcrumbedStackedPanel lecturesPanel = new BreadcrumbedStackedPanel("lectures", getTranslator(), lecturesCtrl);
				lecturesPanel.pushController(translate(NLS_VIEW_LECTURES), lecturesCtrl);
				lecturesCtrl.setBreadcrumbPanel(lecturesPanel);
				lecturesPanel.setInvisibleCrumb(1);
				return lecturesPanel;
			});
		}
		
		if(taxonomyModule.isEnabled() && (isUserManagerOf || isRolesManagerOf || isAdminOf || isPrincipalOf)) {
			userTabP.addTab(ureq, translate(NLS_VIEW_COMPETENCES),  uureq -> {
				boolean canModify = isUserManagerOf || isRolesManagerOf || isAdminOf;
				competencesCtrl = new CompetencesOverviewController(uureq, getWindowControl(), null, identity, canModify, false);
				listenTo(competencesCtrl);
				BreadcrumbedStackedPanel competencePanel = new BreadcrumbedStackedPanel("competences", getTranslator(), competencesCtrl);
				competencePanel.pushController(translate(NLS_VIEW_COMPETENCES), competencesCtrl);
				competencesCtrl.setBreadcrumbPanel(competencePanel);
				competencePanel.setInvisibleCrumb(1);
				return competencePanel;
			});
		}
		
		if(curriculumModule.isEnabled() && (isUserManagerOf || isRolesManagerOf || isAdminOf || isPrincipalOf)) {
			userTabP.addTab(ureq, translate(NLS_VIEW_CURRICULUM),  uureq -> {
				curriculumCtr = new CurriculumListController(uureq, getWindowControl(), identity);
				listenTo(curriculumCtr);
				BreadcrumbedStackedPanel curriculumPanel = new BreadcrumbedStackedPanel("curriculums", getTranslator(), curriculumCtr);
				curriculumPanel.pushController(translate(NLS_VIEW_CURRICULUM), curriculumCtr);
				curriculumCtr.setBreadcrumbPanel(curriculumPanel);
				curriculumPanel.setInvisibleCrumb(1);
				return curriculumPanel;
			});
		}
		
		if(gradingModule.isEnabled()) {
			userTabP.addTab(ureq, translate(NLS_VIEW_GRADER),  uureq -> {
				graderOverviewCtrl = new GraderUserOverviewController(uureq, getWindowControl(), identity);
				listenTo(graderOverviewCtrl);
				BreadcrumbedStackedPanel gradingPanel = new BreadcrumbedStackedPanel("curriculums", getTranslator(), graderOverviewCtrl);
				gradingPanel.pushController(translate(NLS_VIEW_GRADER), graderOverviewCtrl);
				graderOverviewCtrl.setBreadcrumbPanel(gradingPanel);
				gradingPanel.setInvisibleCrumb(1);
				return gradingPanel;
			});
		}

		// now push to velocity
		myContent.put("userTabP", userTabP);
	}

	private boolean isPasswordChangesAllowed(Identity identity) {
		if (managerRoles.isManagerOf(OrganisationRoles.administrator, editedRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, editedRoles)
				|| (managerRoles.isManagerOf(OrganisationRoles.usermanager, editedRoles)
						&& !editedRoles.isAdministrator() && !editedRoles.isRolesManager())) {
			// show pwd form only if user has also right to create new passwords in case
			// of a user that has no password yet
			if(ldapLoginModule.isLDAPEnabled() && ldapLoginManager.isIdentityInLDAPSecGroup(identity)) {
				// it's an ldap-user
				return ldapLoginModule.isPropagatePasswordChangedOnLdapServer();
			}
			return true;
		}
		return false;
	}

	/**
	 * Add some user data to velocity container including the users portrait
	 * @param ureq
	 * @param identity
	 */
	private void exposeUserDataToVC(UserRequest ureq, Identity identity) {
		removeAsListenerAndDispose(portraitCtr);
		portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), identity, true, true);
		myContent.put("portrait", portraitCtr.getInitialComponent());
		removeAsListenerAndDispose(userShortDescrCtr);
		userShortDescrCtr = new UserShortDescription(ureq, getWindowControl(), identity);
		myContent.put("userShortDescription", userShortDescrCtr.getInitialComponent());
	}
}
