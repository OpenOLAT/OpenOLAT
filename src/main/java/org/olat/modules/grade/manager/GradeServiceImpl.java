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

import javax.annotation.PostConstruct;

import org.olat.core.commons.persistence.DB;
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
	private DB dbInstance;
	@Autowired
	private GradeSystemDAO gradeSystemDao;
	@Autowired
	private PerformanceClassDAO performanceClassDao;
	@Autowired
	private GradeScaleDAO gradeScaleDao;
	@Autowired
	private BreakpointDAO breakpointDao;
	@Autowired
	private GradeCalculator gradeCalculator;
	
	@PostConstruct
	public void initPredefinedSystems() {
		new PredefinedGradeSystemsInitializer(dbInstance, this, gradeSystemDao).init();
	}
	
	@Override
	public boolean isGradeServiceIdentifierAvailable(String identifier) {
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setIdentifier(identifier);
		List<GradeSystem> gradeSystems = gradeSystemDao.load(searchParams);
		return gradeSystems.isEmpty();
	}
	
	@Override
	public GradeSystem createGradeSystem(String identifier, GradeSystemType type) {
		return gradeSystemDao.create(identifier, type);
	}

	@Override
	public GradeSystem updateGradeSystem(GradeSystem gradeSystem) {
		return gradeSystemDao.save(gradeSystem);
	}

	@Override
	public void deleteGradeSystem(GradeSystemRef gradeSystem) {
		performanceClassDao.delete(gradeSystem);
		gradeSystemDao.delete(gradeSystem);
	}

	@Override
	public List<GradeSystem> getGradeSystems(GradeSystemSearchParams searchParams) {
		return gradeSystemDao.load(searchParams);
	}
	
	@Override
	public GradeSystem getGradeSystem(RepositoryEntry repositoryEntry, String subIdent) {
		GradeScale gradeScale = getGradeScale(repositoryEntry, subIdent);
		return gradeScale != null? gradeScale.getGradeSystem(): null;
	}

	@Override
	public boolean hasGradeScale(GradeSystemRef gradeSystem) {
		if (gradeSystem == null) return false;
		
		List<GradeScaleStats> stats = gradeScaleDao.loadStats(gradeSystem);
		return stats != null && !stats.isEmpty() && stats.get(0).getCount().longValue() > 0;
	}

	@Override
	public PerformanceClass createPerformanceClass(GradeSystem gradeSystem, String identifier) {
		return performanceClassDao.create(gradeSystem, identifier);
	}

	@Override
	public PerformanceClass updatePerformanceClass(PerformanceClass performanceClass) {
		return performanceClassDao.save(performanceClass);
	}

	@Override
	public void deletePerformanceClass(PerformanceClass performanceClass) {
		performanceClassDao.delete(performanceClass);
	}

	@Override
	public void deletePerformanceClasses(GradeSystemRef gradeSystem) {
		performanceClassDao.delete(gradeSystem);
	}

	@Override
	public List<PerformanceClass> getPerformanceClasses(GradeSystemRef gradeSystem) {
		return performanceClassDao.load(gradeSystem);
	}
	
	@Override
	public GradeScale updateGradeScale(GradeScale gradeScale) {
		return gradeScaleDao.save(gradeScale);
	}
	
	@Override
	public GradeScale updateOrCreateGradeScale(RepositoryEntry repositoryEntry, String subIdent, GradeScale gradeScale) {
		GradeScale currentGradeScale = getGradeScale(repositoryEntry, subIdent);
		if (currentGradeScale == null) {
			currentGradeScale = gradeScaleDao.create(repositoryEntry, subIdent);
		}
		copyAttributes(gradeScale, currentGradeScale);
		return gradeScaleDao.save(currentGradeScale);
	}

	@Override
	public void cloneGradeScale(RepositoryEntry sourceEntry, String sourceIdent, RepositoryEntry targetEntry,
			String targetIdent) {
		GradeScale sourceGradeScale = getGradeScale(sourceEntry, sourceIdent);
		if (sourceGradeScale == null) return;
		GradeScale targetGradeScale = getGradeScale(targetEntry, targetIdent);
		if (targetGradeScale != null) return;
		
		targetGradeScale = gradeScaleDao.create(targetEntry, targetIdent);
		copyAttributes(sourceGradeScale, targetGradeScale);
		targetGradeScale = gradeScaleDao.save(targetGradeScale);
		
		List<Breakpoint> breakpoints = getBreakpoints(sourceGradeScale);
		updateOrCreateBreakpoints(targetGradeScale, breakpoints);
	}

	private void copyAttributes(GradeScale sourceGradeScale, GradeScale targetGradeScale) {
		targetGradeScale.setGradeSystem(sourceGradeScale.getGradeSystem());
		targetGradeScale.setMinScore(sourceGradeScale.getMinScore());
		targetGradeScale.setMaxScore(sourceGradeScale.getMaxScore());
	}

	@Override
	public void deleteGradeScale(RepositoryEntry repositoryEntry, String subIdent) {
		breakpointDao.delete(repositoryEntry, subIdent);
		gradeScaleDao.delete(repositoryEntry, subIdent);
	}
	
	@Override
	public GradeScale getGradeScale(RepositoryEntry repositoryEntry, String subIdent) {
		GradeScaleSearchParams searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry);
		searchParams.setSubIdent(subIdent);
		List<GradeScale> gradeScales = gradeScaleDao.load(searchParams);
		return !gradeScales.isEmpty()? gradeScales.get(0): null;
	}

	@Override
	public List<GradeScaleStats> getGradeScaleStats() {
		return gradeScaleDao.loadStats(null);
	}

	@Override
	public Breakpoint createBreakpoint(GradeScale gradeScale) {
		return breakpointDao.create(gradeScale);
	}

	@Override
	public Breakpoint updateBreakpoint(Breakpoint breakpoint) {
		return breakpointDao.save(breakpoint);
	}
	
	@Override
	public void updateOrCreateBreakpoints(GradeScale gradeScale, List<Breakpoint> breakpoints) {
		GradeSystemType gradeSystemType = gradeScale.getGradeSystem().getType();
		
		List<Breakpoint> currentBreakpoints = breakpointDao.load(gradeScale);
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
				currentBreakpoint = breakpointDao.create(gradeScale);
			}
			if ((breakpoint.getScore() != null && currentBreakpoint.getScore() == null)
					|| (breakpoint.getScore() == null && currentBreakpoint.getScore() != null)
					|| breakpoint.getScore().compareTo(currentBreakpoint.getScore()) != 0
					|| !Objects.equals(breakpoint.getGrade(), currentBreakpoint.getGrade())
					|| !Objects.equals(breakpoint.getBestToLowest(), currentBreakpoint.getBestToLowest())) {
				currentBreakpoint.setScore(breakpoint.getScore());
				currentBreakpoint.setGrade(breakpoint.getGrade());
				currentBreakpoint.setBestToLowest(breakpoint.getBestToLowest());
				breakpointDao.save(currentBreakpoint);
			}
		}
		
		// Delete
		List<Long> keyToDelete = currentBreakpoints.stream()
				.filter(bp -> !idents.contains(getIdent(bp, gradeSystemType)))
				.map(Breakpoint::getKey)
				.collect(Collectors.toList());
		breakpointDao.delete(keyToDelete);
		
	}
	
	private Object getIdent(Breakpoint breakpoint, GradeSystemType gradeSystemType) {
		return GradeSystemType.numeric == gradeSystemType ? breakpoint.getGrade() : breakpoint.getBestToLowest();
	}

	@Override
	public void deleteBreakpoint(Breakpoint breakpoint) {
		breakpointDao.delete(Collections.singletonList(breakpoint.getKey()));
	}
	
	@Override
	public void deleteBreakpoints(GradeScale gradeScale) {
		breakpointDao.delete(gradeScale);
	}

	@Override
	public List<Breakpoint> getBreakpoints(GradeScaleRef gradeScale) {
		return breakpointDao.load(gradeScale);
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
						new BigDecimal(performanceClasses.get(0).getBestToLowest() -1), NumericResolution.whole,
						Rounding.down, null, minScore, maxScore)
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
