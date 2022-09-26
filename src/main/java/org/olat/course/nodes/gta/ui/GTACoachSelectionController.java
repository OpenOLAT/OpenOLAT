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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.ui.tool.AssessedIdentityLargeInfosController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.ui.GTACoachedParticipantListController.MakedEvent;
import org.olat.course.nodes.gta.ui.component.DownloadDocumentMapper;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachSelectionController extends BasicController implements Activateable2 {

	private Controller coachingCtrl;
	private GTACoachedGroupListController groupListCtrl;
	private GTACoachedParticipantListController participantListCtrl;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	private final Link backLink;
	private final Link downloadButton;
	private final Link nextIdentityLink;
	private final Link previousIdentityLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel assessedIdentityStackPanel;

	private final String solutionMapperUri;
	private final DisplayOrDownloadComponent solutionDownloadCmp;
	
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment coachCourseEnv;
	private boolean markedOnly = false;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public GTACoachSelectionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		this.courseEnv = coachCourseEnv.getCourseEnvironment();
		
		mainVC = createVelocityContainer("coach_selection");
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		File solutionsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		solutionMapperUri = registerMapper(ureq, new DownloadDocumentMapper(solutionsDir));
		solutionDownloadCmp = new DisplayOrDownloadComponent("download", null);
		mainVC.put("solutionDownload", solutionDownloadCmp);
		
		downloadButton = LinkFactory.createButton("bulk.download.title", mainVC, this);
		downloadButton.setTranslator(getTranslator());
		downloadButton.setVisible(isDownloadAvailable());
		
		assessedIdentityStackPanel = new TooledStackedPanel("gta-assessed-identity-stack", getTranslator(), this);
		assessedIdentityStackPanel.setNeverDisposeRootController(true);
		assessedIdentityStackPanel.setInvisibleCrumb(0);
		assessedIdentityStackPanel.setShowCloseLink(true, false);
		assessedIdentityStackPanel.pushController(translate("root.participant"), null, new Object());
		
		previousIdentityLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousIdentityLink.setTitle(translate("command.previous"));
		assessedIdentityStackPanel.addTool(previousIdentityLink, Align.rightEdge, true, "o_tool_previous");
		nextIdentityLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextIdentityLink.setTitle(translate("command.next"));
		assessedIdentityStackPanel.addTool(nextIdentityLink, Align.rightEdge, true, "o_tool_next");
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<BusinessGroup> groups;
			CourseGroupManager gm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
			if(coachCourseEnv.isAdmin()) {
				groups = gm.getAllBusinessGroups();
			} else {
				groups = coachCourseEnv.getCoachedGroups();
			}
			
			groups = gtaManager.filterBusinessGroups(groups, gtaNode);
			
			groupListCtrl = new GTACoachedGroupListController(ureq, getWindowControl(), null, coachCourseEnv, gtaNode, groups);
			listenTo(groupListCtrl);
			mainVC.put("list", groupListCtrl.getInitialComponent());
			
			if(groups.size() == 1) {
				doSelectBusinessGroup(ureq, groups.get(0));
			}	
		} else {
			markedOnly = true; // Init marked tab
			participantListCtrl = new GTACoachedParticipantListController(ureq, getWindowControl(), coachCourseEnv, gtaNode, markedOnly);
			listenTo(participantListCtrl);
			mainVC.put("list", participantListCtrl.getInitialComponent());
		}
		
		initSubscription(ureq);
		
		putInitialPanel(mainVC);
	}

	private void initSubscription(UserRequest ureq) {
		removeAsListenerAndDispose(contextualSubscriptionCtr);
		
		PublisherData publisherData = gtaManager.getPublisherData(courseEnv, gtaNode, markedOnly);
		SubscriptionContext subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode, markedOnly);
		contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
		listenTo(contextualSubscriptionCtr);
		mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
	}
	
	private boolean isDownloadAvailable() {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		return config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				|| config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		Long key = entries.get(0).getOLATResourceable().getResourceableId();
		if("Identity".equalsIgnoreCase(type)) {
			if(participantListCtrl != null && participantListCtrl.hasIdentityKey(key)) {
				Identity selectedIdentity = securityManager.loadIdentityByKey(key);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doSelectParticipant(ureq, selectedIdentity).activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if("BusinessGroup".equalsIgnoreCase(type)) {
			if(groupListCtrl != null) {
				BusinessGroup group = groupListCtrl.getBusinessGroup(key);
				if(group != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					doSelectBusinessGroup(ureq, group).activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		} else if("Solution".equals(type) && entries.size() > 1) {
			String path = BusinessControlFactory.getInstance().getPath(entries.get(1));
			String url = solutionMapperUri + "/" + path;
			solutionDownloadCmp.triggerFileDownload(url);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupListCtrl == source) {
			if(event instanceof SelectBusinessGroupEvent) {
				SelectBusinessGroupEvent selectEvent = (SelectBusinessGroupEvent)event;
				doSelectBusinessGroup(ureq, selectEvent.getBusinessGroup());
				backLink.setVisible(true);
			}
		} else if(participantListCtrl == source) {
			if(event instanceof SelectIdentityEvent) {
				SelectIdentityEvent selectEvent = (SelectIdentityEvent)event;
				Identity selectedIdentity = securityManager.loadIdentityByKey(selectEvent.getIdentityKey());
				doSelectParticipant(ureq, selectedIdentity);
				backLink.setVisible(true);
			} else if (event instanceof MakedEvent) {
				markedOnly = ((MakedEvent)event).isMarked();
				initSubscription(ureq);
			}
		}
		
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			back(ureq);
		} else if(downloadButton == source) {
			doBulkDownload(ureq);
		} else if(nextIdentityLink == source) {
			doNextIdentity(ureq);
		} else if(previousIdentityLink == source) {
			doPreviousIdentity(ureq);
		} else if(assessedIdentityStackPanel == source) {
			if(event instanceof PopEvent) {
				back(ureq);
			}
		}
	}
	
	public void reload(UserRequest ureq) {
		if (participantListCtrl != null) {
			participantListCtrl.updateModel(ureq);
		}
		if (groupListCtrl != null) {
			groupListCtrl.updateModel();
		}
	}
	
	private void back(UserRequest ureq) {
		if(coachingCtrl != null) {
			mainVC.remove("selectionStack");
			mainVC.remove(coachingCtrl.getInitialComponent());
			removeAsListenerAndDispose(coachingCtrl);
			coachingCtrl = null;
		}
		backLink.setVisible(false);
		if (participantListCtrl != null) {
			participantListCtrl.updateModel(ureq);			
		}
		if (groupListCtrl != null) {
			groupListCtrl.updateModel();
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		if (participantListCtrl != null) {
			ArchiveOptions asOptions = new ArchiveOptions();
			asOptions.setIdentities(getIdentitesForBulkDownload(ureq));
			OLATResource ores = courseEnv.getCourseGroupManager().getCourseResource();
			ArchiveResource resource = new ArchiveResource(gtaNode, ores, asOptions, getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else if (groupListCtrl != null) {
			OLATResource ores = courseEnv.getCourseGroupManager().getCourseResource();
			GroupBulkDownloadResource resource = new GroupBulkDownloadResource(gtaNode, ores, groupListCtrl.getCoachedGroups(), getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doNextIdentity(UserRequest ureq) {
		assessedIdentityStackPanel.popController(coachingCtrl);
		removeAsListenerAndDispose(coachingCtrl);
		
		if(coachingCtrl instanceof GTAAssessedIdentityController) {
			Identity currentIdentity = ((GTAAssessedIdentityController)coachingCtrl).getAssessedIdentity();
			int nextIndex = participantListCtrl.indexOfIdentity(currentIdentity) + 1;
			int rowCount = participantListCtrl.numOfIdentities();
			if(nextIndex >= 0 && nextIndex < rowCount) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(nextIndex);
				doSelectParticipant(ureq, nextIdentity);
			} else if(rowCount > 0) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(0);
				doSelectParticipant(ureq, nextIdentity);
			}
		}
	}
	
	private void doPreviousIdentity(UserRequest ureq) {
		assessedIdentityStackPanel.popController(coachingCtrl);
		removeAsListenerAndDispose(coachingCtrl);
		
		if(coachingCtrl instanceof GTAAssessedIdentityController) {
			Identity currentIdentity = ((GTAAssessedIdentityController)coachingCtrl).getAssessedIdentity();
			int previousIndex = participantListCtrl.indexOfIdentity(currentIdentity) - 1;
			int rowCount = participantListCtrl.numOfIdentities();
			if(previousIndex >= 0 && previousIndex < rowCount) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(previousIndex);
				doSelectParticipant(ureq, nextIdentity);
			} else if(rowCount > 0) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(rowCount - 1);
				doSelectParticipant(ureq, nextIdentity);
			}
		}
	}

	private List<Identity> getIdentitesForBulkDownload(UserRequest ureq) {
		List<Identity> identities = participantListCtrl.getAssessableIdentities();
		if (markedOnly) {
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			List<Identity> markedIdentities =
					gtaManager.getMarks(entry, gtaNode, ureq.getIdentity()).stream()
							.map(IdentityMark::getParticipant)
							.collect(Collectors.toList());
			identities.retainAll(markedIdentities);
		}
		return identities;
	}
	
	private Activateable2 doSelectBusinessGroup(UserRequest ureq, BusinessGroup group) {
		removeAsListenerAndDispose(coachingCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.clone(group), null);
		coachingCtrl = new GTACoachController(ureq, swControl, courseEnv, gtaNode, coachCourseEnv, group, true, true, false, false);
		listenTo(coachingCtrl);
		mainVC.put("selection", coachingCtrl.getInitialComponent());
		return (Activateable2)coachingCtrl;
	}
	
	private Activateable2 doSelectParticipant(UserRequest ureq, IdentityRef identity) {
		Identity id = securityManager.loadIdentityByKey(identity.getKey());
		return doSelectParticipant(ureq, id);
	}
	
	private Activateable2 doSelectParticipant(UserRequest ureq, Identity identity) {
		removeAsListenerAndDispose(coachingCtrl);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Identity", identity.getKey()), null);
		coachingCtrl = new GTAAssessedIdentityController(ureq, swControl, identity);
		listenTo(coachingCtrl);
		
		String fullName = userManager.getUserDisplayName(identity);
		assessedIdentityStackPanel.pushController(fullName, coachingCtrl);
		mainVC.put("selectionStack", assessedIdentityStackPanel);
		
		int index = participantListCtrl.indexOfIdentity(identity);
		int numOfRows = participantListCtrl.numOfIdentities();
		previousIdentityLink.setEnabled(index > 0);
		nextIdentityLink.setEnabled(index + 1 < numOfRows);

		return (Activateable2)coachingCtrl;
	}
	
	/**
	 * Little wrapper to pack user informations, subscription and coach view
	 * of the task process.
	 * 
	 * Initial date: 16 sept. 2022<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private class GTAAssessedIdentityController extends BasicController implements Activateable2 {
		
		private final GTACoachController userTaskCtrl;
		private final Identity assessedIdentity;
		
		public GTAAssessedIdentityController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity) {
			super(ureq, wControl);
			this.assessedIdentity = assessedIdentity;
			
			VelocityContainer wrapperVC = createVelocityContainer("coach_wrapper");
			
			AssessedIdentityLargeInfosController userInfosCtrl = new AssessedIdentityLargeInfosController(ureq, getWindowControl(),
					assessedIdentity, coachCourseEnv.getCourseEnvironment(), gtaNode);
			listenTo(userInfosCtrl);
			wrapperVC.put("userInfos", userInfosCtrl.getInitialComponent());
			

			wrapperVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
			
			userTaskCtrl = new GTACoachController(ureq, wControl, courseEnv, gtaNode, coachCourseEnv, assessedIdentity, false, true, false, false);
			listenTo(userTaskCtrl);
			wrapperVC.put("selection", userTaskCtrl.getInitialComponent());
			
			putInitialPanel(wrapperVC);
		}
		
		public Identity getAssessedIdentity() {
			return assessedIdentity;
		}

		@Override
		public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
			userTaskCtrl.activate(ureq, entries, state);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
