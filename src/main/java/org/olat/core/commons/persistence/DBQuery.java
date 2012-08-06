/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.type.Type;

/**
 * A <b>DBQuery </b> is
 * 
 * @author Andreas
 *  
 */
public interface DBQuery {

	/** 
	 * @param string
	 * @param value
	 * @return
	 * @see org.hibernate.Query
	 * */
	public abstract DBQuery setLong(String string, long value);

	/**
	 * @param string
	 * @param value
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setString(String string, String value);

	/**
	 * @param name
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setTime(String name, Date date);

	/**
	 * Execute the update or delete statement. The semantics are compliant with the ejb3 Query.executeUpdate() method.
	 * @param nullOrFlushMode either pass null if you don't want to set the FlushMode - otherwise pass the FlushMode you want to set on the query for execution
	 * @return the number of entities updated or deleted
	 */
	public abstract int executeUpdate(FlushMode nullOrFlushMode);
	
	/**
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract List list();

	/**
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract String[] getNamedParameters();

	/**
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract String getQueryString();

	/**
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract Type[] getReturnTypes();

	//public abstract Iterator iterate();

	//public abstract ScrollableResults scroll();

	/**
	 * @param position
	 * @param number
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBigDecimal(int position, BigDecimal number);

	/**
	 * @param name
	 * @param number
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBigDecimal(String name, BigDecimal number);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBinary(int position, byte[] val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBinary(String name, byte[] val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBoolean(int position, boolean val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setBoolean(String name, boolean val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setByte(int position, byte val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setByte(String name, byte val);

	/**
	 * @param cacheable
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCacheable(boolean cacheable);

	/**
	 * @param cacheRegion
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCacheRegion(String cacheRegion);

	/**
	 * @param position
	 * @param calendar
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCalendar(int position, Calendar calendar);

	/**
	 * @param name
	 * @param calendar
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCalendar(String name, Calendar calendar);

	/**
	 * @param position
	 * @param calendar
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCalendarDate(int position, Calendar calendar);

	/**
	 * @param name
	 * @param calendar
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCalendarDate(String name, Calendar calendar);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCharacter(int position, char val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setCharacter(String name, char val);

	/**
	 * @param position
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setDate(int position, Date date);

	/**
	 * @param name
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setDate(String name, Date date);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setDouble(int position, double val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setDouble(String name, double val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setEntity(int position, Object val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setEntity(String name, Object val);

	/**
	 * @param firstResult
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setFirstResult(int firstResult);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setFloat(int position, float val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setFloat(String name, float val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setInteger(int position, int val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setInteger(String name, int val);

	/**
	 * @param position
	 * @param locale
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setLocale(int position, Locale locale);

	/**
	 * @param name
	 * @param locale
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setLocale(String name, Locale locale);

	/**
	 * @param alias
	 * @param lockMode
	 * @see org.hibernate.Query
	 */
	public abstract void setLockMode(String alias, LockMode lockMode);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setLong(int position, long val);

	/**
	 * @param maxResults
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setMaxResults(int maxResults);

	/**
	 * @param position
	 * @param val
	 * @param type
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameter(int position, Object val, Type type);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameter(int position, Object val);

	/**
	 * @param name
	 * @param val
	 * @param type
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameter(String name, Object val, Type type);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameter(String name, Object val);

	/**
	 * @param name
	 * @param vals
	 * @param type
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameterList(String name, Collection vals, Type type);

	/**
	 * @param name
	 * @param vals
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameterList(String name, Collection vals);

	/**
	 * @param name
	 * @param vals
	 * @param type
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameterList(String name, Object[] vals, Type type);

	/**
	 * @param name
	 * @param vals
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setParameterList(String name, Object[] vals);

	/**
	 * @param bean
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setProperties(Object bean);
	
	public DBQuery setProperties(Map map);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setSerializable(int position, Serializable val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setSerializable(String name, Serializable val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setShort(int position, short val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setShort(String name, short val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setString(int position, String val);

	/**
	 * @param position
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setText(int position, String val);

	/**
	 * @param name
	 * @param val
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setText(String name, String val);

	/**
	 * @param position
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setTime(int position, Date date);

	/**
	 * @param timeout
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setTimeout(int timeout);

	/**
	 * @param position
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setTimestamp(int position, Date date);

	/**
	 * @param name
	 * @param date
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract DBQuery setTimestamp(String name, Date date);

	/**
	 * @return
	 * @see org.hibernate.Query
	 */
	public abstract Object uniqueResult();
}