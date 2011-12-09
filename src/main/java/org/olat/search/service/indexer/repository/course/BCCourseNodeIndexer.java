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

import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.olat.search.service.indexer.repository.CourseIndexer;

/**
 * Indexer for (BC) brief case course-node.
 * @author Christian Guretzki
 */
public class BCCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.course.node.bc";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.BCCourseNode";

	private CourseIndexer courseNodeIndexer;

	public BCCourseNodeIndexer() {
		courseNodeIndexer = new CourseIndexer();
	}
	
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		if (Tracing.isDebugEnabled(BCCourseNodeIndexer.class)) Tracing.logDebug("Index Briefcase..." , BCCourseNodeIndexer.class);

    SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
    courseNodeResourceContext.setBusinessControlFor(courseNode);
    courseNodeResourceContext.setDocumentType(TYPE);
    courseNodeResourceContext.setTitle(courseNode.getShortTitle());
    courseNodeResourceContext.setDescription(courseNode.getLongTitle());

		OlatNamedContainerImpl namedContainer = BCCourseNode.getNodeFolderContainer((BCCourseNode) courseNode, course.getCourseEnvironment());
		doIndexVFSContainer(courseNodeResourceContext,namedContainer,indexWriter,"", FolderIndexerAccess.FULL_ACCESS);
    // go further, index my child nodes
		courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
	}

	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}

	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}
	
}
