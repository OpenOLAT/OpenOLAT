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

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for (BC) brief case course-node.
 * @author Christian Guretzki
 */
public class BCCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
	
	private static final Logger log = Tracing.createLoggerFor(BCCourseNodeIndexer.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene have problems with '_' 
	public static final String TYPE = "type.course.node.bc";

	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.BCCourseNode";
	
	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		log.debug("Index Briefcase..." );
		
		BCCourseNode bcNode = (BCCourseNode)courseNode;
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, bcNode, TYPE);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, bcNode);
		indexWriter.addDocument(document);
		
		VFSContainer bcContainer = null;
		
		if(bcNode.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER)){
			bcContainer = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment());
		} else {
			String subpath = courseNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
			if(subpath != null) {
				VFSItem item = course.getCourseEnvironment().getCourseFolderContainer().resolve(subpath);
				if(item instanceof VFSContainer){
					bcContainer = new NamedContainerImpl(courseNode.getShortTitle(), (VFSContainer) item);
				}
			}
		}
		
		if(bcContainer != null) {
			doIndexVFSContainer(courseNodeResourceContext, bcContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}
