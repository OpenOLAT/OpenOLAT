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
package org.olat.modules.qpool.ui.datasource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DefaultResultInfos;
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
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.metadata.QPoolSearchEvent;

/**
 * An empty data source.
 * 
 * 
 * Initial date: 06.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyItemsSource implements QuestionItemsSource {

    @Override
    public String getName() {
        return null;
    }

    @Override
	public void setExtendedSearchParams(QPoolSearchEvent parameters) {
		//
	}

	@Override
    public Controller getSourceController(UserRequest ureq, WindowControl wControl) {
        return null;
    }

	@Override
	public boolean isCreateEnabled() {
		return false;
	}

	@Override
	public boolean isCopyEnabled() {
		return false;
	}

	@Override
	public boolean isImportEnabled() {
		return false;
	}

	@Override
	public boolean isAuthorRightsEnable() {
		return false;
	}

    @Override
    public boolean isRemoveEnabled() {
        return false;
    }

	@Override
	public boolean isBulkChangeEnabled() {
		return false;
	}

    @Override
    public boolean isDeleteEnabled() {
        return false;
    }
	
	@Override
	public boolean isAdminItemSource() {
		return false;
	}
    
    @Override
	public boolean askEditable() {
		return false;
	}

	@Override
	public boolean isStatusFilterEnabled() {
		return false;
	}

	@Override
	public QuestionStatus getStatusFilter() {
		return null;
	}
	
	@Override
	public void setStatusFilter(QuestionStatus questionStatus) {
		// not enabled
	}

	@Override
	public boolean askAddToSource() {
		return false;
	}

	@Override
	public boolean askAddToSourceDefault() {
		return false;
	}

	@Override
	public String getAskToSourceText(Translator translator) {
		return "";
	}

	@Override
	public void addToSource(List<QuestionItem> items, boolean editable) {
		//
	}

	@Override
    public int postImport(List<QuestionItem> items, boolean editable) {
        return 0;
    }

    @Override
    public void removeFromSource(List<QuestionItemShort> items) {
    	//
    }

    @Override
    public int getNumOfItems(boolean withExtendedSearchParams) {
        return 0;
    }

    @Override
    public List<QuestionItemView> getItems(Collection<Long> keys) {
        return Collections.emptyList();
    }

    @Override
    public QuestionItemView getItemWithoutRestrictions(Long itemKey) {
        return null;
    }

    @Override
    public ResultInfos<QuestionItemView> getItems(String query, int firstResult, int maxResults, SortKey... orderBy) {
        return new DefaultResultInfos<>();
    }
}
