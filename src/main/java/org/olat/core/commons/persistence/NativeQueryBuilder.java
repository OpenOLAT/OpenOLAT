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
 * Provide some help to build native SQL queries for Oracle, MySQL and PostreSQL.
 * 
 * Initial date: 02.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NativeQueryBuilder {

	private final StringBuilder sb;
	private final DB dbInstance;

	/**
	 * @param len
	 */
	public NativeQueryBuilder(int len, DB dbInstance) {
		sb = new StringBuilder(len);
		this.dbInstance = dbInstance;
	}

	/**
	 * 
	 */
	public NativeQueryBuilder(DB dbInstance) {
		sb = new StringBuilder(128);
		this.dbInstance = dbInstance;
	}

	/**
	 * @param val
	 * @return Itself
	 */
	public NativeQueryBuilder append(String val) {
		sb.append(val);
		return this;
	}
	
	/**
	 * 
	 * @param val The value to append
	 * @param append If true append happens, if false not
	 * @return Itself
	 */
	public NativeQueryBuilder append(String val, boolean append) {
		if(append) {
			sb.append(val);
		}
		return this;
	}
	
	public NativeQueryBuilder append(String valTrue, String valFalse, boolean choice) {
		if(choice) {
			sb.append(valTrue);
		} else {
			sb.append(valFalse);
		}
		return this;
	}
	
	/**
	 * Append true as boolean for PostgreSQL, 1 for Oracle and MySQL.
	 * @return
	 */
	public NativeQueryBuilder appendTrue() {
		if(dbInstance.isPostgreSQL()) {
			sb.append("true");
		} else {
			sb.append("1");
		}
		return this;
	}
	
	/**
	 * Append false as boolean for PostgreSQL, 0 for Oracle and MySQL.
	 * @return
	 */
	public NativeQueryBuilder appendFalse() {
		if(dbInstance.isPostgreSQL()) {
			sb.append("false");
		} else {
			sb.append("0");
		}
		return this;
	}
	
	/**
	 * Append an "as" for MySQL and PostgreSQL but not Oracle.
	 * @return
	 */
	public NativeQueryBuilder appendAs() {
		if(dbInstance.isOracle()) {
			sb.append(" ");
		} else {
			sb.append(" as ");
		}
		return this;
	}
	
	public NativeQueryBuilder appendToArray(String var) {
		if(dbInstance.isMySQL()) {
			sb.append(" group_concat(").append(var).append(")");
		} else if(dbInstance.isPostgreSQL()) {
			sb.append(" array_to_string(array_agg(").append(var).append("),',')");
		} else if(dbInstance.isOracle()) {
			sb.append(" listagg(").append(var).append(",',') within group (order by ").append(var).append(") ");
		}
		return this;
	}
	
	public NativeQueryBuilder appendOrderBy(SortKey orderBy) {
		sb.append(" order by ").append(orderBy.getKey());
		if(orderBy.isAsc()) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return this;
	}

	/**
	 * @param i
	 * @return Itself
	 */
	public NativeQueryBuilder append(int i) {
		sb.append(i);
		return this;
	}

	/**
	 * @param sMin
	 * @return Itself
	 */
	public NativeQueryBuilder append(long sMin) {
		sb.append(String.valueOf(sMin));
		return this;
	}
	
	public NativeQueryBuilder append(Object o) {
		sb.append(o);
		return this;
	}
	
	public NativeQueryBuilder in(Object... objects) {
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
	
	public NativeQueryBuilder in(Enum<?>... objects) {
		if(objects != null && objects.length > 0) {
			if(objects.length == 1) {
				sb.append(" ='").append(objects[0].name()).append("' ");
			} else {
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
		}
		return this;
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
