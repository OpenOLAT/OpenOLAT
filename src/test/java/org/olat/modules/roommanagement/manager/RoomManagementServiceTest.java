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
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.model.CollisionReport;

/**
 * Initial date: 12 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomManagementServiceTest {

	@Mock private BuildingDAO buildingDao;
	@Mock private RoomBookingDAO roomBookingDao;

	@InjectMocks private RoomManagementServiceImpl sut;

	@Mock private Building building;
	@Mock private Roles roles;
	@Mock private Organisation org;

	// ========== canEditBuilding ==========

	@Test
	public void canEditBuilding_nullRoles_returnsFalse() {
		assertThat(sut.canEditBuilding(building, null)).isFalse();
	}

	@Test
	public void canEditBuilding_sysadmin_returnsTrue() {
		when(roles.isSystemAdmin()).thenReturn(true);

		assertThat(sut.canEditBuilding(building, roles)).isTrue();
	}

	@Test
	public void canEditBuilding_noOrgs_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of());

		assertThat(sut.canEditBuilding(building, roles)).isTrue();
	}

	@Test
	public void canEditBuilding_hasAdminRole_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(true);

		assertThat(sut.canEditBuilding(building, roles)).isTrue();
	}

	@Test
	public void canEditBuilding_noAdminRole_returnsFalse() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);

		assertThat(sut.canEditBuilding(building, roles)).isFalse();
	}

	// ========== isVisibleBuilding ==========

	@Test
	public void isVisibleBuilding_nullRoles_noOrgs_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of());

		assertThat(sut.isVisibleBuilding(building, null, null)).isTrue();
	}

	@Test
	public void isVisibleBuilding_sysadmin_returnsTrue() {
		when(roles.isSystemAdmin()).thenReturn(true);

		assertThat(sut.isVisibleBuilding(building, roles, null)).isTrue();
	}

	@Test
	public void isVisibleBuilding_noOrgs_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of());

		assertThat(sut.isVisibleBuilding(building, roles, null)).isTrue();
	}

	@Test
	public void isVisibleBuilding_hasAdminRole_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(true);

		assertThat(sut.isVisibleBuilding(building, roles, null)).isTrue();
	}

	@Test
	public void isVisibleBuilding_hasUserRole_returnsTrue() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);
		when(roles.hasRole(anyList(), eq(OrganisationRoles.user))).thenReturn(true);

		assertThat(sut.isVisibleBuilding(building, roles, null)).isTrue();
	}

	@Test
	public void isVisibleBuilding_noMatchingRole_returnsFalse() {
		when(buildingDao.getOrganisations(building)).thenReturn(List.of(org));
		when(roles.hasRole(anyList(), eq(OrganisationRoles.administrator))).thenReturn(false);
		when(roles.hasRole(anyList(), eq(OrganisationRoles.user))).thenReturn(false);

		assertThat(sut.isVisibleBuilding(building, roles, null)).isFalse();
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
	public void findCollisions_withBuffer_expandsPreFetchWindow() {
		// Pre-fetch window must be [start - 2*bBefore, end + 2*bAfter] so that existing
		// bookings whose own buffer zones extend into the candidate's buffer envelope are fetched.
		RoomRef room = mock(RoomRef.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

		sut.findCollisions(room, start, end, 15, 10, null);

		ArgumentCaptor<Date> preFetchStartCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Date> preFetchEndCaptor = ArgumentCaptor.forClass(Date.class);
		verify(roomBookingDao).findBufferOverlapping(
				eq(room), eq(start), eq(end),
				preFetchStartCaptor.capture(), preFetchEndCaptor.capture(),
				isNull());
		assertThat(preFetchStartCaptor.getValue()).isEqualTo(DateUtils.addMinutes(start, -30));
		assertThat(preFetchEndCaptor.getValue()).isEqualTo(DateUtils.addMinutes(end, 20));
	}

	@Test
	public void findCollisions_bufferOverlap_reportedAsBuffer() {
		RoomRef room = mock(RoomRef.class);
		RoomBooking booking = mock(RoomBooking.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		// booking hard interval sits inside [sPrime, ePrime]; buffers=0 so bEnv == hard interval
		when(booking.getStartDate()).thenReturn(DateUtils.addMinutes(start, -10));
		when(booking.getEndDate()).thenReturn(start);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of(booking));

		CollisionReport report = sut.findCollisions(room, start, end, 15, 0, null);

		assertThat(report.getHard()).isEmpty();
		assertThat(report.getBuffer()).containsExactly(booking);
		assertThat(report.hasCollisions()).isTrue();
	}

	@Test
	public void findCollisions_existingBookingBufferExtendsIn_reportedAsBuffer() {
		// Existing booking ends just before sPrime in hard terms, but its bufferAfter
		// extends past sPrime — spec says this is a buffer collision.
		RoomRef room = mock(RoomRef.class);
		RoomBooking booking = mock(RoomBooking.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		// bBefore=15 → sPrime = start - 15min
		// booking ends at start - 16min (hard interval misses sPrime by 1 min)
		// but booking.bufferAfter=5 → bEnvEnd = start - 11min > sPrime → collision
		Date bookingEnd = DateUtils.addMinutes(start, -16);
		when(booking.getStartDate()).thenReturn(DateUtils.addMinutes(start, -60));
		when(booking.getEndDate()).thenReturn(bookingEnd);
		when(booking.getBufferAfter()).thenReturn(5);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of(booking));

		CollisionReport report = sut.findCollisions(room, start, end, 15, 0, null);

		assertThat(report.getBuffer()).containsExactly(booking);
	}

	@Test
	public void findCollisions_existingBookingBufferNotReaching_notReportedAsBuffer() {
		// Existing booking ends before sPrime and its buffer does not reach sPrime — no collision.
		RoomRef room = mock(RoomRef.class);
		RoomBooking booking = mock(RoomBooking.class);
		Date start = new Date();
		Date end = DateUtils.addMinutes(start, 60);
		// bBefore=15 → sPrime = start - 15min
		// booking ends at start - 20min, bufferAfter=3 → bEnvEnd = start - 17min < sPrime → no collision
		when(booking.getStartDate()).thenReturn(DateUtils.addMinutes(start, -60));
		when(booking.getEndDate()).thenReturn(DateUtils.addMinutes(start, -20));
		when(booking.getBufferAfter()).thenReturn(3);
		when(roomBookingDao.findHardOverlapping(any(), any(), any(), any())).thenReturn(List.of());
		when(roomBookingDao.findBufferOverlapping(any(), any(), any(), any(), any(), any())).thenReturn(List.of(booking));

		CollisionReport report = sut.findCollisions(room, start, end, 15, 0, null);

		assertThat(report.getBuffer()).isEmpty();
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
