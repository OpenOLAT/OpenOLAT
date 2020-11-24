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
package org.olat.resource.accesscontrol.provider.auto.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder.Status;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderInput;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.resource.accesscontrol.provider.auto.model.AdvanceOrderImpl;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;

/**
 *
 * Initial date: 14.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AutoAccessManagerImplTest {

	private static final Class<? extends AutoAccessMethod> METHOD_CLASS = AutoAccessMethod.class;
	private static final IdentityImpl IDENTITY = new IdentityImpl();
	private static final String FIRST_VALUE = "firstValue";
	private static final String SECOND_VALUE = "secondvalue";
	private static final String THIRD_VALUE = "third value";
	private static final String DISPLAY_NAME = "displayName";

	@Mock
	private IdentifierHandler identifierHandlerMock;
	@Mock
	private InputValidator inputValidator;
	@Mock
	private SplitterFactory splitterFactory;
	@Mock
	private IdentifierValueSplitter splitterMock;
	@Mock
	private AdvanceOrderDAO advanceOrderDaoMock;
	@Mock
	private ACService acServiceMock;
	@Mock
	private AccessControlModule acModuleMock;
	@Mock
	private AdvanceOrderInput advanceOrderInputMock;
	@Mock
	private RepositoryEntryRelationDAO repositoryEntryRelationDaoMock;
	@Mock
	private RepositoryEntry repositoryEntryMock;
	private List<RepositoryEntry> listWithRespositotyEntryMock;
	@Mock
	private OLATResource resourceMock;

	private AutoAccessMethod accessMethodDummy;

	@InjectMocks
	private AutoAccessManagerImpl sut = new  AutoAccessManagerImpl();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		accessMethodDummy = mock(AutoAccessMethod.class);
		List<AccessMethod> accessMethods = Arrays.asList(accessMethodDummy);
		when(acServiceMock.getAvailableMethodsByType(AutoAccessMethod.class)).thenReturn(accessMethods);

		doReturn(METHOD_CLASS).when(advanceOrderInputMock).getMethodClass();
		when(advanceOrderInputMock.getIdentity()).thenReturn(IDENTITY);
		Set<IdentifierKey> keys = new HashSet<>(Arrays.asList(IdentifierKey.internalId, IdentifierKey.externalId));
		when(advanceOrderInputMock.getKeys()).thenReturn(keys);
		when(advanceOrderInputMock.getSplitterType()).thenReturn("splitter");

		when(acModuleMock.isAutoEnabled()).thenReturn(true);

		when(inputValidator.isValid(advanceOrderInputMock)).thenReturn(true);

		when(splitterFactory.getSplitter(anyString())).thenReturn(splitterMock);
		List<String> values = Arrays.asList(FIRST_VALUE, SECOND_VALUE, THIRD_VALUE);
		when(splitterMock.split(isNull())).thenReturn(values);

		when(repositoryEntryMock.getDisplayname()).thenReturn(DISPLAY_NAME);
		when(repositoryEntryMock.getOlatResource()).thenReturn(resourceMock);
		listWithRespositotyEntryMock = new ArrayList<>();
		listWithRespositotyEntryMock.add(repositoryEntryMock);
	}

	private Collection<AdvanceOrder> getPendingAdvanceOrders() {
		Collection<AdvanceOrder> advanceOrders;
		AdvanceOrderImpl ao1 = new AdvanceOrderImpl();
		ao1.setIdentifierKey(IdentifierKey.internalId);
		ao1.setIdentifierValue("abc");
		ao1.setStatus(Status.PENDING);
		ao1.setMethod(accessMethodDummy);
		ao1.setIdentity(IDENTITY);
		AdvanceOrderImpl ao2 = new AdvanceOrderImpl();
		ao2.setIdentifierKey(IdentifierKey.internalId);
		ao2.setIdentifierValue("abc3");
		ao2.setStatus(Status.PENDING);
		ao2.setMethod(accessMethodDummy);
		ao2.setIdentity(IDENTITY);
		advanceOrders = Arrays.asList(ao1, ao2);
		return advanceOrders;
	}

	@Test
	public void shouldNotCreatAdvanceEntriesIfInputIsNotValid() {
		when(inputValidator.isValid(advanceOrderInputMock)).thenReturn(false);

		sut.createAdvanceOrders(advanceOrderInputMock);

		verify(advanceOrderDaoMock, never()).create(any(Identity.class), any(IdentifierKey.class), anyString(), any(AutoAccessMethod.class));
	}

	@Test
	public void shouldAddAnAdvanceEntryForEveryKeyValueCombination() {
		sut.createAdvanceOrders(advanceOrderInputMock);

		verify(advanceOrderDaoMock, times(6)).create(any(Identity.class), any(IdentifierKey.class), anyString(), any(AutoAccessMethod.class));
	}

	@Test
	public void shouldNotGrantAccessIfAlreadyDone() {
		Collection<AdvanceOrder> advanceOrders = new ArrayList<>();
		AdvanceOrderImpl doneAdvanceOrder = new AdvanceOrderImpl();
		doneAdvanceOrder.setStatus(Status.DONE);
		doneAdvanceOrder.setIdentifierKey(IdentifierKey.externalId);
		doneAdvanceOrder.setIdentifierValue("abc");
		advanceOrders.add(doneAdvanceOrder);

		sut.grantAccess(advanceOrders);

		verify(identifierHandlerMock, never()).findRepositoryEntries(any(IdentifierKey.class), anyString());
	}

	@Test
	public void shouldNotGrantAccessIfAutoAccessDisabled() {
		when(acModuleMock.isAutoEnabled()).thenReturn(false);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(identifierHandlerMock, never()).findRepositoryEntries(any(IdentifierKey.class), anyString());
	}

	@Test
	public void shouldNotGrantAccessIfNoResourceFound() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(null);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(acServiceMock, never()).createOffer(any(OLATResource.class), anyString());
		verify(acServiceMock, never()).createOfferAccess(any(Offer.class), any(AccessMethod.class));
		verify(acServiceMock, never()).accessResource(any(Identity.class), any(OfferAccess.class), isNull());
	}

	@Test
	public void shouldMakeOfferBeforeGrantingAccessIfNotExists() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(listWithRespositotyEntryMock);
		when(repositoryEntryRelationDaoMock.hasRole(IDENTITY, repositoryEntryMock, GroupRoles.participant.name())).thenReturn(false);
		when(acServiceMock.getValidOfferAccess(any(OLATResource.class), any(AccessMethod.class))).thenReturn(new ArrayList<>());
		Offer offerMock = mock(Offer.class);
		when(acServiceMock.createOffer(any(OLATResource.class), anyString())).thenReturn(offerMock);
		List<AccessMethod> methods = Arrays.asList(accessMethodDummy);
		when(acServiceMock.getAvailableMethodsByType(METHOD_CLASS)).thenReturn(methods);

		Collection<AdvanceOrder> advanceOrders = getPendingAdvanceOrders();
		sut.grantAccess(advanceOrders);

		verify(acServiceMock, times(2)).createOffer(any(OLATResource.class), anyString());
		verify(acServiceMock, times(2)).createOfferAccess(any(Offer.class), isA(AccessMethod.class));
	}

	@Test
	public void shouldNotMakeOfferBeforeGrantingAccessIfOfferExists() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(listWithRespositotyEntryMock);
		OfferAccess offerAccessDummy = mock(OfferAccess.class);
		List<OfferAccess> offerAccess = Arrays.asList(offerAccessDummy);
		when(acServiceMock.getValidOfferAccess(any(OLATResource.class), any(AccessMethod.class))).thenReturn(offerAccess);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(acServiceMock, never()).createOffer(any(OLATResource.class), isNull());
		verify(acServiceMock, never()).createOfferAccess(any(Offer.class), any(AccessMethod.class));
	}

	@Test
	public void shouldGrantAccessIfNoAccess() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(listWithRespositotyEntryMock);
		OfferAccess offerAccessDummy = mock(OfferAccess.class);
		List<OfferAccess> offerAccess = Arrays.asList(offerAccessDummy);
		when(acServiceMock.getValidOfferAccess(any(OLATResource.class), any(AccessMethod.class))).thenReturn(offerAccess);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(acServiceMock, times(2)).accessResource(IDENTITY, offerAccessDummy, null);
	}

	@Test
	public void shouldNotGrantAccessIfHasAccess() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(listWithRespositotyEntryMock);
		when(repositoryEntryRelationDaoMock.hasRole(IDENTITY, repositoryEntryMock, GroupRoles.participant.name())).thenReturn(true);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(acServiceMock, never()).accessResource(any(Identity.class), any(OfferAccess.class), isNull());
	}

	@Test
	public void shouldMarkAccessOrderAsDone() {
		when(identifierHandlerMock.findRepositoryEntries(any(IdentifierKey.class), anyString())).thenReturn(listWithRespositotyEntryMock);
		OfferAccess offerAccessDummy = mock(OfferAccess.class);
		List<OfferAccess> offerAccess = Arrays.asList(offerAccessDummy);
		when(acServiceMock.getValidOfferAccess(any(OLATResource.class), any(AccessMethod.class))).thenReturn(offerAccess);

		sut.grantAccess(getPendingAdvanceOrders());

		verify(advanceOrderDaoMock, times(2)).accomplishAndSave(any(AdvanceOrder.class), anyBoolean());
	}

	@Test
	public void shouldFindAdvanceOrdersForEveryIdentifierKey() {
		Long key = 1L;
		when(resourceMock.getKey()).thenReturn(key);

		sut.loadPendingAdvanceOrders(repositoryEntryMock);

		verify(identifierHandlerMock, times(IdentifierKey.values().length)).getRepositoryEntryValue(any(IdentifierKey.class), any(RepositoryEntry.class));
	}
	
}
