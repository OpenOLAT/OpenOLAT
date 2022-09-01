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
package org.olat.modules.catalog.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.Taxonomy;

/**
 * 
 * Initial date: 1 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogTaxonomySelectionController extends FormBasicController {

	private SingleSelection taxonomyEl;
	
	private final List<Taxonomy> taxonomies;

	public CatalogTaxonomySelectionController(UserRequest ureq, WindowControl wControl, List<Taxonomy> taxonomies) {
		super(ureq, wControl);
		this.taxonomies = taxonomies;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues taxonomySV = new SelectionValues();
		taxonomies.forEach(taxonomy -> {
			taxonomySV.add(entry(taxonomy.getKey().toString(), taxonomy.getDisplayName()));
		});
		taxonomyEl = uifactory.addDropdownSingleselect("taxonomy.select", formLayout, taxonomySV.keys(), taxonomySV.values());
		taxonomyEl.select(taxonomyEl.getKey(0), true);
		taxonomyEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == taxonomyEl) {
			Taxonomy selectedTaxonomy = taxonomies.stream()
					.filter(taxonomy -> taxonomy.getKey().toString().equals(taxonomyEl.getSelectedKey()))
					.findFirst().get();
			fireEvent(ureq, new TaxonomySelectionEvent(selectedTaxonomy));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}
	
	public static final class TaxonomySelectionEvent extends Event {

		private static final long serialVersionUID = -546542741844230201L;
		
		private final Taxonomy taxonomy;
		
		public TaxonomySelectionEvent(Taxonomy taxonomy) {
			super("taxonomy.selection");
			this.taxonomy = taxonomy;
		}

		public Taxonomy getTaxonomy() {
			return taxonomy;
		}
		
	}

}
