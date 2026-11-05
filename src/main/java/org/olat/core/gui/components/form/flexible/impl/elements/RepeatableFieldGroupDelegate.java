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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.form.flexible.FormItemContainer;

/**
 * Creates the rows of a {@link RepeatableFieldGroupController}.
 * 
 * Initial date: 28 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public interface RepeatableFieldGroupDelegate<R extends RepeatableFieldGroupRow> {
	
	/**
	 * Create the form items of a new, empty row in the container.
	 * 
	 * @param id A unique identifier of the row. Use it as prefix of the item names.
	 * @param container The container of the items
	 * @return The new row
	 */
	R createRow(String id, FormItemContainer container);
	
	void copyRow(R source, R target);
	
	/**
	 * Validate the values of a row and set the errors on its items.
	 * Called after the validation of the items.
	 *
	 * @param row The row
	 * @return true if the row is valid
	 */
	default boolean validateRow(R row) {
		return true;
	}
	
	/**
	 * @param column The index of the column
	 * @return A CSS class for the width of the column, e.g. o_rfg_col_s, or null
	 */
	default String getColumnCssClass(int column) {
		return null;
	}
	
}
