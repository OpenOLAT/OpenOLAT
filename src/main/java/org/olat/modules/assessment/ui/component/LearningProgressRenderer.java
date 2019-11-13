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
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
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
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		LearningProgressComponent lpc = (LearningProgressComponent) source;
		
		if (Boolean.TRUE.equals(lpc.getFullyAssessed())) {
			sb.append("<i class='o_icon o_icon-fw o_lp_done'> </i> ").append(lpc.getTranslator().translate("fully.assessed"));
		} else if (AssessmentEntryStatus.notReady.equals(lpc.getStatus())) {
			// render nothing
		} else {
			renderProgressBar(renderer, sb, ubu, translator, lpc.getCompletion());
		}
	}

	private void renderProgressBar(Renderer renderer, StringOutput sb, URLBuilder ubu, Translator translator,
			float actual) {
		ProgressBar progressBar = new ProgressBar("progress-" + CodeHelper.getRAMUniqueID());
		progressBar.setMax(1.0f);
		progressBar.setWidthInPercent(true);
		progressBar.setPercentagesEnabled(true);
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setActual(actual);
		progressBar.getHTMLRendererSingleton().render(renderer, sb, progressBar, ubu, translator, null, null);
	}

}
