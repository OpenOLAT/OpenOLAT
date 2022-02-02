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
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.assessment.ui.AssessedIdentityListController;

/**
 * 
 * Initial date: 1 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassedCellRenderer extends IconCssCellRenderer {
	
	private final Translator translator;
	
	public PassedCellRenderer(Locale locale) {
		translator = Util.createPackageTranslator(AssessedIdentityListController.class, locale);
	}

	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof Boolean) {
			Boolean passed = (Boolean)val;
			if (passed.booleanValue()) {
				return "o_icon o_icon-fw o_icon_passed";
			}
			return "o_icon o_icon-fw o_icon_failed";
		}
		return "o_icon o_icon-fw o_icon_passed_undefined";
	}

	@Override
	protected String getCellValue(Object val) {
		if (val instanceof Boolean) {
			Boolean passed = (Boolean)val;
			if (passed.booleanValue()) {
				return translator.translate("passed.true");
			}
			return translator.translate("passed.false");
		}
		return translator.translate("passed.undefined");
	}
	
	@Override
	protected String getCssClass(Object val) {
		if (val instanceof Boolean) {
			Boolean passed = (Boolean)val;
			if (passed.booleanValue()) {
				return "o_state o_passed";
			}
			return "o_state o_failed";
		}
		return "o_state o_noinfo";
	}

}
