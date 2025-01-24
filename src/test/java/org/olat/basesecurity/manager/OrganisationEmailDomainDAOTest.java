/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationIdentityEmail;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationEmailDomainDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private OrganisationEmailDomainDAO sut;

	@Test
	public void shouldCreate() {
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		
		OrganisationEmailDomain emailDomain = sut.create(organisation, random());
		dbInstance.commitAndCloseSession();
		
		assertThat(emailDomain.getCreationDate()).isNotNull();
		assertThat(emailDomain.getLastModified()).isNotNull();
		assertThat(emailDomain.isEnabled()).isTrue();
		assertThat(emailDomain.isSubdomainsAllowed()).isFalse();
	}
	
	@Test
	public void shouldDeleteByKey() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain1 = sut.create(organisation, random());
		OrganisationEmailDomain emailDomain2 = sut.create(organisation, random());
		OrganisationEmailDomain emailDomain3 = sut.create(organisation, random());
		dbInstance.commitAndCloseSession();
		
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setOrganisations(List.of(organisation));
		List<OrganisationEmailDomain> emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain1, emailDomain2, emailDomain3);
		
		sut.delete(emailDomain1);
		dbInstance.commitAndCloseSession();
		
		emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain2, emailDomain3);
	}
	
	@Test
	public void shouldDeleteByOrganisation() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain11 = sut.create(organisation1, random());
		OrganisationEmailDomain emailDomain12 = sut.create(organisation1, random());
		OrganisationEmailDomain emailDomain21 = sut.create(organisation2, random());
		dbInstance.commitAndCloseSession();
		
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setOrganisations(List.of(organisation1, organisation2));
		List<OrganisationEmailDomain> emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain11, emailDomain12, emailDomain21);
		
		sut.delete(organisation2);
		dbInstance.commitAndCloseSession();
		
		emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain11, emailDomain12);
	}
	
	@Test
	public void shouldFiler_organisations() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation3 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain11 = sut.create(organisation1, random());
		OrganisationEmailDomain emailDomain12 = sut.create(organisation1, random());
		OrganisationEmailDomain emailDomain21 = sut.create(organisation2, random());
		sut.create(organisation3, random());
		dbInstance.commitAndCloseSession();
		
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setOrganisations(List.of(organisation1, organisation2));
		List<OrganisationEmailDomain> emailDomains = sut.loadEmailDomains(searchParams);
		
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain11, emailDomain12, emailDomain21);
	}
	
	@Test
	public void shouldFiler_domains() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain1 = sut.create(organisation, random());
		OrganisationEmailDomain emailDomain2 = sut.create(organisation, random());
		OrganisationEmailDomain emailDomain3 = sut.create(organisation, random());
		dbInstance.commitAndCloseSession();
		
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setOrganisations(List.of(organisation));
		List<OrganisationEmailDomain> emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain1, emailDomain2, emailDomain3);
		
		searchParams.setDomains(List.of(emailDomain2.getDomain(), emailDomain3.getDomain()));
		emailDomains = sut.loadEmailDomains(searchParams);
		assertThat(emailDomains).containsExactlyInAnyOrder(emailDomain2, emailDomain3);
	}
	
	@Test
	public void shouldFiler_enabled() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain1 = sut.create(organisation, random());
		OrganisationEmailDomain emailDomain2 = sut.create(organisation, random());
		emailDomain2.setEnabled(false);
		emailDomain2 = sut.update(emailDomain2);
		dbInstance.commitAndCloseSession();
		
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setOrganisations(List.of(organisation));
		assertThat(sut.loadEmailDomains(searchParams)).containsExactlyInAnyOrder(emailDomain1, emailDomain2);
		
		searchParams.setEnabled(Boolean.TRUE);
		assertThat(sut.loadEmailDomains(searchParams)).containsExactlyInAnyOrder(emailDomain1);
		
		searchParams.setEnabled(Boolean.FALSE);
		assertThat(sut.loadEmailDomains(searchParams)).containsExactlyInAnyOrder(emailDomain2);
	}
	
	@Test
	public void shouldGetOrganisationIdentityEmails() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation3 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		// Regular users
		createUser(organisation1, random(), "openolat.com");
		createUser(organisation1, random(), "openolat.com");
		createUser(organisation2, random(), "openolat.com");
		// Organisation not in query
		 createUser(organisation3, random(), "openolat.com");
		// Not user role
		Identity identityAuthor = createUser(organisation1, random(), "openolat.com");
		organisationService.addMember(organisation1, identityAuthor, OrganisationRoles.administrator, JunitTestHelper.getDefaultActor());
		organisationService.removeMember(organisation1, identityAuthor, OrganisationRoles.user, true, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		List<OrganisationIdentityEmail> organisationIdentityEmails = sut
				.getOrganisationIdentityEmails(List.of(organisation1.getKey(), organisation2.getKey()));
		
		assertThat(organisationIdentityEmails).hasSize(3);
	}
	
	private Identity createUser(Organisation organisation, String login, String mailDomain) {
		User user = userManager.createUser("orged" + login, "orged" + login, login + "@" + mailDomain);
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				null, null, null, null, null, organisation, null, JunitTestHelper.getDefaultActor());
	}


}
