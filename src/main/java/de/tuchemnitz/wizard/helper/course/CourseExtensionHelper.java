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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.helper.course;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * Helpful methods for course extension with new elements.
 */
public class CourseExtensionHelper {
	
	private static final Logger log = Tracing.createLoggerFor(CourseExtensionHelper.class);
	
	/**
	 * Creates an enrollment course node and appends it to the course. (not
	 * persisted yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created enrollment node
	 */
	public static final CourseNode createEnrollmentNode(final ICourse course, final String shortTitle, final String longTitle) {
		return createNode(course, shortTitle, longTitle, "en");
	}

	/**
	 * Creates a single page course node and appends it to the course. (not
	 * persisted yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created single page node
	 */
	public static final CourseNode createSinglePageNode(final ICourse course, final String shortTitle, final String longTitle) {
		return createNode(course, shortTitle, longTitle, "sp");
	}

	/**
	 * Creates a download folder course node and appends it to the course. (not
	 * persisted yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created download folder node
	 */
	public static final CourseNode createDownloadFolderNode(final ICourse course, final String shortTitle, final String longTitle) {
		return createNode(course, shortTitle, longTitle, "bc");
	}

	/**
	 * Creates a forum course node and appends it to the course. (not persisted
	 * yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created forum node
	 */
	public static final CourseNode createForumNode(final ICourse course, final String shortTitle, final String longTitle) {
		return createNode(course, shortTitle, longTitle, "fo");
	}

	/**
	 * Creates a contact form node and appends it to the course. (not persisted
	 * yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created contact form node
	 */
	public static final CourseNode createContactFormNode(final ICourse course, final String shortTitle, final String longTitle) {
		return createNode(course, shortTitle, longTitle, "co");
	}

	/**
	 * Creates a course node and appends it to the course. (not persisted yet)
	 * 
	 * @param c course object
	 * @param shortTitle short title for node
	 * @param longTitle long title for node
	 * @return created course node
	 */
	public static final CourseNode createNode(ICourse course, final String shortTitle, final String longTitle, final String type) {
		// edit session
		course = CourseFactory.openCourseEditSession(course.getResourceableId());
		final CourseEditorTreeModel cetm = course.getEditorTreeModel();
		final CourseNode rootNode = cetm.getCourseNode(course.getRunStructure().getRootNode().getIdent());

		// create a node with default data
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type);
		CourseNode node = nodeConfig.getInstance();
		node.updateModuleConfigDefaults(true, cetm.getRootNode(), NodeAccessType.of(course));
		node.setShortTitle(shortTitle);
		node.setLongTitle(longTitle);

		// append node to course
		course.getEditorTreeModel().addCourseNode(node, rootNode);
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		if(log.isDebugEnabled()) log.debug("Created new course node: " + nodeConfig.getAlias());
		return node;
	}
}
