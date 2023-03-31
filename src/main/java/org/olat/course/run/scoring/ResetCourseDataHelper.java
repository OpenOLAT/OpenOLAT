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
package org.olat.course.run.scoring;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.model.AssessmentNodesLastModified;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.Role;
import org.olat.modules.reminder.ReminderService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetCourseDataHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ResetCourseDataHelper.class);
	
	private static final DateFormat folderFormat = new SimpleDateFormat("yyyy-MM");
	public static final String ROOT_FOLDER = "archives";
	
	private final CourseEnvironment courseEnv;
	private final Set<ResetBusinessGroupTask> queueResetGTACourseNode = new HashSet<>();
	private final Map<GTACourseNode,List<GroupMembersSet>> gtaNodeToFGroupSets = new HashMap<>();
	private final Map<GTACourseNode,Set<Long>> gtaNodeToFullGroupsMembers = new HashMap<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementMgr;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	public ResetCourseDataHelper(CourseEnvironment courseEnv) {
		this.courseEnv = courseEnv;
		CoreSpringFactory.autowireObject(this);
	}
	
	public MediaResource resetCourse(Identity identity, Identity doer, Role by) {
		return resetCourse(List.of(identity), doer, by);
	}
	
	public MediaResource resetCourse(List<Identity> identities, Identity doer, Role by) {
		Structure structure = courseEnv.getRunStructure();
		List<CourseNode> courseNodes = getCourseNodes(structure.getRootNode());
		return resetCourseNodes(identities, courseNodes, true, doer, by);
	}
	
	public MediaResource resetCourseNode(List<Identity> identities, CourseNode courseNode, boolean resetCourse, Identity doer, Role by) {
		List<CourseNode> courseNodes = getCourseNodes(courseNode);
		return resetCourseNodes(identities, courseNodes, resetCourse, doer, by);
	}
	
	private List<CourseNode> getCourseNodes(CourseNode rootNode) {
		final List<CourseNode> courseNodes = new ArrayList<>();
		new TreeVisitor(node -> {
			if(node instanceof CourseNode cNode) {
				courseNodes.add(cNode);
			}
		}, rootNode, false)
			.visitAll();
		return courseNodes;
	}

	public MediaResource resetCourseNodes(List<Identity> identities, List<CourseNode> courseNodes, boolean resetCourse, Identity doer, Role by) {
		List<VFSLeaf> archiveNames = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			VFSLeaf archiveName = resetCourseNodes(identity, courseNodes, resetCourse, identities, doer, by);
			if(archiveName != null) {
				archiveNames.add(archiveName);
			}
		}
		
		// Check archive is only a sanity check, no archive -> something is wrong
		if(!archiveNames.isEmpty() && !queueResetGTACourseNode.isEmpty()) {
			for(ResetBusinessGroupTask resteGTANodeForGroup:queueResetGTACourseNode) {
				GTACourseNode gtaNode = resteGTANodeForGroup.courseNode();
				BusinessGroup businessGroup = resteGTANodeForGroup.businessGroup();
				gtaNode.resetGroupTaskData(businessGroup, courseEnv, doer);
			}
		}
		
		if(archiveNames.isEmpty()) {
			return null;
		}
		return new ResetCourseDataMediaResource(archiveNames, courseEnv);
	}

	private VFSLeaf resetCourseNodes(Identity assessedIdentity, List<CourseNode> courseNodes,
			boolean resetCourse, List<Identity> identitiesToReset, Identity doer, Role by) {	
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		Roles roles = securityManager.getRoles(assessedIdentity);
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(assessedIdentity, roles, courseEntry);
		UserCourseEnvironment userCourseEnv = UserCourseEnvironmentImpl.load(assessedIdentity, roles, courseEnv, reSecurity);
		VFSContainer rootFolder = VFSManager.getOrCreateContainer(courseEnv.getCourseBaseContainer(), ROOT_FOLDER);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		UserEfficiencyStatementImpl statement = efficiencyStatementMgr.getUserEfficiencyStatementFull(courseEntry, assessedIdentity);
		Certificate lastCertificate = certificatesManager.getLastCertificate(assessedIdentity, courseEntry.getOlatResource().getKey());

		// 1) First the archive
		String folder = "";
		String vfsName = generateFilename(assessedIdentity);
		synchronized(folderFormat) {
			folder = folderFormat.format(new Date());
			rootFolder = VFSManager.getOrCreateContainer(rootFolder, folder);
		}
		VFSLeaf archiveZip = rootFolder.createChildLeaf(vfsName);
		if(archiveZip == null) {
			vfsName = VFSManager.rename(rootFolder, vfsName);
			archiveZip = rootFolder.createChildLeaf(vfsName);
		}
		try(OutputStream out=archiveZip.getOutputStream(true);
				ZipOutputStream zout = new ZipOutputStream(out)) {
			archiveStatement(statement, vfsName, userCourseEnv, zout);
			for(CourseNode courseNode:courseNodes) {
				String path = generatePath(vfsName, courseNode);
				courseNode.archiveForResetUserData(userCourseEnv, zout, path, doer, by);
			}
			zout.flush();
			out.flush();
		} catch(Exception e) {
			log.error("", e);
		}
		
		// 2) Flag the efficiency statement as invalid (not the last one)
		if(resetCourse && statement != null) {
			// Efficiency statement as invalid
			statement.setLastStatement(false);
			statement.setArchivePath(archiveZip.getRelPath());
			if(lastCertificate != null) {
				statement.setArchiveCertificateKey(lastCertificate.getKey());
			}
			efficiencyStatementMgr.updateUserEfficiencyStatement(statement);
			// Make sure there is no last statement to generate a new one
			efficiencyStatementMgr.invalidateEfficiencyStatement(courseEntry, assessedIdentity);
			dbInstance.commit();
		}
		
		// 3) Reset the data (eventually a course node can produce a new efficiency statement)
		try {
			for(CourseNode courseNode:courseNodes) {
				courseNode.resetUserData(userCourseEnv, doer, by);
				if(courseNode instanceof GTACourseNode gtaNode
						&& GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
					Set<Long> fullBusinessGroupMembers = membersOfFullBusinessGroups(gtaNode, identitiesToReset);
					boolean resetTask = fullBusinessGroupMembers.contains(assessedIdentity.getKey());
					if(resetTask) { //queue reset group task
						queueResetGTATask(gtaNode, assessedIdentity);
					}
				}
			}
		} catch(Exception e) {
			log.error("", e);
		}
		
		log.info("Reset data archive for user: {}, course: {} at : {}", assessedIdentity, courseEntry, archiveZip);
		dbInstance.commit();
		
		// 4) Evaluate all
		userCourseEnv.getScoreAccounting().evaluateAll();
		dbInstance.commitAndCloseSession();
		
		if(resetCourse) {
			CourseConfig config = userCourseEnv.getCourseEnvironment().getCourseConfig();
			UserEfficiencyStatementImpl userEfficiencyStatement = efficiencyStatementMgr.getUserEfficiencyStatementFull(courseEntry, assessedIdentity);
			if(userEfficiencyStatement != null || config.isEfficencyStatementEnabled()) {
				// There is an efficiency statement, ignore the course setting and make it up-to-date
				AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
				List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.getAssessmentNodeDataList(userCourseEnv, lastModifications, true, true, true);
				efficiencyStatementMgr.updateUserEfficiencyStatement(assessedIdentity, courseEnv, assessmentNodeList, lastModifications, courseEntry);
			}
			// Flag the certificate, ready for recertification
			if(lastCertificate != null) {
				certificatesManager.archiveCertificate(lastCertificate);
			}
			// Increment run of course
			userCourseInformationsManager
					.incrementUserCourseInformationsRun(courseEntry.getOlatResource(), assessedIdentity);
			// Delete sent reminders
			reminderService.resetSentReminders(courseEntry, assessedIdentity);
			
			// user activity logging
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_RESET, 
					getClass(), 
					LoggingResourceable.wrap(course),
					LoggingResourceable.wrap(assessedIdentity));
		} else {
			// user activity logging
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_NODE_RESET, 
					getClass(),
					LoggingResourceable.wrap(course),
					LoggingResourceable.wrap(assessedIdentity));
		}
		
		dbInstance.commitAndCloseSession();

		// notify about changes
		ResetCourseDataEvent rcde = new ResetCourseDataEvent(assessedIdentity, courseEntry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(rcde, course);
		
		return archiveZip;
	}
	
	/**
	 * From the list of identities to reset, find the ones which are all referenced in a business
	 * groups to reset the task of GTA course element.
	 * 
	 * @param gtaNode The course element
	 * @param identities The whole list of identities to reset
	 * @return
	 */
	private Set<Long> membersOfFullBusinessGroups(GTACourseNode gtaNode, List<Identity> identities) {
		if(gtaNodeToFullGroupsMembers.containsKey(gtaNode)) {
			return gtaNodeToFullGroupsMembers.get(gtaNode);
		}

		List<BusinessGroup> businessGroups = gtaManager.getBusinessGroups(gtaNode);
		Map<Long,BusinessGroup> businessGroupsMap = businessGroups.stream()
				.collect(Collectors.toMap(BusinessGroup::getKey, group -> group, (u, v) -> u));
		
		List<BusinessGroupMembership> businessGroupMemberships = businessGroupService.getBusinessGroupsMembership(businessGroups);
		
		Map<Long,List<Long>> groupKeysToMembers = new HashMap<>();
		for(BusinessGroupMembership businessGroupMembership:businessGroupMemberships) {
			List<Long> members = groupKeysToMembers
					.computeIfAbsent(businessGroupMembership.getGroupKey(), key -> new ArrayList<>());
			if(businessGroupMembership.isParticipant()) {
				members.add(businessGroupMembership.getIdentityKey());
			}
		}
		
		List<Long> identitiesKeys = identities.stream()
				.map(Identity::getKey)
				.toList();
		
		Set<Long> fullGroupMembers = new HashSet<>();
		List<GroupMembersSet> groupSets = new ArrayList<>();
		for(Map.Entry<Long, List<Long>> entry:groupKeysToMembers.entrySet()) {
			List<Long> membersKeys = entry.getValue();
			if(identitiesKeys.containsAll(membersKeys)) {
				fullGroupMembers.addAll(membersKeys);
				
				BusinessGroup businessGroup = businessGroupsMap.get(entry.getKey());
				groupSets.add(new GroupMembersSet(businessGroup, Set.copyOf(membersKeys)));
			}	
		}
		gtaNodeToFGroupSets.put(gtaNode, groupSets);
		gtaNodeToFullGroupsMembers.put(gtaNode, fullGroupMembers);
		return fullGroupMembers;
	}
	
	private void queueResetGTATask(GTACourseNode gtaNode, Identity identity) {
		List<GroupMembersSet> groupSets = gtaNodeToFGroupSets.get(gtaNode);
		if(groupSets != null && !groupSets.isEmpty()) {
			for(GroupMembersSet groupSet:groupSets) {
				if(groupSet.participantsKeys().contains(identity.getKey())) {
					queueResetGTACourseNode.add(new ResetBusinessGroupTask(gtaNode, groupSet.businessGroup()));
				}
			}
		}
	}
	
	private void archiveStatement(UserEfficiencyStatementImpl statement, String vfsName, UserCourseEnvironment userCourseEnv, ZipOutputStream zout) throws IOException {
		if(statement != null && StringHelper.containsNonWhitespace(statement.getStatementXml())) {
			zout.putNextEntry(new ZipEntry("EfficiencyStatement.xml"));
			zout.write(statement.getStatementXml().getBytes(StandardCharsets.UTF_8));
			zout.closeEntry();
		} else {
			Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
			AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
			List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.getAssessmentNodeDataList(userCourseEnv, lastModifications, true, true, true);
			EfficiencyStatement effStatement = efficiencyStatementMgr.createEfficiencyStatement(assessedIdentity, courseEnv,
					assessmentNodeList, lastModifications, courseEnv.getCourseGroupManager().getCourseEntry());
			String xml = EfficiencyStatementManager.toXML(effStatement); 
			zout.putNextEntry(new ZipEntry(ZipUtil.concat("/" + vfsName, "EfficiencyStatement.xml")));
			zout.write(xml.getBytes(StandardCharsets.UTF_8));
			zout.closeEntry();
		}
	}

	private String generatePath(String vfsName, CourseNode courseNode) {
		String shortTitle = courseNode.getShortTitle();
		shortTitle = FileUtils.normalizeFilename(shortTitle);
		if(shortTitle.length() > 25) {
			shortTitle = shortTitle.substring(0, 25);
		}
		return "/" + vfsName + "/" + shortTitle + "_" + courseNode.getIdent();
	}
	
	private String generateFilename(Identity identity) {
		String date = Formatter.formatDatetimeWithMinutes(new Date());
		User user = identity.getUser();
		String name = user.getLastName()
				+ "_" + user.getFirstName()
				+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : identity.getName());
		String filename = date + "_" + name +"_data"; 
		return  FileUtils.normalizeFilename(filename) + ".zip";
	}
	
	private record GroupMembersSet(BusinessGroup businessGroup, Set<Long> participantsKeys) {

	}
	
	private record ResetBusinessGroupTask(GTACourseNode courseNode, BusinessGroup businessGroup) {

		@Override
		public int hashCode() {
			return courseNode.getIdent().hashCode() + businessGroup.getKey().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof ResetBusinessGroupTask resetNode) {
				return courseNode.getIdent().equals(resetNode.courseNode().getIdent())
						&& businessGroup.equals(resetNode.businessGroup());
			}
			return false;
		}
	}
}
