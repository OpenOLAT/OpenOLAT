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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course;

import java.util.Iterator;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.servlets.WebDAVProvider;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for CoursefolderWebDAVProvider
 */
public class CoursefolderWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "coursefolders";

	public String getMountPoint() { return MOUNTPOINT; }

	public VFSContainer getContainer(Identity identity) {
		MergeSource cfRoot = new MergeSource(null, null);
		RepositoryManager rm = RepositoryManager.getInstance();
		List courseEntries = rm.queryByOwner(identity, CourseModule.getCourseTypeName());
		
		for (Iterator iter = courseEntries.iterator(); iter.hasNext();) {
			RepositoryEntry re = (RepositoryEntry) iter.next();
			OLATResourceable res = re.getOlatResource();
			ICourse course = CourseFactory.loadCourse(res.getResourceableId());
			VFSContainer courseFolder = course.getCourseFolderContainer();
			//NamedContainerImpl cfContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(course.getCourseTitle()), courseFolder);
			NamedContainerImpl cfContainer;
			cfContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(course.getCourseTitle()), courseFolder);
			cfRoot.addContainer(cfContainer);
		}
		return cfRoot;
	}

}
