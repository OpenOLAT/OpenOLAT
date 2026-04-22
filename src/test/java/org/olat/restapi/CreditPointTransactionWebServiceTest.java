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
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.restapi.CreditPointTransactionVO;
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
public class CreditPointTransactionWebServiceTest extends OlatRestTestCase {

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
					.createOrganisation("Credit-transaction-unit-test", "Credit-transaction-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper.createAndPersistRndAdmin("Cred-transaction-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void addDeposit()
	throws IOException, URISyntaxException {
		// Make a curriculum manager
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("ooc-transaction-1");
		
		CreditPointSystem system = creditPointService.createCreditPointSystem("REST transact coins", "RTT1",
				120, CreditPointExpirationType.DAY, false, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		CreditPointTransactionVO depositTransaction = new CreditPointTransactionVO();
		depositTransaction.setAmount(new BigDecimal("100"));
		depositTransaction.setType("deposit");
		depositTransaction.setNote("Initial deposit");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("creditpoints")
				.path("system").path(system.getKey().toString())
				.path("transactions").path(user.getKey().toString())
				.build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, depositTransaction);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		CreditPointTransactionVO result = conn.parse(response, CreditPointTransactionVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals("deposit", result.getType());
		Assert.assertNotNull(result.getAmount());
		Assert.assertTrue(new BigDecimal("100").compareTo(result.getAmount()) == 0);
		Assert.assertEquals("Initial deposit", result.getNote());
	}
	
}
