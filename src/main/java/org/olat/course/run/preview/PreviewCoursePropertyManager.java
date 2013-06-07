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

package org.olat.course.run.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.resource.OLATResource;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCoursePropertyManager extends BasicManager implements CoursePropertyManager {

	/**
	 * Hashmap contains hasmaps
	 */
	private Map properties = new HashMap();
	
	/**
	 * Creates a new course proprerty manager that stores properties per instance.
	 */
	public PreviewCoursePropertyManager() {
		//
	}

	@Override
	public OLATResource getCourseResource() {
		return null;
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#createCourseNodePropertyInstance(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.group.BusinessGroup, java.lang.String, java.lang.Float, java.lang.Long, java.lang.String, java.lang.String)
	 */
	public Property createCourseNodePropertyInstance(CourseNode node, Identity identity, BusinessGroup group, String name, Float floatValue,
			Long longValue, String stringValue, String textValue) {
		Property p = PropertyManager.getInstance().createProperty();
		p.setCategory(buildCourseNodePropertyCategory(node));
		p.setIdentity(identity);
		p.setGrp(null);
		p.setName(name);
		p.setLongValue(longValue);
		p.setFloatValue(floatValue);
		p.setStringValue(stringValue);
		p.setTextValue(textValue);
		return p;
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#deleteProperty(org.olat.properties.Property)
	 */
	public void deleteProperty(Property p) {
		List propertyList = getListOfProperties(p);
		for (int i=0; i < propertyList.size(); i++) {
			Property propertyElement = (Property)propertyList.get(i);
			if (propertyElement.getLongValue().equals(p.getLongValue())
					&& propertyElement.getFloatValue().equals(p.getFloatValue())
					&& propertyElement.getStringValue().equals(p.getStringValue())
					&& propertyElement.getTextValue().equals(p.getTextValue())) {
				propertyList.remove(i);
				break;
			}
		}
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#saveProperty(org.olat.properties.Property)
	 */
	public void saveProperty(Property p) {
		List propertyList = getListOfProperties(p);
		// since this is a save (only done once after creation) we
		// can safely add it to the list without looking for duplicates
		propertyList.add(p);
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#updateProperty(org.olat.properties.Property)
	 */
	public void updateProperty(Property p) {
		throw new AssertException("Not implemented for preview.");
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#listCourseNodeProperties(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.group.BusinessGroup, java.lang.String)
	 */
	public List listCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		throw new AssertException("Not implemented for preview.");
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#findCourseNodeProperties(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.group.BusinessGroup, java.lang.String)
	 */
	public List findCourseNodeProperties(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		List propertiesList = (List)properties.get(buildPropertyHashKey(buildCourseNodePropertyCategory(node), (identity == null ? "" : identity.getName()), grp, name));
		if (propertiesList == null) propertiesList = new ArrayList();
		return propertiesList;
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#findCourseNodeProperty(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.group.BusinessGroup, java.lang.String)
	 */
	public Property findCourseNodeProperty(CourseNode node, Identity identity, BusinessGroup grp, String name) {
		List propertyList = (List)properties.get(buildPropertyHashKey(buildCourseNodePropertyCategory(node), (identity == null ? "" : identity.getName()), grp, name));
		if (propertyList == null) return null;
		return (Property)propertyList.get(0);
	}

	@Override
	public List<Property> findCourseNodeProperties(CourseNode node, List<Identity> identities, String name) {
		return Collections.emptyList();
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#deleteNodeProperties(org.olat.course.nodes.CourseNode, java.lang.String)
	 */
	public void deleteNodeProperties(CourseNode courseNode, String name) {
		String category = buildCourseNodePropertyCategory(courseNode);
		Object[] keys = properties.keySet().toArray();
		for (int i=0; i < keys.length; i++) {
			String key = (String)keys[i];
			if (key.startsWith(category) && key.endsWith(name)) {
				properties.remove(key);
			}
		}
	}

	/**
	 * A property key consists of Category, Identity, Group and Name.
	 * Each property can have multiple values for the same given key.
	 * This returns the list of properties with the same key.
	 * 
	 * @param p
	 * @return list of properties with the same key
	 */
	private List getListOfProperties(Property p) {
		String propertyKey = buildPropertyHashKey(p);
		// get the list of properties for this key...
		List propertyList = (List)properties.get(propertyKey);
		if (propertyList == null) {
			propertyList = new ArrayList();
			properties.put(propertyKey, propertyList);
		}
		return propertyList;
	}

	private String buildPropertyHashKey(Property p) {//TODO username
    return buildPropertyHashKey(p.getCategory(), (p.getIdentity() == null) ? "" : p.getIdentity().getName(), p.getGrp(), p.getName());
	}
	
	private String buildPropertyHashKey(String category, String identityName, BusinessGroup group, String name) {
    return (category + identityName + (group == null ? "" : group.getKey().toString()) + name);
	}
	
	private String buildCourseNodePropertyCategory(CourseNode node) {
    String type = (node.getType().length() > 4 ? node.getType().substring(0,4) : node.getType());
    return ("NID:" + type + "::" + node.getIdent());
	}
	

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#getAnonymizedUserName(org.olat.core.id.Identity)
	 */
	public String getAnonymizedUserName(Identity identity) {
		throw new AssertException("Not implemented for preview.");
	}
	
	/**
	 * @see org.olat.course.properties.CoursePropertyManager#deleteAllCourseProperties()
	 */
	public void deleteAllCourseProperties() {
		throw new AssertException("Not implemented for preview.");
	}

	/**
	 * @see org.olat.course.properties.CoursePropertyManager#getAllIdentitiesWithCourseAssessmentData()
	 */
	public List<Identity> getAllIdentitiesWithCourseAssessmentData(Collection<Identity> excludeIdentities) {
		throw new AssertException("Not implemented for preview.");
	}
}
