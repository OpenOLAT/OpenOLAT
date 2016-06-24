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
package org.olat.modules.portfolio;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BinderSecurityCallback {
	
	/**
	 * Can edit the edit the content of this binder inclusive sections
	 * and pages.
	 * @return
	 */
	public boolean canEditBinder();
	
	/**
	 * Can edit the edit the meta-data in this binder inclusive meta-data
	 * of sections and pages.
	 * @return
	 */
	public boolean canEditMetadataBinder();
	
	public boolean canAddSection();
	
	public boolean canEditSection();
	
	public boolean canAddPage();
	
	public boolean canEditPage(Page page);
	
	public boolean canEditAccessRights(PortfolioElement element);
	
	public boolean canViewElement(PortfolioElement element);
	
	public boolean canComment(PortfolioElement element);
	
	public boolean canReview(PortfolioElement element);
	
	public boolean canBinderAssessment();
	
	public boolean canAssess(Section section);
 

}
