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
package org.olat.resource.accesscontrol.provider.auto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder.Status;
import org.olat.resource.accesscontrol.provider.auto.manager.AdvanceOrderDAO;
import org.olat.resource.accesscontrol.provider.auto.manager.SemicolonSplitter;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;
import org.olat.shibboleth.manager.ShibbolethAutoAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AutoAccessManagerTest extends OlatTestCase {
	

	private AccessMethod accessMethod;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AdvanceOrderDAO advanceOrderDAO;
	@Autowired
	private ACMethodDAO acMethodDAO;
	@Autowired
	private AccessControlModule acModule;
	
	@Autowired
	private AutoAccessManager sut;
	
	@Before
	public void setUp() {
		acMethodDAO.enableMethod(ShibbolethAutoAccessMethod.class, true);
		List<AccessMethod> freeMethods = acMethodDAO.getAvailableMethodsByType(ShibbolethAutoAccessMethod.class);
		accessMethod = freeMethods.get(0);
	}
	
	@Test
	public void shouldCreateAdvanceOrder() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String value1 = random();
		String value2 = random();
		List<String> values = List.of(value1, value2);
		String rawValue = values.stream().collect(Collectors.joining(";"));
		String splitterType = SemicolonSplitter.TYPE;
		
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, rawValue, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(advanceOrders).hasSize(2);
		for (AdvanceOrder advanceOrder : advanceOrders) {
			softly.assertThat(advanceOrder.getIdentity()).isEqualTo(identity);
			softly.assertThat(advanceOrder.getIdentifierKey()).isEqualTo(IdentifierKey.externalId);
			softly.assertThat(advanceOrder.getIdentifierValue()).isIn(values);
			softly.assertThat(advanceOrder.getMethod()).isEqualTo(accessMethod);
			softly.assertThat(advanceOrder.getStatus()).isEqualTo(Status.PENDING);
		}
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateAdvanceOrderOnlyOnce() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String value1 = random();
		String splitterType = SemicolonSplitter.TYPE;
		
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, value1, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		assertThat(advanceOrders).hasSize(1);
	}
	
	@Test
	public void shouldCreateAdvanceOrderReuseCanceled() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String value1 = random();
		String splitterType = SemicolonSplitter.TYPE;
		
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, value1, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		for (AdvanceOrder advanceOrder : sut.loadAdvanceOrders(searchParams)) {
			if (advanceOrder.getIdentifierValue().equals(value1)) {
				advanceOrder.setStatus(Status.CANCELED);
				advanceOrderDAO.save(advanceOrder);
			}
		}
		dbInstance.commitAndCloseSession();
		
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(advanceOrders).hasSize(1);
		AdvanceOrder advanceOrder = advanceOrders.stream().limit(1).findAny().get();
		softly.assertThat(advanceOrder.getStatus()).isEqualTo(Status.CANCELED);
		
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		advanceOrders = sut.loadAdvanceOrders(searchParams);
		softly.assertThat(advanceOrders).hasSize(1);
		advanceOrder = advanceOrders.stream().limit(1).findAny().get();
		softly.assertThat(advanceOrder.getStatus()).isEqualTo(Status.PENDING);
		softly.assertAll();
	}

	@Test
	public void shouldCreateAdvanceOrderNotReuseDone() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String value1 = random();
		String splitterType = SemicolonSplitter.TYPE;
		
		// Create advance order
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, value1, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		// Make booking
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		for (AdvanceOrder advanceOrder : sut.loadAdvanceOrders(searchParams)) {
			if (advanceOrder.getIdentifierValue().equals(value1)) {
				advanceOrder.setStatus(Status.DONE);
				advanceOrderDAO.save(advanceOrder);
			}
		}
		dbInstance.commitAndCloseSession();
		
		// Verify
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(advanceOrders).hasSize(1);
		AdvanceOrder advanceOrder = advanceOrders.stream().limit(1).findAny().get();
		softly.assertThat(advanceOrder.getStatus()).isEqualTo(Status.DONE);
		
		// Try to create advance order again
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		// verify
		advanceOrders = sut.loadAdvanceOrders(searchParams);
		softly.assertThat(advanceOrders).hasSize(1);
		advanceOrder = advanceOrders.stream().limit(1).findAny().get();
		softly.assertThat(advanceOrder.getStatus()).isEqualTo(Status.DONE);
		softly.assertAll();
	}
	
	@Test
	public void shouldCancelAdvanceOrder() {
		acModule.setAutoCancelation(true);
		acModule.setAutoMultiBooking(false);
		
		// Create 3 advance orders
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String valuePending = random();
		String valuePendingCanceled = random();
		String valueDoneCanceled = random();
		List<String> values = List.of(valuePending, valuePendingCanceled, valueDoneCanceled);
		String rawValue = values.stream().collect(Collectors.joining(";"));
		String splitterType = SemicolonSplitter.TYPE;
		
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, rawValue, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		for (AdvanceOrder advanceOrder : sut.loadAdvanceOrders(searchParams)) {
			if (advanceOrder.getIdentifierValue().equals(valueDoneCanceled)) {
				advanceOrder.setStatus(Status.DONE);
				advanceOrderDAO.save(advanceOrder);
			}
		}
		dbInstance.commitAndCloseSession();
		
		// Again, but only 1 of 3 keys
		AdvanceOrderInput input2 = new TestingAdvanceOrderInput(identity, keys, valuePending, splitterType);
		sut.createAdvanceOrders(input2);
		dbInstance.commitAndCloseSession();
		
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(advanceOrders).hasSize(3);
		AdvanceOrder advanceOrderPending = advanceOrders.stream().filter(ao -> valuePending.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderPending.getStatus()).isEqualTo(Status.PENDING);
		AdvanceOrder advanceOrderPendingCanceled = advanceOrders.stream().filter(ao -> valuePendingCanceled.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderPendingCanceled.getStatus()).isEqualTo(Status.CANCELED);
		AdvanceOrder advanceOrderDoneCanceled = advanceOrders.stream().filter(ao -> valueDoneCanceled.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderDoneCanceled.getStatus()).isEqualTo(Status.CANCELED);
		softly.assertAll();
	}
	
	@Test
	public void shouldNotCancelIfDisabled() {
		acModule.setAutoCancelation(false);
		acModule.setAutoMultiBooking(false);
		
		// Create 3 advance orders
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Set<IdentifierKey> keys = Set.of(IdentifierKey.externalId);
		String valuePending = random();
		String valuePendingCanceled = random();
		String valueDoneCanceled = random();
		List<String> values = List.of(valuePending, valuePendingCanceled, valueDoneCanceled);
		String rawValue = values.stream().collect(Collectors.joining(";"));
		String splitterType = SemicolonSplitter.TYPE;
		
		AdvanceOrderInput input = new TestingAdvanceOrderInput(identity, keys, rawValue, splitterType);
		sut.createAdvanceOrders(input);
		dbInstance.commitAndCloseSession();
		
		AdvanceOrderSearchParams searchParams = new AdvanceOrderSearchParams();
		searchParams.setIdentitfRef(identity);
		for (AdvanceOrder advanceOrder : sut.loadAdvanceOrders(searchParams)) {
			if (advanceOrder.getIdentifierValue().equals(valueDoneCanceled)) {
				advanceOrder.setStatus(Status.DONE);
				advanceOrderDAO.save(advanceOrder);
			}
		}
		dbInstance.commitAndCloseSession();
		
		// Again, but only 1 of 3 keys
		AdvanceOrderInput input2 = new TestingAdvanceOrderInput(identity, keys, valuePending, splitterType);
		sut.createAdvanceOrders(input2);
		dbInstance.commitAndCloseSession();
		
		Collection<AdvanceOrder> advanceOrders = sut.loadAdvanceOrders(searchParams);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(advanceOrders).hasSize(3);
		AdvanceOrder advanceOrderPending = advanceOrders.stream().filter(ao -> valuePending.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderPending.getStatus()).isEqualTo(Status.PENDING);
		AdvanceOrder advanceOrderPendingCanceled = advanceOrders.stream().filter(ao -> valuePendingCanceled.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderPendingCanceled.getStatus()).isEqualTo(Status.PENDING);
		AdvanceOrder advanceOrderDoneCanceled = advanceOrders.stream().filter(ao -> valueDoneCanceled.equals(ao.getIdentifierValue())).findFirst().get();
		softly.assertThat(advanceOrderDoneCanceled.getStatus()).isEqualTo(Status.DONE);
		softly.assertAll();
	}

	private final static class TestingAdvanceOrderInput implements AdvanceOrderInput {

		private final Identity identity;
		private final Set<IdentifierKey> keys;
		private final String rawValue;
		private final String splitterType;

		public TestingAdvanceOrderInput(Identity identity, Set<IdentifierKey> keys, String rawValue, String splitterType) {
			this.identity = identity;
			this.keys = keys;
			this.rawValue = rawValue;
			this.splitterType = splitterType;
		}

		@Override
		public Class<? extends AutoAccessMethod> getMethodClass() {
			return ShibbolethAutoAccessMethod.class;
		}

		@Override
		public Identity getIdentity() {
			return identity;
		}

		@Override
		public Set<IdentifierKey> getKeys() {
			return keys;
		}

		@Override
		public String getRawValues() {
			return rawValue;
		}

		@Override
		public String getSplitterType() {
			return splitterType;
		}
		
	}

}
