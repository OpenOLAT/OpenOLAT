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
*/ 

package org.olat.core.id;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.olat.core.logging.AssertException;
import org.olat.core.util.i18n.I18nModule;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class IdentityEnvironment implements Serializable {
	
	private static final long serialVersionUID = -5813083210685147201L;
	private Identity identity = null;
	private Roles roles = null;
	private Map<String, String> attributes = null;
	private Locale locale = null;
	
	/**
	 * 
	 */
	public IdentityEnvironment() {
	// defaults all to null
	}
	
	public IdentityEnvironment(Identity identity, Roles roles) {
		this.identity = identity;
		this.roles = roles;
	}

	/**
	 * @param attributes
	 */
	public void setAttributes(Map<String,String> attributes) {
		if (this.attributes != null) throw new AssertException("can only set attributes once");
		if (identity == null) throw new AssertException("must set identity before setting attribues");
		this.attributes = attributes;
		
		// for the usertracking project needed:
		// the volatile, e.g. not db stored attributes must be available on the User Object besides the UserProperties.
		// The UserProperties (firstname, lastname, email, skypeId etc.) and the Attributes (e.g. shibboleth attributes) are very close related.
		// Attributes are typically set during login process.
		//
		// remains still as attributes in the IdentityEnvironment, as this is use in Course Condition Interpreter (and we are short before 6.3.0 relase 2010-02-02)
		//
		User user = identity.getUser();
		user.setIdentityEnvironmentAttributes(attributes);
	}

	/**
	 * 
	 * @param addAttribues
	 */
	public void addAttributes(Map<String,String> addAttribues){
		if(this.attributes == null) throw new AssertException("set attributes first");
		//identity must be not null, which is asserted because setAttributes must be calld first.
		this.attributes.putAll(addAttribues);//changes also the attribues already set previously in the User
	}
	
	/**
	 * @param identity
	 */
	public void setIdentity(Identity identity) {
		if (this.identity != null) throw new AssertException("can only set identity once!");
		this.identity = identity;
	}

	/**
	 * @param roles
	 */
	public void setRoles(Roles roles) {
		if (this.roles != null && !this.roles.equals(roles)) {
			throw new AssertException("can only set Roles once");
		}
		this.roles = roles;
	}

	/**
	 * @return The attributes map
	 */
	public Map<String,String> getAttributes() {
		return attributes;
	}

	/**
	 * @return The identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @return The users roles
	 */
	public Roles getRoles() {
		return roles;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "identity: " + identity + ", roles: " + roles + ", attributes: " + attributes + " , super:" + super.toString();
	}

	/**
	 * never returns null: if the locale is not set yet, then the olat's default
	 * locale is returned
	 * 
	 * @return Locale
	 */
	public Locale getLocale() {
		return locale == null ? I18nModule.getDefaultLocale() : locale;
	}

	/**
	 * @param locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}