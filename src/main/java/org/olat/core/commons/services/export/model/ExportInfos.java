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
package org.olat.core.commons.services.export.model;

import java.util.Date;

import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 2 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportInfos {
	
	private VFSLeaf zipLeaf;
	
	private String title;
	private Date creationDate;
	private Date expirationDate;
	private Identity creator;
	private String description;
	
	private Task runningTask;
	
	public ExportInfos(String title, Task runningTask) {
		this.title = title;
		this.runningTask = runningTask;
		creationDate = runningTask.getCreationDate();
		creator = runningTask.getCreator();
	}
	
	public ExportInfos(VFSLeaf zipLeaf, VFSMetadata zipMetadata) {
		this.zipLeaf = zipLeaf;
		
		creator = zipMetadata.getFileInitializedBy();
		creationDate = zipMetadata.getCreationDate();
		expirationDate = zipMetadata.getExpirationDate();
		description = zipMetadata.getComment();

		if(StringHelper.containsNonWhitespace(zipMetadata.getTitle())) {
			title = zipMetadata.getTitle();
		} else {
			title = zipLeaf.getName();
		}
	}
	
	public boolean isNew() {
		return runningTask != null && TaskStatus.newTask.equals(runningTask.getStatus());
	}
	
	public boolean isRunning() {
		return runningTask != null && TaskStatus.inWork.equals(runningTask.getStatus());
	}
	
	public boolean isCancelled() {
		return runningTask != null && TaskStatus.cancelled.equals(runningTask.getStatus());
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Identity getCreator() {
		return creator;
	}
	
	public VFSLeaf getZipLeaf() {
		return zipLeaf;
	}
	
	public Task getTask() {
		return runningTask;
	}

}
