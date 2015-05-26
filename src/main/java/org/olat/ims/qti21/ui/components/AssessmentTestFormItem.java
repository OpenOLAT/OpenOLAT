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

import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.advanceTestPart;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.endTestPart;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.exitTest;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.finishItem;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.itemSolution;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.response;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.reviewItem;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.reviewTestPart;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.selectItem;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event.testPartNavigation;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestFormItem extends FormItemImpl {
	
	private final AssessmentTestComponent component;
	
	private String mapperUri;
	
	public AssessmentTestFormItem(String name) {
		super(name);
		component = new AssessmentTestComponent(name + "_cmp", this);
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

	public TestSessionController getTestSessionController() {
		return component.getTestSessionController();
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		component.setTestSessionController(testSessionController);
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
		if(uri.startsWith(selectItem.getPath())) {
			String sub = uri.substring(selectItem.getPath().length());
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(selectItem, sub, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(finishItem.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(finishItem, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(reviewItem.getPath())) {
			String sub = uri.substring(reviewItem.getPath().length());
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(reviewItem, sub, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(itemSolution.getPath())) {
			String sub = uri.substring(itemSolution.getPath().length());
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(itemSolution, sub, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(testPartNavigation.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(testPartNavigation, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(response.getPath())) {
			final Map<Identifier, StringResponseData> stringResponseMap = extractStringResponseData();
			//TODO Extract and import file responses (if appropriate)
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(response, stringResponseMap, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(endTestPart.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(endTestPart, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(advanceTestPart.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(advanceTestPart, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(reviewTestPart.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(reviewTestPart, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(exitTest.getPath())) {
			QTIWorksAssessmentTestEvent event = new QTIWorksAssessmentTestEvent(exitTest, this);
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