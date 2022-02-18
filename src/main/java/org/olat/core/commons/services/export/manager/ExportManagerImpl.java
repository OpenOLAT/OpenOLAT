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
package org.olat.core.commons.services.export.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportTask;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ExportManagerImpl implements ExportManager {
	

	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Override
	public VFSContainer getExportContainer(RepositoryEntry entry, String resSubPath) {
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			VFSContainer rootFolder = VFSManager.getOrCreateContainer(courseEnv.getCourseBaseContainer(), ROOT_FOLDER);
			return VFSManager.getOrCreateContainer(rootFolder, resSubPath);
		}
		return null;
	}
	
	@Override
	public void startExport(ExportTask task, Identity creator, OLATResource resource, String resSubPath) {
		taskExecutorManager.execute(task, creator, resource, resSubPath, null);
	}

	@Override
	public void deleteExport(ExportInfos export) {
		VFSLeaf exportFile = export.getZipLeaf();
		if(exportFile != null) {
			exportFile.deleteSilently();
		}
	}

	@Override
	public void cancelExport(ExportInfos export) {
		Task task = export.getTask();
		if(task != null) {
			taskExecutorManager.cancel(task);
		} else {
			deleteExport(export);
		}
	}

	@Override
	public List<ExportInfos> getResultsExport(RepositoryEntry courseEntry, String resSubPath) {
		List<ExportInfos> exports = new ArrayList<>();
		
		List<String> taskEndings = new ArrayList<>();
		List<Task> runningTasks = taskExecutorManager.getTasks(courseEntry.getOlatResource(), resSubPath);
		for(Task runningTask:runningTasks) {
			try {
				ExportTask exportTask = taskExecutorManager.getPersistedRunnableTask(runningTask, ExportTask.class);
				if(exportTask != null) {
					ExportInfos resultsExport = new ExportInfos(exportTask.getTitle(), runningTask);
					exports.add(resultsExport);
					taskEndings.add("_" + runningTask.getKey().toString() + ".zip");
				}
			} catch (Exception e) {
				//
			}
		}
		
		VFSContainer exportContainer = getExportContainer(courseEntry, resSubPath);
		if(exportContainer != null) {
			List<VFSItem> zipItems = exportContainer.getItems(new ZIPLeafFilter());
			for(VFSItem zipItem:zipItems) {
				boolean running = false;
				for(String taskEnding:taskEndings) {
					if(zipItem.getName().endsWith(taskEnding)) {
						running = true;
					}
				}
				if(!running && zipItem instanceof VFSLeaf) {
					VFSLeaf zipLeaf = (VFSLeaf)zipItem;
					VFSMetadata metadata = vfsRepositoryService.getMetadataFor(zipLeaf);
					ExportInfos resultsExport = new ExportInfos(zipLeaf, metadata);
					exports.add(resultsExport);
				}
			}
		}
		
		return exports;
	}
	
	private static class ZIPLeafFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			String name = vfsItem.getName();
			return !name.startsWith(".") && !name.equals("__MACOSX") && name.endsWith(".zip")
					&& vfsItem instanceof VFSLeaf;
		}
	}
}
