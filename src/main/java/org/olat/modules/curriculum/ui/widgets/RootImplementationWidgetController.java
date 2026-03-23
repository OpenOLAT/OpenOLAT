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
package org.olat.modules.curriculum.ui.widgets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 20 Mar 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RootImplementationWidgetController extends ImplementationWidgetController {

	private static final String BASE_BUSINESS_PATH = "[CurriculumAdmin:0]";

	@Autowired
	private CurriculumService curriculumService;

	public RootImplementationWidgetController(UserRequest ureq, WindowControl wControl, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, secCallback);

		initForm(ureq);
	}

	@Override
	protected String getBaseBusinessPath() {
		return BASE_BUSINESS_PATH;
	}

	@Override
	public String getId() {
		return "root-implementation-widget-v1";
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("relevant");
		Set<String> figureKeys = Set.of(
				CurriculumElementStatus.preparation.name(),
				CurriculumElementStatus.provisional.name(),
				CurriculumElementStatus.confirmed.name(),
				"pendingMemberships");
		prefs.setFocusFigureKeys(figureKeys);
		prefs.setNumRows(5);
		return prefs;
	}

	@Override
	protected void createAdditionalIndicators(FormLayoutContainer widgetCont) {
		createIndicator(widgetCont, "pendingMemberships", "filter.pending.memberships", "[Implementations:0][PendingMemberships:0]");
	}

	@Override
	protected void reload() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(getIdentity());
		searchParams.setImplementationsOnly(true);
		List<CurriculumElementInfos> elementInfos = curriculumService.getCurriculumElementsWithInfos(searchParams);

		Map<CurriculumElementStatus, Long> statusToCount = elementInfos.stream()
				.collect(Collectors.groupingBy(e -> e.curriculumElement().getElementStatus(), Collectors.counting()));
		updateIndicators(statusToCount);

		long pendingCount = elementInfos.stream()
				.filter(e -> e.numOfPending() > 0)
				.count();
		keyToIndicatorLink.get("pendingMemberships").setI18nKey(IndicatorsFactory.createLinkText(
				translate("filter.pending.memberships"),
				String.valueOf(pendingCount)));

		loadTableRows(filterElements(elementInfos));
	}

	private List<CurriculumElement> filterElements(List<CurriculumElementInfos> elementInfos) {
		if (StringHelper.containsNonWhitespace(keyFigureKey)) {
			if ("relevant".equals(keyFigureKey)) {
				return elementInfos.stream()
						.filter(e -> RELEVANT_STATUS.contains(e.curriculumElement().getElementStatus()))
						.map(CurriculumElementInfos::curriculumElement)
						.toList();
			}
			if ("pendingMemberships".equals(keyFigureKey)) {
				return elementInfos.stream()
						.filter(e -> e.numOfPending() > 0)
						.map(CurriculumElementInfos::curriculumElement)
						.toList();
			}
			if (CurriculumElementStatus.isValueOf(keyFigureKey)) {
				CurriculumElementStatus status = CurriculumElementStatus.valueOf(keyFigureKey);
				return elementInfos.stream()
						.filter(e -> status == e.curriculumElement().getElementStatus())
						.map(CurriculumElementInfos::curriculumElement)
						.toList();
			}
		}
		return elementInfos.stream()
				.map(CurriculumElementInfos::curriculumElement)
				.toList();
	}

}
