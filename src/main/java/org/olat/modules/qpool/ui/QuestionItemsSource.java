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
package org.olat.modules.qpool.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ui.metadata.QPoolSearchEvent;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItemsSource {
	
	public String getName();
	
	public Controller getSourceController(UserRequest ureq, WindowControl wControl);
	
	public boolean isCreateEnabled();

	public boolean isCopyEnabled();

	public boolean isImportEnabled();
	
	public boolean isRemoveEnabled();
	
	public boolean isAuthorRightsEnable();

	public boolean isDeleteEnabled();
	
	public boolean isBulkChangeEnabled();

	public boolean askEditable();
	
	public boolean isAdminItemSource();
	
	public boolean isStatusFilterEnabled();
	
	public QuestionStatus getStatusFilter();
		
	public void setStatusFilter(QuestionStatus questionStatus);
	
	public boolean askAddToSource();

	public boolean askAddToSourceDefault();

	public String getAskToSourceText(Translator translator);
	
	public void addToSource(List<QuestionItem> items, boolean editable);

	public int postImport(List<QuestionItem> items, boolean editable);
	
	public void removeFromSource(List<QuestionItemShort> items);
	
	public int getNumOfItems(boolean withExtendedSearchParams);
	
	public void setExtendedSearchParams(QPoolSearchEvent parameters);
	
	public List<QuestionItemView> getItems(Collection<Long> keys);
	
	/**
	 * Load the item view without any predefined restrictions. This can be
	 * necessary when a reload is done after the item is possibly already removed
	 * from the source.
	 * 
	 * @param itemKey
	 * @return
	 */
	public QuestionItemView getItemWithoutRestrictions(Long key);
	
	public ResultInfos<QuestionItemView> getItems(String query, int firstResult, int maxResults, SortKey... orderBy);

}
