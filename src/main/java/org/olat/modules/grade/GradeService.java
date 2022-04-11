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
package org.olat.modules.grade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface GradeService {
	
	public boolean isGradeServiceIdentifierAvailable(String identifier);

	public GradeSystem createGradeSystem(String identifier, GradeSystemType type);
	
	public GradeSystem updateGradeSystem(GradeSystem gradeSystem);
	
	public void deleteGradeSystem(GradeSystemRef gradeSystem);
	
	public List<GradeSystem> getGradeSystems(GradeSystemSearchParams searchParams);

	public GradeSystem getGradeSystem(RepositoryEntryRef repositoryEntry, String subIdent);
	
	public boolean hasGradeScale(GradeSystemRef gradeSystem);
	
	public PerformanceClass createPerformanceClass(GradeSystem gradeSystem, String identifier);
	
	public PerformanceClass updatePerformanceClass(PerformanceClass performanceClass);
	
	public void deletePerformanceClass(PerformanceClass performanceClass);
	
	public void deletePerformanceClasses(GradeSystemRef gradeSystem);

	public List<PerformanceClass> getPerformanceClasses(GradeSystemRef gradeSystem);
	
	public GradeScale updateGradeScale(GradeScale gradeScale);
	
	public GradeScale updateOrCreateGradeScale(RepositoryEntry repositoryEntry, String subIdent, GradeScale gradeScale);
	
	public void cloneGradeScale(RepositoryEntryRef sourceEntry, String sourceIdent, RepositoryEntry targetEntry,
			String targetIdent);

	public void deleteGradeScale(RepositoryEntryRef repositoryEntry, String subIdent);
	
	public GradeScale getGradeScale(RepositoryEntryRef repositoryEntry, String subIdent);
	
	public List<GradeScaleStats> getGradeScaleStats();
	
	public Breakpoint createBreakpoint(GradeScale gradeScale);
	
	public Breakpoint updateBreakpoint(Breakpoint breakpoint);
	
	/**
	 * Creates or updates the breakpoints. The sync is done by getBestToLowest().
	 * Superfluous breakpoints are deleted.
	 * 
	 */
	public void updateOrCreateBreakpoints(GradeScale gradeScale, List<Breakpoint> breakpoints);
	
	public void deleteBreakpoint(Breakpoint breakpoint);

	public void deleteBreakpoints(GradeScale gradeScale);
	
	public List<Breakpoint> getBreakpoints(GradeScaleRef gradeScale);

	/** 
	 * Get all possible grades.
	 */
	public List<String> getGrades(GradeSystem gradeSystem, BigDecimal minScore, BigDecimal maxScore);
	
	/**
	 * Returns initial lower bounds for the performances classes. The
	 * PerformanceClass.getBestToLowest() acts as the key in the map.
	 */
	public Map<Integer, BigDecimal> getInitialTextLowerBounds(List<PerformanceClass> performanceClasses,
			BigDecimal minScore, BigDecimal maxScore);
	
	public NavigableSet<GradeScoreRange> getGradeScoreRanges(GradeScale gradeScale, Locale locale);
	
	public NavigableSet<GradeScoreRange> getGradeScoreRanges(GradeSystem gradeSystem, List<Breakpoint> breakpoints,
			BigDecimal minScore, BigDecimal maxScore, Locale locale);

	public GradeScoreRange getGradeScoreRange(NavigableSet<GradeScoreRange> gradeScoreRanges, Float score);

	public BigDecimal getMinPassedScore(GradeScale gradeScale);

}
