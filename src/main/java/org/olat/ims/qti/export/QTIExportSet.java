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

package org.olat.ims.qti.export;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.ims.qti.QTIResult;

/**
 * Description: TODO
 * 
 * @author Alexander Schneider
 */
public class QTIExportSet {
	private QTIResult qtir;
	
	public QTIExportSet(QTIResult qtir){
		this.qtir = qtir;
	}

	public String getFirstName(){
		return qtir.getResultSet().getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
	}
	
	public String getLastName(){
		return qtir.getResultSet().getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
	}
	
	public String getLogin(){
		return qtir.getResultSet().getIdentity().getName();
	}
	
	public String getInstitutionalMatriculationNumber(){
		return qtir.getResultSet().getIdentity().getUser().getProperty(UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, null);
	}
	
	public String getInstitutionalEmployeeNumber(){
		return qtir.getResultSet().getIdentity().getUser().getProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, null);
	}

	public float getScore(){
		return qtir.getResultSet().getScore();
	}
	
	public boolean getIsPassed(){
		//<OLATCE-982>
		Boolean passed = qtir.getResultSet().getIsPassed();
		return passed!=null?passed:false;
		//</OLATCE-982>
	}
	
	public String getIp(){
		return qtir.getIp();
	}
	
	public Identity getIdentity(){
		return qtir.getResultSet().getIdentity();
	}
	
	public Date getLastModified(){
	 return qtir.getLastModified();
	}
	
	public Long getDuration(){
		return qtir.getResultSet().getDuration();
	}
}
