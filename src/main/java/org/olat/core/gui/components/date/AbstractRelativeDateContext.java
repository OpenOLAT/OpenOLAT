/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.date;

import java.util.Date;

import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * Base implementation of {@link RelativeDateContext} that owns the generic
 * sentence frames ({@code relative.date.display.*}).
 * <p>
 * Subclasses supply the anchor-specific bits by implementing
 * {@link #anchorRefLabel(String)} and {@link #anchorDate(String)}.
 * <p>
 * <b>Null-date invariant:</b> when {@link #anchorDate(String)} returns
 * {@code null} (or the offset date cannot be resolved), the display value
 * carries the {@code "Kein Datum"} prefix and the warn-icon CSS
 * {@code "o_icon o_icon_warn"}. When a date resolves, no icon is set.
 *
 * Initial date: 2026-06-16<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public abstract class AbstractRelativeDateContext implements RelativeDateContext {

	private final Translator translator;

	/**
	 * @param domainTranslator translator covering the caller's own i18n package
	 *                         (including caller's package as fallback); locale is
	 *                         derived via {@link Translator#getLocale()}
	 */
	protected AbstractRelativeDateContext(Translator domainTranslator) {
		this.translator = Util.createPackageTranslator(AbstractRelativeDateContext.class, domainTranslator.getLocale(), domainTranslator);
	}

	protected Translator getTranslator() {
		return translator;
	}

	/**
	 * The anchor label in dative/genitive form, substituted into the sentence
	 * frame as {@code {2}}, e.g.
	 * {@code "dem Beginn des Durchführungszeitraums"}.
	 */
	protected abstract String anchorRefLabel(String anchorId);

	/**
	 * The anchor's absolute date; {@code null} when not available.
	 */
	protected abstract Date anchorDate(String anchorId);

	@Override
	public RelativeDateDisplayValue getDisplayValue(String ref, String unitKey, Integer value) {
		if (ref == null) {
			return new RelativeDateDisplayValue("", null);
		}

		String direction;
		String anchorId;
		if (ref.startsWith("SAME_DAY_")) {
			direction = "SAME_DAY";
			anchorId = ref.substring("SAME_DAY_".length());
		} else if (ref.startsWith("BEFORE_")) {
			direction = "BEFORE";
			anchorId = ref.substring("BEFORE_".length());
		} else if (ref.startsWith("AFTER_")) {
			direction = "AFTER";
			anchorId = ref.substring("AFTER_".length());
		} else {
			return new RelativeDateDisplayValue("", null);
		}

		Date anchor = anchorDate(anchorId);
		Date resolved = resolveDate(direction, anchor, unitKey, value);

		String prefix;
		String iconCss;
		if (!isResolvedDateVisible()) {
			prefix = "";
			iconCss = null;
		} else if (resolved != null) {
			prefix = Formatter.getInstance(translator.getLocale()).formatDate(resolved) + " – ";
			iconCss = null;
		} else {
			prefix = translator.translate("relative.date.no.date") + " – ";
			iconCss = "o_icon o_icon_warn";
		}

		String anchorLabel = anchorRefLabel(anchorId);
		String rule;
		if ("SAME_DAY".equals(direction)) {
			rule = translator.translate("relative.date.display.same.day", anchorLabel);
		} else {
			String unitLabel = unitKey != null ? translator.translate(toUnitI18nKey(unitKey, value)) : "";
			String valueStr = value != null ? String.valueOf(value) : "";
			String key = "BEFORE".equals(direction) ? "relative.date.display.before" : "relative.date.display.after";
			rule = translator.translate(key, valueStr, unitLabel, anchorLabel);
		}

		return new RelativeDateDisplayValue(prefix + rule, iconCss);
	}

	private static String toUnitI18nKey(String unitKey, Integer value) {
		String base = "relative.date.unit." + unitKey.toLowerCase().replaceAll("s$", "");
		return (value != null && value == 1) ? base : base + "s";
	}

	@Override
	public SelectionValues getUnitSelectionValues() {
		SelectionValues sv = new SelectionValues();
		sv.add(SelectionValues.entry("DAYS",   translator.translate("relative.date.unit.days")));
		sv.add(SelectionValues.entry("WEEKS",  translator.translate("relative.date.unit.weeks")));
		sv.add(SelectionValues.entry("MONTHS", translator.translate("relative.date.unit.months")));
		sv.add(SelectionValues.entry("YEARS",  translator.translate("relative.date.unit.years")));
		return sv;
	}

	@Override
	public Date resolveDate(RelativeDateSelection selection) {
		if (selection == null) {
			return null;
		}
		String ref = selection.getRefKey();
		if (ref == null) {
			return null;
		}
		String direction;
		String anchorId;
		if (!selection.isOffsetEnabled()) {
			anchorId = ref;
			direction = "SAME_DAY";
		} else {
			anchorId = ref;
			direction = selection.getDirection() != null ? selection.getDirection().name() : "BEFORE";
		}
		Date anchor = anchorDate(anchorId);
		String unitKey = selection.getUnitKey();
		Integer value = selection.getValue();
		return resolveDate(direction, anchor, unitKey, value);
	}

	private static Date resolveDate(String direction, Date anchor, String unitKey, Integer value) {
		if (anchor == null) {
			return null;
		}
		if ("SAME_DAY".equals(direction)) {
			return anchor;
		}
		if (unitKey == null || value == null || value <= 0) {
			return null;
		}
		int signed = "BEFORE".equals(direction) ? -value : value;
		return computeByUnitKey(anchor, unitKey, signed);
	}

	private static Date computeByUnitKey(Date anchor, String unitKey, int signed) {
		String lk = unitKey.toLowerCase();
		if (lk.endsWith(".day") || lk.endsWith(".days") || lk.equals("days")) {
			return DateUtils.addDays(anchor, signed);
		}
		if (lk.endsWith(".week") || lk.endsWith(".weeks") || lk.equals("weeks")) {
			return DateUtils.addWeeks(anchor, signed);
		}
		if (lk.endsWith(".month") || lk.endsWith(".months") || lk.equals("months")) {
			return DateUtils.addMonth(anchor, signed);
		}
		if (lk.endsWith(".year") || lk.endsWith(".years") || lk.equals("years")) {
			return DateUtils.addYears(anchor, signed);
		}
		return null;
	}

}
