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
package org.olat.ims.qti21.ui;

import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarFactory;
import org.olat.core.gui.components.progressbar.ProgressBarRenderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.ui.AssessmentResultController.Results;

/**
 * 
 * Initial date: 11 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentResultScoreRenderer {
	
	private final Translator translator;
	
	public AssessmentResultScoreRenderer(Translator translator) {
		this.translator = translator;
	}
	
	public String renderTest(Results results, String domId) {
		ProgressBar progressBar = createRadial(results, results.getCalculatedPassed(), RenderSize.large);
		progressBar.setAriaHidden(true);
		return render(progressBar, domId);
	}
	
	public String renderSection(Results results, String domId) {
		ProgressBar progressBar = createRadial(results, results.getPass(), RenderSize.medium);
		progressBar.setAriaLabel(results.getTitle());
		return render(progressBar, domId);
	}
	
	public String renderItem(Results results) {
		ProgressBar progressBar = createProgressBar(results, null);
		progressBar.setAriaHidden(true);
		return render(progressBar, null);
	}
	
	private ProgressBar createRadial(Results results, Boolean passed, RenderSize renderSize) {
		ProgressBar progressBar = createProgressBar(results, passed);
		progressBar.setRenderStyle(RenderStyle.radial);
		progressBar.setRenderSize(renderSize);
		progressBar.setLabelFormat(LabelFormat.percent);
		progressBar.setProgressAnimationEnabled(true);
		progressBar.setInfo(getScoreInfo(results));
		if (passed != null) {
			progressBar.setCssClass(passed.booleanValue() ? "o_progress_passed" : "o_progress_failed");
		}
		return progressBar;
	}
	
	private ProgressBar createProgressBar(Results results, Boolean passed) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("score-" + CodeHelper.getRAMUniqueID(), translator.getLocale());
		progressBar.setMax(100);
		BarColor color = passed == null ? BarColor.primary : (passed.booleanValue() ? BarColor.success : BarColor.danger);
		String scorePercent = results.getScorePercent();
		ProgressBarFactory.setValue(progressBar, StringHelper.containsNonWhitespace(scorePercent) ? Float.parseFloat(scorePercent) : 0.0f, color);
		return progressBar;
	}
	
	private String getScoreInfo(Results results) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<span class='o_score'>").append(translator.translate("table.header.score")).append(": ")
			.append("<span title=\"").append(StringHelper.escapeHtml(translator.translate("results.score.yourscore")))
			.append("\" class='o_sel_assessmentitem_score text-muted xsmall'>").append(results.getScore()).append("</span></span>");
		if (results.hasMaxScore()) {
			sb.append("<span class='o_score_max'>").append(translator.translate("results.of")).append(" ")
				.append("<span title=\"").append(StringHelper.escapeHtml(translator.translate("score.max")))
				.append("\" class='o_sel_assessmentitem_maxscore text-muted xsmall'>").append(results.getMaxScore()).append("</span></span>");
		}
		return sb.toString();
	}
	
	private String render(ProgressBar progressBar, String domId) {
		StringOutput target = new StringOutput(1024);
		ProgressBarRenderer.renderProgressBar(target, progressBar, domId);
		return target.toString();
	}
	
}
