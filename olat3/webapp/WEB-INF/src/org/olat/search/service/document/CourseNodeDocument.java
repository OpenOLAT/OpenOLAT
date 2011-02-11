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

package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.core.commons.services.search.OlatDocument;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class CourseNodeDocument extends OlatDocument {
	private static final OLog log = Tracing.createLoggerFor(CourseNodeDocument.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.course.node";
	
	public CourseNodeDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, CourseNode courseNode) {
		CourseNodeDocument courseNodeDocument = new CourseNodeDocument();	

		// Set all know attributes
		courseNodeDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		if (searchResourceContext.getDocumentType() != null && !searchResourceContext.getDocumentType().equals("") ) {
			courseNodeDocument.setDocumentType(searchResourceContext.getDocumentType());
		} else {
		  courseNodeDocument.setDocumentType(TYPE);
		}
		courseNodeDocument.setCssIcon("o_course_icon");
		courseNodeDocument.setTitle(courseNode.getShortTitle());
		courseNodeDocument.setDescription(courseNode.getLongTitle());
		// Get dates from paraent object via context because course node has no dates 
		courseNodeDocument.setCreatedDate(searchResourceContext.getCreatedDate());
		courseNodeDocument.setLastChange(searchResourceContext.getLastModified());
		courseNodeDocument.setParentContextType(searchResourceContext.getParentContextType());
		courseNodeDocument.setParentContextName(searchResourceContext.getParentContextName());
    // unused course-node attributtes
		//	courseNode.getShortName();
    //	courseNode.getType();

		if (log.isDebug()) log.debug(courseNodeDocument.toString());
		return courseNodeDocument.getLuceneDocument();
	}

}
