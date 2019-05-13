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
package org.olat.ldap.manager;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ldap.model.LDAPGroup;

/**
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPGroupVisitor implements LDAPVisitor {
	
	private static final Logger log = Tracing.createLoggerFor(LDAPGroupVisitor.class);

	private final List<LDAPGroup> groups = new ArrayList<LDAPGroup>();
	
	public List<LDAPGroup> getGroups() {
		return groups;
	}

	@Override
	public void visit(SearchResult searchResult) throws NamingException {
		Attributes resAttributes = searchResult.getAttributes();
		Attribute memberAttr = resAttributes.get("member");
		Attribute cnAttr = resAttributes.get("cn");

		if(memberAttr != null) {
			LDAPGroup group = new LDAPGroup();
			Object cn = cnAttr.get();
			if(cn instanceof String) {
				group.setCommonName((String)cn);
			}

			List<String> members = new ArrayList<String>();
			try {
				for(NamingEnumeration<?> memberEn = memberAttr.getAll(); memberEn.hasMoreElements(); ) {
					Object member = memberEn.next();
					if(member instanceof String) {
						members.add((String)member);
					}
				}
			} catch (NamingException e) {
				log.error("", e);
			}
			group.setMembers(members);
			groups.add(group);
		}
	}
}
