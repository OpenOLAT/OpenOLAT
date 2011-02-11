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

/**
 * 
 * Description:<br>
 * SecurityCallback
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface EPSecurityCallback {
	
	public boolean isRestrictionsEnabled();
	
	public boolean isOwner();
	
	public boolean canEditStructure();
	
	public boolean canShareMap();
	
	public boolean canAddArtefact();
	
	public boolean canRemoveArtefactFromStruct();
	
	public boolean canAddStructure();
	
	public boolean canAddPage();

	public boolean canCommentAndRate();
	
	public boolean canSubmitAssess();
	
	public boolean canView();
}
