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
package org.olat.repository.ui.list;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.modules.curriculum.ui.CurriculumElementRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-03-09<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OverviewReservationDetailController extends BasicController {

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private MapperService mapperService;

	public OverviewReservationDetailController(UserRequest ureq, WindowControl wControl,
			OverviewReservationRow row) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		VelocityContainer mainVC = createVelocityContainer("overview_reservation_detail");

		mainVC.contextPut("externalRef", row.getExternalRef());
		mainVC.contextPut("translatedType", row.getTranslatedType());
		mainVC.contextPut("title", row.getDisplayName());

		if (StringHelper.containsNonWhitespace(row.getDescription())) {
			mainVC.contextPut("description", row.getDescription());
		}

		if (row.isThumbnailAvailable()) {
			mainVC.contextPut("thumbnailUrl", row.getThumbnailRelPath());
		}

		if (row.getCurriculumElementKey() != null) {
			CurriculumElement element = curriculumService.getCurriculumElement(() -> row.getCurriculumElementKey());
			if (element != null) {
				RepositoryEntryImageMapper reImageMapper = RepositoryEntryImageMapper.mapper210x140();
				MapperKey reMapperKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, reImageMapper);

				CurriculumElementImageMapper ceImageMapper = CurriculumElementImageMapper.mapper210x140();
				MapperKey ceMapperKey = mapperService.register(null, CurriculumElementImageMapper.MAPPER_ID_210_140, ceImageMapper);

				List<MembershipRow> curriculumElementRows = buildMembershipRows(element,
						row.getReservations(), reImageMapper, reMapperKey, ceImageMapper, ceMapperKey);
				if (!curriculumElementRows.isEmpty()) {
					mainVC.contextPut("curriculumElementRows", curriculumElementRows);
				}
			}
		}

		putInitialPanel(mainVC);
	}

	private List<MembershipRow> buildMembershipRows(CurriculumElement element, List<ResourceReservation> reservations,
			RepositoryEntryImageMapper reImageMapper, MapperKey reMapperKey, CurriculumElementImageMapper ceImageMapper,
			MapperKey ceMapperKey) {
		Set<Long> pendingElementKeys = reservations.stream()
				.map(r -> r.getResource().getResourceableId())
				.collect(Collectors.toSet());

		Collator collator = Collator.getInstance(getLocale());

		List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(element);
		List<CurriculumElement> sortedDescendants = sortedByComparator(descendants, element);
		List<CurriculumElement> pendingDescendants = sortedDescendants.stream()
				.filter(d -> pendingElementKeys.contains(d.getKey()))
				.toList();

		Map<Long, VFSThumbnailInfos> ceThumbnails = ceImageMapper.getThumbnails(pendingDescendants);

		List<CurriculumElement> allElements = new ArrayList<>();
		if (pendingElementKeys.contains(element.getKey())) {
			allElements.add(element);
		}
		allElements.addAll(pendingDescendants);
		Map<Long, Set<RepositoryEntry>> entriesByElementKey = curriculumService.getCurriculumElementKeyToRepositoryEntries(allElements);

		List<RepositoryEntry> mainCourses = List.of();
		if (pendingElementKeys.contains(element.getKey())) {
			mainCourses = new ArrayList<>(entriesByElementKey.getOrDefault(element.getKey(), Set.of()));
			mainCourses.sort(Comparator.comparing(RepositoryEntry::getDisplayname, collator));
		}

		Map<Long, List<RepositoryEntry>> coursesByDescKey = new HashMap<>();
		List<RepositoryEntry> allCourses = new ArrayList<>(mainCourses);
		for (CurriculumElement desc : pendingDescendants) {
			List<RepositoryEntry> courses = new ArrayList<>(entriesByElementKey.getOrDefault(desc.getKey(), Set.of()));
			courses.sort(Comparator.comparing(RepositoryEntry::getDisplayname, collator));
			coursesByDescKey.put(desc.getKey(), courses);
			allCourses.addAll(courses);
		}

		Map<Long, VFSThumbnailInfos> reThumbnails = reImageMapper.getRepositoryThumbnails(allCourses);

		List<MembershipRow> rows = new ArrayList<>();

		if (!mainCourses.isEmpty()) {
			rows.add(new MembershipRow(null, null, null, null,
					toMembershipRows(mainCourses, reThumbnails, reMapperKey)));
		}

		for (CurriculumElement desc : pendingDescendants) {
			List<RepositoryEntry> courses = coursesByDescKey.get(desc.getKey());
			String translatedType = desc.getType() != null ? desc.getType().getDisplayName() : null;
			VFSThumbnailInfos ceThumbnail = ceThumbnails.get(desc.getKey());
			String ceThumbnailUrl = ceThumbnail != null
					? CurriculumElementImageMapper.getImageURL(ceMapperKey.getUrl(), ceThumbnail.metadata(), ceThumbnail.thumbnailMetadata())
					: null;
			rows.add(new MembershipRow(desc.getIdentifier(), translatedType, desc.getDisplayName(), ceThumbnailUrl,
					toMembershipRows(courses, reThumbnails, reMapperKey)));
		}

		return rows;
	}

	private List<MembershipRow> toMembershipRows(List<RepositoryEntry> entries,
			Map<Long, VFSThumbnailInfos> reThumbnails, MapperKey reMapperKey) {
		List<MembershipRow> rows = new ArrayList<>(entries.size());
		for (RepositoryEntry entry : entries) {
			String translatedType = translate(entry.getOlatResource().getResourceableTypeName());
			VFSThumbnailInfos reThumbnail = reThumbnails.get(entry.getKey());
			String thumbnailUrl = reThumbnail != null
					? RepositoryEntryImageMapper.getImageURL(reMapperKey.getUrl(), reThumbnail.metadata(), reThumbnail.thumbnailMetadata())
					: null;
			rows.add(new MembershipRow(entry.getExternalRef(), translatedType, entry.getDisplayname(), thumbnailUrl, List.of()));
		}
		return rows;
	}

	private List<CurriculumElement> sortedByComparator(List<CurriculumElement> descendants, CurriculumElement root) {
		Map<Long, CurriculumElementRow> rowsByKey = new HashMap<>();
		rowsByKey.put(root.getKey(), new CurriculumElementRow(root));
		for (CurriculumElement desc : descendants) {
			rowsByKey.put(desc.getKey(), new CurriculumElementRow(desc));
		}
		for (CurriculumElement desc : descendants) {
			if (desc.getParent() != null) {
				rowsByKey.get(desc.getKey()).setParent(rowsByKey.get(desc.getParent().getKey()));
			}
		}
		List<CurriculumElementRow> rows = descendants.stream()
				.map(d -> rowsByKey.get(d.getKey()))
				.collect(Collectors.toList());
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		return rows.stream().map(CurriculumElementRow::getCurriculumElement).toList();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public static class MembershipRow {
		private final String externalRef;
		private final String translatedType;
		private final String displayName;
		private final String thumbnailUrl;
		private final List<MembershipRow> children;

		MembershipRow(String externalRef, String translatedType, String displayName, String thumbnailUrl,
				List<MembershipRow> children) {
			this.externalRef = externalRef;
			this.translatedType = translatedType;
			this.displayName = displayName;
			this.thumbnailUrl = thumbnailUrl;
			this.children = children;
		}

		public String getExternalRef() {
			return externalRef;
		}

		public String getTranslatedType() {
			return translatedType;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getThumbnailUrl() {
			return thumbnailUrl;
		}

		public List<MembershipRow> getChildren() {
			return children;
		}
	}
}
