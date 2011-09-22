/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.nodes.ScormCourseNode;
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
		return (VFSContainer)canonicalRoot.resolve("scorm");
	}
	
	/**
	 * Return the container where the LMS save the datas for a user.
	 * @param username
	 * @param courseEnv
	 * @param node
	 * @return
	 */
	public static VFSContainer getScoDirectory(String username, CourseEnvironment courseEnv, ScormCourseNode node) {
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
