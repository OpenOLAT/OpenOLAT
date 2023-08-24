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
package org.olat.modules.cemedia.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.cemedia.ui.MediaDataModel.MediaCols;
import org.olat.modules.portfolio.ui.model.MediaRow;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 1 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDataModelSorterDelegate extends SortableFlexiTableModelDelegate<MediaRow> {
	
	private static final MediaCols[] COLS = MediaCols.values();
	
	private final Translator translator;
	
	public MediaDataModelSorterDelegate(SortKey orderBy, MediaDataModel model, Translator translator, Locale locale) {
		super(orderBy, model, locale);
		this.translator = translator;
	}

	@Override
	protected void sort(List<MediaRow> rows) {
		Comparator<MediaRow> comparator;
		
		MediaCols col = COLS[getColumnIndex()];
		switch(col) {
			case type: comparator = new TypeComparator(); break;
			case tags: comparator = new TagsComparator(); break;
			case taxonomyLevels, taxonomyLevelsPaths: comparator = new TaxonomyLevelComparator(translator); break;
			default: comparator = new DefaultComparator(); break;
		}
		
		if(!isAsc() && col != MediaCols.tags && col != MediaCols.taxonomyLevels && col != MediaCols.taxonomyLevelsPaths) {
			comparator = new ReverseComparator(comparator);
		}
		Collections.sort(rows, comparator);
	}
	
	@Override
	protected void reverse(List<MediaRow> rows) {
		//do nothing
	}
	
	private class TaxonomyLevelComparator implements Comparator<MediaRow> {
		
		private final Comparator<TaxonomyLevel> levelComparator;
		
		public TaxonomyLevelComparator(Translator translator) {
			levelComparator = CatalogV2UIFactory.getTaxonomyLevelComparator(translator);
		}

		@Override
		public int compare(MediaRow t1, MediaRow t2) {
			int c = 0;
			if(!t1.hasTaxonomyLevels() && t2.hasTaxonomyLevels()) {
				c = 1;
			} else if(t1.hasTaxonomyLevels() && !t2.hasTaxonomyLevels()) {
				c = -1;
			} else {
				if(t1.hasTaxonomyLevels() && t2.hasTaxonomyLevels()) {
					List<TaxonomyLevel> r1 = t1.getTaxonomyLevels();
					List<TaxonomyLevel> r2 = t2.getTaxonomyLevels();
					for(int i=0; i<r1.size() && i<r2.size() && c == 0; i++) {
						TaxonomyLevel level1 = r1.get(i);
						TaxonomyLevel level2 = r2.get(i);
						c = levelComparator.compare(level1, level2);
					}
				} else {
					c = compareString(t1.getTitle(), t2.getTitle());
				}
				
				if(!isAsc()) {
					c = -1 * c;
				}
			}
			return c;
		}
	}
	
	private class TagsComparator implements Comparator<MediaRow> {
		
		@Override
		public int compare(MediaRow t1, MediaRow t2) {	
			int c = 0;
			if(!t1.hasTags() && t2.hasTags()) {
				c = 1;
			} else if(t1.hasTags() && !t2.hasTags()) {
				c = -1;
			} else {
				if(t1.hasTags() && t2.hasTags()) {
					List<String> r1 = t1.getTags();
					List<String> r2 = t2.getTags();
					for(int i=0; i<r1.size() && i<r2.size() && c == 0; i++) {
						String tag1 = r1.get(i);
						String tag2 = r2.get(i);
						c = compareString(tag1, tag2);
					}
				} else {
					c = compareString(t1.getTitle(), t2.getTitle());
				}
				
				if(!isAsc()) {
					c = -1 * c;
				}
			}
			return c;
		}
	}
	
	private class TypeComparator implements Comparator<MediaRow> {
		@Override
		public int compare(MediaRow t1, MediaRow t2) {
			String r1 = t1.getType();
			String r2 = t2.getType();
			
			int c = compareString(r1, r2);
			if(c == 0) {
				c = compareString(t1.getTitle(), t2.getTitle());
			}
			return c;
		}
	}
}
