/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
