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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.type.Type;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
/**
 * A <b>DBQueryImpl</b> is a wrapper around a Hibernate Query object.
 * 
 * @author Andreas Ch. Kapp
 *
 */
public class DBQueryImpl implements DBQuery {
	
	private static final OLog log = Tracing.createLoggerFor(DBQueryImpl.class);

	private Query query = null;
	
	/**
	 * Default construcotr.
	 * @param q
	 */
	public DBQueryImpl(Query q) {
		query = q;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setLong(java.lang.String, long)
	 */
	public DBQuery setLong(String string, long value) {
		query.setLong(string, value);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setString(java.lang.String, java.lang.String)
	 */
	public DBQuery setString(String string, String value) {
		query.setString(string, value);
		return this;
	}
	
	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setTime(java.lang.String, java.util.Date)
	 */
	public DBQuery setTime(String name, Date date){
		query.setTime(name, date);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#executeUpdate(FlushMode)
	 */
	public int executeUpdate(FlushMode nullOrFlushMode) {
		if (nullOrFlushMode!=null) {
			query.setFlushMode(nullOrFlushMode);
		}
		return query.executeUpdate();
	}
	
	/**
	 * @see org.olat.core.commons.persistence.DBQuery#list()
	 */
	public List list() {
		try{
			return query.list();
		}
		catch (HibernateException he) {
			String msg ="Error in list()" ; 
			throw new DBRuntimeException(msg, he);
		}
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#getNamedParameters()
	 */
	public String[] getNamedParameters() {
		try {
			return query.getNamedParameters();
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("GetNamedParameters failed. ", e);
		}	
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#getQueryString()
	 */
	public String getQueryString() {
		return query.getQueryString();
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#getReturnTypes()
	 */
	public Type[] getReturnTypes() {
		
		try {
			return query.getReturnTypes();
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("GetReturnTypes failed. ", e);
		}
	}

	/**
	 * @return iterator
	 */
	public Iterator iterate() {
		try {
			return query.iterate();
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("Iterate failed. ", e);
		}
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBigDecimal(int, java.math.BigDecimal)
	 */
	public DBQuery setBigDecimal(int position, BigDecimal number) {
		query.setBigDecimal(position, number);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public DBQuery setBigDecimal(String name, BigDecimal number) {
		query.setBigDecimal(name, number);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBinary(int, byte[])
	 */
	public DBQuery setBinary(int position, byte[] val) {
		query.setBinary(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBinary(java.lang.String, byte[])
	 */
	public DBQuery setBinary(String name, byte[] val) {
		query.setBinary(name, val);
		return this;	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBoolean(int, boolean)
	 */
	public DBQuery setBoolean(int position, boolean val) {
		query.setBoolean(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setBoolean(java.lang.String, boolean)
	 */
	public DBQuery setBoolean(String name, boolean val) {
		query.setBoolean(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setByte(int, byte)
	 */
	public DBQuery setByte(int position, byte val) {
		query.setByte(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setByte(java.lang.String, byte)
	 */
	public DBQuery setByte(String name, byte val) {
		query.setByte(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCacheable(boolean)
	 */
	public DBQuery setCacheable(boolean cacheable) {
		query.setCacheable(cacheable);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCacheRegion(java.lang.String)
	 */
	public DBQuery setCacheRegion(String cacheRegion) {
		query.setCacheRegion(cacheRegion);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCalendar(int, java.util.Calendar)
	 */
	public DBQuery setCalendar(int position, Calendar calendar) {
		query.setCalendar(position, calendar);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCalendar(java.lang.String, java.util.Calendar)
	 */
	public DBQuery setCalendar(String name, Calendar calendar) {
		query.setCalendar(name, calendar);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCalendarDate(int, java.util.Calendar)
	 */
	public DBQuery setCalendarDate(int position, Calendar calendar) {
		query.setCalendarDate(position, calendar);
		return this;

	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCalendarDate(java.lang.String, java.util.Calendar)
	 */
	public DBQuery setCalendarDate(String name, Calendar calendar) {
		query.setCalendarDate(name, calendar);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCharacter(int, char)
	 */
	public DBQuery setCharacter(int position, char val) {
		query.setCharacter(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setCharacter(java.lang.String, char)
	 */
	public DBQuery setCharacter(String name, char val) {
		query.setCharacter(name, val);
		return this;

	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setDate(int, java.util.Date)
	 */
	public DBQuery setDate(int position, Date date) {
		query.setDate(position, date);
		return this;

	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setDate(java.lang.String, java.util.Date)
	 */
	public DBQuery setDate(String name, Date date) {
		query.setDate(name, date);
		return this;	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setDouble(int, double)
	 */
	public DBQuery setDouble(int position, double val) {
		query.setDouble(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setDouble(java.lang.String, double)
	 */
	public DBQuery setDouble(String name, double val) {
		query.setDouble(name, val);
		return this;	
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setEntity(int, java.lang.Object)
	 */
	public DBQuery setEntity(int position, Object val) {
		query.setEntity(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setEntity(java.lang.String, java.lang.Object)
	 */
	public DBQuery setEntity(String name, Object val) {
		query.setEntity(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setFirstResult(int)
	 */
	public DBQuery setFirstResult(int firstResult) {
		query.setFirstResult(firstResult);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setFloat(int, float)
	 */
	public DBQuery setFloat(int position, float val) {
		query.setFloat(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setFloat(java.lang.String, float)
	 */
	public DBQuery setFloat(String name, float val) {
		query.setFloat(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setInteger(int, int)
	 */
	public DBQuery setInteger(int position, int val) {
		query.setInteger(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setInteger(java.lang.String, int)
	 */
	public DBQuery setInteger(String name, int val) {
		query.setInteger(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setLocale(int, java.util.Locale)
	 */
	public DBQuery setLocale(int position, Locale locale) {
		query.setLocale(position, locale);
		return this;
	}


	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setLocale(java.lang.String, java.util.Locale)
	 */
	public DBQuery setLocale(String name, Locale locale) {
		query.setLocale(name, locale);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setLockMode(java.lang.String, org.hibernate.LockMode)
	 */
	public void setLockMode(String alias, LockMode lockMode) {
		query.setLockMode(alias, lockMode);

	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setLong(int, long)
	 */
	public DBQuery setLong(int position, long val) {
		query.setLong(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setMaxResults(int)
	 */
	public DBQuery setMaxResults(int maxResults) {
		query.setMaxResults(maxResults);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameter(int, java.lang.Object, org.hibernate.type.Type)
	 */
	public DBQuery setParameter(int position, Object val, Type type) {
		query.setParameter(position, val, type);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameter(int, java.lang.Object)
	 */
	public DBQuery setParameter(int position, Object val) {
		try {
			query.setParameter(position, val);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameter(java.lang.String, java.lang.Object, org.hibernate.type.Type)
	 */
	public DBQuery setParameter(String name, Object val, Type type) {
		query.setParameter(name, val, type);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameter(java.lang.String, java.lang.Object)
	 */
	public DBQuery setParameter(String name, Object val) {
		try {
			query.setParameter(name, val);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameterList(java.lang.String, java.util.Collection, org.hibernate.type.Type)
	 */
	public DBQuery setParameterList(String name, Collection vals, Type type) {
		try {
			query.setParameterList(name, vals, type);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameterList(java.lang.String, java.util.Collection)
	 */
	public DBQuery setParameterList(String name, Collection vals) {
		try {
			query.setParameterList(name, vals);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameterList(java.lang.String, java.lang.Object[], org.hibernate.type.Type)
	 */
	public DBQuery setParameterList(String name, Object[] vals, Type type) {
		try {
			query.setParameterList(name, vals, type);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setParameterList(java.lang.String, java.lang.Object[])
	 */
	public DBQuery setParameterList(String name, Object[] vals) {
		try {
			query.setParameterList(name, vals);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setProperties(java.lang.Object)
	 */
	public DBQuery setProperties(Object bean) {
		try {
			query.setProperties(bean);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}
	
	public DBQuery setProperties(Map map) {
		try {
			query.setProperties(map);
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setSerializable(int, java.io.Serializable)
	 */
	public DBQuery setSerializable(int position, Serializable val) {
		query.setSerializable(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setSerializable(java.lang.String, java.io.Serializable)
	 */
	public DBQuery setSerializable(String name, Serializable val) {
		query.setSerializable(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setShort(int, short)
	 */
	public DBQuery setShort(int position, short val) {
		query.setShort(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setShort(java.lang.String, short)
	 */
	public DBQuery setShort(String name, short val) {
		query.setShort(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setString(int, java.lang.String)
	 */
	public DBQuery setString(int position, String val) {
		query.setString(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setText(int, java.lang.String)
	 */
	public DBQuery setText(int position, String val) {
		query.setText(position, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setText(java.lang.String, java.lang.String)
	 */
	public DBQuery setText(String name, String val) {
		query.setText(name, val);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setTime(int, java.util.Date)
	 */
	public DBQuery setTime(int position, Date date) {
		query.setTime(position, date);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setTimeout(int)
	 */
	public DBQuery setTimeout(int timeout) {
		query.setTimeout(timeout);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setTimestamp(int, java.util.Date)
	 */
	public DBQuery setTimestamp(int position, Date date) {
		query.setTimestamp(position, date);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#setTimestamp(java.lang.String, java.util.Date)
	 */
	public DBQuery setTimestamp(String name, Date date) {
		query.setTimestamp(name, date);
		return this;
	}

	/**
	 * @see org.olat.core.commons.persistence.DBQuery#uniqueResult()
	 */
	public Object uniqueResult() {
		try {
			return query.uniqueResult();
		}
		catch (HibernateException e) {
			throw new DBRuntimeException("DBQuery error. ", e);
		}
	}

}
