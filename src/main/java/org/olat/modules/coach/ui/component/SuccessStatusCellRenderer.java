/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui.component;

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
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.modules.coach.ui.CourseListController;

/**
 *
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SuccessStatusCellRenderer extends ProgressBarCellRenderer implements CustomCellRenderer {

	private final Translator customTranslator;
	private final Translator coachTranslator;

	public SuccessStatusCellRenderer(Locale locale) {
		this(null, locale);
	}

	public SuccessStatusCellRenderer(Translator translator) {
		this(translator, translator.getLocale());
	}
	
	private SuccessStatusCellRenderer(Translator customTranslator, Locale locale) {
		super(RenderStyle.horizontal, RenderSize.medium, LabelFormat.none, BarColor.passed);
		this.customTranslator = customTranslator;
		this.coachTranslator = Util.createPackageTranslator(CourseListController.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		renderStatus(target, cellValue, translator);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		renderStatus(sb, val, customTranslator);
	}

	private void renderStatus(StringOutput target, Object cellValue, Translator translator) {
		if(cellValue instanceof SuccessStatus status) {
			long passed = status.numPassed();
			long failed = status.numFailed();
			long undefined = status.numUndefined();
			long total = passed + failed + undefined;
			long passedPercent = (total == 0L) ? 0L : Math.round(100.0d * ((double)passed / (double)total));
			long failedPercent = (total == 0L) ? 0L : max100Percent(Math.round(100.0d * ((double)failed / (double)total)), passedPercent);
			long undefinedPercent = (total == 0L) ? 0L : max100Percent(Math.round(100.0d * ((double)undefined / (double)total)), passedPercent, failedPercent);
			String tooltip = translator.translate("success.status.tooltip",
					String.valueOf(passed), String.valueOf(passedPercent),
					String.valueOf(failed), String.valueOf(failedPercent),
					String.valueOf(undefined), String.valueOf(undefinedPercent));
			target.append("<div class='o_coaching_status' title='").append(StringHelper.escapeForHtmlAttribute(tooltip)).append("'>");
			super.render(null, target, status, 0, null, null, translator);
			renderNumber(target, status);
			target.append("</div>");
		}
	}
	
	@Override
	protected ProgressBar createProgressBar(Object cellValue, Translator translator) {
		if (cellValue instanceof SuccessStatus status) {
			ProgressBar progressBar = createProgressBar(translator.getLocale());
			progressBar.setMax(status.numPassed() + status.numFailed() + status.numUndefined());
			progressBar.setSegments(List.of(
					new Segment(status.numPassed(), BarColor.passed, coachTranslator.translate("passed.true")),
					new Segment(status.numFailed(), BarColor.failed, coachTranslator.translate("passed.false"))));
			return progressBar;
		}
		return null;
	}
	
	private long max100Percent(long val, long... precedentVals) {
		long total = 0l;
		for(long precedentVal:precedentVals) {
			total += precedentVal;
		}
		
		long diff = 100 - val - total;
		if(diff < 0l) {
			return val + diff;
		}
		return val;
	}
	
	private void renderNumber(StringOutput target, SuccessStatus status) {
		target.append("<div class='o_coaching_numbers'>")
		      .append(status.numPassed()).append("\u202F&vert;\u202F")
		      .append(status.numFailed()).append("\u202F&vert;\u202F")
		      .append(status.numUndefined())
		      .append("</div>");
	}
}
