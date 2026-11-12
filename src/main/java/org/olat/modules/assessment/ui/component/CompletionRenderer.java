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
import org.olat.core.gui.components.progressbar.ProgressBarFactory;
import org.olat.core.gui.components.progressbar.ProgressBarRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 22 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompletionRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		CompletionComponent ccp = (CompletionComponent) source;

		Translator completionTranslator = ccp.getCompletionTranslator();
		
		sb.append("<div id='").append(ccp.getFormItem().getFormDispatchId()).append("'");
		if(ccp.isEnded()) {
			sb.append(" class='o_sel_ended'>").append(completionTranslator.translate("run.ended"));
		} else {
			sb.append(">");
			ProgressBar progressBar = ProgressBarFactory.createProgressBar("completion-" + CodeHelper.getRAMUniqueID(), completionTranslator.getLocale());
			progressBar.setMax(1);
			ProgressBarFactory.setValue(progressBar, ccp.getCompletion() == null ? 0.0f : ccp.getCompletion().floatValue(), BarColor.neutral);
			progressBar.setAriaLabel(completionTranslator.translate("table.header.completion"));
			ProgressBarRenderer.renderProgressBar(sb, progressBar, null);
		}
		sb.append("</div>");
	}
}