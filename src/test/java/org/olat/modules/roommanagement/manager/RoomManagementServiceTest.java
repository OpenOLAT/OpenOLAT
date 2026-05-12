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
package org.olat.modules.roommanagement.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.model.CollisionReport;

/**
 * Initial date: 12 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomManagementServiceTest {

	@Mock private LocationDAO locationDao;
	@Mock private RoomBookingDAO roomBookingDao;

	@InjectMocks private RoomManagementServiceImpl sut;

	@Mock private Location location;
	@Mock private Roles roles;
	@Mock private Organisation org;

	// ========== canEditLocation ==========

	@Test
	public void canEditLocation_nullRoles_returnsFalse() {
		assertThat(sut.canEditLocation(location, null)).isFalse();
	}

	@Test
	public void canEditLocation_sysadmin_returnsTrue() {
		when(roles.isSystemAdmin()).thenReturn(true);

		assertThat(sut.canEditLocation(location, roles)).isTrue();
	}

	@Test
	public void canEditLocation_noOrgs_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of());

		assertThat(sut.canEditLocation(location, roles)).isTrue();
	}

	@Test
	public void canEditLocation_hasAdminRole_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(true);

		assertThat(sut.canEditLocation(location, roles)).isTrue();
	}

	@Test
	public void canEditLocation_noAdminRole_returnsFalse() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);

		assertThat(sut.canEditLocation(location, roles)).isFalse();
	}

	// ========== isVisibleLocation ==========

	@Test
	public void isVisibleLocation_nullRoles_noOrgs_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of());

		assertThat(sut.isVisibleLocation(location, null, null)).isTrue();
	}

	@Test
	public void isVisibleLocation_sysadmin_returnsTrue() {
		when(roles.isSystemAdmin()).thenReturn(true);

		assertThat(sut.isVisibleLocation(location, roles, null)).isTrue();
	}

	@Test
	public void isVisibleLocation_noOrgs_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of());

		assertThat(sut.isVisibleLocation(location, roles, null)).isTrue();
	}

	@Test
	public void isVisibleLocation_hasAdminRole_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(true);

		assertThat(sut.isVisibleLocation(location, roles, null)).isTrue();
	}

	@Test
	public void isVisibleLocation_hasUserRole_returnsTrue() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);
		when(roles.hasRole(anyList(), eq(OrganisationRoles.user))).thenReturn(true);

		assertThat(sut.isVisibleLocation(location, roles, null)).isTrue();
	}

	@Test
	public void isVisibleLocation_noMatchingRole_returnsFalse() {
		when(locationDao.getOrganisations(location)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);
		when(roles.hasRole(anyList(), eq(OrganisationRoles.user))).thenReturn(false);

		assertThat(sut.isVisibleLocation(location, roles, null)).isFalse();
	}

	// ========== findCollisions ==========

	@Test
	public void findCollisions_hardOverlap_reportedAsHard() {
		RoomRef room = mock(RoomRef.class);
		RoomBooking booking = mock(RoomBooking.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		when(roomBookingDao.findHardOverlapping(room, start, end, null)).thenReturn(List.of(booking));

		CollisionReport report = sut.findCollisions(room, start, end, 0, 0, null);

		assertThat(report.getHard()).containsExactly(booking);
		assertThat(report.getBuffer()).isEmpty();
	}

	@Test
	public void findCollisions_noBuffer_bufferQuerySkipped() {
		RoomRef room = mock(RoomRef.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		when(roomBookingDao.findHardOverlapping(room, start, end, null)).thenReturn(List.of());

		sut.findCollisions(room, start, end, 0, 0, null);

		verify(roomBookingDao, never()).findBufferOverlapping(any(), any(), any(), any(), any(), any());
	}

	@Test
	public void findCollisions_withBuffer_expandsInterval() {
		RoomRef room = mock(RoomRef.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

		sut.findCollisions(room, start, end, 15, 10, null);

		ArgumentCaptor<Date> sPrimeCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Date> ePrimeCaptor = ArgumentCaptor.forClass(Date.class);
		verify(roomBookingDao).findBufferOverlapping(
				eq(room), eq(start), eq(end),
				sPrimeCaptor.capture(), ePrimeCaptor.capture(),
				isNull());
		assertThat(sPrimeCaptor.getValue()).isEqualTo(DateUtils.addMinutes(start, -15));
		assertThat(ePrimeCaptor.getValue()).isEqualTo(DateUtils.addMinutes(end, 10));
	}

	@Test
	public void findCollisions_bufferOverlap_reportedAsBuffer() {
		RoomRef room = mock(RoomRef.class);
		RoomBooking booking = mock(RoomBooking.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of(booking));

		CollisionReport report = sut.findCollisions(room, start, end, 15, 0, null);

		assertThat(report.getHard()).isEmpty();
		assertThat(report.getBuffer()).containsExactly(booking);
		assertThat(report.hasCollisions()).isTrue();
	}

	// ========== copyBookingsForLectureBlock ==========

	@Test
	public void copyBookingsForLectureBlock_managedFlag_returnsZero() {
		LectureBlock source = mock(LectureBlock.class);
		LectureBlock target = mock(LectureBlock.class);

		try (MockedStatic<LectureBlockManagedFlag> flagMock = mockStatic(LectureBlockManagedFlag.class)) {
			flagMock.when(() -> LectureBlockManagedFlag.isManaged(source, LectureBlockManagedFlag.room))
					.thenReturn(true);

			int copied = sut.copyBookingsForLectureBlock(source, target, null);

			assertThat(copied).isEqualTo(0);
			verify(roomBookingDao, never()).getBookingsForLectureBlock(any());
		}
	}

	@Test
	public void copyBookingsForLectureBlock_noBookings_returnsZero() {
		LectureBlock source = mock(LectureBlock.class);
		LectureBlock target = mock(LectureBlock.class);
		when(roomBookingDao.getBookingsForLectureBlock(source)).thenReturn(List.of());

		try (MockedStatic<LectureBlockManagedFlag> flagMock = mockStatic(LectureBlockManagedFlag.class)) {
			flagMock.when(() -> LectureBlockManagedFlag.isManaged(source, LectureBlockManagedFlag.room))
					.thenReturn(false);

			int copied = sut.copyBookingsForLectureBlock(source, target, null);

			assertThat(copied).isEqualTo(0);
		}
	}
}
