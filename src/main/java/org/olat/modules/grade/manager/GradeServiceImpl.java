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
package org.olat.modules.grade.manager;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScaleRef;
import org.olat.modules.grade.GradeScaleSearchParams;
import org.olat.modules.grade.GradeScaleStats;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemRef;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradeServiceImpl implements GradeService {
	
	@Autowired
	private GradeSystemDAO gradeSystemDAO;
	@Autowired
	private PerformanceClassDAO performanceClassDAO;
	@Autowired
	private GradeScaleDAO gradeScaleDAO;
	@Autowired
	private BreakpointDAO breakpointDAO;
	@Autowired
	private GradeCalculator gradeCalculator;
	
	
	@Override
	public boolean isGradeServiceIdentifierAvailable(String identifier) {
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setIdentifier(identifier);
		List<GradeSystem> gradeSystems = gradeSystemDAO.load(searchParams);
		return gradeSystems.isEmpty();
	}
	
	@Override
	public GradeSystem createGradeSystem(String identifier, GradeSystemType type) {
		return gradeSystemDAO.create(identifier, type);
	}

	@Override
	public GradeSystem updateGradeSystem(GradeSystem gradeSystem) {
		return gradeSystemDAO.save(gradeSystem);
	}

	@Override
	public void deleteGradeSystem(GradeSystemRef gradeSystem) {
		performanceClassDAO.delete(gradeSystem);
		gradeSystemDAO.delete(gradeSystem);
	}

	@Override
	public List<GradeSystem> getGradeSystems(GradeSystemSearchParams searchParams) {
		return gradeSystemDAO.load(searchParams);
	}
	
	@Override
	public GradeSystem getGradeSystem(RepositoryEntry repositoryEntry, String subIdent) {
		GradeScale gradeScale = getGradeScale(repositoryEntry, subIdent);
		return gradeScale != null? gradeScale.getGradeSystem(): null;
	}

	@Override
	public boolean isInUse(GradeSystemRef gradeSystem) {
		if (gradeSystem == null) return false;
		
		List<GradeScaleStats> stats = gradeScaleDAO.loadStats(gradeSystem);
		return stats != null && !stats.isEmpty() && stats.get(0).getCount().longValue() > 0;
	}

	@Override
	public PerformanceClass createPerformanceClass(GradeSystem gradeSystem, String identifier) {
		return performanceClassDAO.create(gradeSystem, identifier);
	}

	@Override
	public PerformanceClass updatePerformanceClass(PerformanceClass performanceClass) {
		return performanceClassDAO.save(performanceClass);
	}

	@Override
	public void deletePerformanceClass(PerformanceClass performanceClass) {
		performanceClassDAO.delete(performanceClass);
	}

	@Override
	public void deletePerformanceClasses(GradeSystemRef gradeSystem) {
		performanceClassDAO.delete(gradeSystem);
	}

	@Override
	public List<PerformanceClass> getPerformanceClasses(GradeSystemRef gradeSystem) {
		return performanceClassDAO.load(gradeSystem);
	}
	
	@Override
	public GradeScale updateGradeScale(GradeScale gradeScale) {
		return gradeScaleDAO.save(gradeScale);
	}
	
	@Override
	public GradeScale updateOrCreateGradeScale(RepositoryEntry repositoryEntry, String subIdent, GradeScale gradeScale) {
		GradeScale currentGradeScale = getGradeScale(repositoryEntry, subIdent);
		if (currentGradeScale == null) {
			currentGradeScale = gradeScaleDAO.create(repositoryEntry, subIdent);
		}
		currentGradeScale.setGradeSystem(gradeScale.getGradeSystem());
		currentGradeScale.setMinScore(gradeScale.getMinScore());
		currentGradeScale.setMaxScore(gradeScale.getMaxScore());
		return gradeScaleDAO.save(currentGradeScale);
	}

	@Override
	public void deleteGradeScale(RepositoryEntry repositoryEntry, String subIdent) {
		breakpointDAO.delete(repositoryEntry, subIdent);
		gradeScaleDAO.delete(repositoryEntry, subIdent);
	}
	
	@Override
	public GradeScale getGradeScale(RepositoryEntry repositoryEntry, String subIdent) {
		GradeScaleSearchParams searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry);
		searchParams.setSubIdent(subIdent);
		List<GradeScale> gradeScales = gradeScaleDAO.load(searchParams);
		return !gradeScales.isEmpty()? gradeScales.get(0): null;
	}

	@Override
	public List<GradeScaleStats> getGradeScaleStats() {
		return gradeScaleDAO.loadStats(null);
	}

	@Override
	public Breakpoint createBreakpoint(GradeScale gradeScale) {
		return breakpointDAO.create(gradeScale);
	}

	@Override
	public Breakpoint updateBreakpoint(Breakpoint breakpoint) {
		return breakpointDAO.save(breakpoint);
	}
	
	@Override
	public void updateOrCreateBreakpoints(GradeScale gradeScale, List<Breakpoint> breakpoints) {
		GradeSystemType gradeSystemType = gradeScale.getGradeSystem().getType();
		
		List<Breakpoint> currentBreakpoints = breakpointDAO.load(gradeScale);
		Map<Object, Breakpoint> identToBreakpoint = currentBreakpoints.stream()
				.filter(bp -> getIdent(bp, gradeSystemType) != null)
				.collect(Collectors.toMap(bp -> getIdent(bp, gradeSystemType), Function.identity()));
		
		Set<Object> idents = new HashSet<>(breakpoints.size());
		// Create or update
		for (Breakpoint breakpoint : breakpoints) {
			Object ident = getIdent(breakpoint, gradeSystemType);
			idents.add(ident);
			
			Breakpoint currentBreakpoint = identToBreakpoint.get(ident);
			if (currentBreakpoint == null) {
				currentBreakpoint = breakpointDAO.create(gradeScale);
			}
			if ((breakpoint.getValue() != null && currentBreakpoint.getValue() == null)
					|| (breakpoint.getValue() == null && currentBreakpoint.getValue() != null)
					|| breakpoint.getValue().compareTo(currentBreakpoint.getValue()) != 0
					|| !Objects.equals(breakpoint.getGrade(), currentBreakpoint.getGrade())
					|| !Objects.equals(breakpoint.getBestToLowest(), currentBreakpoint.getBestToLowest())) {
				currentBreakpoint.setValue(breakpoint.getValue());
				currentBreakpoint.setGrade(breakpoint.getGrade());
				currentBreakpoint.setBestToLowest(breakpoint.getBestToLowest());
				breakpointDAO.save(currentBreakpoint);
			}
		}
		
		// Delete
		List<Long> keyToDelete = currentBreakpoints.stream()
				.filter(bp -> !idents.contains(getIdent(bp, gradeSystemType)))
				.map(Breakpoint::getKey)
				.collect(Collectors.toList());
		breakpointDAO.delete(keyToDelete);
		
	}
	
	private Object getIdent(Breakpoint breakpoint, GradeSystemType gradeSystemType) {
		return GradeSystemType.numeric == gradeSystemType ? breakpoint.getGrade() : breakpoint.getBestToLowest();
	}

	@Override
	public void deleteBreakpoint(Breakpoint breakpoint) {
		breakpointDAO.delete(Collections.singletonList(breakpoint.getKey()));
	}
	
	@Override
	public void deleteBreakpoints(GradeScale gradeScale) {
		breakpointDAO.delete(gradeScale);
	}

	@Override
	public List<Breakpoint> getBreakpoints(GradeScaleRef gradeScale) {
		return breakpointDAO.load(gradeScale);
	}
	
	@Override
	public List<String> getGrades(GradeSystem gradeSystem, BigDecimal minScore, BigDecimal maxScore) {
		if (gradeSystem == null || GradeSystemType.numeric != gradeSystem.getType()) return Collections.emptyList();
		
		return gradeCalculator
				.createNumericalRanges(new BigDecimal(gradeSystem.getLowestGrade().intValue()),
						new BigDecimal(gradeSystem.getBestGrade().intValue()), gradeSystem.getResolution(),
						gradeSystem.getRounding(), null, minScore, maxScore)
				.stream()
				.map(GradeScoreRange::getGrade)
				.collect(Collectors.toList());
	}

	@Override
	public Map<Integer, BigDecimal> getInitialTextLowerBounds(List<PerformanceClass> performanceClasses,
			BigDecimal minScore, BigDecimal maxScore) {
		if (performanceClasses == null || performanceClasses.isEmpty()) return Collections.emptyMap();
		
		return gradeCalculator
				.createNumericalRanges(
						new BigDecimal(performanceClasses.get(performanceClasses.size() - 1).getBestToLowest()),
						new BigDecimal(performanceClasses.get(0).getBestToLowest()), NumericResolution.whole,
						Rounding.nearest, null, minScore, maxScore)
				.stream()
				.collect(Collectors.toMap(range -> Integer.valueOf(range.getGrade()), GradeScoreRange::getLowerBound));
	}

	@Override
	public NavigableSet<GradeScoreRange> getGradeScoreRanges(GradeScale gradeScale, Locale locale) {
		GradeSystem gradeSystem = gradeScale.getGradeSystem();
		return getGradeScoreRanges(gradeSystem, getBreakpoints(gradeScale), gradeScale.getMinScore(),
				gradeScale.getMaxScore(), locale);
	}

	@Override
	public NavigableSet<GradeScoreRange> getGradeScoreRanges(GradeSystem gradeSystem, List<Breakpoint> breakpoints,
			BigDecimal minScore, BigDecimal maxScore, Locale locale) {
		if (GradeSystemType.numeric == gradeSystem.getType()) {
			return gradeCalculator.createNumericalRanges(new BigDecimal(gradeSystem.getLowestGrade().intValue()),
					new BigDecimal(gradeSystem.getBestGrade().intValue()), gradeSystem.getResolution(),
					gradeSystem.getRounding(), gradeSystem.getCutValue(), minScore, maxScore, breakpoints);
		} else if (GradeSystemType.text == gradeSystem.getType()) {
			Translator translator = Util.createPackageTranslator(GradeUIFactory.class, locale);
			return gradeCalculator.getTextGradeScoreRanges(getPerformanceClasses(gradeSystem), breakpoints, minScore,
					maxScore, translator);
		}
		return Collections.emptyNavigableSet();
	}

	@Override
	public GradeScoreRange getGradeScoreRange(NavigableSet<GradeScoreRange> gradeScoreRanges, Float score) {
		return gradeCalculator.getGrade(gradeScoreRanges, score);
	}

	@Override
	public BigDecimal getMinPassedScore(GradeScale gradeScale) {
		BigDecimal minPassedScore = null;
		if (gradeScale != null) {
			// Translated grade is not used. We can use any locale.
			Optional<GradeScoreRange> minPassedRange = getGradeScoreRanges(gradeScale, Locale.ENGLISH).stream()
					.sorted(Collections.reverseOrder())
					.filter(GradeScoreRange::isPassed)
					.findFirst();
			if (minPassedRange.isPresent()) {
				GradeScoreRange range = minPassedRange.get();
				if (range.isLowerBoundInclusive()) {
					minPassedScore = range.getLowerBound();
				} else {
					minPassedScore = range.getLowerBound().add(new BigDecimal("0.001"));
				}
			}
		}
		return minPassedScore;
	}

}
