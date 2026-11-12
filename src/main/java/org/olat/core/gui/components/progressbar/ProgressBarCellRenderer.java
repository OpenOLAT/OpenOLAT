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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 10 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProgressBarCellRenderer implements FlexiCellRenderer {
	
	private final RenderStyle renderStyle;
	private final RenderSize renderSize;
	private final LabelFormat labelFormat;
	private final BarColor barColor;
	
	public ProgressBarCellRenderer(RenderStyle renderStyle, RenderSize renderSize, LabelFormat labelFormat, BarColor barColor) {
		this.renderStyle = renderStyle;
		this.renderSize = renderSize;
		this.labelFormat = labelFormat;
		this.barColor = barColor;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		ProgressBar progressBar = createProgressBar(cellValue, translator.getLocale());
		if (progressBar != null) {
			ProgressBarRenderer.renderProgressBar(target, progressBar, null);
		}
	}
	
	protected ProgressBar createProgressBar(Object cellValue, Locale locale) {
		if (cellValue instanceof Number number) {
			ProgressBar progressBar = createProgressBar(locale);
			progressBar.setMax(1);
			ProgressBarFactory.setValue(progressBar, number.floatValue(), barColor);
			return progressBar;
		}
		return null;
	}
	
	protected ProgressBar createProgressBar(Locale locale) {
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("progress-" + CodeHelper.getRAMUniqueID(), locale);
		progressBar.setRenderStyle(renderStyle);
		progressBar.setRenderSize(renderSize);
		progressBar.setLabelFormat(labelFormat);
		return progressBar;
	}
	
}
