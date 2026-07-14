/**

 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.PositionController.Tabs;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.position.PositionListOverviewController;
import org.olat.modules.selectus.ui.report.PositionReportAttributesController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Manage the positions
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingMainController extends BasicController implements Activateable2 {

	private final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private CloseableModalController acceptPolicyBox;
	private PositionController currentOverviewController;
	private PositionAcceptRatingPolicyController acceptPolicyController;
	private PositionReportAttributesController reportAttributesController;
	private final PositionListOverviewController positionOverviewController;

	@Autowired
	private RecruitingService selectusService;
	
	private final RecruitingSecurityCallback secCallback;
	
	public RecruitingMainController(UserRequest ureq, WindowControl wControl, RecruitingSecurityCallback secCallback) {
		super(ureq, wControl);
	
		this.secCallback = secCallback;

		stackPanel = new TooledStackedPanel("crumb.positions", getTranslator(), this);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setInvisibleCrumb(0);
		positionOverviewController = new PositionListOverviewController(ureq, wControl, stackPanel, secCallback);
		listenTo(positionOverviewController);

		stackPanel.pushController(translate("crumb.positions"), positionOverviewController);

		putInitialPanel(stackPanel);
		addToHistory(ureq, OresHelper.createOLATResourceableType("Positions"), null);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == currentOverviewController) {
					positionOverviewController.loadUnreadNotificationsBadge(true);
					currentOverviewController = null;
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == positionOverviewController) {
			if(event == Event.CHANGED_EVENT) {
				positionOverviewController.reload();
			} else if(event instanceof SelectPositionLightEvent) {
				SelectPositionLightEvent spe = (SelectPositionLightEvent)event;
				Position position = selectusService.getPosition(spe.getPosition().getKey());
				selectPosition(ureq, position, null, PositionController.Tabs.applications, false, false, null);
			}  else if(event instanceof SelectPositionEvent) {
				SelectPositionEvent spe = (SelectPositionEvent)event;
				Position position = spe.getPosition();
				if(position != null && position.getKey() != null) {
					position = selectusService.getPosition(position.getKey());
					selectPosition(ureq, position, null, PositionController.Tabs.applications, false, spe.isEdit(), null);
				}
			} else if(event instanceof SelectApplicationEvent) {
				SelectApplicationEvent sae = (SelectApplicationEvent)event;
				Application appToSelect = sae.getApplication();
				Position position = selectusService.getPosition(appToSelect.getPosition().getKey());
				selectPosition(ureq, position, appToSelect, PositionController.Tabs.applications, false, false, sae.getActivation());
			}
		} else if (source instanceof PositionController) {
			if(event == Event.CHANGED_EVENT) {
				positionOverviewController.reload();
			} else if(event == Event.CLOSE_EVENT) {
				stackPanel.popUpToRootController(ureq);
				positionOverviewController.reload();
			}
			super.event(ureq, source, event);
		} else if (acceptPolicyBox == source || acceptPolicyController == source) {
			if(event == Event.DONE_EVENT) {
				Position position = acceptPolicyController.getPosition();
				ApplicationRef appToSelect = acceptPolicyController.getApplicationToSelect();
				List<ContextEntry> activation = acceptPolicyController.getActivation();
				List<AcceptPolicyEnum> policyToAcceptList = acceptPolicyController.getPolicyToAcceptList();
				if(acceptPolicyController == source) {
					acceptPolicyBox.deactivate();
				}
				cleanUp();
				if(policyToAcceptList.isEmpty()) {
					selectPosition(ureq, position, appToSelect, Tabs.applications, true, false, activation);
				} else {
					AcceptPolicyEnum policyToAccept = policyToAcceptList.get(0);
					doAcceptPolicy(ureq, position, appToSelect, policyToAcceptList, policyToAccept, activation);
				}
			}
		} else if(source == cmc) {
			cleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(acceptPolicyController);
		removeAsListenerAndDispose(acceptPolicyBox);
		removeAsListenerAndDispose(cmc);
		acceptPolicyController = null;
		acceptPolicyBox = null;
		cmc = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		removeModalControllers();
		
		if(entries == null || entries.isEmpty()) {
			activateDefault(ureq);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Position".equalsIgnoreCase(type) || "Positions".equalsIgnoreCase(type) || "Item".equalsIgnoreCase(type)) {
				Long positionKey = entries.get(0).getOLATResourceable().getResourceableId();
				if(positionOverviewController.hasPositionWith(positionKey)) {
					if(currentOverviewController != null
							&& currentOverviewController.getPosition().getKey().equals(positionKey)
							&& stackPanel.hasController(currentOverviewController)) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						currentOverviewController.activate(ureq, subEntries, entries.get(0).getTransientState());
					} else {
						Position position = selectusService.getPosition(positionKey);
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						selectPosition(ureq, position, null, PositionController.Tabs.applications, false, false, subEntries);
					}
				} else {
					logAudit("Try to access an unauthorized position:" + positionKey, null);
				}
			} else if("Logs".equals(type)
					|| "MyApplication".equals(type) || "MyApplications".equals(type)) {
				positionOverviewController.activate(ureq, entries, state);	
			}
		}
	}
	
	private void activateDefault(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		boolean admin = roles.isSelectusManager() || roles.isAdministrator();
		if(!admin && positionOverviewController.getNumOfPositions() == 1 && !positionOverviewController.hasFeedbacks()) {
			stackPanel.popUpToRootController(ureq);
			Long positionKey = positionOverviewController.getPositionKeyAt(0);
			Position position = selectusService.getPosition(positionKey);
			selectPosition(ureq, position, null, PositionController.Tabs.applications, false, false, null);
		} else if(!admin && positionOverviewController.getNumOfPositions() == 0 && positionOverviewController.hasFeedbacks()) {
			stackPanel.popUpToRootController(ureq);
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType("Feedbacks");
			positionOverviewController.activate(ureq, entries, null);
		} else if(!admin && positionOverviewController.getNumOfApplications() == 1) {
			stackPanel.popUpToRootController(ureq);
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType("MyApplications");
			positionOverviewController.activate(ureq, entries, null);
		} else {
			stackPanel.popUpToRootController(ureq);
			removeControllerListener(currentOverviewController);
			currentOverviewController = null;
		}
	}

	private void selectPosition(UserRequest ureq, Position position, ApplicationRef appToSelect,
			PositionController.Tabs selectedTab, boolean force, boolean edit, List<ContextEntry> activation) {
		
		if(PositionStatus.reporting.name().equals(position.getStatus())) {
			doOpenReportingPosition(ureq, position);
		} else if(force || isAdministrator(position, ureq)) {
			doOpenPosition(ureq, position, appToSelect, selectedTab, edit, activation);
		} else {
			List<AcceptPolicyEnum> policyToAcceptList = policyToAccept(ureq, position);
			if(policyToAcceptList.isEmpty()) {
				doOpenPosition(ureq, position, appToSelect, selectedTab, edit, activation);
			} else {
				AcceptPolicyEnum policyToAccept = policyToAcceptList.get(0);
				doAcceptPolicy(ureq, position, appToSelect, policyToAcceptList, policyToAccept, activation);
			}
		}
	}
	
	private List<AcceptPolicyEnum> policyToAccept(UserRequest ureq, Position position) {
		UserSession usess = ureq.getUserSession();
		List<AcceptPolicyEnum> policyToAcceptList = selectusService.needToAcceptPositionPolicies(position, getIdentity());
		for(Iterator<AcceptPolicyEnum> policyIt=policyToAcceptList.iterator(); policyIt.hasNext(); ) {
			AcceptPolicyEnum policy = policyIt.next();
			Object accepted = usess.getEntry(RecruitingHelper.sessionKeyAcceptedPolicy(position, policy));
			if(accepted instanceof Boolean && ((Boolean)accepted).booleanValue()) {
				policyIt.remove();
			}
		}
		return policyToAcceptList;
	}
	
	private void doAcceptPolicy(UserRequest ureq, Position position, ApplicationRef appToSelect,
			List<AcceptPolicyEnum> policyToAcceptList, AcceptPolicyEnum policyToAccept, List<ContextEntry> activation) {
		if(acceptPolicyController != null) return;
		
		acceptPolicyController = new PositionAcceptRatingPolicyController(ureq, getWindowControl(), position, appToSelect,
				policyToAcceptList, policyToAccept, activation);
		listenTo(acceptPolicyController);
		
		String title;
		if(policyToAccept == AcceptPolicyEnum.ratingPolicy) {
			title = translate("edit.rating.policy");
		} else {
			title = translate("message.committee.title");
		}
		acceptPolicyBox = new CloseableModalController(getWindowControl(), null, acceptPolicyController.getInitialComponent(), true, title, false, false);
		acceptPolicyBox.activate();
		listenTo(acceptPolicyBox);
	}

	private PositionController doOpenPosition(UserRequest ureq, Position position, ApplicationRef appToSelect,
			PositionController.Tabs selectedTab, boolean edit, List<ContextEntry> activation) {
		stackPanel.popUpToRootController(ureq);
		removeControllerListener(currentOverviewController);
		removeAsListenerAndDispose(reportAttributesController);
		
		PositionRole positionRole = selectusService.getRole(position, getIdentity());
		
		// Reduce the roles to the ones of the position's organisation
		Roles roles = ureq.getUserSession().getRoles();
		Organisation organisation = position.getOrganisation();
		RolesByOrganisation rolesOfPosition = roles.getRoles(organisation);
		if(rolesOfPosition == null) {
			roles = roles.isGuestOnly() ? Roles.guestRoles() : Roles.userRoles();
		} else {
			roles = Roles.valueOf(List.of(rolesOfPosition), false);
		}
		RecruitingSecurityCallback reduceSecCallback = new RecruitingSecurityCallbackImpl(roles, CommitteeMembershipsStats.empty());
		RecruitingPositionSecurityCallback positionSecCallback
			= new RecruitingPositionSecurityCallbackImpl(reduceSecCallback, position, getIdentity(), roles, positionRole);
		
		OLATResourceable positionRes = OresHelper.createOLATResourceableInstance(Position.class, position.getKey());
		WindowControl swControl = addToHistory(ureq, positionRes, null);
		currentOverviewController = new PositionController(ureq, swControl, stackPanel,
				position, selectedTab, positionSecCallback);
		String shortTitle = position.getMLShortTitle(getLocale());
		stackPanel.pushController(shortTitle, currentOverviewController);
		listenTo(currentOverviewController);
		if(appToSelect != null) {
			currentOverviewController.selectApplication(ureq, appToSelect, activation);
		} else if(edit) {
			currentOverviewController.doEditPosition(ureq);
		}
		currentOverviewController.activate(ureq, activation, null);
		return currentOverviewController;
	}
	
	private void doOpenReportingPosition(UserRequest ureq, Position position) {
		if(!secCallback.canReportingPosition()) return;
		
		stackPanel.popUpToRootController(ureq);
		removeControllerListener(currentOverviewController);
		removeControllerListener(reportAttributesController);
		
		reportAttributesController = new PositionReportAttributesController(ureq, getWindowControl(), position, true, true);
		listenTo(reportAttributesController);
		
		String shortTitle = position.getMLShortTitle(getLocale());
		stackPanel.pushController(shortTitle, reportAttributesController);
	}
	
	private boolean isAdministrator(Position position, UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		if(position.getOrganisation() != null) {
			return roles.hasSomeRoles(position.getOrganisation(), OrganisationRoles.selectusmanager);
		}
		return false;
	}
}
