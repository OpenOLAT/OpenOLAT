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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course;

import java.io.File;

import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.config.CourseConfig;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Description:<BR/>
 * Interface of the OLAT course. The course has a course environment and
 * a run structure and some other fields.
 * <p>
 * Initial Date:  2004/10/11 13:55:48
 * @author Felix Jost
 */
public interface ICourse extends OLATResourceable {
	
	/**
	 * Name of folder within course root directory where nodes export their data.
	 */
	public static final String EXPORTED_DATA_FOLDERNAME = "export";

	/**
	 * @return The course run structure
	 */
	public Structure getRunStructure();
	
	/**
	 * @return The course editor tree model for this course
	 */
	public CourseEditorTreeModel getEditorTreeModel();
	
	public void postCopy(CourseEnvironmentMapper envMapper, ICourse sourceCourse);
	
	public void postCopyCourse(CourseEnvironmentMapper envMapper, ICourse sourceCourse, CopyCourseContext context);
	
	public void postImport(File importDirectory, CourseEnvironmentMapper envMapper);
	
	/**
	 * Return the container to files for this course.
	 * (E.g. "/course/123/")
	 * @return the container to files for this course
	 */
	public LocalFolderImpl getCourseBaseContainer();
	
	/**
	 * Return the container to the coursefolder of this course. (E.g.
	 * "COURSEBASEPATH/coursefolder/"). !! This is for administration or
	 * internal use!! There is no permission check, make sure your controller
	 * does check if user is allowed to see stuff, e.g. shared folder
	 * 
	 * @return the container to the coursefolder of this course
	 */
	public VFSContainer getCourseFolderContainer();
	
	/**
	 * Return the merged course container with the desired directories:
	 * course folder, shared resource folder, folders course elements 
	 * and participants folder elements.
	 * 
	 * @return the container to the coursefolder of this course
	 */
	public VFSContainer getCourseFolderContainer(CourseContainerOptions options);
	
	
	/**
	 * Give the possibility to override the read-only mode of the containers
	 * if the course is closed.
	 * 
	 * @param overrideReadOnly
	 * @return
	 */
	public VFSContainer getCourseFolderContainer(boolean overrideReadOnly);
	
	/**
	 * The course folder that the user specified by its identity environment
	 * can see and use. Used by WebDAV access.
	 * 
	 * @param identityEnv
	 * @return The aggreagted course folder
	 */
	public VFSContainer getCourseFolderContainer(IdentityEnvironment identityEnv);
	
	public LocalFolderImpl getCourseExportDataDir();

	/**
	 * @return The course title. This is the display name of the course repository entry
	 * or the short title of the course run structure root node if the repository entry 
	 * has not been created yet
	 */
	public String getCourseTitle();

	/**
	 * @return The course environment of this course
	 */
	public CourseEnvironment getCourseEnvironment();

	/**
	 * @return The course configuration
	 */
	public CourseConfig getCourseConfig();
}
