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

import java.net.URI;

import uk.ac.ed.ph.jqtiplus.internal.util.ObjectUtilities;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Base for {@link ItemRenderingRequest} and {@link TestRenderingRequest} containing things
 * that are required in both cases.
 *
 * @param <P> specific subtype of {@link AbstractRenderingOptions} attached to this request.
 *
 * @author David McKain
 */
public abstract class AbstractRenderingRequest<P extends AbstractRenderingOptions> {

    private P renderingOptions;

    private ResourceLocator assessmentResourceLocator;

    private URI assessmentResourceUri;

    private boolean authorMode;

    /* Validation information copied from AssessmentPackage */
    private boolean validated;
    private boolean launchable;
    private int errorCount;
    private int warningCount;
    private boolean valid;

    //----------------------------------------------------

    public P getRenderingOptions() {
        return renderingOptions;
    }

    public void setRenderingOptions(final P renderingOptions) {
        this.renderingOptions = renderingOptions;
    }


    public ResourceLocator getAssessmentResourceLocator() {
        return assessmentResourceLocator;
    }

    public void setAssessmentResourceLocator(final ResourceLocator assessmentResourceLocator) {
        this.assessmentResourceLocator = assessmentResourceLocator;
    }


    public URI getAssessmentResourceUri() {
        return assessmentResourceUri;
    }

    public void setAssessmentResourceUri(final URI assessmentResourceUri) {
        this.assessmentResourceUri = assessmentResourceUri;
    }


    public boolean isValidated() {
        return validated;
    }

    public void setValidated(final boolean validated) {
        this.validated = validated;
    }


    public boolean isLaunchable() {
        return launchable;
    }

    public void setLaunchable(final boolean launchable) {
        this.launchable = launchable;
    }


    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(final int errorCount) {
        this.errorCount = errorCount;
    }


    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(final int warningCount) {
        this.warningCount = warningCount;
    }


    public boolean isValid() {
        return valid;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }


    public boolean isAuthorMode() {
        return authorMode;
    }

    public void setAuthorMode(final boolean authorMode) {
        this.authorMode = authorMode;
    }

    //----------------------------------------------------

    @Override
    public final String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
