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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.condition.additionalconditions.AdditionalConditionAnswerContainer;
import org.olat.course.run.preview.PreviewIdentity;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * 
 * Description:<br>
 * This class managed the answer container for a condition.
 * 
 * <P>
 * Initial Date: 17.09.2010 <br>
 * 
 * @author bja
 */
public class CourseNodePasswordManagerImpl implements CourseNodePasswordManager {

	private static final String ATTR_COURSE_NODE_PREFIX = "course-node-password-prfx-";

	private static CourseNodePasswordManagerImpl INSTANCE;
	static {
		INSTANCE = new CourseNodePasswordManagerImpl();
	}

	/**
	 * @return singleton instance
	 */
	public static CourseNodePasswordManagerImpl getInstance() {
		return INSTANCE;
	}

	private CourseNodePasswordManagerImpl() {
		// no public constructor
	}

	@Override
	public String getAnswer(IdentityEnvironment identityEnv, Long courseId, String nodeIdent) {
		Identity identity = identityEnv == null ? null : identityEnv.getIdentity();
		
		String value;
		if(identityEnv == null || identityEnv.getAttributes() == null) {// REST calls
			value = null;
		} else {
			String key = generateKey(courseId, Long.valueOf(nodeIdent));
			value = identityEnv.getAttributes().get(key);
			if(value == null && !identityEnv.getRoles().isGuestOnly()) {
				PropertyManager pm = PropertyManager.getInstance();
				List<Property> properties = pm.listProperties(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME,
						Long.valueOf(nodeIdent), null, AdditionalConditionAnswerContainer.RESOURCE_NAME);
				for (Property property : properties) {
					String pKey = generateKey(property.getLongValue(), property.getResourceTypeId());
					identityEnv.getAttributes().put(pKey, property.getStringValue());
				}
				value = identityEnv.getAttributes().get(key);
			}
		}
		return value;
	}
	
	private String generateKey(Long courseId, Long nodeIdent) {
		return ATTR_COURSE_NODE_PREFIX + courseId + " " + nodeIdent;
	}

	@Override
	public void removeAnswerContainerFromCache(Identity identity) {
		//
	}

	/**
	 * persist answer container to database
	 * 
	 * @param identity
	 * @param answers
	 */
	private void persistAnswerContainer(IdentityEnvironment identityEnv, Long courseId, Long nodeIdent, String value) {
		if(identityEnv == null || identityEnv.getRoles() == null
				|| identityEnv.getIdentity() == null || identityEnv.getAttributes() == null) return;
		
		identityEnv.getAttributes().put(generateKey(courseId, nodeIdent), value);
		
		Roles roles = identityEnv.getRoles();
		Identity identity = identityEnv.getIdentity();
		if(!(identity instanceof PreviewIdentity) && !roles.isGuestOnly()) {
			PropertyManager pm = PropertyManager.getInstance();
			List<Property> properties = pm.listProperties(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME,
					nodeIdent, null, AdditionalConditionAnswerContainer.RESOURCE_NAME);
			if (properties != null && !properties.isEmpty()) {
				// it exists properties with this key and from this identity
				boolean pwdFounded = false;
				for (Property prop : properties) {
					if (courseId.equals(prop.getLongValue())) {
						if(!value.equals(prop.getStringValue())) {
							// same course id and same node id
							// not same pwd --> update
							prop.setStringValue(value);
							pm.updateProperty(prop);
						}
						// same store
						pwdFounded = true;
						break;
					}
				}
				if (!pwdFounded) {
					Property p = pm.createUserPropertyInstance(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, null, courseId, value, null);
					p.setResourceTypeName(AdditionalConditionAnswerContainer.RESOURCE_NAME);
					p.setResourceTypeId(nodeIdent);
					pm.saveProperty(p);
				}
			} else {
				// it exists nothing properties with this key and from this identity
				Property p = pm.createUserPropertyInstance(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, null, courseId, value, null);
				p.setResourceTypeName(AdditionalConditionAnswerContainer.RESOURCE_NAME);
				p.setResourceTypeId(nodeIdent);
				pm.saveProperty(p);
			}
		}
	}

	@Override
	public void updatePwd(IdentityEnvironment identityEnv, String nodeIdentifier, Long courseId, String value) {
		persistAnswerContainer(identityEnv, courseId, Long.valueOf(nodeIdentifier), value);
	}
}
