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
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractCourseNodeIndexer implements CourseNodeIndexer {
	
	private final String type;
	private final String supportedType;
	
	public AbstractCourseNodeIndexer(String type, String supportedType) {
		this.type = type;
		this.supportedType = supportedType;
	}
	
	public String getType() {
		return type;
	}

	@Override
	public String getSupportedTypeName() {
		return supportedType;
	}

	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
	throws IOException, InterruptedException {
		//
	}
	
	@Override
	public void doIndex(SearchResourceContext courseResourceContext, ICourse course, CourseNode node, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(courseResourceContext, node, getType());
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, node);
		indexWriter.addDocument(document);
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}
}
