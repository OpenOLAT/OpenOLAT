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
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.rendering.AbstractRenderingOptions;
import org.olat.ims.qti21.ui.rendering.AbstractRenderingRequest;
import org.olat.ims.qti21.ui.rendering.AssessmentRenderer;
import org.olat.ims.qti21.ui.rendering.ItemRenderingOptions;
import org.olat.ims.qti21.ui.rendering.ItemRenderingRequest;
import org.olat.ims.qti21.ui.rendering.SerializationMethod;
import org.olat.ims.qti21.ui.rendering.TerminatedRenderingRequest;

import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemComponentRenderer extends DefaultComponentRenderer {
	
	private AssessmentRenderer assessmentRenderer = new AssessmentRenderer();

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		boolean iframePostEnabled = flags.isIframePostEnabled();
		
		AssessmentItemComponent cmp = (AssessmentItemComponent)source;
		ItemSessionController itemSessionController = cmp.getItemSessionController();
		AssessmentItemFormItem item = cmp.getQtiItem();
		
		//if(itemSessionController.getItemSessionState().isEnded()) {
		//	sb.append("<h1>The End <small>say the renderer</small></h1>");
		//} else {
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
	        final ItemRenderingOptions renderingOptions = new ItemRenderingOptions();
	        configureBaseRenderingOptions(sessionBaseUrl, mapperUrl, renderingOptions);
	        renderingOptions.setEndUrl(sessionBaseUrl + "close");
	        renderingOptions.setSolutionUrl(sessionBaseUrl + "solution");
	        renderingOptions.setSoftResetUrl(sessionBaseUrl + "reset-soft");
	        renderingOptions.setHardResetUrl(sessionBaseUrl + "reset-hard");
	        renderingOptions.setExitUrl(sessionBaseUrl + "exit");
	
	        Writer writer = new StringWriter();
	        StreamResult result = new StreamResult(writer);
	        renderCurrentCandidateItemSessionState(cmp.getCandidateSessionContext(), renderingOptions, result, cmp);
	        
	        String output = writer.toString();
        	output = replaces(output);
        	sb.append(output);
		//}
	}
	
	private String replaces(String result) {
		int index = result.indexOf("<body");
		String output;
		if(index > 0) { 
			output = result.substring(index + 5);
			index = output.indexOf("</body>");
			output = output.substring(0, index);
			output = output.replace("<form action", "<form target='oaa0' action");
			output = output.replace("<form method", "<form target='oaa0' method");
		} else {
			output = "class='o_error'>ERROR";
		}
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
	
    private void renderCurrentCandidateItemSessionState(CandidateSessionContext candidateSessionContext,
            ItemRenderingOptions renderingOptions, StreamResult result, AssessmentItemComponent component) {
        
    	final UserTestSession candidateSession = candidateSessionContext.getCandidateSession();
        if (candidateSession != null && candidateSession.isExploded()) {
            renderExploded(renderingOptions, result, component);
        } else if (candidateSessionContext.isTerminated()) {
            renderTerminated(renderingOptions, result, component);
        } else {
            /* Look up most recent event */
            final CandidateEvent latestEvent = candidateSessionContext.getLastEvent();// assertSessionEntered(candidateSession);

            /* Load the ItemSessionState */
            final ItemSessionState itemSessionState = component.getItemSessionController().getItemSessionState();// candidateDataService.loadItemSessionState(latestEvent);

            /* Touch the session's duration state if appropriate */
            if (itemSessionState.isEntered() && !itemSessionState.isEnded() && !itemSessionState.isSuspended()) {
                final Date timestamp = candidateSessionContext.getCurrentRequestTimestamp();
                final ItemSessionController itemSessionController = component.getItemSessionController();
                itemSessionController.touchDuration(timestamp);
            }

            /* Render event */
            renderItemEvent(latestEvent, itemSessionState, renderingOptions, result, component);
        }
    }
	
    private void renderExploded(AbstractRenderingOptions renderingOptions, StreamResult result, AssessmentItemComponent component) {
        assessmentRenderer.renderExploded(createTerminatedRenderingRequest(renderingOptions, component), result);
    }

    private void renderTerminated(AbstractRenderingOptions renderingOptions, StreamResult result, AssessmentItemComponent component) {
        assessmentRenderer.renderTeminated(createTerminatedRenderingRequest(renderingOptions, component), result);
    }

    private TerminatedRenderingRequest createTerminatedRenderingRequest(AbstractRenderingOptions renderingOptions,
    		AssessmentItemComponent component) {
        final TerminatedRenderingRequest renderingRequest = new TerminatedRenderingRequest();
        initRenderingRequest(renderingRequest, renderingOptions, component);
        return renderingRequest;
    }
	
    private void renderItemEvent(CandidateEvent candidateEvent, ItemSessionState itemSessionState,
            ItemRenderingOptions renderingOptions, StreamResult result, AssessmentItemComponent component) {
        
    	final CandidateItemEventType itemEventType = candidateEvent.getItemEventType();


        /* Create and partially configure rendering request */
        final ItemRenderingRequest renderingRequest = new ItemRenderingRequest();
        initRenderingRequest(renderingRequest, renderingOptions, component);
        renderingRequest.setItemSessionState(itemSessionState);
        renderingRequest.setPrompt("" /* itemDeliverySettings.getPrompt() */);

        /* If session has terminated, render appropriate state and exit */
        if (itemSessionState.isExited()) {
            assessmentRenderer.renderTeminated(createTerminatedRenderingRequest(renderingRequest.getRenderingOptions(), component), result);
            return;
        }

        /* Detect "modal" events. These will cause a particular rendering state to be
         * displayed, which candidate will then leave.
         */
        if (itemEventType==CandidateItemEventType.SOLUTION) {
            renderingRequest.setSolutionMode(true);
        }

        /* Now set candidate action permissions depending on state of session */
        if (itemEventType==CandidateItemEventType.SOLUTION || itemSessionState.isEnded()) {
            /* Item session is ended (closed) */
            renderingRequest.setEndAllowed(false);
            renderingRequest.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenEnded() */);
            renderingRequest.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenEnded() */);
            renderingRequest.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenEnded() */);
            renderingRequest.setCandidateCommentAllowed(false);
        }
        else if (itemSessionState.isOpen()) {
            /* Item session is open (interacting) */
            renderingRequest.setEndAllowed(true /* itemDeliverySettings.isAllowEnd() */);
            renderingRequest.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenOpen() */);
            renderingRequest.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenOpen() */);
            renderingRequest.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenOpen() */);
            renderingRequest.setCandidateCommentAllowed(false /* itemDeliverySettings.isAllowCandidateComment() */);
        }
        else {
            throw new OLATRuntimeException("Item has not been entered yet. We do not currently support rendering of this state.", null);
        }

        /* Finally pass to rendering layer */
       // candidateAuditLogger.logItemRendering(candidateEvent);
        //final List<CandidateEventNotification> notifications = candidateEvent.getNotifications();
        try {
            assessmentRenderer.renderItem(renderingRequest, result);
        } catch (final RuntimeException e) {
            /* Rendering is complex and may trigger an unexpected Exception (due to a bug in the XSLT).
             * In this case, the best we can do for the candidate is to 'explode' the session.
             * See bug #49.
             */
            //handleExplosion(e, candidateSession);
            assessmentRenderer.renderExploded(createTerminatedRenderingRequest(renderingOptions, component), result);
        }
    }
	
    private <P extends AbstractRenderingOptions> void initRenderingRequest(AbstractRenderingRequest<P> renderingRequest, P renderingOptions, AssessmentItemComponent component) {
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