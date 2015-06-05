/* Copyright (c) 2012-2013, University of Edinburgh.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * This software is derived from (and contains code from) QTItools and MathAssessEngine.
 * QTItools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package org.olat.ims.qti21.ui.rendering;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BeanPropertyBindingResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import uk.ac.ed.ph.jqtiplus.internal.util.Assert;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.EffectiveItemSessionControl;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPartSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.state.marshalling.ItemSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.state.marshalling.TestSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.FileResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.SimpleXsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;

/**
 * This key service performs the actual rendering of items and tests, supporting the
 * rendering of specific states and particular modal states (e.g. reviewing an item
 * in a test during testPart review).
 * <p>
 * This stands separately from the main QTIWorks domain model and is potentially usable
 * outside of the rest of the QTIWorks engine. I've chosen not to make it a separate module
 * for now.
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>An instance of this class is safe to use concurrently by multiple threads.</li>
 *   <li>If using outside QTIWorks engine, remember to set the necessary properties then call {@link #init()}</li>
 * </ul>
 *
 * @author David McKain
 */
public class AssessmentRenderer {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentRenderer.class);

    private static final URI serializeXsltUri;
    private static final URI ctopXsltUri;
    private static final URI itemStandaloneXsltUri;
    private static final URI testItemXsltUri;
    private static final URI testEntryXsltUri;
    private static final URI testPartNavigationXsltUri;
    private static final URI testPartFeedbackXsltUri;
    private static final URI testFeedbackXsltUri;
    private static final URI terminatedXsltUri;
    private static final URI explodedXsltUri;
	
    static {
    	String path;
    	if(Settings.isDebuging() && StringHelper.containsNonWhitespace(WebappHelper.getSourcePath())) {
    		path = "file://" + WebappHelper.getSourcePath().replace("/src/main/java", "/src/main/resources");
    	} else {
    		path = "classpath:";
    	}

		serializeXsltUri = URI.create(path + "/rendering-xslt/serialize.xsl");
	    ctopXsltUri = URI.create(path + "/rendering-xslt/ctop.xsl");
	    itemStandaloneXsltUri = URI.create(path + "/rendering-xslt/item-standalone.xsl");
	    testItemXsltUri = URI.create(path + "/rendering-xslt/test-item.xsl");
	    testEntryXsltUri = URI.create(path + "/rendering-xslt/test-entry.xsl");
	    testPartNavigationXsltUri = URI.create(path + "/rendering-xslt/test-testpart-navigation.xsl");
	    testPartFeedbackXsltUri = URI.create(path + "/rendering-xslt/test-testpart-feedback.xsl");
	    testFeedbackXsltUri = URI.create(path + "/rendering-xslt/test-feedback.xsl");
	    terminatedXsltUri = URI.create(path + "/rendering-xslt/terminated.xsl");
	    explodedXsltUri = URI.create(path + "/rendering-xslt/exploded.xsl");
    }

    /** Manager for the XSLT stylesheets, created during init. */
    private XsltStylesheetManager stylesheetManager;

    //----------------------------------------------------
    public AssessmentRenderer() {
    	if(Settings.isDebuging() && StringHelper.containsNonWhitespace(WebappHelper.getSourcePath())) {
    		stylesheetManager = new XsltStylesheetManager(new FileResourceLocator(), null);
    	} else {
    		stylesheetManager = new XsltStylesheetManager(new ClassPathResourceLocator(), new SimpleXsltStylesheetCache());
    	}
    }

    //----------------------------------------------------

    /**
     * Renders the given {@link ItemRenderingRequest}, sending the result to the provided JAXP {@link Result}.
     * <p>
     * The rendering shows the current state of the item, unless {@link ItemRenderingRequest#isSolutionMode()}
     * returns true, in which case the model solution is rendered.
     * <p>
     * NB: If you're using a {@link StreamResult} then you probably want to wrap it around an
     * {@link OutputStream} rather than a {@link Writer}. Remember that you are responsible for
     * closing the {@link OutputStream} or {@link Writer} afterwards!
     * The caller is responsible for closing this stream afterwards.
     */
    public void renderItem(final ItemRenderingRequest request, final Result result) {
        Assert.notNull(request, "request");
        Assert.notNull(result, "result");

        /* Check request is valid */
        final BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "itemRenderingRequest");
        //jsr303Validator.validate(request, errors);
        if (errors.hasErrors()) {
            throw new IllegalArgumentException("Invalid " + request.getClass().getSimpleName()
                    + " Object: " + errors);
        }

        /* Pass request info to XSLT as parameters */
        final Map<String, Object> xsltParameters = new HashMap<String, Object>();
        setBaseRenderingParameters(xsltParameters, request, null);

        /* Pass ItemSessionState (as DOM Document) */
        final ItemSessionState itemSessionState = request.getItemSessionState();
        xsltParameters.put("itemSessionState", ItemSessionStateXmlMarshaller.marshal(itemSessionState).getDocumentElement());

        /* Set control parameters */
        xsltParameters.put("prompt", request.getPrompt());
        xsltParameters.put("solutionMode", Boolean.valueOf(request.isSolutionMode()));
        xsltParameters.put("endAllowed", Boolean.valueOf(request.isEndAllowed()));
        xsltParameters.put("softSoftResetAllowed", Boolean.valueOf(request.isSoftResetAllowed()));
        xsltParameters.put("hardResetAllowed", Boolean.valueOf(request.isHardResetAllowed()));
        xsltParameters.put("solutionAllowed", Boolean.valueOf(request.isSolutionAllowed()));
        xsltParameters.put("candidateCommentAllowed", Boolean.valueOf(request.isCandidateCommentAllowed()));

        /* Set action URLs */
        final ItemRenderingOptions renderingOptions = request.getRenderingOptions();
        xsltParameters.put("endUrl", renderingOptions.getEndUrl());
        xsltParameters.put("softResetUrl", renderingOptions.getSoftResetUrl());
        xsltParameters.put("hardResetUrl", renderingOptions.getHardResetUrl());
        xsltParameters.put("exitUrl", renderingOptions.getExitUrl());
        xsltParameters.put("solutionUrl", renderingOptions.getSolutionUrl());

        /* Perform transform */
        doTransform(request, itemStandaloneXsltUri, xsltParameters, result);
    }

    /**
     * Renders the given {@link TestRenderingRequest}, sending the result to the
     * provided JAXP {@link Result}.
     * <p>
     * NB: If you're using a {@link StreamResult} then you probably want to wrap it around an
     * {@link OutputStream} rather than a {@link Writer}. Remember that you are responsible for
     * closing the {@link OutputStream} or {@link Writer} afterwards!
     *
     * @throws QtiWorksRenderingException if an unexpected Exception happens during rendering
     */
    public void renderTest(final TestRenderingRequest request, final Result result) {
        Assert.notNull(request, "renderingRequest");
        Assert.notNull(result, "result");

        /* Check request is valid */
        final BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "testRenderingRequest");
        //jsr303Validator.validate(request, errors);
        if (errors.hasErrors()) {
            throw new IllegalArgumentException("Invalid " + request.getClass().getSimpleName()
                    + " Object: " + errors);
        }

        /* Set up general XSLT parameters */
        final Map<String, Object> xsltParameters = new HashMap<String, Object>();
        setBaseRenderingParameters(xsltParameters, request, null);

        final TestSessionController testSessionController = request.getTestSessionController();
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        xsltParameters.put("testSessionState", TestSessionStateXmlMarshaller.marshal(testSessionState).getDocumentElement());
        xsltParameters.put("testSystemId", request.getAssessmentResourceUri().toString());

        /* Pass rendering options */
        final TestRenderingOptions renderingOptions = request.getRenderingOptions();
        xsltParameters.put("testPartNavigationUrl", renderingOptions.getTestPartNavigationUrl());
        xsltParameters.put("selectTestItemUrl", renderingOptions.getSelectTestItemUrl());
        xsltParameters.put("advanceTestItemUrl", renderingOptions.getAdvanceTestItemUrl());
        xsltParameters.put("endTestPartUrl", renderingOptions.getEndTestPartUrl());
        xsltParameters.put("reviewTestPartUrl", renderingOptions.getReviewTestPartUrl());
        xsltParameters.put("reviewTestItemUrl", renderingOptions.getReviewTestItemUrl());
        xsltParameters.put("showTestItemSolutionUrl", renderingOptions.getShowTestItemSolutionUrl());
        xsltParameters.put("advanceTestPartUrl", renderingOptions.getAdvanceTestPartUrl());
        xsltParameters.put("exitTestUrl", renderingOptions.getExitTestUrl());

        final TestRenderingMode testRenderingMode = request.getTestRenderingMode();
        if (testRenderingMode==TestRenderingMode.ITEM_REVIEW) {
            doRenderTestItemReview(request, xsltParameters, result);
        }
        else if (testRenderingMode==TestRenderingMode.ITEM_SOLUTION) {
            doRenderTestItemSolution(request, xsltParameters, result);
        }
        else {
            /* Render current state */
            final TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
            if (testSessionState.isEnded()) {
                /* At end of test, so show overall test feedback */
                doRenderTestFeedback(request, xsltParameters, result);
            }
            else if (currentTestPartKey!=null) {
                final TestPartSessionState currentTestPartSessionState = testSessionState.getTestPartSessionStates().get(currentTestPartKey);
                final TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
                if (currentItemKey!=null) {
                    /* An item is selected, so render it in appropriate state */
                    doRenderCurrentTestItem(request, xsltParameters, result);
                }
                else {
                    /* No item selected */
                    if (currentTestPartSessionState.isEnded()) {
                        /* testPart has ended, so must be showing testPart feedback */
                        doRenderTestPartFeedback(request, xsltParameters, result);
                    }
                    else {
                        /* testPart not ended, so we must be showing the navigation menu in nonlinear mode */
                        doRenderTestPartNavigation(request, xsltParameters, result);
                    }
                }
            }
            else {
                /* No current testPart == start of multipart test */
                doRenderTestEntry(request, xsltParameters, result);
            }
        }
    }

    private void doRenderTestEntry(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        doTransform(request, testEntryXsltUri, xsltParameters, result);
    }

    private void doRenderTestPartNavigation(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        /* Determine whether candidate may exist testPart */
        final TestSessionController testSessionController = request.getTestSessionController();
        xsltParameters.put("endTestPartAllowed", Boolean.valueOf(testSessionController.mayEndCurrentTestPart()));

        doTransform(request, testPartNavigationXsltUri, xsltParameters, result);
    }

    private void doRenderTestPartFeedback(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        doTransform(request, testPartFeedbackXsltUri, xsltParameters, result);
    }

    private void doRenderTestFeedback(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        doTransform(request, testFeedbackXsltUri, xsltParameters, result);
    }

    private void doRenderCurrentTestItem(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        /* Extract the item to be rendered */
        final TestSessionController testSessionController = request.getTestSessionController();
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        final TestPlanNodeKey itemKey = testSessionState.getCurrentItemKey();

        /* Set item parameters */
        final URI itemSystemId = setTestItemParameters(request, itemKey, xsltParameters);

        /* Set specific parameters for this rendering */
        final TestPart currentTestPart = testSessionController.getCurrentTestPart();
        final NavigationMode navigationMode = currentTestPart.getNavigationMode();
        xsltParameters.put("reviewMode", Boolean.FALSE);
        xsltParameters.put("solutionMode", Boolean.FALSE);
        xsltParameters.put("advanceTestItemAllowed", Boolean.valueOf(navigationMode==NavigationMode.LINEAR && testSessionController.mayAdvanceItemLinear()));
        xsltParameters.put("testPartNavigationAllowed", Boolean.valueOf(navigationMode==NavigationMode.NONLINEAR));
        xsltParameters.put("endTestPartAllowed", Boolean.valueOf(navigationMode==NavigationMode.LINEAR && testSessionController.mayEndCurrentTestPart()));

        /* We finally do the transform on the _item_ (NB!) */
        doTransform(request, itemSystemId, testItemXsltUri, xsltParameters, result);
    }

    private void doRenderTestItemReview(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        /* Extract item to review */
        final TestPlanNodeKey reviewItemKey = request.getModalItemKey();

        /* Set item parameters */
        final URI itemSystemId = setTestItemParameters(request, reviewItemKey, xsltParameters);

        /* Set specific parameters for this rendering */
        xsltParameters.put("reviewMode", Boolean.TRUE);
        xsltParameters.put("solutionMode", Boolean.FALSE);
        xsltParameters.put("testPartNavigationAllowed", Boolean.FALSE);
        xsltParameters.put("advanceTestItemAllowed", Boolean.FALSE);
        xsltParameters.put("endTestPartAllowed", Boolean.FALSE);

        /* We finally do the transform on the _item_ (NB!) */
        doTransform(request, itemSystemId, testItemXsltUri, xsltParameters, result);
    }

    private void doRenderTestItemSolution(final TestRenderingRequest request,
            final Map<String, Object> xsltParameters, final Result result) {
        /* Extract item to review */
        final TestPlanNodeKey solutionItemKey = request.getModalItemKey();

        /* Set item parameters */
        final URI itemSystemId = setTestItemParameters(request, solutionItemKey, xsltParameters);

        /* Set specific parameters for this rendering */
        xsltParameters.put("reviewMode", Boolean.TRUE);
        xsltParameters.put("solutionMode", Boolean.TRUE);
        xsltParameters.put("testPartNavigationAllowed", Boolean.FALSE);
        xsltParameters.put("advanceTestItemAllowed", Boolean.FALSE);
        xsltParameters.put("endTestPartAllowed", Boolean.FALSE);

        /* We finally do the transform on the _item_ (NB!) */
        doTransform(request, itemSystemId, testItemXsltUri, xsltParameters, result);
    }
    
    /**
     * Renders an exploded session, sending the result to the provided JAXP {@link Result}.
     * <p>
     * NB: If you're using a {@link StreamResult} then you probably want to wrap it around an
     * {@link OutputStream} rather than a {@link Writer}. Remember that you are responsible for
     * closing the {@link OutputStream} or {@link Writer} afterwards!
     */
    public void renderExploded(final TerminatedRenderingRequest request, final Result result) {
        Assert.notNull(result, "result");

        final Map<String, Object> xsltParameters = new HashMap<String, Object>();
        setBaseRenderingParameters(xsltParameters, request, null);
        xsltParameters.put("exitSessionUrl", request.getExitSessionUrl());

        doTransform(request, null, explodedXsltUri, xsltParameters, result);
    }

    /**
     * Renders a terminated session, sending the result to the provided JAXP {@link Result}.
     * <p>
     * NB: If you're using a {@link StreamResult} then you probably want to wrap it around an
     * {@link OutputStream} rather than a {@link Writer}. Remember that you are responsible for
     * closing the {@link OutputStream} or {@link Writer} afterwards!
     */
    public void renderTeminated(final TerminatedRenderingRequest request, final Result result) {
        Assert.notNull(result, "result");

        final Map<String, Object> xsltParameters = new HashMap<String, Object>();
        setBaseRenderingParameters(xsltParameters, request, null);
        xsltParameters.put("exitSessionUrl", request.getExitSessionUrl());

        doTransform(request, null, terminatedXsltUri, xsltParameters, result);
    }


    //----------------------------------------------------

    private URI setTestItemParameters(final TestRenderingRequest request, final TestPlanNodeKey itemKey,
            final Map<String, Object> xsltParameters) {
        final TestSessionController testSessionController = request.getTestSessionController();
        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        final TestPlanNode itemRefNode = testSessionState.getTestPlan().getNode(itemKey);
        if (itemRefNode==null) {
            throw new QtiWorksRenderingException("Failed to locate item with key " + itemKey + " in TestPlan");
        }
        final ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemKey);
        if (itemSessionState==null) {
            throw new QtiWorksRenderingException("Failed to locate ItemSessionState for item with key " + itemKey);
        }

        /* Add item-specific parameters */
        xsltParameters.put("itemSessionState", ItemSessionStateXmlMarshaller.marshal(itemSessionState).getDocumentElement());
        xsltParameters.put("itemKey", itemKey.toString());

        /* Pass ItemSessionControl parameters */
        /* (Add any future additional itemSessionControl parameters here as required) */
        final EffectiveItemSessionControl effectiveItemSessionControl = itemRefNode.getEffectiveItemSessionControl();
        xsltParameters.put("allowComment", Boolean.valueOf(effectiveItemSessionControl.isAllowComment()));
        xsltParameters.put("showFeedback", Boolean.valueOf(effectiveItemSessionControl.isShowFeedback()));
        xsltParameters.put("showSolution", Boolean.valueOf(effectiveItemSessionControl.isShowSolution()));

        /* The caller should reset the following parameters to suit */
        xsltParameters.put("reviewMode", Boolean.FALSE);
        xsltParameters.put("solutionMode", Boolean.FALSE);
        xsltParameters.put("testPartNavigationAllowed", Boolean.FALSE);
        xsltParameters.put("advanceTestItemAllowed", Boolean.FALSE);
        xsltParameters.put("endTestPartAllowed", Boolean.FALSE);

        return itemRefNode.getItemSystemId();
    }

    private <P extends AbstractRenderingOptions> void setBaseRenderingParameters(final Map<String, Object> xsltParameters,
            final AbstractRenderingRequest<P> request, final List<String> notifications) {
        setBaseRenderingParameters(xsltParameters);

        /* Pass notifications */
        if (notifications!=null) {
            //xsltParameters.put("notifications", new XsltParamBuilder().notificationsToElements(notifications));
        }

        /* Pass common control parameters */
        xsltParameters.put("validated", Boolean.valueOf(request.isValidated()));
        xsltParameters.put("launchable", Boolean.valueOf(request.isLaunchable()));
        xsltParameters.put("errorCount", Integer.valueOf(request.getErrorCount()));
        xsltParameters.put("warningCount", Integer.valueOf(request.getWarningCount()));
        xsltParameters.put("valid", Boolean.valueOf(request.isValid()));
        xsltParameters.put("authorMode", Boolean.valueOf(request.isAuthorMode()));

        /* Pass common action URLs */
        final P renderingOptions = request.getRenderingOptions();
        xsltParameters.put("responseUrl", renderingOptions.getResponseUrl());
        xsltParameters.put("serveFileUrl", renderingOptions.getServeFileUrl());
        xsltParameters.put("authorViewUrl", renderingOptions.getAuthorViewUrl());
        xsltParameters.put("sourceUrl", renderingOptions.getSourceUrl());
        xsltParameters.put("stateUrl", renderingOptions.getStateUrl());
        xsltParameters.put("resultUrl", renderingOptions.getResultUrl());
        xsltParameters.put("validationUrl", renderingOptions.getValidationUrl());

    }

    private void setBaseRenderingParameters(final Map<String, Object> xsltParameters) {
        xsltParameters.put("qtiWorksVersion", "1.0-SNAPSH0T-OO");
        xsltParameters.put("webappContextPath", "");
        xsltParameters.put("fullWebappContextPath", Settings.createServerURI());
        xsltParameters.put("olatWebappContextPath", Settings.getServerContextPathURI());
        StringOutput target = new StringOutput();
        StaticMediaDispatcher.renderStaticURI(target, "", true);
        xsltParameters.put("staticContextPath", target.toString());
    }

    //----------------------------------------------------

    /**
     * Invokes the transformation pipeline on the "main" assessment XML extracted from the
     * given renderingRequest, using the XSLT at the given URI and specified parameters. The result
     * is sent to the given {@link Result} Object.
     *
     * @param renderingRequest request to be rendered, must not be null
     * @param rendererStylesheetUri XSLT URI, must not be null
     * @param xsltParameters optional parameters
     * @param result {@link Result} to generate, which must not be null
     */
    private void doTransform(final AbstractRenderingRequest<?> renderingRequest, final URI rendererStylesheetUri,
            final Map<String, Object> xsltParameters, final Result result) {
        doTransform(renderingRequest, renderingRequest.getAssessmentResourceUri(), rendererStylesheetUri,
                xsltParameters, result);
    }

    /**
     * Invokes the transformation pipeline on the XML resource at the given inputUri, loaded using
     * the {@link ResourceLocator} specified by the given renderingRequest, using the XSLT at the
     * given URI and specified parameters. The result is sent to the given {@link Result} Object.
     * <p>
     *
     * @param renderingRequest request to be rendered, must not be null
     * @param inputUri URI of the XML to pass to the XSLT pipeline. If null, a well-formed empty
     *   document will be passed, which is useful if the output doesn't depend on the input XML
     *   at all, or when generating error pages.
     * @param rendererStylesheetUri XSLT URI, must not be null
     * @param xsltParameters optional parameters
     * @param result {@link Result} to generate, which must not be null.
     */
    private void doTransform(final AbstractRenderingRequest<?> renderingRequest, final URI inputUri,
            final URI rendererStylesheetUri, final Map<String, Object> xsltParameters, final Result result) {
        Assert.notNull(renderingRequest);
        Assert.notNull(rendererStylesheetUri);
        Assert.notNull(result);
        /* We do this as an XML pipeline:
         *
         * Input --> Rendering XSLT --> MathML C-to-P --> Serialization XSLT --> Result
         *
         * NB: I'm not bothering to set up LexicalHandlers, so comments and things like that won't
         * be passed through the pipeline. If that becomes important, change the code below to
         * support that.
         */
         /* First obtain the required compiled stylesheets. */
        final TransformerHandler rendererTransformerHandler = stylesheetManager.getCompiledStylesheetHandler(rendererStylesheetUri, renderingRequest.getAssessmentResourceLocator());
        final TransformerHandler mathmlTransformerHandler = stylesheetManager.getCompiledStylesheetHandler(ctopXsltUri, null);
        final TransformerHandler serializerTransformerHandler = stylesheetManager.getCompiledStylesheetHandler(serializeXsltUri, null);

        /* Pass necessary parameters to renderer */
        final Transformer rendererTransformer = rendererTransformerHandler.getTransformer();
        if (inputUri!=null) {
            rendererTransformer.setParameter("systemId", inputUri);
        }
        if (xsltParameters!=null) {
            for (final Entry<String, Object> paramEntry : xsltParameters.entrySet()) {
                rendererTransformer.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }

        /* Configure the serializer */
        final Transformer serializerTransformer = serializerTransformerHandler.getTransformer();
        final AbstractRenderingOptions renderingOptions = renderingRequest.getRenderingOptions();
        final SerializationMethod serializationMethod = renderingRequest.getRenderingOptions().getSerializationMethod();

        serializerTransformer.setParameter("serializationMethod", serializationMethod.toString());
        serializerTransformer.setParameter("outputMethod", serializationMethod.getMethod());
        serializerTransformer.setParameter("contentType", serializationMethod.getContentType());
        serializerTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializerTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializerTransformer.setOutputProperty(OutputKeys.MEDIA_TYPE, serializationMethod.getContentType());
        serializerTransformer.setOutputProperty(OutputKeys.METHOD, serializationMethod.getMethod());
        serializerTransformer.setOutputProperty("include-content-type", "no");

        /* If we're writing to an OutputStream, then select the encoding to use */
        final StreamResult streamResult = (result instanceof StreamResult) ? (StreamResult) result : null;
        final boolean isOutputStreamResult = streamResult!=null && streamResult.getOutputStream()!=null;
        final String outputStreamEncoding = renderingOptions.getEncoding()!=null ? renderingOptions.getEncoding() : "UTF-8";
        if (isOutputStreamResult) {
            serializerTransformer.setOutputProperty(OutputKeys.ENCODING, outputStreamEncoding);
        }

        /* If we're building HTML5, send its custom pseudo-DOCTYPE to the Result, as we can't generate this in XSLT. */
        /* No doc type, we produce only a fragment
        if (streamResult!=null && serializationMethod==SerializationMethod.HTML5_MATHJAX) {
            final String html5Doctype = "<!DOCTYPE html>\n";
            try {
                if (isOutputStreamResult) {
                    // Need to send doctype in correct encoding
                   streamResult.getOutputStream().write(html5Doctype.getBytes(outputStreamEncoding));
                }
                else if (streamResult.getWriter()!=null) {
                   streamResult.getWriter().write(html5Doctype);
                }
            }
            catch (final Exception e) {
                throw new QtiWorksRenderingException("Could not write HTML5 prolog to result", e);
            }
        }*/

        /* Set up the XML source */
        final InputSource assessmentSaxSource;
        if (inputUri!=null) {
            final ResourceLocator assessmentResourceLocator = renderingRequest.getAssessmentResourceLocator();
            final InputStream assessmentStream = assessmentResourceLocator.findResource(inputUri);
            assessmentSaxSource = new InputSource(assessmentStream);
            assessmentSaxSource.setSystemId(inputUri.toString());
        }
        else {
            /* (null inputUri, so we'll pass an empty well-formed XML document) */
            assessmentSaxSource = new InputSource(new StringReader("<null/>"));
        }


        /* Now join the pipeline together (it's clearest to work backwards here)
         *
         * NB: I'm not bothering to set up LexicalHandlers, so comments and things like that won't
         * be passed through the pipeline. If that becomes important, change the code below to
         * support that.
         */
        serializerTransformerHandler.setResult(result);
        final SAXResult mathmlResult = new SAXResult(serializerTransformerHandler);
        mathmlTransformerHandler.setResult(mathmlResult);
        final SAXResult rendererResult = new SAXResult(mathmlTransformerHandler);
        rendererTransformerHandler.setResult(rendererResult);
        final XMLReader xmlReader = XmlUtilities.createNsAwareSaxReader(false);
        xmlReader.setContentHandler(rendererTransformerHandler);

        /* Finally we run the pipeline */
        try {
            xmlReader.parse(assessmentSaxSource);
        }
        catch (final Exception e) {
            logger.error("Rendering XSLT pipeline failed for request {}", renderingRequest, e);
            throw new QtiWorksRenderingException("Unexpected Exception running rendering XML pipeline", e);
        }
    }
}
