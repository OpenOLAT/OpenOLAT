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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.condition.additionalconditions.AdditionalConditionAnswerContainer;
import org.olat.course.condition.additionalconditions.PasswordStore;
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

	private final Map<Long, AdditionalConditionAnswerContainer> cache = new ConcurrentHashMap<Long, AdditionalConditionAnswerContainer>();

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

	/**
	 * @see de.bps.course.nodes.CourseNodePasswordManager#getAnswerContainer(org.olat.core.id.Identity)
	 */
	@Override
	public AdditionalConditionAnswerContainer getAnswerContainer(Identity identity) {
		AdditionalConditionAnswerContainer acac;
		if(identity == null) {
			acac = new AdditionalConditionAnswerContainer();
		} else if (cache.containsKey(identity.getKey())) {
			acac = cache.get(identity.getKey());
		} else {
			PropertyManager pm = PropertyManager.getInstance();
			List<Property> properties = pm.listProperties(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, null, null, AdditionalConditionAnswerContainer.RESOURCE_NAME);
			if(properties == null) {
				acac = null;
			} else {
				acac = new AdditionalConditionAnswerContainer();
				for (Object object : properties) {
					Property property = (Property) object;
					PasswordStore store = new PasswordStore();
					store.setPassword(property.getStringValue());
					store.setCourseId(property.getLongValue());
					store.setNodeIdent(property.getResourceTypeId());
					acac.insertAnswer(Long.toString(property.getResourceTypeId()), property.getLongValue(), store);
				}
				cache.put(identity.getKey(), acac);
			}
		}
		return acac;
	}

	@Override
	public AdditionalConditionAnswerContainer removeAnswerContainerFromCache(Identity identity) {
		if(identity == null) return null;
		return cache.remove(identity.getKey());
	}

	/**
	 * persist answer container to database
	 * 
	 * @param identity
	 * @param answers
	 */
	private void persistAnswerContainer(Identity identity, AdditionalConditionAnswerContainer answers) {
		if(identity instanceof PreviewIdentity) {
			//preview identity are not persistable
			cache.put(identity.getKey(), answers);
		} else if (!answers.isContainerEmpty()) {
			boolean updateInDatabase = false;
			PropertyManager pm = PropertyManager.getInstance();
			Map<String, Object> container = answers.getContainer();
			for (String key : container.keySet()) {
				PasswordStore store = (PasswordStore) container.get(key);
				List<Property> properties = pm.listProperties(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME,
						Long.valueOf(store.getNodeIdent()), null, AdditionalConditionAnswerContainer.RESOURCE_NAME);
				if (properties != null && properties.size() > 0) {
					// it exists properties with this key and from this identity
					boolean pwdFounded = false;
					for (Property prop : properties) {
						if (store.getCourseId().equals(prop.getLongValue())) {
							if(!store.getPassword().equals(prop.getStringValue())) {
								// same course id and same node id
								// not same pwd --> update
								prop.setStringValue(store.getPassword());
								pm.updateProperty(prop);
								updateInDatabase = true;
							}
							// same store
							pwdFounded = true;
							break;
						}
					}
					if (!pwdFounded) {
						Property p = pm.createUserPropertyInstance(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, null, store.getCourseId(),
								store.getPassword(), null);
						p.setResourceTypeName(AdditionalConditionAnswerContainer.RESOURCE_NAME);
						p.setResourceTypeId(Long.valueOf(store.getNodeIdent()));
						pm.saveProperty(p);
						updateInDatabase = true;
					}
				} else {
					// it exists nothing properties with this key and from this identity
					Property p = pm.createUserPropertyInstance(identity, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, null, store.getCourseId(),
							store.getPassword(), null);
					p.setResourceTypeName(AdditionalConditionAnswerContainer.RESOURCE_NAME);
					p.setResourceTypeId(Long.valueOf(store.getNodeIdent()));
					pm.saveProperty(p);
					updateInDatabase = true;
				}
			}
			if (updateInDatabase) {
				cache.put(identity.getKey(), answers);
			}
		}
	}

	/**
	 * @see de.bps.course.nodes.CourseNodePasswordManager#updatePwd(org.olat.core.id.Identity, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updatePwd(Identity identity, String nodeIdentifier, Long courseId, String value) {
		AdditionalConditionAnswerContainer answers = getAnswerContainer(identity);

		if (answers == null) {
			answers = new AdditionalConditionAnswerContainer();
		}
		PasswordStore pwdStore = (PasswordStore)answers.getAnswers(nodeIdentifier, courseId);
		
		if(pwdStore != null) {
			pwdStore.setPassword(value);
		} else {
			PasswordStore store = new PasswordStore();
			store.setPassword(value);
			store.setNodeIdent(Long.valueOf(nodeIdentifier));
			store.setCourseId(Long.valueOf(courseId));
			answers.insertAnswer(nodeIdentifier, courseId, store);
		}
		persistAnswerContainer(identity, answers);
	}

	/**
	 * @see de.bps.course.nodes.CourseNodePasswordManager#deleteAllPasswordsFor(java.lang.Long)
	 */
	public void deleteAllPasswordsFor(OLATResourceable ores) {
		PropertyManager pm = PropertyManager.getInstance();
		List<Property> properties = pm.listProperties(null, null, AdditionalConditionAnswerContainer.RESOURCE_NAME,
				null, null, AdditionalConditionAnswerContainer.RESOURCE_NAME, ores.getResourceableId(), null);
		for (Property p : properties) {
			Long nodeId = p.getResourceTypeId();
			Long courseId = p.getLongValue();
			removeAnswers(nodeId, courseId);
			pm.deleteProperty(p);
		}
	}

	private void removeAnswers(Long nodeId, Long courseId) {
		for (Long key : cache.keySet()) {
			AdditionalConditionAnswerContainer acac = cache.get(key);
			if (acac.containsAnswer(Long.toString(nodeId), courseId)) {
				acac.removeAnswer(Long.toString(nodeId), courseId);
			}
		}
	}
}
