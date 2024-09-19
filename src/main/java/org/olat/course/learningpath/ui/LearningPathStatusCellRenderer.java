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
package org.olat.course.learningpath.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.course.learningpath.LearningPathStatus;

/**
 * 
 * Initial date: 13 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LearningPathStatusCellRenderer extends LabelCellRenderer {

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof LearningPathStatus status) {
			return translator.translate(status.getI18nKey());
		}
		return null;
	}

	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof LearningPathStatus status) {
			return status.getCssClass();
		}
		return null;
	}

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof LearningPathStatus status) {
			return status.getCssClass();
		}
		return null;
	}


}
