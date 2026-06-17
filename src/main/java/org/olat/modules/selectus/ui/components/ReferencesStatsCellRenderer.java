/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferencesStatsCellRenderer implements FlexiCellRenderer {
	
	private final ReferenceType referenceType;
	
	public ReferencesStatsCellRenderer(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof ApplicationRefereeStats) {
			ApplicationRefereeStats stats = (ApplicationRefereeStats)cellValue;
			switch(referenceType) {
				case expert:
					renderStats(target, stats.getNumOfExperts(), stats.getNumOfSubmittedExperts());
					break;
				case recommendation:
					renderStats(target, stats.getNumOfRecommendations(), stats.getNumOfSubmittedRecommendations());
					break;
				case comparativeAssessmentExpert:
					renderStats(target, stats.getNumOfComparativeExperts(), stats.getNumOfSubmittedComparativeExperts());
					break;	
			}
		}
	}
	
	private void renderStats(StringOutput sb, int numOf, int numOfSubmitted) {
		sb.append(numOfSubmitted).append(" / ").append(numOf);
	}
}
