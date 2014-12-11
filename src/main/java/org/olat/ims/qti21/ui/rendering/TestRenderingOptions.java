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

import uk.ac.ed.ph.jqtiplus.internal.util.ObjectUtilities;

/**
 * Rendering options used when rendering tests.
 *
 * @author David McKain
 */
public final class TestRenderingOptions extends AbstractRenderingOptions {

    private static final long serialVersionUID = -1121298920996208870L;

    private String testPartNavigationUrl;

    private String selectTestItemUrl;

    private String advanceTestItemUrl;

    private String endTestPartUrl;

    private String reviewTestPartUrl;

    private String reviewTestItemUrl;

    private String showTestItemSolutionUrl;

    private String advanceTestPartUrl;

    private String exitTestUrl;

    //----------------------------------------------------

    public String getSelectTestItemUrl() {
        return selectTestItemUrl;
    }

    public void setSelectTestItemUrl(final String selectTestItemUrl) {
        this.selectTestItemUrl = selectTestItemUrl;
    }


    public String getAdvanceTestItemUrl() {
        return advanceTestItemUrl;
    }

    public void setAdvanceTestItemUrl(final String advanceTestItemUrl) {
        this.advanceTestItemUrl = advanceTestItemUrl;
    }


    public String getEndTestPartUrl() {
        return endTestPartUrl;
    }

    public void setEndTestPartUrl(final String endTestPartUrl) {
        this.endTestPartUrl = endTestPartUrl;
    }


    public String getReviewTestPartUrl() {
        return reviewTestPartUrl;
    }

    public void setReviewTestPartUrl(final String reviewTestPartUrl) {
        this.reviewTestPartUrl = reviewTestPartUrl;
    }


    public String getReviewTestItemUrl() {
        return reviewTestItemUrl;
    }

    public void setReviewTestItemUrl(final String reviewTestItemUrl) {
        this.reviewTestItemUrl = reviewTestItemUrl;
    }


    public String getShowTestItemSolutionUrl() {
        return showTestItemSolutionUrl;
    }

    public void setShowTestItemSolutionUrl(final String showTestItemSolutionUrl) {
        this.showTestItemSolutionUrl = showTestItemSolutionUrl;
    }


    public String getTestPartNavigationUrl() {
        return testPartNavigationUrl;
    }

    public void setTestPartNavigationUrl(final String testPartNavigationUrl) {
        this.testPartNavigationUrl = testPartNavigationUrl;
    }


    public String getAdvanceTestPartUrl() {
        return advanceTestPartUrl;
    }

    public void setAdvanceTestPartUrl(final String advanceTestPartUrl) {
        this.advanceTestPartUrl = advanceTestPartUrl;
    }


    public String getExitTestUrl() {
        return exitTestUrl;
    }

    public void setExitTestUrl(final String exitTestUrl) {
        this.exitTestUrl = exitTestUrl;
    }

    //----------------------------------------------------

    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
