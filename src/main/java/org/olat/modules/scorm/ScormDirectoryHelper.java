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
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * <P>
 * Initial Date:  14 august 2009 <br>
 * @author srosse
 */
public class ScormDirectoryHelper {
	
/**
 * Return the SCORM Root folder
 */
	public static VFSContainer getScormRootFolder() {
		VFSContainer canonicalRoot = new LocalFolderImpl(new File(FolderConfig.getCanonicalRoot()));
		VFSContainer scormContainer = (VFSContainer)canonicalRoot.resolve("scorm");
		if (scormContainer == null) {
			scormContainer = canonicalRoot.createChildContainer("scorm");
		}
		return scormContainer;
	}
	
	/**
	 * Return the container where the LMS save the datas for a user.
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return
	 */
	public static VFSContainer getScoDirectory(String username, CourseEnvironment courseEnv, CourseNode node) {
		Long courseId = courseEnv.getCourseResourceableId();
		VFSItem userFolder = ScormDirectoryHelper.getScormRootFolder().resolve(username);
		if(userFolder != null) {
			VFSItem scoFolder = userFolder.resolve(courseId.toString() + "-" + node.getIdent());
			if(scoFolder instanceof VFSContainer) {
				return (VFSContainer)scoFolder;
			}
		}
		return null;
	}
}
