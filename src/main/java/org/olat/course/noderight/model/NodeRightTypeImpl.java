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
package org.olat.course.noderight.model;

import java.util.Collection;

import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightType;

/**
 * 
 * Initial date: 30 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightTypeImpl implements NodeRightType {
	
	private String identifier;
	private Class<?> translatorBaseClass;
	private String i18nKey;
	private boolean cssClassEnabled;
	private Collection<NodeRightRole> roles;
	private NodeRight defaultRight;
	
	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	@Override
	public Class<?> getTranslatorBaseClass() {
		return translatorBaseClass;
	}

	public void setTranslatorBaseClass(Class<?> translatorBaseClass) {
		this.translatorBaseClass = translatorBaseClass;
	}

	@Override
	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	@Override
	public boolean isCssClassEnabled() {
		return cssClassEnabled;
	}

	public void setCssClassEnabled(boolean enabled) {
		this.cssClassEnabled = enabled;
	}

	@Override
	public Collection<NodeRightRole> getRoles() {
		return roles;
	}
	
	public void setRoles(Collection<NodeRightRole> roles) {
		this.roles = roles;
	}
	
	@Override
	public NodeRight getDefaultRight() {
		return defaultRight;
	}
	
	public void setDefaultRight(NodeRight defaultRight) {
		this.defaultRight = defaultRight;
	}

}
