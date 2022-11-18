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
package org.olat.course.wizard.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.INodeFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.PublishProcess;
import org.olat.course.editor.PublishSetInformations;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.PublishTreeModel;
import org.olat.course.wizard.AssessmentModeDefaults;
import org.olat.course.wizard.CertificateDefaults;
import org.olat.course.wizard.CourseDisclaimerContext;
import org.olat.course.wizard.CourseNodeTitleContext;
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.IQTESTCourseNodeDefaults;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.repository.wizard.InfoMetadata;
import org.olat.repository.wizard.RepositoryWizardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseWizardServiceImpl implements CourseWizardService {

	private static final Logger log = Tracing.createLoggerFor(CourseWizardServiceImpl.class);
	
	@Autowired
	private RepositoryWizardService repositoryEntryWizardService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;

	@Override
	public void updateRepositoryEntry(RepositoryEntryRef entryRef, InfoMetadata infoMetadata) {
		repositoryEntryWizardService.updateRepositoryEntry(entryRef, infoMetadata);
	}

	@Override
	public void updateEntryStatus(Identity executor, RepositoryEntry entry, RepositoryEntryStatusEnum status) {
		RepositoryEntry updatedEntry = repositoryManager.setStatus(entry, status);
		
		MultiUserEvent modifiedEvent = new EntryChangedEvent(updatedEntry, executor, Change.modifiedAtPublish, "coursewizard");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, updatedEntry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		log.debug("Status of RepositoryEntry changed to '{}'.", status);
	}
	
	@Override
	public void addRepositoryMembers(Identity executor, Roles roles, RepositoryEntry entry,
			Collection<Identity> coaches, Collection<Identity> participants) {
		repositoryEntryWizardService.addRepositoryMembers(executor, roles, entry, coaches, participants);
	}
	
	@Override
	public void changeAccessAndProperties(Identity executor, AccessAndProperties accessAndProps, boolean fireEvents) {
		repositoryEntryWizardService.changeAccessAndProperties(executor, accessAndProps, fireEvents);
	}
	
	@Override
	public ICourse startCourseEditSession(RepositoryEntry entry) {
		OLATResourceable courseOres = entry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			log.debug("Course already edited by an other session. resourceableId={}", courseOres.getResourceableId());
			return null;
		}
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		log.debug("Course edit session started. resourceableId={}", courseOres.getResourceableId());
		return course;
	}

	@Override
	public void finishCourseEditSession(ICourse course) {
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		log.debug("Course edit session finished. resourceableId={}", course.getResourceableId());
	}
	
	@Override
	public void publishCourse(Identity executor, ICourse course) {
		Locale locale = Locale.ENGLISH;
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		PublishProcess publishProcess = PublishProcess.getInstance(course, cetm, locale);
		PublishTreeModel publishTreeModel = publishProcess.getPublishTreeModel();
 
		if (publishTreeModel.hasPublishableChanges()) {
			List<String> nodeToPublish = new ArrayList<>();
			visitPublishModel(publishTreeModel.getRootNode(), publishTreeModel, nodeToPublish);

			//only add selection if changes were possible
			for(Iterator<String> selectionIt=nodeToPublish.iterator(); selectionIt.hasNext(); ) {
				String ident = selectionIt.next();
				TreeNode node = publishProcess.getPublishTreeModel().getNodeById(ident);
				if(!publishTreeModel.isSelectable(node)) {
					selectionIt.remove();
				}
			}
			
			publishProcess.createPublishSetFor(nodeToPublish);
			
			PublishSetInformations set = publishProcess.testPublishSet(locale);
			StatusDescription[] status = set.getWarnings();
			boolean errors = false;
			for (StatusDescription description : status) {
				if (description.isError()) {
					errors = true;
					log.error("Status error by publish: {}", description.getLongDescription(locale));
				}
			}
			if (errors) {
				return;
			}
			
			PublishEvents publishEvents = publishProcess.getPublishEvents();
			try {
				publishProcess.applyPublishSet(executor, locale, false);
			} catch (Exception e) {
				log.error("",  e);
			}
			
			if (publishEvents.getPostPublishingEvents().size() > 0) {
				OLATResourceable courseOres = OresHelper.clone(course);
				for (MultiUserEvent event:publishEvents.getPostPublishingEvents()) {
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, courseOres);
				}
			}
		}
	}
	
	private void visitPublishModel(TreeNode node, INodeFilter filter, Collection<String> nodeToPublish) {
		int numOfChildren = node.getChildCount();
		for (int i = 0; i < numOfChildren; i++) {
			INode child = node.getChildAt(i);
			if (child instanceof TreeNode && filter.isVisible(child)) {
				nodeToPublish.add(child.getIdent());
				visitPublishModel((TreeNode)child, filter, nodeToPublish);
			}
		}
	}
	
	@Override
	public void setDisclaimerConfigs(ICourse course, CourseDisclaimerContext disclaimerContext) {
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();

		courseConfig.setDisclaimerEnabled(1, disclaimerContext.isTermsOfUseEnabled());
		if (disclaimerContext.isTermsOfUseEnabled()) {
			courseConfig.setDisclaimerTitle(1, disclaimerContext.getTermsOfUseTitle());
			courseConfig.setDisclaimerTerms(1, disclaimerContext.getTermsOfUseContent());
			courseConfig.setDisclaimerLabel(1, 1, disclaimerContext.getTermsOfUseLabel1());
			courseConfig.setDisclaimerLabel(1, 2, disclaimerContext.getTermsOfUseLabel2());
		}

		courseConfig.setDisclaimerEnabled(2, disclaimerContext.isDataProtectionEnabled());
		if (disclaimerContext.isDataProtectionEnabled()) {
			courseConfig.setDisclaimerTitle(2, disclaimerContext.getDataProtectionTitle());
			courseConfig.setDisclaimerTerms(2, disclaimerContext.getDataProtectionContent());
			courseConfig.setDisclaimerLabel(2, 1, disclaimerContext.getDataProtectionLabel1());
			courseConfig.setDisclaimerLabel(2, 2, disclaimerContext.getDataProtectionLabel2());
		}
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
	}

	@Override
	public void setCertificateConfigs(ICourse course, CertificateDefaults defaults) {
		CourseConfig courseConfig = course.getCourseConfig();
		courseConfig.setAutomaticCertificationEnabled(defaults.isAutomaticCertificationEnabled());
		courseConfig.setManualCertificationEnabled(defaults.isManualCertificationEnabled());
		courseConfig.setCertificateCustom1(defaults.getCertificateCustom1());
		courseConfig.setCertificateCustom2(defaults.getCertificateCustom2());
		courseConfig.setCertificateCustom3(defaults.getCertificateCustom3());
		if (defaults.getTemplate() != null) {
			courseConfig.setCertificateTemplate(defaults.getTemplate().getKey());
		}
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
	}
	
	@Override
	public void createIQTESTCourseNode(ICourse course, IQTESTCourseNodeDefaults defaults) {
		CourseNode node = createCourseNode(course, IQTESTCourseNode.TYPE, defaults);
		ModuleConfiguration moduleConfig = node.getModuleConfiguration();
		
		if (defaults.getModuleConfig() != null) {
			moduleConfig.putAll(defaults.getModuleConfig());
		}
		
		if (defaults.getReferencedEntry() != null) {
			moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
			IQEditController.setIQReference(defaults.getReferencedEntry(), moduleConfig);
		}
	}
	
	private CourseNode createCourseNode(ICourse course, String nodeType, CourseNodeTitleContext context) {
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseNode rootNode = cetm.getCourseNode(cetm.getRootNode().getIdent());
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(nodeType);
		CourseNode createdNode = nodeConfig.getInstance();
		createdNode.updateModuleConfigDefaults(true, cetm.getRootNode(), NodeAccessType.of(course));
		createdNode.setLongTitle(context.getLongTitle());
		createdNode.setShortTitle(context.getShortTitle());
		createdNode.setDescription(context.getDescription());
		cetm.addCourseNode(createdNode, rootNode);
		log.debug("Course node '{}' of type {} created.", createdNode.getShortTitle(), nodeType);
		return createdNode;
	}

	@Override
	public void createAssessmentMode(ICourse course, AssessmentModeDefaults defaults) {
		if (defaults.isEnabled() && defaults.getBegin() != null && defaults.getEnd() != null) {
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			AssessmentMode assessmentMode = assessmentModeManager.createAssessmentMode(courseEntry);
			assessmentMode.setName(defaults.getName());
			assessmentMode.setBegin(defaults.getBegin());
			assessmentMode.setLeadTime(defaults.getLeadTime());
			assessmentMode.setEnd(defaults.getEnd());
			assessmentMode.setFollowupTime(defaults.getFollowUpTime());
			assessmentMode.setManualBeginEnd(defaults.isManualBeginEnd());
			assessmentMode.setTargetAudience(AssessmentMode.Target.course);
			assessmentModeManager.merge(assessmentMode, false);
		}
	}

}
