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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.ui.list.AbstractInfoPageGetStartedController;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.resource.OLATResource;

/**
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoPageGetStartedController extends AbstractInfoPageGetStartedController {

	private final CurriculumElement element;

	public CurriculumElementInfoPageGetStartedController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config, CurriculumElement element) {
		super(ureq, wControl, config);
		this.element = element;

		init(ureq);
	}

	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", StringHelper.escapeHtml(element.getType().getDisplayName()));
	}

	@Override
	protected OLATResource getResource() {
		return element.getResource();
	}

}
