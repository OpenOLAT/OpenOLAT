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
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: Jan 22, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumImplementationWidgetController extends ImplementationWidgetController {

	private final CurriculumRef curriculum;
	private final String baseBusinessPath;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumImplementationWidgetController(UserRequest ureq, WindowControl wControl, CurriculumRef curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, secCallback);
		this.curriculum = curriculum;
		baseBusinessPath = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + curriculum.getKey() + "]";

		initForm(ureq);
	}

	@Override
	protected String getBaseBusinessPath() {
		return baseBusinessPath;
	}

	@Override
	public String getId() {
		return "curriculum-implementation-widget-v1-" + curriculum.getKey();
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("relevant");
		Set<String> figureKeys = Set.of(
				CurriculumElementStatus.preparation.name(),
				CurriculumElementStatus.provisional.name(),
				CurriculumElementStatus.confirmed.name());
		prefs.setFocusFigureKeys(figureKeys);
		prefs.setNumRows(5);
		return prefs;
	}

	@Override
	protected void reload() {
		List<CurriculumElement> elements = curriculumService.getCurriculumElementsByCurriculums(List.of(curriculum)).stream()
				.filter(element -> element.getParent() == null)
				.toList();

		Map<CurriculumElementStatus, Long> statusToCount = elements.stream()
				.collect(Collectors.groupingBy(CurriculumElement::getElementStatus, Collectors.counting()));
		updateIndicators(statusToCount);

		loadTableRows(filterElements(elements));
	}

	private List<CurriculumElement> filterElements(List<CurriculumElement> elements) {
		if (StringHelper.containsNonWhitespace(keyFigureKey)) {
			if ("relevant".equals(keyFigureKey)) {
				return elements.stream()
						.filter(e -> RELEVANT_STATUS.contains(e.getElementStatus()))
						.toList();
			}
			if (CurriculumElementStatus.isValueOf(keyFigureKey)) {
				CurriculumElementStatus status = CurriculumElementStatus.valueOf(keyFigureKey);
				return elements.stream()
						.filter(e -> status == e.getElementStatus())
						.toList();
			}
		}
		return elements;
	}

}
