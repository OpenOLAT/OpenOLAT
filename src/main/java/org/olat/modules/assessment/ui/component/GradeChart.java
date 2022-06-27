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
package org.olat.modules.assessment.ui.component;

import java.util.List;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.chart.DefaultD3Component;
import org.olat.modules.grade.GradeSystem;

/**
 * 
 * Initial date: 18 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeChart extends DefaultD3Component {
	
	private static final GradeChartRenderer RENDERER = new GradeChartRenderer();
	
	private GradeSystem gradeSystem;
	private List<GradeCount> gradeCounts;

	public GradeChart(String name) {
		super(name);
	}
	
	public GradeSystem getGradeSystem() {
		return gradeSystem;
	}

	public void setGradeSystem(GradeSystem gradeSystem) {
		this.gradeSystem = gradeSystem;
	}



	public List<GradeCount> getGradeCounts() {
		return gradeCounts;
	}

	public void setGradeCounts(List<GradeCount> gradeCounts) {
		this.gradeCounts = gradeCounts;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public static final class GradeCount {
		
		private final String grade;
		private Long count;
		
		public GradeCount(String grade, Long count) {
			this.grade = grade;
			this.count = count;
		}

		public String getGrade() {
			return grade;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
		
	}

}
