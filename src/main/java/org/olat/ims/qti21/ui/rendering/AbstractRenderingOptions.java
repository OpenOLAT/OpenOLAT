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

import java.io.Serializable;

import uk.ac.ed.ph.jqtiplus.internal.util.ObjectUtilities;

/**
 * General base options for passing to the {@link AssessmentRenderer}.
 *
 * @author David McKain
 */
public abstract class AbstractRenderingOptions implements Serializable {

    private static final long serialVersionUID = 971871443108075384L;

    /** Required {@link SerializationMethod} */

    private SerializationMethod serializationMethod;

    /** Encoding to use (when sending the result to an OutputStream). UTF-8 will be used if not specified */
    private String encoding;

    private String responseUrl;

    private String serveFileUrl;

    private String authorViewUrl;

    private String sourceUrl;

    private String stateUrl;

    private String resultUrl;

    private String validationUrl;

    //----------------------------------------------------

    public SerializationMethod getSerializationMethod() {
        return serializationMethod;
    }

    public void setSerializationMethod(final SerializationMethod serializationMethod) {
        this.serializationMethod = serializationMethod;
    }


    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }


    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(final String responseUrl) {
        this.responseUrl = responseUrl;
    }


    public String getServeFileUrl() {
        return serveFileUrl;
    }

    public void setServeFileUrl(final String serveFileUrl) {
        this.serveFileUrl = serveFileUrl;
    }


    public String getAuthorViewUrl() {
        return authorViewUrl;
    }

    public void setAuthorViewUrl(final String authorViewUrl) {
        this.authorViewUrl = authorViewUrl;
    }


    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(final String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }


    public String getStateUrl() {
        return stateUrl;
    }

    public void setStateUrl(final String stateUrl) {
        this.stateUrl = stateUrl;
    }


    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(final String resultUrl) {
        this.resultUrl = resultUrl;
    }


    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(final String validationUrl) {
        this.validationUrl = validationUrl;
    }

    //----------------------------------------------------

    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
