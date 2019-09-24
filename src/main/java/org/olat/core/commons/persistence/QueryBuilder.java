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
package org.olat.core.commons.persistence;

/**
 * 
 * Initial date: 21 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QueryBuilder implements Appendable {

	private boolean and = false;
	private boolean groupBy = false;
	private final StringBuilder sb;

	/**
	 * @param len
	 */
	public QueryBuilder(int len) {
		sb = new StringBuilder(len);
	}

	/**
	 * 
	 */
	public QueryBuilder() {
		sb = new StringBuilder(128);
	}

	/**
	 * @param val
	 * @return Itself
	 */
	public QueryBuilder append(String val) {
		sb.append(val);
		return this;
	}
	
	/**
	 * 
	 * @param val The value to append
	 * @param append If true append happens, if false not
	 * @return Itself
	 */
	public QueryBuilder append(String val, boolean append) {
		if(append) {
			sb.append(val);
		}
		return this;
	}
	
	@Override
	public QueryBuilder append(CharSequence csq) {
		sb.append(csq);
		return this;
	}

	@Override
	public QueryBuilder append(CharSequence csq, int start, int end) {
		sb.append(csq, start, end);
		return this;
	}

	@Override
	public QueryBuilder append(char c) {
		sb.append(c);
		return this;
	}

	public QueryBuilder append(String valTrue, String valFalse, boolean choice) {
		if(choice) {
			sb.append(valTrue);
		} else {
			sb.append(valFalse);
		}
		return this;
	}

	/**
	 * @param i
	 * @return Itself
	 */
	public QueryBuilder append(int i) {
		sb.append(i);
		return this;
	}

	/**
	 * @param sMin
	 * @return Itself
	 */
	public QueryBuilder append(long sMin) {
		sb.append(sMin);
		return this;
	}
	
	/**
	 * @param b The boolean
	 * @return Itself
	 */
	public QueryBuilder append(boolean b) {
		sb.append(b);
		return this;
	}
	
	/**
	 * @param o The object
	 * @return Itself
	 */
	public QueryBuilder append(Object o) {
		sb.append(o);
		return this;
	}
	
	public QueryBuilder where() {
		return and();
	}
	
	public QueryBuilder and() {
		if(and) {
			sb.append(" and ");
		} else {
			and = true;
			sb.append(" where ");
		}
		return this;
	}
	
	public QueryBuilder groupBy() {
		if(groupBy) {
			sb.append(" , ");
		} else {
			groupBy = true;
			sb.append(" group by ");
		}
		return this;
	}
	
	public QueryBuilder in(Object... objects) {
		if(objects != null && objects.length > 0) {
			sb.append(" in ('");
			boolean first = true;
			for(Object object:objects) {
				if(object != null) {
					if(first) {
						first = false;
					} else {
						sb.append("','");
					}
					sb.append(object);
				}
			}
			sb.append("')");
		}
		return this;
	}
	
	public QueryBuilder in(Enum<?>... objects) {
		if(objects != null && objects.length > 0) {
			sb.append(" in ('");
			boolean first = true;
			for(Enum<?> object:objects) {
				if(object != null) {
					if(first) {
						first = false;
					} else {
						sb.append("','");
					}
					sb.append(object.name());
				}
			}
			sb.append("')");
		}
		return this;
	}
	
	public QueryBuilder likeFuzzy(String var, String key, String dbVendor) {
		if(dbVendor.equals("mysql")) {
			append(" ").append(var).append(" like :").append(key);
		} else {
			append(" lower(").append(var).append(") like :").append(key);
		}
		if(dbVendor.equals("oracle")) {
			append(" escape '\\'");
		}
		return this;
	}
	
	public QueryBuilder appendAsc(boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return this;
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
