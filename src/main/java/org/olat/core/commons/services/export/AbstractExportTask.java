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
package org.olat.core.commons.services.export;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 16 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractExportTask implements ExportTask {

	private static final long serialVersionUID = 8147341801712456303L;
	
	protected transient Task task;

	@Override
	public void setTask(Task task) {
		this.task = task;
	}

	protected VFSMetadata fillMetadata(VFSLeaf exportZip, String title, String description) {
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		VFSMetadata meta = vfsRepositoryService.getMetadataFor(exportZip);
		if (task.getCreator() != null) {
			meta.setFileInitializedBy(task.getCreator());
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			meta.setCreator(userManager.getUserDisplayName(task.getCreator()));
		}
		meta.setTitle(title);
		meta.setComment(description);
		meta.setSource(task.getKey().toString());
		meta.setExpirationDate(CalendarUtils.endOfDay(DateUtils.addDays(new Date(), 10)));
		meta = vfsRepositoryService.updateMetadata(meta);
		DBFactory.getInstance().commitAndCloseSession();
		return meta;
	}

}
