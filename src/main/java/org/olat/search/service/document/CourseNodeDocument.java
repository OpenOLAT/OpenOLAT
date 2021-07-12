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

package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class CourseNodeDocument extends OlatDocument {

	private static final long serialVersionUID = -2035945166792451137L;

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to search for certain document type and lucene has problems with '_' 
	public final static String TYPE = "type.course.node";
	
	public CourseNodeDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, CourseNode courseNode) {
		CourseNodeDocument courseNodeDocument = new CourseNodeDocument();	

		// Set all know attributes
		courseNodeDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		if (StringHelper.containsNonWhitespace(searchResourceContext.getDocumentType())) {
			courseNodeDocument.setDocumentType(searchResourceContext.getDocumentType());
		} else {
			courseNodeDocument.setDocumentType(TYPE);
		}
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
		if(nodeConfig != null && StringHelper.containsNonWhitespace(nodeConfig.getIconCSSClass())) {
			courseNodeDocument.setCssIcon(nodeConfig.getIconCSSClass());
		} else {
			courseNodeDocument.setCssIcon("o_course_icon");
		}
		
		if(StringHelper.containsNonWhitespace(courseNode.getLongTitle())) {
			courseNodeDocument.setTitle(courseNode.getLongTitle());
		} else if(StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
			courseNodeDocument.setTitle(courseNode.getShortTitle());
		}
		if(StringHelper.containsNonWhitespace(courseNode.getDescription())) {
			String description = courseNode.getDescription();
			description = FilterFactory.getHtmlTagsFilter().filter(description);
			courseNodeDocument.setDescription(description);
		}
		if(StringHelper.containsNonWhitespace(courseNode.getObjectives())) {
			String objectives = courseNode.getObjectives();
			objectives = FilterFactory.getHtmlTagsFilter().filter(objectives);
			courseNodeDocument.setContent(objectives);
		}
		
		// Get dates from parent object via context because course node has no dates 
		courseNodeDocument.setCreatedDate(searchResourceContext.getCreatedDate());
		courseNodeDocument.setLastChange(searchResourceContext.getLastModified());
		courseNodeDocument.setParentContextType(searchResourceContext.getParentContextType());
		courseNodeDocument.setParentContextName(searchResourceContext.getParentContextName());

		return courseNodeDocument.getLuceneDocument();
	}
}