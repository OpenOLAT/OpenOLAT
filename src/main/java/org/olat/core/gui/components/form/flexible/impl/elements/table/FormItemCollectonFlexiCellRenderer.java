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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 17 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormItemCollectonFlexiCellRenderer implements FlexiCellRenderer {
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof FormItemCollectionCell collectionCell) {
			target.append("<div class=\"o_item_collection_cell\">");
			target.append("<div class=\"o_cell_items\">");
			// The FormItemCollection hat to been wrapped because FlexiTableClassicRenderer likes to render FormItemCollections self.
			for (FormItem formItem : collectionCell.getCollection().getFormItems()) {
				renderFormItem(renderer, target, ubu, translator, null, source.getFormItem(), formItem);
			}
			target.append("</div>");
			target.append("</div>");
		}
	}
	
	private void renderFormItem(Renderer renderer, StringOutput target, URLBuilder ubu, Translator translator,
			RenderResult renderResult, FlexiTableElementImpl ftE, FormItem formItem) {
		formItem.setTranslator(translator);
		if(ftE.getRootForm() != formItem.getRootForm()) {
			formItem.setRootForm(ftE.getRootForm());
		}
		ftE.addFormItem(formItem);
		if(formItem.isVisible()) {
			Component cmp = formItem.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, translator, renderResult, new String[] { "tablecell" });
			cmp.setDirty(false);
		} else if(formItem.getComponent() != null) {
			formItem.getComponent().setDirty(false);
		}
	}
	
	public static final class FormItemCollectionCell {
		
		private final FormItemCollection collection;

		public FormItemCollectionCell(FormItemCollection collection) {
			this.collection = collection;
		}

		public FormItemCollection getCollection() {
			return collection;
		}
		
	}

}
