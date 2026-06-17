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

/**
 * Strategy that provides anchor-aware display text and reference selection
 * entries for a {@link RelativeDatePickerController}.
 * <p>
 * Callers implement this interface in their own package with their own
 * translator, keeping domain-specific wording out of the core picker.
 *
 * Initial date: 2026-06-16<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public interface RelativeDateContext {

	/**
	 * Builds the read-only display text for a stored relative-date rule.
	 *
	 * @param ref      composed ref string, e.g. {@code "BEFORE_BEGIN"},
	 *                 {@code "AFTER_END"}, {@code "SAME_DAY_BEGIN"}
	 * @param unitKey  i18n key for the unit label chosen by the caller
	 *                 (e.g. {@code "unit.days"}, {@code "offer.unit.day"});
	 *                 the caller is responsible for singular/plural selection;
	 *                 may be {@code null} for SAME_DAY rules
	 * @param value    offset value; {@code null} or 0 for SAME_DAY
	 * @return display value with text and optional warn-icon CSS
	 */
	RelativeDateDisplayValue getDisplayValue(String ref, String unitKey, Integer value);

	/**
	 * Returns the reference-selection entries for the picker's {@code refEl}
	 * radio group (one entry per anchor, with or without a resolved date).
	 */
	SelectionValues getReferenceSelectionValues();

	/**
	 * Returns the unit-selection entries for the picker's unit button group.
	 * The standard four entries (days, weeks, months, years) are provided by
	 * {@link AbstractRelativeDateContext#getUnitSelectionValues()}.
	 */
	SelectionValues getUnitSelectionValues();

	/**
	 * Resolves a {@link RelativeDateSelection} to an absolute date.
	 * Returns {@code null} when the anchor date is not available or the
	 * selection is a SAME_DAY rule without an offset.
	 */
	Date resolveDate(RelativeDateSelection selection);

}
