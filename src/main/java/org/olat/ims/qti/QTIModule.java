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
*/

package org.olat.ims.qti;

import java.util.List;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date:  13.11.2002
 *
 * @author Mike Stock
 */
public class QTIModule extends AbstractOLATModule {	
	private static boolean isValidating = false;
	private static final String CONFIG_VALIDATING = "validating";
	private List<RepositoryHandler> qtiRepositoryHandlers;
	
	/**
	 * @return true if qti xml files should be validated
	 */
	public static boolean isValidating() { return isValidating; }

	
	/**
	 * [used by spring]
	 */
	private QTIModule() {
		//
	}
	
	@Override
	public void init() {
		for (RepositoryHandler qtiRepositoryHandler : qtiRepositoryHandlers) {
			RepositoryHandlerFactory.registerHandler(qtiRepositoryHandler, 11);
		}
	}

	@Override
	protected void initDefaultProperties() {
		isValidating = getBooleanConfigParameter(CONFIG_VALIDATING, false);
		
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	/**
	 * [SPRING]
	 * @param qtiFileHandlers
	 */
	public void setQtiRepositoryHandlers(List<RepositoryHandler> qtiRepositoryHandlers) {
		this.qtiRepositoryHandlers = qtiRepositoryHandlers;
	}

}
