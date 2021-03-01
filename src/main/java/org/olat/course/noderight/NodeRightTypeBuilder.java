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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.noderight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.course.noderight.NodeRight.EditMode;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.model.NodeRightGrantImpl;
import org.olat.course.noderight.model.NodeRightImpl;
import org.olat.course.noderight.model.NodeRightTypeImpl;

/**
 * 
 * Initial date: 30 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightTypeBuilder {
	
	private final String identifier;
	private final Set<NodeRightRole> roles = new HashSet<>();
	private final Set<NodeRightRole> defaultRoles = new HashSet<>();
	private Class<?> translatorBaseClass;
	private String i18nKey;
	private boolean cssClassEnabled;
	
	public static NodeRightTypeBuilder ofIdentifier(String identifier) {
		return new NodeRightTypeBuilder(identifier);
	}
	
	private NodeRightTypeBuilder(String identifier) {
		this.identifier = identifier;
	}
	
	public NodeRightType build() {
		NodeRightTypeImpl nodeRightType = new NodeRightTypeImpl();
		nodeRightType.setIdentifier(identifier);
		nodeRightType.setTranslatorBaseClass(translatorBaseClass);
		nodeRightType.setI18nKey(i18nKey);
		nodeRightType.setCssClassEnabled(cssClassEnabled);
		nodeRightType.setRoles(new HashSet<>(roles));
		
		List<NodeRightGrant> defautlGrants = new ArrayList<>(defaultRoles.size());
		for (NodeRightRole defaultRole : defaultRoles) {
			NodeRightGrantImpl defaultGrant = new NodeRightGrantImpl();
			defaultGrant.setRole(defaultRole);
			defautlGrants.add(defaultGrant);
		}

		NodeRightImpl defaultRight = new NodeRightImpl();
		defaultRight.setEditMode(EditMode.regular);
		defaultRight.setTypeIdentifier(identifier);
		defaultRight.setGrants(defautlGrants);
		nodeRightType.setDefaultRight(defaultRight);
		return nodeRightType;
	}
	
	public NodeRightTypeBuilder setLabel(Class<?> translatorBaseClass, String i18nKey) {
		this.translatorBaseClass = translatorBaseClass;
		this.i18nKey = i18nKey;
		return this;
	}
	
	public NodeRightTypeBuilder enableCssClass() {
		this.cssClassEnabled = true;
		return this;
	}
	
	public NodeRightTypeBuilder addRole(NodeRightRole role, boolean defaultEnabled) {
		roles.add(role);
		if (defaultEnabled) {
			defaultRoles.add(role);
		}
		return this;
	}

}
