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

import java.util.Map;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.chart.DefaultD3Component;

/**
 * 
 * Initial date: 16 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreChart extends DefaultD3Component {
	
	private static final ScoreChartRenderer RENDERER = new ScoreChartRenderer();
	
	private Double minScore;
	private Double maxScore;
	private Map<Integer, Long> scoreToCount;

	public ScoreChart(String name) {
		super(name);
	}
	
	public Double getMinScore() {
		return minScore;
	}

	public void setMinScore(Double minScore) {
		this.minScore = minScore;
		setDirty(true);
	}

	public Double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
		setDirty(true);
	}

	public Map<Integer, Long> getScoreToCount() {
		return scoreToCount;
	}

	public void setScoreToCount(Map<Integer, Long> scoreToCount) {
		this.scoreToCount = scoreToCount;
		setDirty(true);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
