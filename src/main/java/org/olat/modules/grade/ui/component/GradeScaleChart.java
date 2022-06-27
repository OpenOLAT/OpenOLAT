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
package org.olat.modules.grade.ui.component;

import java.util.List;
import java.util.NavigableSet;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.chart.DefaultD3Component;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeSystem;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleChart extends DefaultD3Component {
	
	private static final GradeScaleChartRenderer RENDERER = new GradeScaleChartRenderer();
	
	private GradeSystem gradeSystem;
	private List<Breakpoint> breakpoints;
	private NavigableSet<GradeScoreRange> gradeScoreRanges;

	public GradeScaleChart(String name) {
		super(name);
	}

	public GradeSystem getGradeSystem() {
		return gradeSystem;
	}

	public void setGradeSystem(GradeSystem gradeSystem) {
		this.gradeSystem = gradeSystem;
	}

	public List<Breakpoint> getBreakpoints() {
		return breakpoints;
	}

	public void setBreakpoints(List<Breakpoint> breakpoints) {
		this.breakpoints = breakpoints;
	}

	public NavigableSet<GradeScoreRange> getGradeScoreRanges() {
		return gradeScoreRanges;
	}

	public void setGradeScoreRanges(NavigableSet<GradeScoreRange> gradeScoreRanges) {
		this.gradeScoreRanges = gradeScoreRanges;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
