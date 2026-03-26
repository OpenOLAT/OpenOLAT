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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;

/**
 *
 * Initial date: 20 Mar 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RootImplementationWidgetController extends ImplementationWidgetController {

	private static final String BASE_BUSINESS_PATH = "[CurriculumAdmin:0]";

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
	protected void reload() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(getIdentity());
		searchParams.setImplementationsOnly(true);
		List<CurriculumElementInfos> elementInfos = curriculumService.getCurriculumElementsWithInfos(searchParams);

		loadElementInfos(elementInfos);
	}

}
