/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: Jul 12, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface CourseNodeWithFiles {

	/**
	 * retrieve quota for a courseNode
	 *
	 * @param identity
	 * @param roles
	 * @param entry
	 * @param quotaManager
	 * @return Quota
	 */
	Quota getQuota(Identity identity, Roles roles, RepositoryEntry entry, QuotaManager quotaManager);

	VFSContainer getNodeContainer(CourseEnvironment courseEnvironment);

	/**
	 * @return true is storage is external
	 */
	boolean isStorageExtern();

	/**
	 * @return true if storage of courseNode is inside the course folder
	 */
	boolean isStorageInCourseFolder();
}
