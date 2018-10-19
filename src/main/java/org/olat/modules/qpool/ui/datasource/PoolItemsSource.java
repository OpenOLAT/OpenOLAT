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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 26.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolItemsSource extends DefaultItemsSource {
	
	private final Pool pool;
	
	public PoolItemsSource(Identity me, Roles roles, Locale locale, Pool pool) {
		super(me, roles, locale, pool.getName());
		this.pool = pool;
		getDefaultParams().setPoolKey(pool.getKey());
	}
	
	public Pool getPool() {
		return pool;
	}

	@Override
	public void removeFromSource(List<QuestionItemShort> items) {
		qpoolService.removeItemsInPool(items, pool);
	}
	
	@Override
	public boolean askEditable() {
		return true;
	}

	@Override
	public boolean askAddToSource() {
		return true;
	}

	@Override
	public boolean askAddToSourceDefault() {
		return false;
	}

	@Override
	public String getAskToSourceText(Translator translator) {
		return translator.translate("pool.add.to.source", new String[] {pool.getName()});
	}

	@Override
	public void addToSource(List<QuestionItem> items, boolean editable) {
		qpoolService.addItemsInPools(items, Collections.singletonList(pool), editable);
	}

	@Override
	public int postImport(List<QuestionItem> items, boolean editable) {
		if(items == null || items.isEmpty()) return 0;
		addToSource(items, editable);
		return items.size();
	}

	@Override
	public boolean isCreateEnabled() {
		return true;
	}

	@Override
	public boolean isCopyEnabled() {
		return true;
	}

	@Override
	public boolean isImportEnabled() {
		return true;
	}

	@Override
	public boolean isAuthorRightsEnable() {
		return true;
	}

	@Override
	public boolean isDeleteEnabled() {
		return false;
	}

	@Override
	public boolean isBulkChangeEnabled() {
		return true;
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

}
