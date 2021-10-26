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
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.ui.component.DownloadDocumentMapper;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachSelectionController extends BasicController implements Activateable2 {

	private GTACoachController coachingCtrl;
	private GTACoachedGroupListController groupListCtrl;
	private GTACoachedParticipantListController participantListCtrl;
	
	private final Link backLink;
	private final Link downloadButton;
	private final VelocityContainer mainVC;

	private final String solutionMapperUri;
	private final DisplayOrDownloadComponent solutionDownloadCmp;
	
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment coachCourseEnv;
	private final boolean markedOnly;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public GTACoachSelectionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode, boolean markedOnly) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		this.courseEnv = coachCourseEnv.getCourseEnvironment();
		this.markedOnly = markedOnly;
		
		mainVC = createVelocityContainer("coach_selection");
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		File solutionsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		solutionMapperUri = registerMapper(ureq, new DownloadDocumentMapper(solutionsDir));
		solutionDownloadCmp = new DisplayOrDownloadComponent("download", null);
		mainVC.put("solutionDownload", solutionDownloadCmp);
		
		downloadButton = LinkFactory.createButton("bulk.download.title", mainVC, this);
		downloadButton.setTranslator(getTranslator());
		downloadButton.setVisible(isDownloadAvailable());
		
		PublisherData publisherData = gtaManager.getPublisherData(courseEnv, gtaNode, markedOnly);
		SubscriptionContext subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode, markedOnly);
		ContextualSubscriptionController contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
		listenTo(contextualSubscriptionCtr);
		mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
		
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
			participantListCtrl = new GTACoachedParticipantListController(ureq, getWindowControl(), coachCourseEnv, gtaNode, markedOnly);
			listenTo(participantListCtrl);
			mainVC.put("list", participantListCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}
	
	private boolean isDownloadAvailable() {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		return config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				|| config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
	}

	@Override
	protected void doDispose() {
		//
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

	private List<Identity> getIdentitesForBulkDownload(UserRequest ureq) {
		List<Identity> identities = participantListCtrl.getAssessableIdentities();
		if (markedOnly) {
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			List<Identity> markedIdentities =
					gtaManager.getMarks(entry, gtaNode, ureq.getIdentity()).stream()
							.map(mark -> mark.getParticipant())
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
		return coachingCtrl;
	}
	
	private Activateable2 doSelectParticipant(UserRequest ureq, Identity identity) {
		removeAsListenerAndDispose(coachingCtrl);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Identity", identity.getKey()), null);
		coachingCtrl = new GTACoachController(ureq, swControl, courseEnv, gtaNode, coachCourseEnv, identity, true, true, false, false);
		listenTo(coachingCtrl);
		mainVC.put("selection", coachingCtrl.getInitialComponent());
		return coachingCtrl;
	}
}
