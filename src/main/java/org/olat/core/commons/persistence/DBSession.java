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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 *

 * @author Felix Jost
 */
class DBSession {
	private Session hibernateSession;
	private DBTransaction trxWrapper = null;
	
	protected DBSession(Session hibernateSession) {
		this.hibernateSession = hibernateSession;
	}
	
	/**
	 * OLAT-3652: allow clearing of transaction - needed to avoid side-effects of subsequent session usage with error-in-transaction
	 */
	protected void clearTransaction() {
		if ( (hibernateSession != null) && hibernateSession.isOpen()) {
			throw new DBRuntimeException("clearTransaction expected session to be closed at this point!");
		}
		trxWrapper = null;
	}
	
	protected DBTransaction beginDbTransaction() {
		if (trxWrapper == null || trxWrapper.isCommitted()) {
			try {
				Transaction trx = hibernateSession.beginTransaction();
				trxWrapper = new DBTransaction(trx);
				return trxWrapper;
			} catch (HibernateException e) {
				throw new DBRuntimeException("DBSession - could not begin DBTransaction", e);
			}
		} else {
			throw new DBRuntimeException(
				"Nested transactions are not allowed! " 
				+ "Don't use atomar hibernate calls but the equvalent version that requires your current transaction object."
				);
		}
	}

	/**
	 * @return Session
	 */
	protected Session getHibernateSession() {
		return hibernateSession;
	}


	/**
	 * Close the Hibernate Session an clean up connection.
	 */
	protected void close() {
		try {
			if ( (hibernateSession != null) && hibernateSession.isOpen()) {
				Codepoint.codepoint(DBImpl.class, "closeSession");
				if ( (getTransaction() != null) && getTransaction().isInTransaction()
						&& !getTransaction().isRolledBack()) {
					throw new AssertException("Try to close un-committed session");
				}
				hibernateSession.close();
			}
		} catch (HibernateException e) {
			throw new DBRuntimeException("Close Session error.", e);
		} catch (Exception e) {
			throw new DBRuntimeException("Error in dbsession.close: ", e);
		} finally {
			try {
				if (hibernateSession != null) {
					if (hibernateSession.isOpen())	hibernateSession.close();
				}
			} catch (Exception e) {
				// we did our best to close the hibernate session
				throw new DBRuntimeException("in finally of DBSession.java / closeSession(), closesession failed again ", e);
			} 
		}
	}

	boolean contains(Object object) {
		return this.hibernateSession.contains(object);
	}

	/**
	 * @return the current transaction or null.
	 */
	protected DBTransaction getTransaction() {
		return trxWrapper;
	}

	/**
	 * @return true if session is open
	 */
	boolean isOpen() {
		return hibernateSession.isOpen();
	}	
	
}
