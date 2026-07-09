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
package org.olat.repository;

import java.util.Date;

import org.olat.core.gui.components.date.AbstractRelativeDateContext;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * {@link org.olat.core.gui.components.date.RelativeDateContext} for dates
 * relative to the execution period (begin and end) of a
 * {@link org.olat.repository.RepositoryEntry} or
 * {@link org.olat.modules.curriculum.CurriculumElement}.
 * <p>
 * The canonical translation keys for execution-period wording live in this
 * package's {@code _i18n/} directory.
 *
 * Initial date: 2026-06-16<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ExecutionPeriodRelativeDateContext extends AbstractRelativeDateContext {

	private static final String BEGIN = "BEGIN";
	private static final String END = "END";

	private final Date beginDate;
	private final Date endDate;
	private final boolean showResolvedDate;

	private ExecutionPeriodRelativeDateContext(Translator callerTranslator, Date beginDate, Date endDate, boolean showResolvedDate) {
		super(Util.createPackageTranslator(ExecutionPeriodRelativeDateContext.class, callerTranslator.getLocale(), callerTranslator));
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.showResolvedDate = showResolvedDate;
	}

	public static ExecutionPeriodRelativeDateContext of(Translator callerTranslator, RepositoryEntry entry) {
		RepositoryEntryLifecycle lifecycle = entry != null ? entry.getLifecycle() : null;
		return of(callerTranslator,
				lifecycle != null ? lifecycle.getValidFrom() : null,
				lifecycle != null ? lifecycle.getValidTo() : null);
	}

	public static ExecutionPeriodRelativeDateContext of(Translator callerTranslator, CurriculumElement element) {
		return of(callerTranslator,
				element != null ? element.getBeginDate() : null,
				element != null ? element.getEndDate() : null);
	}

	public static ExecutionPeriodRelativeDateContext of(Translator callerTranslator, Date beginDate, Date endDate) {
		return new ExecutionPeriodRelativeDateContext(callerTranslator, beginDate, endDate, true);
	}

	public static ExecutionPeriodRelativeDateContext noExecutionPeriod(Translator callerTranslator) {
		return new ExecutionPeriodRelativeDateContext(callerTranslator, null, null, false);
	}

	@Override
	public boolean isResolvedDateVisible() {
		return showResolvedDate;
	}

	@Override
	protected String anchorRefLabel(String anchorId) {
		return switch (anchorId) {
			case BEGIN -> getTranslator().translate("relative.date.exec.period.begin.ref");
			case END -> getTranslator().translate("relative.date.exec.period.end.ref");
			default -> anchorId;
		};
	}

	@Override
	protected String anchorPlainLabel(String anchorId) {
		return switch (anchorId) {
			case BEGIN -> getTranslator().translate("relative.date.exec.period.begin.plain");
			case END -> getTranslator().translate("relative.date.exec.period.end.plain");
			default -> anchorId;
		};
	}

	@Override
	protected Date anchorDate(String anchorId) {
		return switch (anchorId) {
			case BEGIN -> beginDate;
			case END -> endDate;
			default -> null;
		};
	}

	@Override
	public SelectionValues getReferenceSelectionValues() {
		SelectionValues sv = new SelectionValues();
		if (!showResolvedDate) {
			sv.add(SelectionValues.entry(BEGIN, getTranslator().translate("relative.date.exec.period.begin.plain")));
			sv.add(SelectionValues.entry(END, getTranslator().translate("relative.date.exec.period.end.plain")));
		} else {
			Formatter fmt = Formatter.getInstance(getTranslator().getLocale());
			String noDateIcon = getTranslator().translate("relative.date.no.date.with.icon");
			sv.add(SelectionValues.entry(BEGIN,
					beginDate != null
							? getTranslator().translate("relative.date.exec.period.begin.with.date", fmt.formatDate(beginDate))
							: getTranslator().translate("relative.date.exec.period.begin.no.date", noDateIcon)));
			sv.add(SelectionValues.entry(END,
					endDate != null
							? getTranslator().translate("relative.date.exec.period.end.with.date", fmt.formatDate(endDate))
							: getTranslator().translate("relative.date.exec.period.end.no.date", noDateIcon)));
		}
		return sv;
	}

}
