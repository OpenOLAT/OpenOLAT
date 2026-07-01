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
package org.olat.modules.lecture.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOptionGroup;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;

/**
 * ObjectSelectionSource that provides rooms grouped by availability for a given
 * event time window. Each option shows the building color square as its image,
 * the room reference and description as its title, and the building reference
 * as its subtitle.
 *
 * Initial date: 30 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSelectionSource implements ObjectSelectionSource {

	private final Translator translator;
	private final Collator collator;
	private final RoomManagementService roomManagementService;
	private final Roles roles;
	private final Date startDate;
	private final Date endDate;
	private final LectureBlock lectureBlock;
	private final Collection<Room> selectedRooms;
	private final int participantCount;

	// Lazily loaded
	private Map<Long, Room> allRoomsMap;
	private Map<Long, RoomBooking> occupiedByOther;

	public RoomSelectionSource(Translator translator, RoomManagementService roomManagementService,
			Roles roles, Date startDate, Date endDate, LectureBlock lectureBlock,
			Collection<Room> selectedRooms, int participantCount) {
		this.translator = translator;
		this.collator = Collator.getInstance(translator.getLocale());
		this.roomManagementService = roomManagementService;
		this.roles = roles;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lectureBlock = lectureBlock;
		this.selectedRooms = selectedRooms;
		this.participantCount = participantCount;
	}

	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return selectedRooms.stream().map(r -> r.getKey().toString()).toList();
	}

	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		return buildDisplayValue(selectedRooms);
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		initRooms();
		List<Room> rooms = keys.stream()
				.map(k -> allRoomsMap.get(Long.valueOf(k)))
				.filter(Objects::nonNull)
				.toList();
		return buildDisplayValue(rooms);
	}

	private ObjectDisplayValues buildDisplayValue(Collection<Room> rooms) {
		String ariaTitle = rooms.stream()
				.map(this::getRoomLabel)
				.filter(Objects::nonNull)
				.sorted(collator)
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(ariaTitle, ariaTitle);
	}

	private String getRoomLabel(Room room) {
		String ref = StringHelper.containsNonWhitespace(room.getExternalRef()) ? room.getExternalRef() : "";
		String desc = room.getDescription();
		return StringHelper.containsNonWhitespace(desc) && !desc.equals(ref) ? ref + " · " + desc : ref;
	}

	@Override
	public List<ObjectOptionGroup> getOptionGroups(Locale locale) {
		initRooms();

		List<ObjectOptionValues> available = new ArrayList<>();
		List<ObjectOptionValues> occupied = new ArrayList<>();

		for (Room room : allRoomsMap.values()) {
			ObjectOptionValues option = buildOption(room);
			if (occupiedByOther.containsKey(room.getKey())) {
				occupied.add(option);
			} else {
				available.add(option);
			}
		}

		Comparator<ObjectOptionValues> byTitle = (a, b) -> collator.compare(
				a.getTitle() != null ? a.getTitle() : "",
				b.getTitle() != null ? b.getTitle() : "");
		available.sort(byTitle);
		occupied.sort(byTitle);

		List<ObjectOptionGroup> groups = new ArrayList<>();
		if (!available.isEmpty()) {
			groups.add(ObjectOptionGroup.of(translator.translate("lecture.rooms.available"), available));
		}
		if (!occupied.isEmpty()) {
			groups.add(ObjectOptionGroup.of(translator.translate("lecture.rooms.occupied"), occupied));
		}
		return groups;
	}

	private ObjectOptionValues buildOption(Room room) {
		String key = room.getKey().toString();
		String title = buildOptionTitle(room);
		String subTitle = buildingRef(room.getBuilding());
		String imageHtml = buildColorSquareHtml(room.getBuilding());
		return new ObjectOptionValues(key, null, title, subTitle, null, null, imageHtml);
	}

	/** Full title line: "<ref> · <desc> | [⚠ ]<seats> seats" */
	private String buildOptionTitle(Room room) {
		StringBuilder sb = new StringBuilder();
		String ref = StringHelper.containsNonWhitespace(room.getExternalRef()) ? room.getExternalRef() : "";
		sb.append(ref);
		String desc = room.getDescription();
		if (StringHelper.containsNonWhitespace(desc) && !desc.equals(room.getExternalRef())) {
			sb.append(" · ").append(desc);
		}
		if (room.getSeats() != null) {
			sb.append(" | ");
			if (participantCount > 0 && room.getSeats() < participantCount) {
				sb.append("⚠ "); // ⚠ WARNING SIGN
			}
			sb.append(room.getSeats()).append(" ").append(translator.translate("lecture.rooms.seats"));
		}
		return sb.toString();
	}

	private String buildColorSquareHtml(Building building) {
		if (building == null) return null;
		String colorCss = building.getColorCss();
		if (!StringHelper.containsNonWhitespace(colorCss)) return null;
		return "<div class=\"o_color " + StringHelper.escapeHtml(colorCss)
				+ "\"><small><i class=\"o_icon o_icon_square\"></i></small></div>";
	}

	private String buildingRef(Building building) {
		if (building == null) return null;
		return StringHelper.containsNonWhitespace(building.getExternalRef())
				? building.getExternalRef()
				: building.getDescription();
	}

	private void initRooms() {
		if (allRoomsMap != null) return;

		List<RoomBooking> bookings = roomManagementService.getBookings(startDate, endDate);
		occupiedByOther = new HashMap<>();
		for (RoomBooking booking : bookings) {
			boolean isOwn = lectureBlock != null && lectureBlock.getKey() != null
					&& booking.getLectureBlock() != null
					&& booking.getLectureBlock().getKey().equals(lectureBlock.getKey());
			if (!isOwn) {
				occupiedByOther.put(booking.getRoom().getKey(), booking);
			}
		}

		SearchRoomParameters params = new SearchRoomParameters();
		params.setStatus(List.of(RoomStatus.active));
		List<Room> rooms = roomManagementService.searchRooms(params, roles);
		allRoomsMap = rooms.stream().collect(Collectors.toMap(Room::getKey, r -> r));
	}

	public Room getRoomByKey(Long key) {
		initRooms();
		return allRoomsMap.get(key);
	}

	@Override
	public boolean isBrowserAvailable() {
		return true;
	}

	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection, Collection<String> selectedKeys) {
		initRooms();
		List<Room> preSelected = selectedKeys.stream()
				.map(k -> allRoomsMap.get(Long.valueOf(k)))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return (UserRequest lureq, WindowControl lwControl) ->
				new AddRoomsController(lureq, lwControl, startDate, endDate, preSelected, lectureBlock);
	}

	@Override
	public void addMissingOptions(Collection<String> keys) {
		// no-op
	}
}
