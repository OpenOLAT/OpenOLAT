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
package org.olat.upgrade;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.nodes.ta.TaskController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_11_2_1 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_11_2_1.class);

	private static final int BATCH_SIZE = 50;
	private static final String STATUS_OLD_TASK_ELEMENT = "STATUS OLD TASK ELEMENT";
	private static final String VERSION = "OLAT_11.2.1";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;

	public OLATUpgrade_11_2_1() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= upgradeStatus(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_11_2_1 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_11_2_1 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(STATUS_OLD_TASK_ELEMENT)) {
			int counter = 0;
			final Roles roles = Roles.administratorRoles();
			final SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(roles);
			params.setResourceTypes(Collections.singletonList("CourseModule"));

			List<RepositoryEntry> courses;
			do {
				courses = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, 50, true);
				for(RepositoryEntry course:courses) {
					processCourse(course);
				}
				counter += courses.size();
				log.info(Tracing.M_AUDIT, "Course for checklist and deprecated tasks migration processed: " + courses.size() + ", total courses processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(courses.size() == BATCH_SIZE);

			
			uhd.setBooleanDataValue(STATUS_OLD_TASK_ELEMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean processCourse(RepositoryEntry entry) {
		try {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode rootNode = course.getRunStructure().getRootNode();
			
			final List<TACourseNode> taskNodes = new ArrayList<>();
			
			new TreeVisitor(new Visitor() {
				@Override
				public void visit(INode node) {
					if(node instanceof TACourseNode) {
						taskNodes.add((TACourseNode)node);
					}
					
				}
			}, rootNode, false).visitAll();
			
			for(TACourseNode taskNode:taskNodes) {
				processTaskCourseNode(course, entry, taskNode);
			}
			return true;
		} catch(CorruptedCourseException e) {
			log.warn("Corrupted course: " + entry.getDisplayname() + " (" + entry.getKey() + ")", e);
			return true;
		} catch (Exception e) {
			log.error("", e);
			return true;
		}
	}
	
	private boolean processTaskCourseNode(ICourse course, RepositoryEntry entry, TACourseNode courseNode) {
		List<AssessmentEntry> assessmentEntries = getAssessmentEntries(entry, courseNode);
		if(assessmentEntries.size() > 0) {
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
	
			File dropbox = new File(FolderConfig.getCanonicalRoot(), DropboxController.getDropboxPathRelToFolderRoot(courseEnv, courseNode));
			File returnBox = new File(FolderConfig.getCanonicalRoot(), ReturnboxController.getReturnboxPathRelToFolderRoot(courseEnv, courseNode));
			
			for(AssessmentEntry assessmentEntry:assessmentEntries) {
				Identity assessedIdentity = assessmentEntry.getIdentity();
				
				boolean changed = false;
				List<Property> properties = cpm.findCourseNodeProperties(courseNode, assessedIdentity, null, TaskController.PROP_ASSIGNED);
				if(properties != null && properties.size() > 0) {
					assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
				} else {
					File identityDropbox = new File(dropbox, assessedIdentity.getName());
					File identityReturnBox = new File(returnBox, assessedIdentity.getName());
					if(hasBoxedFiles(identityDropbox, identityReturnBox)) {
						assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
					}	
				}
				
				if(changed) {
					courseEnv.getAssessmentManager().updateAssessmentEntry(assessmentEntry);
				}
			}
			dbInstance.commitAndCloseSession();
		}
		return true;
	}
	
	private boolean hasBoxedFiles(File identityDropbox, File identityReturnBox) {
		if(identityDropbox.exists()) {
			String[] droppedFilenames = identityDropbox.list(new SystemFilter());
			if(droppedFilenames.length > 0) {
				return true;
			}
		}
		
		if(identityReturnBox.exists()) {
			String[] returnededFilenames = identityReturnBox.list(new SystemFilter());
			if(returnededFilenames.length > 0) {
				return true;
			}
			
		}
		return false;
	}
	

	
	/**
	 * Return the list of not started assessment entries.
	 * @param entry
	 * @param courseNode
	 * @return A list of assessment entries with status null or notStarted
	 */
	private List<AssessmentEntry> getAssessmentEntries(RepositoryEntry entry, CourseNode courseNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("select aentry from assessmententry aentry")
		  .append(" inner join fetch aentry.identity as assessedIdentity")
		  .append(" inner join fetch assessedIdentity.user as assessedUser")
		  .append(" where aentry.repositoryEntry.key=:repoEntryKey and aentry.subIdent=:subIdent")
		  .append(" and (aentry.status is null or aentry.status='").append(AssessmentEntryStatus.notStarted).append("')");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repoEntryKey", entry.getKey())
				.setParameter("subIdent", courseNode.getIdent())
				.getResultList();
	}
	
	private static class SystemFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return StringHelper.containsNonWhitespace(name) && !name.startsWith(".");
		}
	}
}
