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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.search.service.indexer.AbstractIndexer;

/**
 * 
 * Description:<br>
 * TODO: Class Description for EPIndexer
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPIndexer extends AbstractIndexer {

	/**
	 * 
	 */
	public EPIndexer() {
	// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unused")
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSupportedTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

}
