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
package org.olat.modules.quality.generator.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.quality.generator.QualityPreviewStatus;

/**
 * 
 * Initial date: 25.01.2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PreviewStatusCellRenderer extends LabelCellRenderer {

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof QualityPreviewStatus status) {
			return QualityPreviewStatus.getElementLightCss(status);
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof QualityPreviewStatus status) {
			return QualityPreviewStatus.getIconCss(status);
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof QualityPreviewStatus status) {
			return QualityPreviewStatus.getTranslatedStatus(translator, status);
		}
		return null;
	}

}
