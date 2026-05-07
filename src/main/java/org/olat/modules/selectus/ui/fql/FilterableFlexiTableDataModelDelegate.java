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
package org.olat.modules.selectus.ui.fql;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

import com.frentix.flexiql.FlexiQLSyntaxException;
import com.frentix.flexiql.FlexiQueryErrorListener;
import com.frentix.flexiql.FlexiQueryValueProvider;
import com.frentix.flexiql.FlexiQueryVisitor;
import com.frentix.flexiql.ql.FlexiQLLexer;
import com.frentix.flexiql.ql.FlexiQLParser;

/**
 * 
 * Initial date: 8 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 * @param <U>
 */
public class FilterableFlexiTableDataModelDelegate<U> {
	
	private static final Logger log = Tracing.createLoggerFor(FilterableFlexiTableDataModelDelegate.class);

	private final Formatter formatter;
	private final Translator translator;
	private final SortableFlexiTableDataModel<U> tableModel;
	
	public FilterableFlexiTableDataModelDelegate(SortableFlexiTableDataModel<U> tableModel, Translator translator) {
		this.translator = translator;
		this.tableModel = tableModel;
		this.formatter = Formatter.getInstance(translator.getLocale());
	}
	
	public Translator getTranslator() {
		return translator;
	}
	
	public FlexiTableDataModel<U> getTableModel() {
		return tableModel;
	}
	
	public FilteredResults<U> flexiSearch(String searchString, String query, List<U> backupRows) {
		boolean allErrors = false;
		List<U> resultingRows;
		if(StringHelper.containsNonWhitespace(query)) {
			List<U> filteredRows = new ArrayList<>(backupRows.size());	
			FilterableFlexiQueryValueProvider<U> valueProvider = createQueryValueProvider();
			for(U row:backupRows) {
				try {
					CharStream stream = CharStreams.fromString(query);
					FlexiQLLexer lexer = new FlexiQLLexer(stream);
					FlexiQLParser parser = new FlexiQLParser(new CommonTokenStream(lexer));
					FlexiQueryErrorListener errorListener = new FlexiQueryErrorListener(query);
					parser.addErrorListener(errorListener);
					valueProvider.setRow(row);
					FlexiQueryVisitor visitor = new FlexiQueryVisitor(valueProvider, translator.getLocale());
					Object result = visitor.visit(parser.parse());
					if(result instanceof Boolean && ((Boolean)result).booleanValue()) {
						filteredRows.add(row);
					}
					allErrors |= errorListener.hasErrors() || visitor.hasErrors();
				} catch(FlexiQLSyntaxException e) {
					log.warn("FlexiQL syntax error for query: {} char at: {}", e.getQuery(), e.getCharPositionInLine(), e);
					allErrors |= true;
				} catch (Exception e) {
					log.error("", e);
					allErrors |= true;
				}
			}
			
			if(StringHelper.containsNonWhitespace(searchString)) {
				filteredRows = fullTextSearch(searchString, filteredRows);
			}

			resultingRows = filteredRows;
		} else  if(StringHelper.containsNonWhitespace(searchString)) {
			List<U> filteredRows = new ArrayList<>(backupRows);	
			filteredRows = fullTextSearch(searchString, filteredRows);
			resultingRows = filteredRows;
		} else {
			resultingRows = backupRows;
		}
		return new FilteredResults<>(resultingRows, allErrors);	
	}
	
	protected final List<U> fullTextSearch(String search, List<U> rows) {
		if(StringHelper.containsNonWhitespace(search)) {
			FlexiTableColumnModel columnModel = tableModel.getTableColumnModel();
			int count = columnModel.getColumnCount();

			List<U> filteredRows = new ArrayList<>(rows.size());
			Set<U> deduplicatedRows = new HashSet<>();

			for(int i=0; i<count; i++) {
				FlexiColumnModel col = columnModel.getColumnModel(i);
				if(/* !columnModel.isColumnModelVisible(col) || */ col.getColumnIndex() < 0) {
					continue;
				}
				for(U row:rows) {
					if(deduplicatedRows.contains(row)) {
						continue;
					}
					
					if(accept(col, row, search)) {
						filteredRows.add(row);
						deduplicatedRows.add(row);
					}
				}
			}
			
			return filteredRows;
		} 
		return rows;
	}
	
	protected boolean accept(FlexiColumnModel col, U row, String searchString) {
		Object val = tableModel.getValueAt(row, col.getColumnIndex());
		if(val instanceof String) {
			return acceptString((String)val, searchString);
		} else if(val instanceof String[]) {
			String[] valArr = (String[])val;
			for(int i=valArr.length; i-->0; ) {
				if(acceptString(valArr[i], searchString)) {
					return true;
				}
			}
			return false;
		} else if(val instanceof Date) {
			return acceptDate((Date)val, searchString);
		} else if(val instanceof Number) {
			return ((Number)val).toString().equals(searchString);
		}
		return false;
	}
	
	protected final boolean acceptDate(Date val, String searchString) {
		String dateString = formatter.formatDate(val);//TODO selectus fql formatter.formatDateTextual(val);
		return acceptString(dateString, searchString);
	}
	
	protected final boolean acceptString(String val, String searchString) {
		return val != null && val.toLowerCase().contains(searchString.toLowerCase());
	}
	
	protected FilterableFlexiQueryValueProvider<U> createQueryValueProvider() {
		return new FilterableFlexiQueryValueProvider<>();
	}
	
	public static class FilterableFlexiQueryValueProvider<V> implements FlexiQueryValueProvider {
		
		private V row;

		@Override
		public Object getValue(String identifier) {
			return null;
		}
		
		public V getRow() {
			return row;
		}

		public void setRow(V row) {
			this.row = row;
			
		}
	}
	
	public static class FilteredResults<T> {
		
		private final List<T> rows;
		private final boolean allErrors;
		
		public FilteredResults(List<T> rows, boolean allErrors) {
			this.rows = rows;
			this.allErrors = allErrors;
		}

		public List<T> getRows() {
			return rows;
		}

		public boolean isAllErrors() {
			return allErrors;
		}
	}
}
