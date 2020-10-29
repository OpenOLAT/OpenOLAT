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
package org.olat.course.nodes.gta.manager;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.modules.ModuleConfiguration;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAUserDataManager implements UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(GTAUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;

	@Override
	public String getExporterID() {
		return "gta";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File tasksArchiveDir = new File(archiveDirectory, "Tasks");
		tasksArchiveDir.mkdir();
		
		int count = 0;
		List<Task> tasks = gtaManager.getTasks(identity);
		for(Task task:tasks) {
			try {
				TaskList taskList = task.getTaskList();
				ICourse course = CourseFactory.loadCourse(taskList.getEntry().getOlatResource().getResourceableId());
				if(course == null) continue;
				
				CourseNode node = course.getRunStructure().getNode(taskList.getCourseNodeIdent());
				if(node instanceof GTACourseNode) {
					String archiveName = node.getIdent() + "_" + StringHelper.transformDisplayNameToFileSystemName(node.getShortName());
					File taskArchiveDir = new File(tasksArchiveDir, archiveName); 
					exportTask(identity, task, (GTACourseNode)node, course, taskArchiveDir);
				}
				if(count++ % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			} catch (CorruptedCourseException e) {
				log.warn("", e);
			} catch (Exception e) {
				log.error("", e);
				dbInstance.rollbackAndCloseSession();
			}
		}
	}
	
	private void exportTask(Identity assessedIdentity, Task task, GTACourseNode node , ICourse course, File taskArchiveDir) {
		int flow = 0;//for beautiful ordering
		ModuleConfiguration config = node.getModuleConfiguration();
		if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			File submitDirectory = gtaManager.getSubmitDirectory(course.getCourseEnvironment(), node, assessedIdentity);
			String submissionDirName = (++flow) + "_submissions";
			copyDirContentToDir(submitDirectory, new File(taskArchiveDir, submissionDirName));
		}

		if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionsDir = gtaManager.getCorrectionDirectory(course.getCourseEnvironment(), node, assessedIdentity);
			String correctionDirName = (++flow) + "_corrections";
			copyDirContentToDir(correctionsDir, new File(taskArchiveDir, correctionDirName));
		}
		
		if(task != null && config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			int numOfIteration = task.getRevisionLoop();
			for(int i=1; i<=numOfIteration; i++) {
				File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(course.getCourseEnvironment(), node, i, assessedIdentity);
				String revisionDirName = (++flow) + "_revisions_" + i;
				copyDirContentToDir(revisionDirectory, new File(taskArchiveDir, revisionDirName));
			}
		}
	}
	
	private void copyDirContentToDir(File source, File target) {
		File[] sourceFiles = source.listFiles(SystemFileFilter.FILES_ONLY);
		if(sourceFiles != null && sourceFiles.length > 0) {
			target.mkdirs();
			for(File sourceFile:sourceFiles) {
				FileUtils.copyFileToDir(sourceFile, target, "");
			}
		}
	}
}
