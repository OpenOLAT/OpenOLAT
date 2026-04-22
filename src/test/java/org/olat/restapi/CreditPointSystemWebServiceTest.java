/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.restapi.CreditPointSystemVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private OrganisationService organisationService;
	
	private static IdentityWithLogin defaultUnitTestAdministrator;
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Credit-point-unit-test", "Credit-point-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper.createAndPersistRndAdmin("Cred-point-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void getCreditPointSystems()
	throws IOException, URISyntaxException {
		// Make a curriculum manager
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("coins-1");
		organisationService.addMember(defaultUnitTestOrganisation, user.getIdentity(), OrganisationRoles.curriculummanager,
				defaultUnitTestAdministrator.getIdentity());
		
		CreditPointSystem system = creditPointService.createCreditPointSystem("REST test coins", "RTC1",
				120, CreditPointExpirationType.DAY, false, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(user);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("creditpoints").path("system").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		List<CreditPointSystemVO> systems = conn.parseList(response, CreditPointSystemVO.class);
		Assert.assertNotNull(systems);
		Assertions.assertThat(systems)
			.hasSizeGreaterThanOrEqualTo(1);
		
		Optional<CreditPointSystemVO> loadedSystem = systems.stream()
				.filter(sys -> system.getKey().equals(sys.getKey()))
				.findFirst();
		Assert.assertTrue(loadedSystem.isPresent());
		Assert.assertEquals("REST test coins", loadedSystem.get().getName());
		Assert.assertEquals("RTC1", loadedSystem.get().getLabel());
		Assert.assertEquals(Integer.valueOf(120), loadedSystem.get().getDefaultExpiration());
		Assert.assertEquals(CreditPointExpirationType.DAY.name(), loadedSystem.get().getDefaultExpirationUnit());
		Assert.assertEquals(CreditPointSystemStatus.active.name(), loadedSystem.get().getStatus());
		Assert.assertEquals(Boolean.FALSE, loadedSystem.get().getRolesRestrictions());
		Assert.assertEquals(Boolean.FALSE, loadedSystem.get().getOrganisationsRestrictions());
	}
}
