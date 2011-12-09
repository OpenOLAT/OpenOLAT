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

import java.util.HashMap;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.CourseNode;

/**
 * Factory to get an indexer for certain course-node type. E.g. FoCourseNodeIndexer. 
 * @author Christian Guretzki
 */
public class CourseNodeIndexerFactory {
	private static final OLog log = Tracing.createLoggerFor(CourseNodeIndexerFactory.class);

	private static CourseNodeIndexerFactory INSTANCE;
	private Map<String,CourseNodeIndexer> indexerMap = new HashMap<String,CourseNodeIndexer>();
	
	static { INSTANCE = new CourseNodeIndexerFactory(); }
	
	/**
	 * 
	 */
	private CourseNodeIndexerFactory() {
		// singleton
	}

	public static CourseNodeIndexerFactory getInstance() {
		return INSTANCE;
	}

	public void registerIndexer(CourseNodeIndexer indexer) {
		indexerMap.put(indexer.getSupportedTypeName(), indexer);
	}
	

	/**
	 * Get the repository handler for this repository entry.
	 * @param re
	 * @return the handler or null if no appropriate handler could be found
	 */
	public CourseNodeIndexer getCourseNodeIndexer(CourseNode node) {
		String courseNodeName = node.getClass().getName();
		CourseNodeIndexer courseNodeIndexer = indexerMap.get(courseNodeName);
    if (courseNodeIndexer != null) {
    	return courseNodeIndexer;
    } else {
    	if (log.isDebug()) log.debug("No indexer found for node=" + node);
			return null;
    }
	}


}
