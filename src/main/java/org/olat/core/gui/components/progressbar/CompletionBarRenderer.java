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
package org.olat.core.gui.components.progressbar;

import java.util.Locale;

import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CompletionBarRenderer {
	
	private final Locale locale;
	
	public CompletionBarRenderer(Locale locale) {
		this.locale = locale;
	}
	
	public String render(String percent) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("completion-" + CodeHelper.getRAMUniqueID(), locale);
		progressBar.setMax(100);
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setCssClass("o_progress_completion");
		progressBar.setAriaHidden(true);
		ProgressBarFactory.setValue(progressBar, StringHelper.containsNonWhitespace(percent) ? Float.parseFloat(percent) : 0.0f, BarColor.neutral);
		StringOutput target = new StringOutput(256);
		ProgressBarRenderer.renderProgressBar(target, progressBar, null);
		return target.toString();
	}
	
}
