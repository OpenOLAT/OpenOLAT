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

import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.response;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFormItem extends FormItemImpl {
	
	private final AssessmentItemComponent component;
	
	private String mapperUri;
	
	public AssessmentItemFormItem(String name) {
		super(name);
		component = new AssessmentItemComponent(name + "_cmp", this);
	}
	
	public String getMapperUri() {
		return mapperUri;
	}

	public void setMapperUri(String mapperUri) {
		this.mapperUri = mapperUri;
	}

	public URI getAssessmentObjectUri() {
		return component.getAssessmentObjectUri();
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		component.setAssessmentObjectUri(assessmentObjectUri);
	}

	public ItemSessionController getItemSessionController() {
		return component.getItemSessionController();
	}

	public void setItemSessionController(ItemSessionController itemSessionController) {
		component.setItemSessionController(itemSessionController);
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return component.getCandidateSessionContext();
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		component.setCandidateSessionContext(candidateSessionContext);
	}

	public ResourceLocator getResourceLocator() {
		return component.getResourceLocator();
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		component.setResourceLocator(resourceLocator);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String uri = ureq.getModuleURI();
		if(uri.startsWith(solution.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(solution, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(response.getPath())) {
			final Map<Identifier, StringResponseData> stringResponseMap = extractStringResponseData();
			//TODO Extract and import file responses (if appropriate)
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(response, stringResponseMap, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(resethard.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(resethard, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(resetsoft.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(resetsoft, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(close.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(close, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(exit.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(exit, this);
			getRootForm().fireFormEvent(ureq, event);
		}
	}
	
	private Map<Identifier, StringResponseData> extractStringResponseData() {
        final Map<Identifier, StringResponseData> responseMap = new HashMap<Identifier, StringResponseData>();

        final Set<String> parameterNames = getRootForm().getRequestParameterSet();;
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
	

	@Override
	public void reset() {
		//
	}
}