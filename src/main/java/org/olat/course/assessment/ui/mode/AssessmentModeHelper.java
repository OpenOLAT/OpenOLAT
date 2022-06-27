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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;

/**
 * 
 * Initial date: 13 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeHelper {
	
	private final Translator translator;
	
	public AssessmentModeHelper(Translator translator) {
		this.translator = translator;
	}
	
	public String getCssClass(AssessmentMode mode) {
		return getStatus(mode).cssClass();
	}
	
	public String getStatusLabel(AssessmentMode mode) {
		Status status = getStatus(mode);
		return getStatusLabel(status);
	}
	
	public String getStatusLabel(Status status) {
		return translator.translate("assessment.mode.status.".concat(status.name()));
	}
	
	public Status getStatus(AssessmentMode mode) {
		return mode != null && mode.getStatus() != null ? mode.getStatus() : Status.none;
	}
	
	public String getBeginEndDate(AssessmentMode mode) {
		Date begin = mode.getBegin();
		Date end = mode.getEnd();
		Formatter formatter = Formatter.getInstance(translator.getLocale());

		String[] args = new String[] {
			formatter.formatDate(begin),				// 0
			formatter.formatTimeShort(begin),			// 1
			formatter.formatDate(end),				// 0
			formatter.formatTimeShort(end),				// 2
			Integer.toString(mode.getLeadTime()),		// 3
			Integer.toString(mode.getFollowupTime())	// 4
		};
		
		String i18nKey;
		if(DateUtils.isSameDay(begin, end)) {
			i18nKey = "date.and.time.text.same.day";
		} else {
			i18nKey = "date.and.time.text";
		}
		return translator.translate(i18nKey, args);
	}
	
	/**
	 * The method synchronize the elements but you need to merge the assessment mode afterwards.
	 * 
	 * @param curriculumElementKeys
	 * @param assessmentMode
	 * @param target
	 * @param assessmentModeMgr
	 * @param curriculumService
	 */
	public static void updateCurriculumElementsRelations(final List<Long> curriculumElementKeys, final AssessmentMode assessmentMode,
			final Target target, final AssessmentModeManager assessmentModeMgr, final CurriculumService curriculumService) {
		if(curriculumElementKeys.isEmpty() || target == Target.course || target == Target.groups) {
			if(!assessmentMode.getCurriculumElements().isEmpty()) {
				List<AssessmentModeToCurriculumElement> currentElements = new ArrayList<>(assessmentMode.getCurriculumElements());
				for(AssessmentModeToCurriculumElement modeToElement:currentElements) {
					assessmentModeMgr.deleteAssessmentModeToCurriculumElement(modeToElement);
				}
				assessmentMode.getCurriculumElements().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToCurriculumElement> currentElements = new ArrayList<>(assessmentMode.getCurriculumElements());
			for(AssessmentModeToCurriculumElement modeToElement:currentElements) {
				Long currentKey = modeToElement.getCurriculumElement().getKey();
				if(!curriculumElementKeys.contains(currentKey)) {
					assessmentMode.getCurriculumElements().remove(modeToElement);
					assessmentModeMgr.deleteAssessmentModeToCurriculumElement(modeToElement);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long curriculumElementKey:curriculumElementKeys) {
				if(!currentKeys.contains(curriculumElementKey)) {
					CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
					AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(assessmentMode, element);
					assessmentMode.getCurriculumElements().add(modeToElement);
				}
			}
		}
	}
	
	public static void updateBusinessGroupRelations(final List<Long> groupKeys, final AssessmentMode assessmentMode, final Target target,
			final AssessmentModeManager assessmentModeMgr, final BusinessGroupService businessGroupService) {
		//update groups
		if(groupKeys.isEmpty() || target == Target.course || target == Target.curriculumEls) {
			if(!assessmentMode.getGroups().isEmpty()) {
				List<AssessmentModeToGroup> currentGroups = new ArrayList<>(assessmentMode.getGroups());
				for(AssessmentModeToGroup modeToGroup:currentGroups) {
					assessmentModeMgr.deleteAssessmentModeToGroup(modeToGroup);
				}
				assessmentMode.getGroups().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToGroup> currentGroups = new ArrayList<>(assessmentMode.getGroups());
			for(AssessmentModeToGroup modeToGroup:currentGroups) {
				Long currentKey = modeToGroup.getBusinessGroup().getKey();
				if(!groupKeys.contains(currentKey)) {
					assessmentMode.getGroups().remove(modeToGroup);
					assessmentModeMgr.deleteAssessmentModeToGroup(modeToGroup);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long groupKey:groupKeys) {
				if(!currentKeys.contains(groupKey)) {
					BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
					AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(assessmentMode, group);
					assessmentMode.getGroups().add(modeToGroup);
				}
			}
		}
	}
	
	public static void updateAreaRelations(final List<Long> areaKeys, final AssessmentMode assessmentMode, final Target target,
			final AssessmentModeManager assessmentModeMgr, final BGAreaManager areaMgr) {
		//update areas
		if(areaKeys.isEmpty() || target == Target.course || target == Target.curriculumEls) {
			if(!assessmentMode.getAreas().isEmpty()) {
				List<AssessmentModeToArea> currentAreas = new ArrayList<>(assessmentMode.getAreas());
				for(AssessmentModeToArea modeToArea:currentAreas) {
					assessmentModeMgr.deleteAssessmentModeToArea(modeToArea);
				}
				assessmentMode.getAreas().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToArea> currentAreas = new ArrayList<>(assessmentMode.getAreas());
			for(AssessmentModeToArea modeToArea:currentAreas) {
				Long currentKey = modeToArea.getArea().getKey();
				if(!areaKeys.contains(currentKey)) {
					assessmentMode.getAreas().remove(modeToArea);
					assessmentModeMgr.deleteAssessmentModeToArea(modeToArea);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long areaKey:areaKeys) {
				if(!currentKeys.contains(areaKey)) {
					BGArea area = areaMgr.loadArea(areaKey);
					AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(assessmentMode, area);
					assessmentMode.getAreas().add(modeToArea);
				}
			}
		}
	}
}
