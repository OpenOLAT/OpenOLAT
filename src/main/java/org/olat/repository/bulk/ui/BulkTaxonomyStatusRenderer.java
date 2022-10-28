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
package org.olat.repository.bulk.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.bulk.ui.TaxonomyController.BulkTaxonomyRow.BulkTaxonomyStatus;

/**
 * 
 * Initial date: 21 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BulkTaxonomyStatusRenderer extends LabelCellRenderer {
	
	private final Translator translator;
	
	public BulkTaxonomyStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof BulkTaxonomyStatus) {
			BulkTaxonomyStatus status = (BulkTaxonomyStatus)val;
			switch (status) {
			case add: return "o_icon o_icon_add o_icon-fw";
			case remove: return "o_icon o_icon_remove o_icon-fw";
			default:
			}
		}
		return null;
	}

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof BulkTaxonomyStatus) {
			BulkTaxonomyStatus status = (BulkTaxonomyStatus)val;
			switch (status) {
			case add: return "o_bulk_status_add";
			case remove: return "o_bulk_status_remove";
			default:
			}
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator transl) {
		if (val instanceof BulkTaxonomyStatus) {
			BulkTaxonomyStatus status = (BulkTaxonomyStatus)val;
			switch (status) {
			case add: return translator.translate("settings.bulk.taxonomy.status.add");
			case remove: return translator.translate("settings.bulk.taxonomy.status.remove");
			default:
			}
		}
		return null;
	}

}
