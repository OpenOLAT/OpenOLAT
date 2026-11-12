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
package org.olat.course.nodes.practice.ui.renders;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.progressbar.ProgressBarCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.practice.ui.Levels;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LevelBarsCellRenderer extends ProgressBarCellRenderer {

	public LevelBarsCellRenderer() {
		super(RenderStyle.horizontal, RenderSize.medium, LabelFormat.none, BarColor.primary);
	}

	@Override
	protected ProgressBar createProgressBar(Object cellValue, Translator translator) {
		if (cellValue instanceof Levels levels) {
			ProgressBar progressBar = createProgressBar(translator.getLocale());
			progressBar.setMax(levels.getTotal());
			progressBar.setSegments(getSegments(levels, translator));
			return progressBar;
		}
		return null;
	}
	
	public static List<Segment> getSegments(Levels levels, Translator translator) {
		List<Segment> segments = new ArrayList<>(levels.getNumOfLevels() + 1);
		for (int i = 1; i <= levels.getNumOfLevels(); i++) {
			segments.add(new Segment(levels.getLevel(i), BarColor.primary, levels.getCssClass(i),
					translator.translate("level.stat.percent") + " " + i));
		}
		segments.add(new Segment(levels.getNot(), BarColor.primary, levels.getNotCssClass(), translator.translate("level.stat.not")));
		return segments;
	}
}
