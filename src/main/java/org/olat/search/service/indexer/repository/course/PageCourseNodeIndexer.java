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
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PageCourseNode;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.document.PageDocument;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageCourseNodeIndexer extends DefaultIndexer implements CourseNodeIndexer {
	
	public static final String TYPE = "type.course.node.page";
	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.PageCourseNode";
	
	@Autowired
	private PageService pageService;
	
	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
	
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
	throws IOException, InterruptedException {
		//
	}
	
	@Override
	public void doIndex(SearchResourceContext courseResourceContext, ICourse course, CourseNode node, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(courseResourceContext, node, TYPE);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, node);
		indexWriter.addDocument(document);
		
		if(node instanceof PageCourseNode pageNode) {
			Long pageKey = pageNode.getPageReferenceKey();
			Page page = pageService.getFullPageByKey(pageKey);
			if(page != null) {
				List<Document> partDocuments = PageDocument.createDocument(courseNodeResourceContext, page, pageNode);
				for(Document partDocument:partDocuments) {
					indexWriter.addDocument(partDocument);
				}
			}
		}
	}
}
