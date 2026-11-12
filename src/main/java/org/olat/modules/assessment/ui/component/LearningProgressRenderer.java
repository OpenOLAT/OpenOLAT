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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarFactory;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 13 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningProgressRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		LearningProgressComponent lpc = (LearningProgressComponent) source;
		
		if (Boolean.TRUE.equals(lpc.getFullyAssessed())) {
			if (lpc.isChartVisible()) {
				sb.append("<i class='o_icon o_icon-fw o_lp_done'> </i>");
			}
			if (lpc.isChartVisible() && lpc.isLabelVisible()) {
				sb.append(" ");
			}
			if (lpc.isLabelVisible()) {
				sb.append(lpc.getTranslator().translate("fully.assessed"));
			}
		} else if (AssessmentEntryStatus.notReady.equals(lpc.getStatus())) {
			// render nothing
		} else {
			if (lpc.isChartVisible()) {
				renderProgressBar(renderer, sb, ubu, translator, lpc.getCompletion(), lpc.isLabelVisible());
			}
			if (lpc.isChartVisible() && lpc.isLabelVisible()) {
				sb.append(" ");
			}
			if (lpc.isLabelVisible()) {
				sb.append(String.valueOf(Math.round(lpc.getCompletion() * 100d))).append("%");
			}
		}
	}

	private void renderProgressBar(Renderer renderer, StringOutput sb, URLBuilder ubu, Translator translator,
			float actual, boolean ariaHidden) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("progress-" + CodeHelper.getRAMUniqueID(), translator.getLocale());
		progressBar.setMax(1.0f);
		progressBar.setRenderStyle(RenderStyle.radial);
		progressBar.setRenderSize(RenderSize.inline);
		ProgressBarFactory.setValue(progressBar, actual, BarColor.success);
		progressBar.setAriaHidden(ariaHidden);
		progressBar.getHTMLRendererSingleton().render(renderer, sb, progressBar, ubu, translator, null, null);
	}

}
