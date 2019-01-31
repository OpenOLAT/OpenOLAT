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

package org.olat.user.restapi;

import java.util.Collections;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  26 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Examples {
	
	public static final UserVO SAMPLE_USERVO = new UserVO();
	public static final UserVOes SAMPLE_USERVOes = new UserVOes();
	public static final RolesVO SAMPLE_ROLESVO = new RolesVO();
	public static final StatusVO SAMPLE_STATUSVO = new StatusVO();
	public static final PreferencesVO SAMPLE_PREFERENCESVO = new PreferencesVO();
	
	public static final OrganisationVO SAMPLE_ORGANISATIONVO = new OrganisationVO();
	
	public static final OrganisationTypeVO SAMPLE_ORGANISATIONTYPEVO = new OrganisationTypeVO();
	
	public static final RelationRoleVO SAMPLE_RELATIONROLEVO = new RelationRoleVO();
	public static final IdentityToIdentityRelationVO SAMPLE_IDENTITYTOIDENTITYRELATIONVO = new IdentityToIdentityRelationVO();
  
	static {
		SAMPLE_USERVO.setKey(345l);
		SAMPLE_USERVO.setFirstName("John");
		SAMPLE_USERVO.setLastName("Smith");
		SAMPLE_USERVO.setLogin("john");
		SAMPLE_USERVO.setEmail("john.smith@frentix.com");
		SAMPLE_USERVO.setPassword("");
		SAMPLE_USERVO.putProperty("telPrivate", "238456782");
		SAMPLE_USERVO.putProperty("telMobile", "238456782");
		SAMPLE_USERVOes.setUsers(new UserVO[]{SAMPLE_USERVO});

		SAMPLE_ROLESVO.setAuthor(true);
  	
		SAMPLE_STATUSVO.setStatus(Integer.valueOf(2));
  	
		SAMPLE_PREFERENCESVO.setLanguage("de");
		
		SAMPLE_ORGANISATIONVO.setKey(4587L);
		SAMPLE_ORGANISATIONVO.setIdentifier("HEROL-2");
		SAMPLE_ORGANISATIONVO.setDisplayName("Herol 2");
		SAMPLE_ORGANISATIONVO.setCssClass("o_icon_beautiful");
		SAMPLE_ORGANISATIONVO.setDescription("An organisation description");
		SAMPLE_ORGANISATIONVO.setExternalId("IDEXT78");
		SAMPLE_ORGANISATIONVO.setManagedFlagsString("all");
		SAMPLE_ORGANISATIONVO.setOrganisationTypeKey(38l);
		SAMPLE_ORGANISATIONVO.setParentOrganisationKey(3l);
		SAMPLE_ORGANISATIONVO.setRootOrganisationKey(1l);
		
		SAMPLE_ORGANISATIONTYPEVO.setKey(38l);
		SAMPLE_ORGANISATIONTYPEVO.setIdentifier("OWL-1");
		SAMPLE_ORGANISATIONTYPEVO.setCssClass("o_icon_owl");
		SAMPLE_ORGANISATIONTYPEVO.setDescription("An organization type");
		SAMPLE_ORGANISATIONTYPEVO.setDisplayName("Organization type");
		SAMPLE_ORGANISATIONTYPEVO.setExternalId("OWL-1-ext");
		SAMPLE_ORGANISATIONTYPEVO.setManagedFlagsString("externalId");
		
		SAMPLE_RELATIONROLEVO.setKey(56l);
		SAMPLE_RELATIONROLEVO.setExternalId("RO-1");
		SAMPLE_RELATIONROLEVO.setExternalRef("ROR-2");
		SAMPLE_RELATIONROLEVO.setManagedFlags("delete");
		SAMPLE_RELATIONROLEVO.setRights(Collections.singletonList("myRight"));
		
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setKey(234l);
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setExternalId("ID-2-ID-256");
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setIdentitySourceKey(34019l);
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setIdentityTargetKey(23100l);
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setRelationRole("Supervisor");
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setRelationRoleKey(23l);
		SAMPLE_IDENTITYTOIDENTITYRELATIONVO.setManagedFlagsString("all");
		
	}
}
