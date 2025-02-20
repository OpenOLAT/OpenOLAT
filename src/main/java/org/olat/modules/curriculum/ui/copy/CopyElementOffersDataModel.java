/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementOffersDataModel extends DefaultFlexiTableDataModel<CopyOfferRow>
implements SortableFlexiTableDataModel<CopyOfferRow> {
	
	private static final CopyOfferCols[] COLS = CopyOfferCols.values();
	
	private final Translator translator;
	
	public CopyElementOffersDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CopyOfferRow> sorted = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(sorted);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CopyOfferRow offerRow = this.getObject(row);
		return getValueAt(offerRow, col);
	}

	@Override
	public Object getValueAt(CopyOfferRow row, int col) {
		return switch(COLS[col]) {
			case type -> row.getType();
			case label -> row.getOffer().getLabel();
			case publishedIn -> row;
			case organisations -> getOfferOrganisations(row);
			case availableIn -> row.getValidFromToEl() == null
					? translator.translate("offer.available.in.status.curriculum.element")
					: row.getValidFromToEl();
			case price -> getPrice(row);
			default -> "ERROR";
		};
	}
	
	private String getPrice(CopyOfferRow offerRow) {
		if(offerRow.getOfferAccess().getMethod().isPaymentMethod()) {
			return PriceFormat.fullFormat(offerRow.getOffer().getPrice());
		}
		return null;
	}
	
	private String getOfferOrganisations(CopyOfferRow offerRow) {
		StringBuilder sb = new StringBuilder();
		if(offerRow.getOrganisations() != null) {
			for(Organisation organisation:offerRow.getOrganisations()) {
				if(sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(organisation.getDisplayName());
			}
		}
		return sb.toString();
	}
	
	public enum CopyOfferCols implements FlexiSortableColumnDef {
		type("table.header.type"),
		label("table.header.offer.label"),
		publishedIn("table.header.published.in"),
		organisations("table.header.released.for"),
		availableIn("offer.available.in"),
		price("booking.offer.price");

		private final String i18nKey;

		private CopyOfferCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return  true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
