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
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ContactTracingRegistrationDAOTest extends OlatTestCase {
	
	private Random random = new Random();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ContactTracingLocationDAO locationDao;
	@Autowired
	private ContactTracingRegistrationDAO sut;
	
	private boolean randomBoolean() {
		return random.nextInt(6) % 2 == 0;
	}
	
	@Test
	public void shouldCreateAndPersistRegistration() {
		ContactTracingLocation location = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		Date deletionDate = new Date();
		Date startDate = new Date();
		ContactTracingRegistration registration = sut.create(location, startDate, deletionDate);
		dbInstance.commitAndCloseSession();
		
		String city = random();
		registration.setCity(city);
		String email = random();
		registration.setEmail(email);
		Date endDate = new Date();
		registration.setEndDate(endDate);
		String extraAddressLine = random();
		registration.setExtraAddressLine(extraAddressLine);
		String firstName = random();
		registration.setFirstName(firstName);
		String genericEmail = random();
		registration.setGenericEmail(genericEmail);
		String institutionalEmail = random();
		registration.setInstitutionalEmail(institutionalEmail);
		String lastName = random();
		registration.setLastName(lastName);
		String mobilePhone = random();
		registration.setMobilePhone(mobilePhone);
		String nickName = random();
		registration.setNickName(nickName);
		String officePhone = random();
		registration.setOfficePhone(officePhone);
		String privatePhone = random();
		registration.setPrivatePhone(privatePhone);
		String zipCode = random();
		registration.setZipCode(zipCode);
		registration = sut.persist(registration);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(registration.getKey()).isNotNull();
		softly.assertThat(registration.getCreationDate()).isNotNull();
		softly.assertThat(registration.getStartDate()).isNotNull();
		softly.assertThat(registration.getEndDate()).isNotNull();
		softly.assertThat(registration.getDeletionDate()).isNotNull();
		softly.assertThat(registration.getCity()).isEqualTo(city);
		softly.assertThat(registration.getEmail()).isEqualTo(email);
		softly.assertThat(registration.getExtraAddressLine()).isEqualTo(extraAddressLine);
		softly.assertThat(registration.getFirstName()).isEqualTo(firstName);
		softly.assertThat(registration.getGenericEmail()).isEqualTo(genericEmail);
		softly.assertThat(registration.getInstitutionalEmail()).isEqualTo(institutionalEmail);
		softly.assertThat(registration.getLastName()).isEqualTo(lastName);
		softly.assertThat(registration.getMobilePhone()).isEqualTo(mobilePhone);
		softly.assertThat(registration.getNickName()).isEqualTo(nickName);
		softly.assertThat(registration.getOfficePhone()).isEqualTo(officePhone);
		softly.assertThat(registration.getPrivatePhone()).isEqualTo(privatePhone);
		softly.assertThat(registration.getZipCode()).isEqualTo(zipCode);
		softly.assertAll();
	}

	@Test
	public void shouldDeleteByLocations() {
		ContactTracingLocation location1 = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = sut.persist(sut.create(location1, new Date(), new Date()));
		ContactTracingRegistration registration12 = sut.persist(sut.create(location1, new Date(), new Date()));
		ContactTracingLocation location2 = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration21 = sut.persist(sut.create(location2, new Date(), new Date()));
		ContactTracingLocation locationNotDeleted = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registrationNotDeleted = sut.persist(sut.create(locationNotDeleted, new Date(), new Date()));
		dbInstance.commitAndCloseSession();
		
		sut.deleteEntries(List.of(location1, location2));
		dbInstance.commitAndCloseSession();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getByKey(registration11.getKey())).isNull();
		softly.assertThat(sut.getByKey(registration12.getKey())).isNull();
		softly.assertThat(sut.getByKey(registration21.getKey())).isNull();
		softly.assertThat(sut.getByKey(registrationNotDeleted.getKey())).isNotNull();
		softly.assertAll();
	}

	@Test
	public void shouldPruneRegistartions() {
		ContactTracingLocation location = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = sut.persist(sut.create(location, new Date(), DateUtils.addHours(new Date(), -1)));
		ContactTracingRegistration registration12 = sut.persist(sut.create(location, new Date(), DateUtils.addHours(new Date(), -2)));
		ContactTracingLocation location2 = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration21 = sut.persist(sut.create(location2, new Date(), DateUtils.addDays(new Date(), -1)));
		ContactTracingLocation locationPruned = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registrationNotPruned = sut.persist(sut.create(locationPruned, new Date(), DateUtils.addHours(new Date(), 11)));
		dbInstance.commitAndCloseSession();
		
		sut.pruneEntries(new Date());
		dbInstance.commitAndCloseSession();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getByKey(registration11.getKey())).isNull();
		softly.assertThat(sut.getByKey(registration12.getKey())).isNull();
		softly.assertThat(sut.getByKey(registration21.getKey())).isNull();
		softly.assertThat(sut.getByKey(registrationNotPruned.getKey())).isNotNull();
		softly.assertAll();
	}

	@Test
	public void shouldFilterByLocation() {
		ContactTracingLocation location1 = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = sut.persist(sut.create(location1, new Date(), new Date()));
		ContactTracingRegistration registration12 = sut.persist(sut.create(location1, new Date(), new Date()));
		ContactTracingLocation locationOther = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registrationOther = sut.persist(sut.create(locationOther, new Date(), new Date()));
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams searchParams = new ContactTracingSearchParams();
		searchParams.setLocation(location1);
		List<ContactTracingRegistration> registrations = sut.getRegistrations(searchParams);
		
		assertThat(registrations)
				.containsExactlyInAnyOrder(registration11, registration12)
				.doesNotContain(registrationOther);
	}
	
	@Test
	public void shouldFilterByStart() {
		ContactTracingLocation location = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = sut.persist(sut.create(location, DateUtils.addHours(new Date(), 1), new Date()));
		ContactTracingRegistration registration12 = sut.persist(sut.create(location, DateUtils.addHours(new Date(), 2), new Date()));
		ContactTracingRegistration registrationOther = sut.persist(sut.create(location, DateUtils.addHours(new Date(), -1), new Date()));
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams searchParams = new ContactTracingSearchParams();
		searchParams.setLocation(location);
		searchParams.setStartDate(new Date());
		List<ContactTracingRegistration> registrations = sut.getRegistrations(searchParams);
		
		assertThat(registrations)
				.containsExactlyInAnyOrder(registration11, registration12)
				.doesNotContain(registrationOther);
	}

	@Test
	public void shouldFilterByEnd() {
		ContactTracingLocation location = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		ContactTracingRegistration registration11 = sut.create(location, new Date(), new Date());
		registration11.setEndDate(DateUtils.addHours(new Date(), -1));
		registration11 = sut.persist(registration11);
		ContactTracingRegistration registration12 = sut.create(location, new Date(), new Date());
		registration12.setEndDate(DateUtils.addHours(new Date(), -2));
		registration12 = sut.persist(registration12);
		ContactTracingRegistration registrationOther = sut.create(location, new Date(), new Date());
		registrationOther.setEndDate(DateUtils.addHours(new Date(), 1));
		registrationOther = sut.persist(registrationOther);
		dbInstance.commitAndCloseSession();
		
		ContactTracingSearchParams searchParams = new ContactTracingSearchParams();
		searchParams.setLocation(location);
		searchParams.setEndDate(new Date());
		List<ContactTracingRegistration> registrations = sut.getRegistrations(searchParams);
		
		assertThat(registrations)
				.containsExactlyInAnyOrder(registration11, registration12)
				.doesNotContain(registrationOther);
	}
	
	@Test
	public void shouldTestIfAnyRegistrationAvailable() {
		ContactTracingLocation location1 = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), randomBoolean(), random(), random(), false);
		sut.persist(sut.create(location1, new Date(), DateUtils.addHours(new Date(), -1)));
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.anyRegistrationAvailable()).isTrue();
	}
	
	public void shouldAddSeatNumber() {
		ContactTracingLocation location = locationDao.createAndPersistLocation(random(), random(), random(), random(), random(), random(), true, random(), random(), false);
		
		ContactTracingRegistration registration1 = sut.create(location, new Date(), new Date());
		String registration1SeatNumber = "24";
		registration1.setSeatNumber(registration1SeatNumber);
		registration1 = sut.persist(registration1);
		
		ContactTracingRegistration registration2 = sut.create(location, new Date(), new Date());
		registration2 = sut.persist(registration2);
		
		ContactTracingSearchParams searchParams = new ContactTracingSearchParams();
		searchParams.setLocation(location);
		List<ContactTracingRegistration> registrations = sut.getRegistrations(searchParams);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(registrations).containsExactlyInAnyOrder(registration1, registration2);
		softly.assertThat(registration1.getSeatNumber().equals(registration1SeatNumber));
		softly.assertThat(registration2.getSeatNumber()).isNull();;
		softly.assertAll();
		
		dbInstance.commitAndCloseSession();
	}
}
