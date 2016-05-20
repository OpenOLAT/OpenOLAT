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
package org.olat.ims.qti21.model.audit;

import java.util.Date;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;

import uk.ac.ed.ph.jqtiplus.types.FileResponseData;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;

/**
 * 
 * Initial date: 20.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentResponseData implements AssessmentResponse {
	
	private Date creationDate;
	private ResponseData data;
	private Identifier responseIdentifier;
	
	public AssessmentResponseData(Identifier responseIdentifier, ResponseData data) {
		this.data = data;
		creationDate = new Date();
		this.responseIdentifier = responseIdentifier;
	}
	
	@Override
	public Long getKey() {
		return -1l;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	@Override
	public Date getLastModified() {
		return creationDate;
	}

	@Override
	public void setLastModified(Date date) {
		//
	}

	@Override
	public String getResponseIdentifier() {
		return responseIdentifier.toString();
	}

	@Override
	public String getStringuifiedResponse() {
		StringBuilder stringuifiedResponse = new StringBuilder();
		if(data.getType() == ResponseDataType.STRING) {
			List<String> stringuifiedResponses = ((StringResponseData)data).getResponseData();
			if(stringuifiedResponses != null) {
				for(String string:stringuifiedResponses) {
					if(stringuifiedResponse.length() > 0) stringuifiedResponse.append(", ");
					stringuifiedResponse.append(string);
				}
			}
		} else if(data.getType() == ResponseDataType.FILE) {
			String filename = ((FileResponseData)data).getFileName();
			if(StringHelper.containsNonWhitespace(filename)) {
				stringuifiedResponse.append(filename);
			}
		}
		
		return stringuifiedResponse.toString();
	}

	@Override
	public void setStringuifiedResponse(String response) {
		//
	}

	@Override
	public AssessmentItemSession getAssessmentItemSession() {
		return null;
	}
}
