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
package org.olat.ims.qti21.ui;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.CandidateEvent;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.ui.rendering.AbstractRenderingOptions;
import org.olat.ims.qti21.ui.rendering.AbstractRenderingRequest;
import org.olat.ims.qti21.ui.rendering.AssessmentRenderer;
import org.olat.ims.qti21.ui.rendering.SerializationMethod;
import org.olat.ims.qti21.ui.rendering.TerminatedRenderingRequest;
import org.olat.ims.qti21.ui.rendering.TestRenderingMode;
import org.olat.ims.qti21.ui.rendering.TestRenderingOptions;
import org.olat.ims.qti21.ui.rendering.TestRenderingRequest;

import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ComponentRenderer extends DefaultComponentRenderer {
	
	private AssessmentRenderer assessmentRenderer = new AssessmentRenderer();

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		boolean iframePostEnabled = flags.isIframePostEnabled();
		
		QTI21Component cmp = (QTI21Component)source;
		TestSessionController testSessionController = cmp.getTestSessionController();
		QTI21FormItem item = cmp.getQtiItem();
		
		if(testSessionController.getTestSessionState().isEnded()) {
			sb.append("<h1>Ended</h1>");
		} else {
			Component rootFormCmp = item.getRootForm().getInitialComponent();
			
			URLBuilder formUbuBuilder = renderer.getUrlBuilder().createCopyFor(rootFormCmp);
			StringOutput formUrl = new StringOutput();
			formUbuBuilder.buildURI(formUrl,
					new String[] { Form.FORMID, "dispatchuri", "dispatchevent" },
					new String[] { Form.FORMCMD, item.getFormDispatchId(), "0" },
					iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			
	        /* Create appropriate options that link back to this controller */
	        final String sessionBaseUrl = formUrl.toString();
	        final String mapperUrl = item.getMapperUri();
	        final TestRenderingOptions renderingOptions = new TestRenderingOptions();
	        configureBaseRenderingOptions(sessionBaseUrl, mapperUrl, renderingOptions);
	        renderingOptions.setTestPartNavigationUrl(sessionBaseUrl + "test-part-navigation");
	        renderingOptions.setSelectTestItemUrl(sessionBaseUrl + "select-item");
	        renderingOptions.setAdvanceTestItemUrl(sessionBaseUrl + "finish-item");
	        renderingOptions.setReviewTestPartUrl(sessionBaseUrl + "review-test-part");
	        renderingOptions.setReviewTestItemUrl(sessionBaseUrl + "review-item");
	        renderingOptions.setShowTestItemSolutionUrl(sessionBaseUrl + "item-solution");
	        renderingOptions.setEndTestPartUrl(sessionBaseUrl + "end-test-part");
	        renderingOptions.setAdvanceTestPartUrl(sessionBaseUrl + "advance-test-part");
	        renderingOptions.setExitTestUrl(sessionBaseUrl + "exit-test");
	
	        Writer writer = new StringWriter();
	        StreamResult result = new StreamResult(writer);
	        renderCurrentCandidateTestSessionState(testSessionController, renderingOptions, result, cmp);
	        
	        String output = writer.toString();
        	output = replaces(output);
        	sb.append(output);
		}
	}
	
	private String replaces(String result) {
		int index = result.indexOf("<body");
		String output = result.substring(index + 5);
		index = output.indexOf("</body>");
		output = output.substring(0, index);
		output = output.replace("<form action", "<form target='oaa0' action");
		output = output.replace("<form method", "<form target='oaa0' method");
		return "<div " + output + "</div>";
	}
	
	private void configureBaseRenderingOptions(final String sessionBaseUrl, final String mapperUrl, final AbstractRenderingOptions renderingOptions) {
        renderingOptions.setSerializationMethod(SerializationMethod.HTML5_MATHJAX);
        renderingOptions.setSourceUrl(sessionBaseUrl + "source");
        renderingOptions.setStateUrl(sessionBaseUrl + "state");
        renderingOptions.setResultUrl(sessionBaseUrl + "result");
        renderingOptions.setValidationUrl(sessionBaseUrl + "validation");
        renderingOptions.setServeFileUrl(mapperUrl + "/file");
        renderingOptions.setAuthorViewUrl(sessionBaseUrl + "author-view");
        renderingOptions.setResponseUrl(sessionBaseUrl + "response");
    }
	
	private void renderCurrentCandidateTestSessionState(TestSessionController testSessionController,
			TestRenderingOptions renderingOptions, StreamResult result, QTI21Component component) {
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		CandidateSessionContext candidateSessionContext = component.getCandidateSessionContext();
		final UserTestSession candidateSession = candidateSessionContext.getCandidateSession();
		
        if (candidateSession.isExploded()) {
            renderExploded(candidateSessionContext, renderingOptions, result, component);
        }
	
        if (candidateSessionContext.isTerminated()) {
            renderTerminated(candidateSessionContext, renderingOptions, result, component);
        } else {
			/* Look up most recent event */
			   // final CandidateEvent latestEvent = assertSessionEntered(candidateSession);
			
			/* Load the TestSessionState and create a TestSessionController */
			//final TestSessionState testSessionState = candidateDataService.loadTestSessionState(latestEvent);
			//final TestSessionController testSessionController = createTestSessionController(candidateSession, testSessionState);
			
			/* Touch the session's duration state if appropriate */
			if (testSessionState.isEntered() && !testSessionState.isEnded()) {
			    final Date timestamp = candidateSessionContext.getCurrentRequestTimestamp();
			    testSessionController.touchDurations(timestamp);
			}
			
			/* Render event */
			renderTestEvent(testSessionController, renderingOptions, result, component);
		}
	}
	
    private void renderExploded(CandidateSessionContext candidateSessionContext, AbstractRenderingOptions renderingOptions, StreamResult result, QTI21Component component) {
        assessmentRenderer.renderExploded(createTerminatedRenderingRequest(candidateSessionContext, renderingOptions, component), result);
    }

    private void renderTerminated(CandidateSessionContext candidateSessionContext, AbstractRenderingOptions renderingOptions, StreamResult result, QTI21Component component) {
        assessmentRenderer.renderTeminated(createTerminatedRenderingRequest(candidateSessionContext, renderingOptions, component), result);
    }

    //----------------------------------------------------

    private TerminatedRenderingRequest createTerminatedRenderingRequest(CandidateSessionContext candidateSessionContext,
    		AbstractRenderingOptions renderingOptions, QTI21Component component) {
        final TerminatedRenderingRequest renderingRequest = new TerminatedRenderingRequest();
        initRenderingRequest(renderingRequest, renderingOptions, component);
        //renderingRequest.setExitSessionUrl(candidateSessionContext.getReturnUrl());
        return renderingRequest;
    }
	
	private void renderTestEvent(final TestSessionController testSessionController,
            final TestRenderingOptions renderingOptions, final StreamResult result, QTI21Component component) {
        
		
		CandidateSessionContext candidateSessionContext = component.getCandidateSessionContext();
		//final CandidateTestEventType testEventType = candidateEvent.getTestEventType();
		CandidateEvent candidateEvent = candidateSessionContext.getLastEvent();
		CandidateTestEventType testEventType = candidateEvent.getTestEventType();
        final UserTestSession candidateSession = candidateSessionContext.getCandidateSession();//candidateEvent.getCandidateSession();

        /* Create and partially configure rendering request */
        final TestRenderingRequest renderingRequest = new TestRenderingRequest();
        initRenderingRequest(renderingRequest, renderingOptions, component);
        renderingRequest.setTestSessionController(testSessionController);

        /* If session has terminated, render appropriate state and exit */
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        if (candidateSessionContext.isTerminated() || testSessionState.isExited()) {
            assessmentRenderer.renderTeminated(createTerminatedRenderingRequest(candidateSessionContext, renderingRequest.getRenderingOptions(), component), result);
            return;
        }

        /* Check for "modal" events first. These cause a particular rendering state to be
         * displayed, which candidate will then leave.
         */
        if (testEventType == CandidateTestEventType.REVIEW_ITEM) {
            // Extract item to review 
            renderingRequest.setTestRenderingMode(TestRenderingMode.ITEM_REVIEW);
            renderingRequest.setModalItemKey(extractTargetItemKey(candidateEvent));
        } else if (testEventType == CandidateTestEventType.SOLUTION_ITEM) {
            // Extract item to show solution 
            renderingRequest.setTestRenderingMode(TestRenderingMode.ITEM_SOLUTION);
            renderingRequest.setModalItemKey(extractTargetItemKey(candidateEvent));
        }

        /* Pass to rendering layer */
        assessmentRenderer.renderTest(renderingRequest, result);
    }
	
    private TestPlanNodeKey extractTargetItemKey(final CandidateEvent candidateEvent) {
        final String keyString = candidateEvent.getTestItemKey();
        try {
            return TestPlanNodeKey.fromString(keyString);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Unexpected Exception parsing TestPlanNodeKey " + keyString, e);
        }
    }
	
	private <P extends AbstractRenderingOptions> void initRenderingRequest(
            final AbstractRenderingRequest<P> renderingRequest, final P renderingOptions, QTI21Component component) {

        renderingRequest.setRenderingOptions(renderingOptions);
        renderingRequest.setAssessmentResourceLocator(component.getResourceLocator());
        renderingRequest.setAssessmentResourceUri(component.getAssessmentObjectUri());
        renderingRequest.setAuthorMode(false);
        renderingRequest.setValidated(true);
        renderingRequest.setLaunchable(true);
        renderingRequest.setErrorCount(0);
        renderingRequest.setWarningCount(0);
        renderingRequest.setValid(true);
    }
}