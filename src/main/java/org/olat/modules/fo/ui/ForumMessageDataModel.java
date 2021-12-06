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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.ui.main.AbstractMemberListController;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumMessageDataModel extends DefaultFlexiTreeTableDataModel<MessageLightViewRow>
	implements SortableFlexiTableDataModel<MessageLightViewRow> {
	
	private Translator translator;
	
	public ForumMessageDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		MessageLightViewRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			if("natural".equals(orderBy.getKey())) {
				List<MessageLightViewRow> objects = getObjects();
				Collections.sort(objects, new MessageTreeRowComparator());
				super.setObjects(objects);
				setHasOpenCloseAll(true);
			} else {
				openAll();
				List<MessageLightViewRow> views = new ForumMessageDataModelSort(orderBy, this, null).sort();
				super.setObjects(views);
				setHasOpenCloseAll(false);
			}
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		MessageLightViewRow view = getObject(row);
		return getValueAt(view, col);
	}

	@Override
	public Object getValueAt(MessageLightViewRow row, int col) {
		if(col >= 0 && col < ForumMessageCols.values().length) {
			switch(ForumMessageCols.values()[col]) {
				case type: return row.getView().getStatusCode();
				case mark: return row.getMarkLink();
				case thread: return row.getView().getTitle();
				case lastModified: return row.getView().getLastModified();
				case newMessage: return row.getView().isNewMessage()? Boolean.TRUE: null;
				default: return "ERROR";
			}
		}
		
		int propPos = col - AbstractMemberListController.USER_PROPS_OFFSET;
		if(StringHelper.containsNonWhitespace(row.getView().getPseudonym())) {
			return propPos == 0 ? row.getView().getPseudonym() : null;
		}
		if(row.getView().isGuest()) {
			return propPos == 0 ? translator.translate("guest") : null;
		}
		return row.getView().getIdentityProp(propPos);
	}
	
	public enum ForumMessageCols implements FlexiSortableColumnDef {
		type("table.header.typeimg"),
		mark("table.header.mark"),
		thread("table.thread"),
		lastModified("table.lastModified"),
		newMessage("table.header.new.message");
		
		private final String i18nKey;
		
		private ForumMessageCols(String i18nKey) {
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
	
	public class ForumMessageDataModelSort extends SortableFlexiTableModelDelegate<MessageLightViewRow> {
		
		public ForumMessageDataModelSort(SortKey orderBy, SortableFlexiTableDataModel<MessageLightViewRow> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
	}

}