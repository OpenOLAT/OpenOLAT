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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 26 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProgressRadialCellRenderer implements FlexiCellRenderer {
	
	private final BarColor barColor;
	
	public ProgressRadialCellRenderer() {
		this(BarColor.primary);
	}

	public ProgressRadialCellRenderer(BarColor barColor) {
		this.barColor = barColor;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Double) {
			Double completion = (Double) cellValue;
			ProgressBar progressBar = new ProgressBar("progress-" + CodeHelper.getRAMUniqueID(), 100,
					completion.floatValue(), Float.valueOf(1), null);
			progressBar.setWidthInPercent(true);
			progressBar.setLabelAlignment(LabelAlignment.none);
			progressBar.setRenderStyle(RenderStyle.radial);
			progressBar.setRenderSize(RenderSize.inline);
			progressBar.setBarColor(barColor);
			progressBar.getHTMLRendererSingleton().render(renderer, target, progressBar, ubu, translator, null, null);
		}
	}

}
