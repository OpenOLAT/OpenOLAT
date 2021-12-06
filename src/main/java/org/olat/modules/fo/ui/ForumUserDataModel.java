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
package org.olat.modules.fo.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.modules.fo.model.ForumUser;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumUserDataModel extends DefaultFlexiTableDataModel<ForumUser>
	implements SortableFlexiTableDataModel<ForumUser> {
	
	private Translator translator;
	
	public ForumUserDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ForumUser> views = new ForumUserDataModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ForumUser forumUser = getObject(row);
		return getValueAt(forumUser, col);
	}

	@Override
	public Object getValueAt(ForumUser row, int col) {
		if(col >= 0 && col < UserCols.values().length) {
			switch(UserCols.values()[col]) {
				case replies: return row.getNumOfReplies();
				case threads: return row.getNumOfThreads();
				case lastModified: return row.getLastModified();
				case numOfWords: return row.getNumOfWords();
				case numOfCharacters: return row.getNumOfCharacters();
			}
		}
		
		int propPos = col - AbstractMemberListController.USER_PROPS_OFFSET;
		if(StringHelper.containsNonWhitespace(row.getPseudonym())) {
			return propPos == 0 ? row.getPseudonym() : null;
		}
		if(row.isGuest()) {
			return propPos == 0 ? translator.translate("guest") : null;
		}
		return row.getIdentityProp(propPos);
	}
	
	public enum UserCols implements FlexiSortableColumnDef {
		replies("table.user.replies"),
		threads("table.user.threads"),
		lastModified("table.lastModified"),
		numOfWords("table.numOfWords"),
		numOfCharacters("table.numOfCharacters");
		
		private final String i18nKey;
		
		private UserCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	public class ForumUserDataModelSort extends SortableFlexiTableModelDelegate<ForumUser> {
		
		public ForumUserDataModelSort(SortKey orderBy, SortableFlexiTableDataModel<ForumUser> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
	}
}
