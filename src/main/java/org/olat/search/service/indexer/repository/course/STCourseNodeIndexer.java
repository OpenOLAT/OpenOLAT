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
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.modules.ModuleConfiguration;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.LeafIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for ST (Structure) course-node. 
 * @author Christian Guretzki
 */
public class STCourseNodeIndexer extends LeafIndexer implements CourseNodeIndexer {
	private static final Logger log = Tracing.createLoggerFor(STCourseNodeIndexer.class);
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.course.node.st";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.STCourseNode";

	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
	throws IOException, InterruptedException {
		//
	}

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter)
	throws IOException,InterruptedException {
		if (log.isDebugEnabled()) log.debug("Index StructureNode...");
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, TYPE);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(document);
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		String displayType = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE);
		String relPath = STCourseNodeEditController.getFileName(config);
		if (relPath != null && displayType != null && displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
			VFSItem displayPage = course.getCourseFolderContainer().resolve(relPath);
			if(displayPage instanceof VFSLeaf) {
				doIndexVFSLeafByMySelf(courseNodeResourceContext, (VFSLeaf)displayPage, indexWriter, relPath);
			}
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}
