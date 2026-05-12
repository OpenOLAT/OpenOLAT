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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
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
	
	private static final DateFormat formatTextual = new SimpleDateFormat("dd MMMMM yyyy", Locale.ENGLISH);
	private static final DateFormat formatTextualDE = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMAN);

	private final Locale locale;
	private final Translator translator;
	private final FlexiQueryTableDataModel<U> tableModel;
	
	public FilterableFlexiTableDataModelDelegate(FlexiQueryTableDataModel<U> tableModel, Translator translator) {
		this.translator = translator;
		locale = translator.getLocale();
		this.tableModel = tableModel;
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
			FilterableFlexiQueryValueProvider<U> valueProvider = new FilterableFlexiQueryValueProvider<>(tableModel);
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
					if(result instanceof Boolean boolResult && boolResult.booleanValue()) {
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
		String dateString = formatDateTextual(val);
		return acceptString(dateString, searchString);
	}
	
	protected final boolean acceptString(String val, String searchString) {
		return val != null && val.toLowerCase().contains(searchString.toLowerCase());
	}
	
	/**
	 * Formats the given date in a the form 11 May 2021.
	 * 
	 * @param date The date
	 * @param locale The locale
	 * @return The formatted string of the specified date
	 */
	public final String formatDateTextual(Date date) {
		if(date == null) return "";
		if(Locale.GERMAN.getLanguage().equals(locale.getLanguage())) {
			synchronized(formatTextualDE) {
				return formatTextualDE.format(date);
			}
		} else {
			synchronized(formatTextual) {
				return formatTextual.format(date);
			}
		}
	}
	
	public static final String toIdentifier(String label) {
		StringBuilder sb = new StringBuilder();
		for(char ch:label.toCharArray()) {
			if(Character.isLetter(ch) || Character.isDigit(ch)) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	private static final class FilterableFlexiQueryValueProvider<V> implements FlexiQueryValueProvider {
		
		private V row;
		private final FlexiQueryTableDataModel<V> dataModel;
		
		public FilterableFlexiQueryValueProvider(FlexiQueryTableDataModel<V> dataModel) {
			this.dataModel = dataModel;
		}

		@Override
		public Object getValue(String identifier) {
			int col = dataModel.getColumn(identifier);
			return dataModel.getRawValueAt(row, col);
		}

		public void setRow(V row) {
			this.row = row;
			
		}
	}
	
	public static final class FilteredResults<T> {
		
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
