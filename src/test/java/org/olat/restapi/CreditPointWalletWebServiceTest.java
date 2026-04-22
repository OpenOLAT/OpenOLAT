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
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.api.Assertions;
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
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.restapi.CreditPointWalletVO;
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
public class CreditPointWalletWebServiceTest extends OlatRestTestCase {
	
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
					.createOrganisation("Credit-wallet-unit-test", "Credit-wallet-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper.createAndPersistRndAdmin("Cred-wallet-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void getCreditPointWallets()
	throws IOException, URISyntaxException {
		// Make an administrator
		IdentityWithLogin user1 = JunitTestHelper.createAndPersistRndUser("wallet-1");
		IdentityWithLogin user2 = JunitTestHelper.createAndPersistRndUser("wallet-2");
		
		// Create a credit point system
		CreditPointSystem system = creditPointService.createCreditPointSystem("REST test coins", "RTC1",
				120, CreditPointExpirationType.DAY, false, false);
		// Create 2 wallets
		CreditPointWallet wallet1 = creditPointService.getOrCreateWallet(user1.getIdentity(), system);
		CreditPointWallet wallet2 = creditPointService.getOrCreateWallet(user2.getIdentity(), system);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("creditpoints")
				.path("system").path(system.getKey().toString())
				.path("wallets")
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		
		List<CreditPointWalletVO> wallets = conn.parseList(response, CreditPointWalletVO.class);
		Assert.assertNotNull(wallets);
		Assertions.assertThat(wallets)
			.hasSizeGreaterThanOrEqualTo(1)
			.allMatch(w -> BigDecimal.ZERO.compareTo(w.getBalance()) == 0)
			.map(CreditPointWalletVO::getKey)
			.containsExactlyInAnyOrder(wallet1.getKey(), wallet2.getKey());
	}
	
	@Test
	public void getCreditPointWalletByIdentity()
	throws IOException, URISyntaxException {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("wallet-3");

		// Create a credit point system
		CreditPointSystem system = creditPointService.createCreditPointSystem("REST test coins", "RTC1",
				120, CreditPointExpirationType.DAY, false, false);
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(user, system);
		dbInstance.commitAndCloseSession();
		
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null,
				"Depot", wallet, user, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("creditpoints")
				.path("system").path(system.getKey().toString())
				.path("wallets").path(user.getKey().toString())
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		
		CreditPointWalletVO walletVo = conn.parse(response, CreditPointWalletVO.class);
		Assert.assertNotNull(walletVo);
		Assert.assertEquals(wallet.getKey(), walletVo.getKey());
		Assert.assertEquals(user.getKey(), walletVo.getIdentityKey());
		Assert.assertEquals(system.getKey(), walletVo.getCreditPointSystemKey());
		Assert.assertNotNull(walletVo.getBalance());
		Assert.assertTrue(new BigDecimal("100").compareTo(walletVo.getBalance()) == 0);
	}
	
	/**
	 * The call doesn't create a wallet if it not exists. It returns an empty
	 * wallet with a balance of 0.00.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getCreditPointWalletForNonUser()
	throws IOException, URISyntaxException {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("wallet-4");

		// Create a credit point system
		CreditPointSystem system = creditPointService.createCreditPointSystem("REST test coins", "RTC1",
				120, CreditPointExpirationType.DAY, false, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("creditpoints")
				.path("system").path(system.getKey().toString())
				.path("wallets").path(user.getKey().toString())
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		
		CreditPointWalletVO walletVo = conn.parse(response, CreditPointWalletVO.class);
		Assert.assertNotNull(walletVo);
		Assert.assertNull(walletVo.getKey());
		Assert.assertEquals(user.getKey(), walletVo.getIdentityKey());
		Assert.assertEquals(system.getKey(), walletVo.getCreditPointSystemKey());
		Assert.assertNotNull(walletVo.getBalance());
		Assert.assertTrue(BigDecimal.ZERO.compareTo(walletVo.getBalance()) == 0);
	}
}
