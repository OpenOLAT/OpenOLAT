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

package org.olat.course.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;

/**
 * Initial Date: May 5, 2004
 * 
 * @author gnaegi<br>
 *         Comment: The course property manager handles all course specific read /
 *         write operations for course properties. Usually other managers use
 *         the course property manager (like assessment manager or audit
 *         manager), only view controllers use the course property manager
 *         directly.
 */
public class PersistingCoursePropertyManager implements CoursePropertyManager {

	private NarrowedPropertyManager pm;
	private Map<Long,String> anonymizerMap;
	private final OLATResourceable ores;
	private static Random random = new Random(System.currentTimeMillis());

	private PersistingCoursePropertyManager(OLATResourceable course) {
		ores = OresHelper.clone(course);
		pm = NarrowedPropertyManager.getInstance(ores);
		// Initialize identity anonymizer map
		anonymizerMap = new HashMap<>();
	}

	/**
	 * Get an instance of the course property manager for this course that
	 * persists properties to the database.
	 * 
	 * @param olatCourse The course
	 * @return The course property manager
	 */
	public static PersistingCoursePropertyManager getInstance(OLATResourceable course) {
		return new PersistingCoursePropertyManager(course);
	}

	@Override
	public Property createCourseNodePropertyInstance(CourseNode node, Identity identity, BusinessGroup group, String name, Float floatValue,
			Long longValue, String stringValue, String textValue) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.createPropertyInstance(identity, group, myCategory, name, floatValue, longValue, stringValue, textValue);
	}

	@Override
	public void deleteProperty(Property p) {
		pm.deleteProperty(p);
	}

	@Override
	public void saveProperty(Property p) {
		pm.saveProperty(p);
	}

	@Override
	public void updateProperty(Property p) {
		pm.updateProperty(p);
	}

	@Override
	public List<Property> listCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.listProperties(identity, grp, myCategory, name);
	}

	@Override
	public int countCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.countProperties(identity, grp, myCategory, name);
	}

	@Override
	public List<Property> findCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.findProperties(identity, grp, myCategory, name);
	}

	@Override
	public Property findCourseNodeProperty(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.findProperty(identity, grp, myCategory, name);
	}

	@Override
	public void appendText(CourseNode node, Identity identity, BusinessGroup grp, String name, String text) {
		String myCategory = buildCourseNodePropertyCategory(node);
		pm.appendTextToProperty(identity, grp, myCategory, name, text);
	}

	@Override
	public Property findCourseNodeProperty(CourseNode node, BusinessGroup grp, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.findProperty(grp, myCategory, name);
	}

	@Override
	public List<Property> findCourseNodeProperties(CourseNode node, List<Identity> identities, String name) {
		String myCategory = buildCourseNodePropertyCategory(node);
		return pm.findProperties(identities, myCategory, name);
	}

	@Override
	public void deleteNodeProperties(CourseNode courseNode, String name) {
		if (courseNode == null) { throw new AssertException("courseNode can not be null when deleting course node properties"); }
		pm.deleteProperties(null, null, buildCourseNodePropertyCategory(courseNode), name);
	}

	private String buildCourseNodePropertyCategory(CourseNode node) {
		String type = (node.getType().length() > 4 ? node.getType().substring(0, 4) : node.getType());
		return ("NID:" + type + "::" + node.getIdent());
	}

	@Override
	public String getAnonymizedUserName(Identity identity) {
		synchronized (anonymizerMap) {
			String anonymizedName = anonymizerMap.get(identity.getKey());
			if (anonymizedName == null) {
				// try to fetch from course properties
				Property anonymizedProperty = pm.findProperty(identity, null, "Anonymizing", "AnonymizedUserName");
				if (anonymizedProperty == null) {
					// not found - create a new anonymized name
					anonymizedName = "RANDOM-" + random.nextInt(100000000);
					// add as course properties
					anonymizedProperty = pm.createPropertyInstance(identity, null, "Anonymizing", "AnonymizedUserName", null, null, anonymizedName,
							null);
					pm.saveProperty(anonymizedProperty);
				} else {
					// property found - use property value from there
					anonymizedName = anonymizedProperty.getStringValue();
				}
				anonymizerMap.put(identity.getKey(), anonymizedName);
			}
			return anonymizedName;
		}
	}

	@Override
	public void deleteAllCourseProperties() {
		pm.deleteAllProperties();
	}

	/**
	 * The specified exclude identities is only a best effort used for performance. If you want
	 * unique identities, deduplicate them afterwards.
	 * 
	 */
	@Override
	public List<Identity> getAllIdentitiesWithCourseAssessmentData(Collection<Identity> excludeIdentities) {
		ICourse course = CourseFactory.loadCourse(ores);
		AssessmentEntryDAO courseNodeAssessmentDao = CoreSpringFactory.getImpl(AssessmentEntryDAO.class);
		return courseNodeAssessmentDao.getAllIdentitiesWithAssessmentData(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
	}
}