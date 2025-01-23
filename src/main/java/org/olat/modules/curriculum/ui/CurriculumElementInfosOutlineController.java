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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.lecture.LectureBlock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosOutlineController extends BasicController {
	
	private static final String LEVEL = "level";
	
	private final CurriculumElementImageMapper curriculumElementImageMapper;
	private final String curriculumElementImageMapperUrl;
	private boolean empty;
	
	@Autowired
	private CurriculumService curriculumService;

	protected CurriculumElementInfosOutlineController(UserRequest ureq, WindowControl wControl,
			CurriculumElement rootElement, List<LectureBlock> lectureBlocks) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_outline");
		putInitialPanel(mainVC);
		
		curriculumElementImageMapper = new CurriculumElementImageMapper(curriculumService);
		curriculumElementImageMapperUrl = registerCacheableMapper(ureq, CurriculumElementImageMapper.DEFAULT_ID,
				curriculumElementImageMapper, CurriculumElementImageMapper.DEFAULT_EXPIRATION_TIME);
		
		List<CurriculumElement> elements = curriculumService.getCurriculumElementsDescendants(rootElement);
		empty = elements.isEmpty();
		
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for (CurriculumElement element:elements) {
			CurriculumElementRow row = new CurriculumElementRow(element);
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		
		Map<Long, List<LectureBlock>> elementKeyToLectureBlocks = lectureBlocks.stream()
				.collect(Collectors.groupingBy(lb -> lb.getCurriculumElement().getKey()));
		Formatter formatter = Formatter.getInstance(getLocale());
		List<OutlineRow> outlineRows = new ArrayList<>(rows.size());
		for (CurriculumElementRow row : rows) {
			OutlineRow outlineRow = new OutlineRow(row);
			outlineRows.add(outlineRow);
			
			String imageUrl = curriculumElementImageMapper.getImageUrl(curriculumElementImageMapperUrl,
					row.getCurriculumElement(), CurriculumElementFileType.teaserImage);
			outlineRow.setThumbnailRelPath(imageUrl);
			
			List<LectureBlock> elementLectureBlocks = elementKeyToLectureBlocks.get(row.getKey());
			if (elementLectureBlocks != null && !elementLectureBlocks.isEmpty()) {
				String numEvents = elementLectureBlocks.size() == 1
						? translate("num.of.event", String.valueOf(elementLectureBlocks.size()))
						: translate("num.of.events", String.valueOf(elementLectureBlocks.size()));
				outlineRow.setNumEvents(numEvents);
			}
			
			StringBuilder dates = new StringBuilder();
			if (row.getBeginDate() != null) {
				dates.append(formatter.formatDate(row.getBeginDate()));
			}
			if (row.getEndDate() != null) {
				if (!dates.isEmpty()) dates.append(" \u2013 ");
				dates.append(formatter.formatDate(row.getEndDate()));
			}
			outlineRow.setPeriod(dates.toString());
			
			outlineRow.setLevels(getLevels(row));
		}
		
		mainVC.contextPut("rows", outlineRows);
	}

	private List<String> getLevels(CurriculumElementRow row) {
		List<String> levels = new ArrayList<>(2);
		
		CurriculumElementRow parent = row.getParent();
		while (parent != null) {
			levels.add(LEVEL);
			parent = parent.getParent();
		}
		
		return levels;
	}
	
	public boolean isEmpty() {
		return empty;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public final static class OutlineRow {
		
		private final String identifier;
		private final String displayName;
		private final String typeName;
		private String thumbnailRelPath;
		private String numEvents;
		private String period;
		private List<String> levels;
		
		public OutlineRow(CurriculumElementRow row) {
			identifier = row.getIdentifier();
			displayName = row.getDisplayName();
			typeName = row.getCurriculumElementType().getDisplayName();
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getTypeName() {
			return typeName;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}

		public void setThumbnailRelPath(String thumbnailRelPath) {
			this.thumbnailRelPath = thumbnailRelPath;
		}

		public boolean isThumbnailAvailable() {
			return StringHelper.containsNonWhitespace(thumbnailRelPath);
		}

		public String getNumEvents() {
			return numEvents;
		}

		public void setNumEvents(String numEvents) {
			this.numEvents = numEvents;
		}

		public String getPeriod() {
			return period;
		}

		public void setPeriod(String period) {
			this.period = period;
		}

		public List<String> getLevels() {
			return levels;
		}

		public void setLevels(List<String> levels) {
			this.levels = levels;
		}
		
	}

}
