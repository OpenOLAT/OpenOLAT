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
package org.olat.modules.topicbroker.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractCSSIconFlexiCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.topicbroker.ui.TBActivityLogContext;
import org.olat.modules.topicbroker.ui.TBUIFactory;

/**
 * 
 * Initial date: Aug 6, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBActivityLogContextRenderer extends AbstractCSSIconFlexiCellRenderer {

	@Override
	protected String getCssClass(Object val) {
		if (val instanceof TBActivityLogContext context) {
			return "o_icon-fw " + TBUIFactory.getLogContextIconCss(context);
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof TBActivityLogContext context) {
			return TBUIFactory.getTranslatedLogContext(translator, context);
		}
		return null;
	}

	@Override
	protected String getHoverText(Object val, Translator translator) {
		return null;
	}

}
