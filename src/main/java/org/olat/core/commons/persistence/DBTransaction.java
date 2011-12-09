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
import org.hibernate.Transaction;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;

/**
 * A <b>DBTransaction</b> is used to encapsulate a Hibernate
 * provided transaction.
 * 
 * @author Andreas Ch. Kapp
 *
 */
final class DBTransaction  {
	
	private Transaction hibernateTransaction = null;
	private boolean rolledBack = false;
	private boolean committed = false;
	private Exception error = null;
	private boolean inTransaction = false;

	DBTransaction(Transaction t) {
		this.hibernateTransaction = t;
		setInTransaction(true);
	}
		
	protected void commit() {
		try {
			if (hibernateTransaction.isActive()
					&& !hibernateTransaction.wasCommitted()) {
				hibernateTransaction.commit();
			} else {
				Tracing.logWarn("Could not call hibernateTransaction.commit() because is not Active or already committed",this.getClass());
			}
			setInTransaction(false);
			this.committed = true;
		} catch (HibernateException e) {
			setErrorAndRollback(e);
			throw new DBRuntimeException("DB commit failed. Could not commit transaction: " + this , e);
		}
	}
	
	/**
	 * Method rollback.
	 */
	protected void rollback() {
		try {
			if (hibernateTransaction.isActive()
					&& !hibernateTransaction.wasRolledBack()) {
				hibernateTransaction.rollback();
			}else {
				// OLAT-3621: raising log level from WARN to ERROR to have this pop up in tests more. plus added a stacktrace for debugging ease.
				Tracing.logError("Could not call hibernateTransaction.rollback() because is not Active or already rolledback", new Exception("DBTransaction.rollback()"), this.getClass());
			}
			this.rolledBack = true;
		} catch (HibernateException e) {
			throw new DBRuntimeException("DB rollback transaction failed. ", e);
		}
	}
	
	/**
	 * @return true if transaction is rolled back or
	 * an error happened.
	 */
	protected boolean isRolledBack() {
		return rolledBack || isError();
	}

	protected boolean isCommitted() {
		return committed;
	}

	/**
	 * @param b
	 */
	protected void setRolledBack(boolean b) {
		rolledBack = b;
	}

	protected Exception getError() {
		return error;
	}
	
	protected boolean isError() {
		return error != null;
	}
	
	/**
	 * Set the Exception to the transaction object. 
	 * The transaction is rolledback.
	 * @param exception
	 */
	protected void setErrorAndRollback(Exception exception) {
		error = exception;
		Tracing.logDebug("Hibernate Error, rolling back", error.toString(), DBTransaction.class);
		rollback();
	}

	/**
	 * @return true if beginTransaction was called until commit was called.
	 */
	protected boolean isInTransaction() {
		return inTransaction;
	}

	/**
	 * @param b
	 */
	private void setInTransaction(boolean b) {
		inTransaction = b;
	}

}
