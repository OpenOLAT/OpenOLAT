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
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
public interface CoursePropertyManager extends IdentityAnonymizerCallback {
	
	/**
	 * Create a course node property in ram
	 * @param node
	 * @param identity
	 * @param group
	 * @param name
	 * @param floatValue
	 * @param longValue
	 * @param stringValue
	 * @param textValue
	 * @return a course node property in ram
	 */
	public Property createCourseNodePropertyInstance(CourseNode node, Identity identity, BusinessGroup group, 
			String name, Float floatValue, Long longValue, String stringValue, String textValue);

	/**
	 * Delete a property
	 * @param p
	 */
	public void deleteProperty(Property p);
	
	/**
	 * Save a property
	 * @param p
	 */
	public void saveProperty(Property p);

	/**
	 * Save or update a property
	 * @param p
	 */
	public void updateProperty(Property p);

	/**
	 * List all course node properties (inexact match. I.e. null values are not taken into account)
	 * @param node
	 * @param identity
	 * @param grp
	 * @param name
	 * @return all course node properties
	 */
	public List<Property> listCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name);
	
	/**
	 * Same as above but only count the properties
	 * @param node
	 * @param identity
	 * @param grp
	 * @param name
	 * @return
	 */
	public int countCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name);
	
	/**
	 * Find course node properties (exact match. I.e. null values are taken into account)
	 * @param node
	 * @param identity
	 * @param grp
	 * @param name
	 * @return matching course node properties
	 */
	public List<Property> findCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name);
	
	/**
	 * Find a specific course node property (exact match. I.e. null values are taken into account)
	 * @param node
	 * @param identity
	 * @param grp
	 * @param name
	 * @return matching course node property
	 */
	public Property findCourseNodeProperty(CourseNode node, Identity identity, BusinessGroup grp, String name);
	

	public void appendText(CourseNode node, Identity identity, BusinessGroup grp, String name, String text);
	
	/**
	 * Find a specific course node property (exact match. I.e. null values are taken into account)
	 * @param node
	 * @param grp
	 * @param name
	 * @return matching course node property
	 */
	public Property findCourseNodeProperty(CourseNode node, BusinessGroup grp, String name);
	

	/**
	 * Find a specific course node property (exact match. I.e. null values are taken into account)
	 * @param node
	 * @param identity
	 * @param grp
	 * @param name
	 * @return matching course node property
	 */
	public List<Property> findCourseNodeProperties(CourseNode node, List<Identity> identities, String name);
	
	/**
	 * Delete all node properties for a given course node and a category.
	 * @param courseNode The course node. Must not be null.
	 * @param name The property name or null if all propertys if this category and node should be deleted
	 */
	public void deleteNodeProperties(CourseNode courseNode, String name);
	
	
	/**
	 * Delete all properties that have been stored for this course - userproperties, group properties 
	 * course properties - just everything. 
	 */
	public void deleteAllCourseProperties();

	/**
	 * @return a list of all identities that have generated any assessment properties within this courses
	 * @param excludeIdentities Exclude a list of identities
	 */
	public List<Identity> getAllIdentitiesWithCourseAssessmentData(Collection<Identity> excludeIdentities);

}
