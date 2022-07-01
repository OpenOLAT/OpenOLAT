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
package org.olat.modules.qpool;

import org.olat.core.commons.services.license.LicenseService;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItem extends QuestionItemShort {

	//general
	
	public String getDescription();
	
	public String getCoverage();
	
	public String getAdditionalInformations();
		
	
	//educational
	public QEducationalContext getEducationalContext();

	//question
	public QItemType getType();
	
	public String getAssessmentType();
	
	//lifecycle
	public String getItemVersion();
	
	//rights
	/**
	 * @deprecated Use @see {@link LicenseService}
	 */
	@Deprecated
	public QLicense getLicense();
	
	/**
	 * @deprecated Use @see {@link LicenseService}
	 */
	@Deprecated
	public String getCreator();

	//technics
	public String getEditor();
	
	public String getEditorVersion();

	//intern
	public String getDirectory();
}
