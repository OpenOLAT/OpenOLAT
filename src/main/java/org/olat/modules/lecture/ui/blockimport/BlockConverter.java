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
package org.olat.modules.lecture.ui.blockimport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 12 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BlockConverter {
	
	private final List<DateFormat> dateFormatters = new ArrayList<>();
	
	private int currentLine = 0;

	private final Map<String,Identity> teachersMap = new HashMap<>();
	private final Map<String,GroupMapping> groupMapping = new HashMap<>();
	private final Map<String,LectureBlock> externalIdToBlocks = new HashMap<>();
	
	private final RepositoryEntry entry;
	private final Group entryDefaultGroup;
	private final List<BusinessGroup> businessGroups;
	private final List<CurriculumElement> curriculumElements;
	private final List<ImportedLectureBlock> lectureBlocks = new ArrayList<>();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public BlockConverter(RepositoryEntry entry, List<LectureBlock> currentBlocks, Locale locale) {
		CoreSpringFactory.autowireObject(this);

		this.entry = entry;
		entryDefaultGroup = repositoryService.getDefaultGroup(entry);
		
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		businessGroups = bgs.findBusinessGroups(new SearchBusinessGroupParams(), entry, 0, -1);
		
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		curriculumElements = curriculumService.getCurriculumElements(entry);
		
		if(currentBlocks != null && !currentBlocks.isEmpty()) {
			for(LectureBlock currentBlock:currentBlocks) {
				if(StringHelper.containsNonWhitespace(currentBlock.getExternalId())) {
					externalIdToBlocks.put(currentBlock.getExternalId(), currentBlock);
				}
			}
		}
		
		//some default patterns
		dateFormatters.add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
		dateFormatters.add(new SimpleDateFormat("dd.MM.yyyy HH:mm"));
		dateFormatters.add(new SimpleDateFormat("dd.MM.yy HH:mm:ss"));
		dateFormatters.add(new SimpleDateFormat("dd.MM.yy HH:mm"));

		dateFormatters.add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
		dateFormatters.add(new SimpleDateFormat("MM/dd/yy HH:mm:ss"));
		dateFormatters.add(new SimpleDateFormat("MM/dd/yyyy HH:mm"));
		dateFormatters.add(new SimpleDateFormat("MM/dd/yy HH:mm"));
		
		DateFormat shortDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		dateFormatters.add(shortDateTimeFormat);
	}
	
	public int getCurrentLine() {
		return currentLine;
	}
	
	public List<ImportedLectureBlock> getImportedLectureBlocks() {
		return lectureBlocks;
	}
	
	public void parse(String input) {
		String[] lines = input.split("\r?\n");
		
		for (int i = 0; i<lines.length; i++) {
			currentLine = i+1;
			
			String line = lines[i];
			if (line.equals("")) {
				continue;
			}
		
			String delimiter = "\t";
			// use comma as fallback delimiter, e.g. for better testing
			if (line.indexOf(delimiter) == -1) {
				delimiter = ",";
			}
			String[] parts = line.split(delimiter);
			if(parts.length > 1) {
				processLine(parts);
			}	
		}
	}
	
	private void processLine(String[] parts) {
		if(parts.length < 9) return;
		
		String externalId = parts[0];
		LectureBlock block = externalIdToBlocks.get(externalId);
		if(block == null) {
			block = lectureService.createLectureBlock(entry);
			block.setExternalId(externalId);
		}

		ImportedLectureBlock importedBlocks = new ImportedLectureBlock(block, currentLine);
		lectureBlocks.add(importedBlocks);
		
		String title = parts[1];
		block.setTitle(title);
		
		int numOfLectures = numberOfLectures(parts[2]);
		block.setPlannedLecturesNumber(numOfLectures);
		
		String date = parts[3];
		Date beginDate = parseDateTime(date, parts[4]);
		Date endDate = parseDateTime(date, parts[5]);
		block.setStartDate(beginDate);
		block.setEndDate(endDate);
		
		String compulsory = parts[6];
		block.setCompulsory("yes".equalsIgnoreCase(compulsory));
		
		Identity teacher = getTeacher(parts[7]);
		if(teacher != null) {
			importedBlocks.getTeachers().add(teacher);
		}

		GroupMapping participants = getParticipants(parts[8]);
		importedBlocks.setGroupMapping(participants);
		
		String location = processOptionalCell(parts, 9);
		block.setLocation(location);

		String description = processOptionalCell(parts, 10);
		block.setDescription(description);
		
		String preparation = processOptionalCell(parts, 11);
		block.setPreparation(preparation);
		
		String comment = processOptionalCell(parts, 12);
		block.setComment(comment);
	}
	
	private String processOptionalCell(String[] parts, int pos) {
		return (parts.length > pos) ? parts[pos] : null;
	}
	
	private GroupMapping getParticipants(String participants) {
		if(!StringHelper.containsNonWhitespace(participants)) return null;
		
		GroupMapping bGroup = groupMapping.get(participants);
		if(bGroup == null) {
			if(participants.equalsIgnoreCase("COURSE")) {
				bGroup = new GroupMapping(GroupMapping.Type.course);
				bGroup.setGroup(entryDefaultGroup);
			} else if(participants.toLowerCase().startsWith("group::")) {
				String identifier = participants.substring(7, participants.length());
				bGroup = new GroupMapping(GroupMapping.Type.businessGroup);
				for(BusinessGroup businesGroup:businessGroups) {
					if(identifier.equalsIgnoreCase(businesGroup.getName())
							|| identifier.equalsIgnoreCase(businesGroup.getExternalId())
							|| identifier.equalsIgnoreCase(businesGroup.getKey().toString())) {
						bGroup.setBusinessGroup(businesGroup);
					}
				}
			} else if(participants.toLowerCase().startsWith("curr::")) {
				String identifier = participants.substring(6, participants.length());
				bGroup = new GroupMapping(GroupMapping.Type.curriculumElement);
				for(CurriculumElement element:curriculumElements) {
					if(identifier.equalsIgnoreCase(element.getDisplayName())
							|| identifier.equalsIgnoreCase(element.getExternalId())
							|| identifier.equalsIgnoreCase(element.getIdentifier())
							|| identifier.equalsIgnoreCase(element.getKey().toString())) {
						bGroup.setCurriculumElement(element);
					}
				}
			}
			
			if(bGroup != null) {
				groupMapping.put(participants, bGroup);
			}
		}
		
		return bGroup;
	}
	
	private Identity getTeacher(String string) {
		Identity teacher = teachersMap.get(string);
		
		if(teacher == null) {
			List<String> names = Arrays.asList(string);
			List<FindNamedIdentity> namedTeachers = securityManager.findIdentitiesBy(names);
			if(namedTeachers.size() == 1) {
				teacher = namedTeachers.get(0).getIdentity();
			}
			if(teacher == null) {
				List<Identity> teachers = userManager.findIdentitiesByEmail(Collections.singletonList(string));
				if(!teachers.isEmpty()) {
					teacher = teachers.get(0);
				}
			}
			
			if(teacher != null) {
				teachersMap.put(string, teacher);
			}
		}
		return teacher;
	}
	
	protected Date parseDateTime(String date, String time) {
		StringBuilder sb = new StringBuilder();
		sb.append(date).append(" ").append(time);
		String datetime = sb.toString();
		
		Date parsed = null;
		Calendar cal = Calendar.getInstance();
		for(DateFormat format:dateFormatters) {
			try {
				format.setLenient(true);
				parsed = format.parse(datetime);
				cal.setTime(parsed);
				if(cal.get(Calendar.YEAR) > 2000) {
					break;
				}
			} catch (ParseException e) {
				//try
			}
		}
		return parsed;
	}
	
	private int numberOfLectures(String string) {
		try {
			int numOfLectures = Integer.parseInt(string);
			if(numOfLectures < 0) {
				numOfLectures = 0;
			} else if (numOfLectures > 12) {
				numOfLectures = 12;
			}
			return numOfLectures;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
