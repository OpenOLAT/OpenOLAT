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
package org.olat.modules.quality.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.quality.QualityDataCollectionStatus;

/**
 * 
 * Initial date: 16.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionStatusCellRenderer extends LabelCellRenderer {

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof QualityDataCollectionStatus) {
			QualityDataCollectionStatus status = (QualityDataCollectionStatus) val;
			return "o_qual_dc_status_" + status.name().toLowerCase() + "_light";
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof QualityDataCollectionStatus) {
			QualityDataCollectionStatus status = (QualityDataCollectionStatus) val;
			return "o_icon_qual_dc_" + status.name().toLowerCase();
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof QualityDataCollectionStatus) {
			QualityDataCollectionStatus status = (QualityDataCollectionStatus) val;
			return translator.translate("data.collection.status." + status.name().toLowerCase());
		}
		return null;
	}

}
