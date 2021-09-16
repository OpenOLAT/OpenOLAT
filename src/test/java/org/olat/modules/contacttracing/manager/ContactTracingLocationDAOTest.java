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
package org.olat.modules.contacttracing.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ContactTracingLocationDAOTest extends OlatTestCase {
	
	private Random random = new Random();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ContactTracingRegistrationDAO registrationDao;
	@Autowired
	private ContactTracingLocationDAO sut;
	
	private boolean randomBoolean() {
		return random.nextInt(6) % 2 == 0;
	}

	@Test
	public void shouldCreateLocation() {
		String reference = random();
		String title = random();
		String building = random();
		String room = random();
		String sector = random();
		String table = random();
		boolean seatNumberEnabled = randomBoolean();
		String qrId = random();
		String qrText = random();
		boolean guestsAllowed = true;
		
		ContactTracingLocation location = sut.createAndPersistLocation(reference, title, building, room, sector, table, seatNumberEnabled, qrId, qrText, guestsAllowed);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(location.getKey()).isNotNull();
		softly.assertThat(location.getCreationDate()).isNotNull();
		softly.assertThat(location.getLastModified()).isNotNull();
		softly.assertThat(location.getReference()).isEqualTo(reference);
		softly.assertThat(location.getTitle()).isEqualTo(title);
		softly.assertThat(location.getBuilding()).isEqualTo(building);
		softly.assertThat(location.getRoom()).isEqualTo(room);
		softly.assertThat(location.getSector()).isEqualTo(sector);
		softly.assertThat(location.getTable()).isEqualTo(table);
		softly.assertThat(location.getQrId()).isEqualTo(qrId);
		softly.assertThat(location.getQrText()).isEqualTo(qrText);
		softly.assertThat(location.isAccessibleByGuests()).isEqualTo(guestsAllowed);
		softly.assertAll();
	}

	@Test
	public void shouldUpdateLocation() {
		ContactTracingLocation location = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();

		String reference = random();
		location.setReference(reference);
		String title = random();
		location.setTitle(title);
		String building = random();
		location.setBuilding(building);
		String room = random();
		location.setRoom(room);
		String sector = random();
		location.setSector(sector);
		String table = random();
		location.setTable(table);
		String qrId = random();
		location.setQrId(qrId);
		String qrText = random();
		location.setQrText(qrText);
		boolean guestsAllowed = true;
		location.setAccessibleByGuests(guestsAllowed);
		location = sut.updateLocation(location);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(location.getKey()).isNotNull();
		softly.assertThat(location.getCreationDate()).isNotNull();
		softly.assertThat(location.getLastModified()).isNotNull();
		softly.assertThat(location.getReference()).isEqualTo(reference);
		softly.assertThat(location.getTitle()).isEqualTo(title);
		softly.assertThat(location.getBuilding()).isEqualTo(building);
		softly.assertThat(location.getRoom()).isEqualTo(room);
		softly.assertThat(location.getSector()).isEqualTo(sector);
		softly.assertThat(location.getTable()).isEqualTo(table);
		softly.assertThat(location.getQrId()).isEqualTo(qrId);
		softly.assertThat(location.getQrText()).isEqualTo(qrText);
		softly.assertThat(location.isAccessibleByGuests()).isEqualTo(guestsAllowed);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByKey() {
		ContactTracingLocation location = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingLocation reloaded = sut.getLocation(location.getKey());
		
		assertThat(reloaded).isEqualTo(location);
	}
	
	@Test
	public void shouldLoadByIdentifier() {
		String qrId = random();
		ContactTracingLocation location = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), qrId, random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingLocation reloaded = sut.getLocation(qrId);
		
		assertThat(reloaded).isEqualTo(location);
	}
	
	@Test
	public void shouldCheckIfQrIdExists() {
		String qrId = random();
		sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), qrId, random(), false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.qrIdExists(qrId)).isTrue();
		softly.assertThat(sut.qrIdExists("NOT_EXISTING_QR_ID")).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteLocations() {
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingLocation locationDeleted1 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingLocation locationDeleted2 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		sut.deleteLocations(List.of(locationDeleted1, locationDeleted2));
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getLocation(location1.getKey())).isNotNull();
		softly.assertThat(sut.getLocation(location2.getKey())).isNotNull();
		softly.assertThat(sut.getLocation(locationDeleted1.getKey())).isNull();
		softly.assertThat(sut.getLocation(locationDeleted2.getKey())).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterByReference() {
		String reference = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation("fuzzy " + reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setReference(reference);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterByTitle() {
		String title = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), title, random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), "fuzzy " + title, random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(random(), title, random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setTitle(title);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterByBuilding() {
		String building = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), random(), building, random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), "fuzzy " + building, random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(random(), random(), building, random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setBuilding(building);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterByRoom() {
		String room = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), random(), random(), room, random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), random(), "fuzzy " + room, random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(random(), random(), random(), room, random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setRoom(room);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterBySector() {
		String sector = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), random(), random(), random(), sector, random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), random(), random(), "fuzzy " + sector, random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(random(), random(), random(), random(), sector, random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setSector(sector);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterByTable() {
		String table = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), table, randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), random(), random(), random(), "fuzzy " + table, randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(random(), random(), random(), random(), random(), table, randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setTable(table);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
	@Test
	public void shouldFilterByStartDate() {
		String reference = random();
		ContactTracingLocation locationEarlier = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationEarlier, DateUtils.addHours(new Date(), -1), new Date(), ImmunityProofLevel.none, null));
		registrationDao.persist(registrationDao.create(locationEarlier, DateUtils.addHours(new Date(), -2), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationLater = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationLater, DateUtils.addHours(new Date(), 1), new Date(), ImmunityProofLevel.none, null));
		registrationDao.persist(registrationDao.create(locationLater, DateUtils.addHours(new Date(), 2), new Date(), ImmunityProofLevel.none, null));
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setReference(reference);
		params.setStartDate(new Date());
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(locationLater)
				.doesNotContain(locationEarlier);
	}
	
	@Test
	public void shouldFilterByEndDate() {
		String reference = random();
		ContactTracingLocation locationEarlier = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = registrationDao.create(locationEarlier, new Date(), new Date(), ImmunityProofLevel.none, null);
		registration11.setEndDate(DateUtils.addHours(new Date(), -1));
		registration11 = registrationDao.persist(registration11);
		ContactTracingRegistration registration12 = registrationDao.create(locationEarlier, new Date(), new Date(), ImmunityProofLevel.none, null);
		registration12.setEndDate(DateUtils.addHours(new Date(), -2));
		registration12 = registrationDao.persist(registration12);
		ContactTracingLocation locationLater = sut.createAndPersistLocation(reference, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration21 = registrationDao.create(locationLater, new Date(), new Date(), ImmunityProofLevel.none, null);
		registration21.setEndDate(DateUtils.addHours(new Date(), 1));
		registration21 = registrationDao.persist(registration21);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setReference(reference);
		params.setEndDate(new Date());
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(locationEarlier)
				.doesNotContain(locationLater);
	}
	
	@Test
	public void shouldFilterByFullText() {
		String fulltext = random();
		ContactTracingLocation location1 = sut.createAndPersistLocation(fulltext, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location1, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation location2 = sut.createAndPersistLocation(random(), random(), "fuzzy " + fulltext, random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(location2, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationOther = sut.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		registrationDao.persist(registrationDao.create(locationOther, new Date(), new Date(), ImmunityProofLevel.none, null));
		ContactTracingLocation locationNoRegistration = sut.createAndPersistLocation(fulltext, random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams params = new ContactTracingSearchParams();
		params.setFullTextSearch(fulltext);
		List<ContactTracingLocation> locations = sut.getLocations(params);
		
		assertThat(locations)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContain(locationOther, locationNoRegistration);
	}
	
}
