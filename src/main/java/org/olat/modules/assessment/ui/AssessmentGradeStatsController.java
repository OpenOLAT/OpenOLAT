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
package org.olat.modules.assessment.ui;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.ui.component.GradeChart;
import org.olat.modules.assessment.ui.component.GradeChart.GradeCount;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentGradeStatsController extends BasicController {
	
	private final VelocityContainer mainVC;
	private GradeChart chart;

	private final NavigableSet<GradeScoreRange> gradeScoreRanges;

	@Autowired
	private GradeService gradeService;

	public AssessmentGradeStatsController(UserRequest ureq, WindowControl wControl, SearchAssessedIdentityParams params) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("stats_grade");
		
		GradeScale gradeScale = gradeService.getGradeScale(params.getEntry(), params.getSubIdent());
		GradeSystem gradeSystem = gradeScale.getGradeSystem();
		gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
		
		mainVC.contextPut("title", translate("grade.stats.title", GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystem)));
		mainVC.contextPut("gradeSystem", GradeUIFactory.translateGradeSystemName(getTranslator(), gradeSystem));
		
		if (GradeSystemType.numeric == gradeSystem.getType()) {
			mainVC.contextPut("resolution", GradeUIFactory.translateResolution(getTranslator(), gradeSystem.getResolution()));
			mainVC.contextPut("rounding", GradeUIFactory.translateRounding(getTranslator(), gradeSystem.getRounding()));
		}
		
		Float min = gradeScale.getMinScore() != null? Float.valueOf(gradeScale.getMinScore().floatValue()): null;
		Float max = gradeScale.getMaxScore() != null? Float.valueOf(gradeScale.getMaxScore().floatValue()): null;
		String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), min, max);
		if (scoreMinMax != null) {
			mainVC.contextPut("scoreMinMax", scoreMinMax);
		}
		
		Optional<GradeScoreRange> minPassed = getMinPassed(gradeScoreRanges);
		if (minPassed.isPresent()) {
			GradeScoreRange minPassedRange = minPassed.get();
			String grade = GradeUIFactory.translatePerformanceClass(getTranslator(),
					minPassedRange.getPerformanceClassIdent(), minPassedRange.getGrade(),
					minPassedRange.getGradeSystemIdent());
			String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), minPassedRange.getGradeSystemIdent());
			String passedWith = translate("grade.score.and.grade", THREE_DIGITS.format(minPassedRange.getLowerBound()), grade, gradeSystemLabel);
			mainVC.contextPut("passedWith", passedWith);
		}
		
		chart = new GradeChart("chart");
		chart.setGradeSystem(gradeSystem);
		mainVC.put("chart", chart);
		
		putInitialPanel(mainVC);
	}
	
	private Optional<GradeScoreRange> getMinPassed(NavigableSet<GradeScoreRange> ranges) {
		return ranges.stream()
				.sorted(Comparator.reverseOrder())
				.filter(range -> range.getPassed() != null && range.getPassed().booleanValue())
				.findFirst();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	

	public void reload(AssessmentStatistics statistics, Map<Integer, Long> scoreToCount) {
		Double avgScore = statistics.getAverageScore();
		if (avgScore != null) {
			GradeScoreRange avgScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, Float.valueOf(avgScore.floatValue()));
			String grade = GradeUIFactory.translatePerformanceClass(getTranslator(),
					avgScoreRange.getPerformanceClassIdent(), avgScoreRange.getGrade(),
					avgScoreRange.getGradeSystemIdent());
			String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), avgScoreRange.getGradeSystemIdent());
			String avgScoreText = translate("grade.score.and.grade", THREE_DIGITS.format(avgScore), grade, gradeSystemLabel);
			mainVC.contextPut("avgScore", avgScoreText);
		} else {
			mainVC.contextRemove("avgScore");
		}
		
		BigDecimal bestScore = statistics.getMaxScore();
		if (bestScore != null) {
			GradeScoreRange bestScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, Float.valueOf(bestScore.floatValue()));
			String grade = GradeUIFactory.translatePerformanceClass(getTranslator(),
					bestScoreRange.getPerformanceClassIdent(), bestScoreRange.getGrade(),
					bestScoreRange.getGradeSystemIdent());
			String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), bestScoreRange.getGradeSystemIdent());
			String bestScoreText = translate("grade.score.and.grade", THREE_DIGITS.format(bestScore), grade, gradeSystemLabel);
			mainVC.contextPut("bestScore", bestScoreText);
		} else {
			mainVC.contextRemove("bestScore");
		}
		
		List<GradeCount> gradeCounts = new ArrayList<>(gradeScoreRanges.size());
		Iterator<GradeScoreRange> rangeIterator = gradeScoreRanges.iterator();
		while(rangeIterator.hasNext()) {
			GradeScoreRange range = rangeIterator.next();
			gradeCounts.add(new GradeCount(range.getGrade(), Long.valueOf(0)));
		}
		Collections.reverse(gradeCounts);
		
		for (Map.Entry<Integer, Long> entry : scoreToCount.entrySet()) {
			// Maybe rounding problems if decimal score rounded to integer. But it's only a overview chart.
			GradeScoreRange range = gradeService.getGradeScoreRange(gradeScoreRanges, Float.valueOf(entry.getKey().floatValue()));
			GradeCount gradeCount = getGradeCount(gradeCounts, range.getGrade());
			gradeCount.setCount(Long.valueOf(gradeCount.getCount().longValue() + entry.getValue().longValue()));
		}
		
		chart.setGradeCounts(gradeCounts);
	}
	
	private GradeCount getGradeCount(List<GradeCount> gradeCounts, String grade) {
		return gradeCounts.stream().filter(gc -> gc.getGrade().equals(grade)).findFirst().orElse(null);
	}

}
