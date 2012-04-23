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

package org.olat.search.service.indexer.repository.course;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManagerFactory;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.ProjectBrokerProjectDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for project-broker course-node.
 * @author Christian Guretzki
 */
public class ProjectBrokerCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
	private OLog log = Tracing.createLoggerFor(this.getClass()); 
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
//	public final static String TYPE_DROPBOX     = "type.course.node.projectbroker.dropbox";
//	public final static String TYPE_RETURNBOX   = "type.course.node.projectbroker.returnbox";
	public static final String TYPE = "type.course.node.projectbroker";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.ProjectBrokerCourseNode";

	
	public ProjectBrokerCourseNodeIndexer() {
	}
	
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
    SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
    courseNodeResourceContext.setBusinessControlFor(courseNode);
    courseNodeResourceContext.setTitle(courseNode.getShortTitle());
    courseNodeResourceContext.setDescription(courseNode.getLongTitle());
       
    // go further, index my projects
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, courseNode);
		if (projectBrokerId != null) {
			List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
			for (Iterator<Project> iterator = projects.iterator(); iterator.hasNext();) {
				Project project = iterator.next();
				Document document = ProjectBrokerProjectDocument.createDocument(courseNodeResourceContext, project);
				indexWriter.addDocument(document);
				doIndexFolders(courseNodeResourceContext, project, indexWriter);
		  }
		} else {
			log.debug("projectBrokerId is null, courseNode=" + courseNode + " , course=" + course);
		}
	}


	private void doIndexFolders(SearchResourceContext searchResourceContext, Project project, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		log.debug("DOES NOT INDEX DROPBOX AND RETURNBOX");
		// RPOBLEM : How we could check access to the projects in checkAccess method (missing courseNode to get project-broker)
		// Index Dropbox
//	    String dropboxFilePath = FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
//	    File fDropboxFolder = new File(dropboxFilePath);
//	    VFSContainer dropboxRootContainer = new LocalFolderImpl(fDropboxFolder);
//	    projectResourceContext.setDocumentType(TYPE_DROPBOX);
//	    doIndexVFSContainer(projectResourceContext, dropboxRootContainer, indexWriter, "");
	    
	    // Index Returnbox
//	    String returnboxFilePath = FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
//	    File fResturnboxFolder = new File(returnboxFilePath);
//	    VFSContainer returnboxRootContainer = new LocalFolderImpl(fResturnboxFolder);
//	    projectResourceContext.setDocumentType(TYPE_RETURNBOX);
//	    doIndexVFSContainer(projectResourceContext, returnboxRootContainer, indexWriter, "");
    }

	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}
