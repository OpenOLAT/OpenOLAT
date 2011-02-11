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
* <p>
*/ 

package org.olat.repository.controllers;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Disposable;
import org.olat.repository.RepositoryEntry;


/**
 * Initial Date:  May 25, 2004
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public interface IAddController extends Disposable {
	
	/**
	 * The transaction component which will be invoked by the repository add controller.
	 * Controllers may return null here if the do not need a GUI-based workflow.
	 * However in such cases, they have to call addCallback.finished() at
	 * constructor time. (See AddCourseController for an example).
	 * @return Component implementing workflow.
	 */
	public Component getTransactionComponent();
	
	/**
	 * Called just before the repository entry gets created.
	 * @return true if transaction could successfully finished, false otherwise.
	 * I IAddController returns false, the repository entry will not be created.
	 */
	public boolean transactionFinishBeforeCreate();
	
	/**
	 * Called after the repository entry has been created.
	 * @param re
	 */
	public void repositoryEntryCreated(RepositoryEntry re);
	
	/**
	 * Called if the repository aborts the transaction. Do any cleanup work.
	 */
	public void transactionAborted();
	
}
