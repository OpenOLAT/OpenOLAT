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
package org.olat.user.ui.organisation.element;

import java.util.Collection;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * Initial date: 2025-03-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface OrgSelectorElement extends FormItem {

	/**
	 * Returns the keys of the org items available in the org selector.
	 * 
	 * @return A set of org keys.
	 */
	Set<Long> getKeys();

	/**
	 * Sets the selection using a collection of org keys.
	 * 
	 * @param orgKeys A collection of org keys to set as selected.
	 */
	void setSelection(Collection<Long> orgKeys);

	/**
	 * Selects a single org.

	 * @param orgKey The org key of the selection.
	 */
	void setSelection(Long orgKey);

	/**
	 * Sets a selection by switching an item off using its orgKey.

	 * @param orgKey The org key of the selection to modify.
	 * @param selected If true, turns the selection on, if false turns it off.
	 */
	void select(Long orgKey, boolean selected);
	
	/**
	 * Set an optional text to display if no item is selected.
	 * 
	 * @param noSelectionText Text to display on the button if no item is selected.
	 */
	void setNoSelectionText(String noSelectionText);

	/**
	 * Returns a single selection or null if nothing is selected. Only call this if multiple selections are
	 * turned off.
	 *
	 * @return The org key of the single selection or null if nothing is selected.
	 */
	Long getSingleSelection();

	/**
	 * The set of selected org keys.
	 *
	 * @return A set of org keys.
	 */
	Set<Long> getSelection();

	Set<Long> getSelectedKeys();
	
	/**
	 * The org selector is in single or in multiple selection mode.
	 * The default mode is single selection mode. 
	 * In single selection mode, org items have the semantics and style of radio buttons. 
	 * In multiple selection mode, the org items have the semantics and style of check boxes.
	 *
	 * @param multipleSelection If true, the org selector is in multiple selection mode.
	 */
	void setMultipleSelection(boolean multipleSelection);

	/**
	 * Returns true if and only if exactly one selection exists.
	 *
	 * @return True if exactly one org is selected.
	 */
	boolean isExactlyOneSelected();
}
