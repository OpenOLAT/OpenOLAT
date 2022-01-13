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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Collection;
import java.util.Comparator;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;

/**
 * 
 * Initial date: 5 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AutoCompletionMultiSelection extends FormItem {
	
	/**
	 * Sets the comparator to sort the selection values. Default comparator is
	 * SelectionValues.VALUE_ASC. If set to null, the selection values are not
	 * sorted e.g. they are displayed like returned by the AutoCompletionSource.
	 *
	 * @param selectionComporator
	 */
	public void setSelectionComporator(Comparator<SelectionValue> selectionComporator);

	/**
	 * Set a placeholder to the search field
	 *
	 * @param placeholder
	 */
	public void setSearchPlaceholder(String placeholder);
	
	public int getSelectedKeysSize();
	
	public Collection<String> getSelectedKeys();
	
	public void setSelectedKeys(Collection<String> keys);

	public SelectionValues getSelection();

	
	public interface AutoCompletionSource {
		
		public SelectionValues getSelectionValues(Collection<String> keys);

		public SearchResult getSearchResult(String searchText);
		
		public class SearchResult {
			
			private final int countTotal;
			private final int countCurrent;
			private final SelectionValues SelectionValues;
			
			public SearchResult(int countTotal, int countCurrent, SelectionValues selectionValues) {
				this.countTotal = countTotal;
				this.countCurrent = countCurrent;
				SelectionValues = selectionValues;
			}
			
			public int getCountTotal() {
				return countTotal;
			}
			
			public int getCountCurrent() {
				return countCurrent;
			}
			
			public SelectionValues getSelectionValues() {
				return SelectionValues;
			}
		}
	
	}
	
	public class FlexiAutoCompletionSelectionEvent extends FormEvent {

		private static final long serialVersionUID = -3442684460452938957L;
		
		private final SelectionValues selectionValues;
		
		public FlexiAutoCompletionSelectionEvent(FormItem source, SelectionValues selectionValues) {
			super("ac-selection", source);
			this.selectionValues = selectionValues;
		}

		public SelectionValues getSelectionValues() {
			return selectionValues;
		}
		
	}
}
