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
package org.olat.modules.lecture.ui.component;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.progressbar.ProgressBarCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;

/**
 * 
 * Initial date: 17 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureStatisticsCellRenderer extends ProgressBarCellRenderer {
	
	private final boolean withNumbers;
	private final Translator lectureTranslator;
	
	public LectureStatisticsCellRenderer(Locale locale) {
		this(locale, false);
	}
	
	public LectureStatisticsCellRenderer(Locale locale, boolean withNumbers) {
		super(RenderStyle.horizontal, RenderSize.medium, LabelFormat.none, BarColor.success);
		this.lectureTranslator = Util.createPackageTranslator(LectureRepositoryAdminController.class, locale);
		this.withNumbers = withNumbers;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof LectureBlockStatistics statistics) {
			if(withNumbers) {
				target.append("<div class='o_coaching_status'>");
				super.render(renderer, target, statistics, row, source, ubu, translator);
				renderNumber(target, statistics);
				target.append("</div>");
			} else {
				super.render(renderer, target, statistics, row, source, ubu, translator);
			}
		}
	}
	
	public String render(LectureBlockStatistics stats, Translator translator) {
		StringOutput target = new StringOutput(2048);
		render(null, target, stats, 0, null, null, translator);
		return target.toString();
	}
	
	@Override
	protected ProgressBar createProgressBar(Object cellValue, Translator translator) {
		if (cellValue instanceof LectureBlockStatistics stats) {
			ProgressBar progressBar = createProgressBar(translator.getLocale());
			progressBar.setMax(stats.getTotalPersonalPlannedLectures());
			progressBar.setSegments(List.of(
					new Segment(stats.getTotalAttendedLectures(), BarColor.success, lectureTranslator.translate("table.legend.attended")),
					new Segment(stats.getTotalAuthorizedAbsentLectures(), BarColor.warning, lectureTranslator.translate("table.legend.authorized")),
					new Segment(stats.getTotalDispensationLectures(), BarColor.info, lectureTranslator.translate("table.legend.dispensed")),
					new Segment(stats.getTotalAbsentLectures(), BarColor.danger, lectureTranslator.translate("table.legend.unauthorized"))));
			return progressBar;
		}
		return null;
	}
	
	private void renderNumber(StringOutput target, LectureBlockStatistics stats) {
		target.append("<div class='o_coaching_numbers'>")
		      .append(stats.getTotalAttendedLectures()).append("\u202F&vert;\u202F")
		      .append(stats.getTotalAuthorizedAbsentLectures()).append("\u202F&vert;\u202F")
		      .append(stats.getTotalDispensationLectures()).append("\u202F&vert;\u202F")
		      .append(stats.getTotalAbsentLectures())
		      .append("</div>");
	}
}
