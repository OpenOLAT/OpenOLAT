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
package org.olat.ims.qti21.ui.components;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentObjectFormItem extends FormItemImpl implements FormItemCollection {

	private final FormSubmit submitButton;
	private Map<String,FormItem> components = new HashMap<>();
	
	public AssessmentObjectFormItem(String name, FormSubmit submitButton) {
		super(name);
		this.submitButton = submitButton;
	}
	
	public FormSubmit getSubmitButton() {
		return submitButton;
	}

	@Override
	public abstract AssessmentObjectComponent getComponent();
	
	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}
	
	public void addFormItem(FormItem item) {
		components.put(item.getName(), item);
	}
	
	public String getMapperUri() {
		return getComponent().getMapperUri();
	}
	
	public void setMapperUri(String mapperUri) {
		getComponent().setMapperUri(mapperUri);
	}
	
	public String getSubmissionMapperUri() {
		return getComponent().getSubmissionMapperUri();
	}
	
	public void setSubmissionMapperUri(String submissionMapperUri) {
		getComponent().setSubmissionMapperUri(submissionMapperUri);
	}
	
	public URI getAssessmentObjectUri() {
		return getComponent().getAssessmentObjectUri();
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		getComponent().setAssessmentObjectUri(assessmentObjectUri);
	}
	
	public ResourceLocator getResourceLocator() {
		return getComponent().getResourceLocator();
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		getComponent().setResourceLocator(resourceLocator);
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return getComponent().getCandidateSessionContext();
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		getComponent().setCandidateSessionContext(candidateSessionContext);
	}
	
	public boolean isCorrectionHelp() {
		return getComponent().isCorrectionHelp();
	}
	
	public void setCorrectionHelp(boolean enable) {
		getComponent().setCorrectionHelp(enable);
	}
	
	protected Map<Identifier, StringResponseData> extractStringResponseData() {
        final Map<Identifier, StringResponseData> responseMap = new HashMap<>();

        final Set<String> parameterNames = getRootForm().getRequestParameterSet();
        for (final String name : parameterNames) {
            if (name.startsWith("qtiworks_presented_")) {
                final String responseIdentifierString = name.substring("qtiworks_presented_".length());
                final Identifier responseIdentifier;
                try {
                    responseIdentifier = Identifier.parseString(responseIdentifierString);
                }
                catch (final QtiParseException e) {
                    //throw new BadResponseWebPayloadException("Bad response identifier encoded in parameter  " + name, e);
                	throw new RuntimeException("Bad response identifier encoded in parameter  " + name, e);
                }
                
                final String[] responseValues = getRootForm().getRequestParameterValues("qtiworks_response_" + responseIdentifierString);
                final StringResponseData stringResponseData = new StringResponseData(responseValues);
                responseMap.put(responseIdentifier, stringResponseData);
            }
        }
        return responseMap;
    }
	
	protected Map<Identifier, MultipartFileInfos> extractFileResponseData() {
		Map<Identifier, MultipartFileInfos> fileResponseMap = new HashMap<>();

		Set<String> parameterNames = new HashSet<>(getRootForm().getRequestMultipartFilesSet());
		parameterNames.addAll(getRootForm().getRequestParameterSet());
		for (String name : parameterNames) {
			if (name.startsWith("qtiworks_uploadpresented_")) {
				String responseIdentifierString = name.substring("qtiworks_uploadpresented_".length());
				Identifier responseIdentifier;
				try {
					responseIdentifier = Identifier.parseString(responseIdentifierString);
				} catch (final QtiParseException e) {
					throw new RuntimeException("Bad response identifier encoded in parameter " + name, e);
				}
				String multipartName = "qtiworks_uploadresponse_" + responseIdentifierString;
				MultipartFileInfos multipartFile = getRootForm().getRequestMultipartFileInfos(multipartName);
				if (multipartFile == null) {
					throw new RuntimeException("Expected to find multipart file with name " + multipartName);
				}
				fileResponseMap.put(responseIdentifier, multipartFile);
			}
		}
		return fileResponseMap;
	}
}
