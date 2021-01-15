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
package org.olat.search.service.indexer.repository.course;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.LeafIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * Initial date: 15 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentCourseNodeIndexer extends LeafIndexer implements CourseNodeIndexer {

	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.DocumentCourseNode";
	public static final String TYPE = "type.course.node.document";
	
	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}

	@Override
	public void doIndex(SearchResourceContext courseResourceContext, ICourse course, CourseNode node,
			OlatFullIndexer indexWriter) throws IOException, InterruptedException {
		DocumentCourseNode docNode = (DocumentCourseNode)node;
		
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(courseResourceContext, docNode, TYPE);
		Document nodeDocument = CourseNodeDocument.createDocument(courseNodeResourceContext, docNode);
		indexWriter.addDocument(nodeDocument);
		
		VFSContainer courseFolderCont = course.getCourseEnvironment()
				.getCourseFolderContainer(CourseContainerOptions.withoutElements());
		VFSLeaf vfsLeaf = docNode.getDocumentSource(courseFolderCont).getVfsLeaf();
		if(vfsLeaf != null) {
			SearchResourceContext fileContext = new SearchResourceContext(courseNodeResourceContext);
			doIndexVFSLeafByMySelf(fileContext, vfsLeaf, indexWriter, SUPPORTED_TYPE_NAME);
		}
	}
}
