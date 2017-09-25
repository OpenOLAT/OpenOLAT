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

import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * @author Christian Guretzki
 */
public interface CourseNodeIndexer extends Indexer {
	
	public void doIndex(SearchResourceContext searchResourceContext, ICourse course, CourseNode node, OlatFullIndexer indexWriter) throws IOException,InterruptedException;

	
	public default SearchResourceContext createSearchResourceContext(SearchResourceContext courseResourceContext, CourseNode node, String type) {
		SearchResourceContext courseNodeResourceContext = new SearchResourceContext(courseResourceContext);
		courseNodeResourceContext.setBusinessControlFor(node);
		courseNodeResourceContext.setDocumentType(type);
    	if(StringHelper.containsNonWhitespace(node.getShortTitle())) {
    		courseNodeResourceContext.setTitle(node.getShortTitle());
    	} else if(StringHelper.containsNonWhitespace(node.getLongTitle())) {
    		courseNodeResourceContext.setTitle(node.getLongTitle());
    	}
		return courseNodeResourceContext;
	}
}
