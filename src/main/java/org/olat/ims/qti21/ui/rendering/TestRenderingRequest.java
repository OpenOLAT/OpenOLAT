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

import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * Request for rendering a particular test navigation screen.
 * <p>
 * The {@link TestRenderingMode} is used to determine what should be generated.
 *
 * @author David McKain
 */
public final class TestRenderingRequest extends AbstractRenderingRequest<TestRenderingOptions> {

    /** {@link TestSessionController} wrapped around the {@link TestSessionState} being rendered. */

    private TestSessionController testSessionController;

    private TestRenderingMode testRenderingMode;
    private TestPlanNodeKey modalItemKey;

    //----------------------------------------------------

    public TestSessionController getTestSessionController() {
        return testSessionController;
    }

    public void setTestSessionController(final TestSessionController testSessionController) {
        this.testSessionController = testSessionController;
    }


    public TestRenderingMode getTestRenderingMode() {
        return testRenderingMode;
    }

    public void setTestRenderingMode(final TestRenderingMode testRenderingMode) {
        this.testRenderingMode = testRenderingMode;
    }


    public TestPlanNodeKey getModalItemKey() {
        return modalItemKey;
    }

    public void setModalItemKey(final TestPlanNodeKey modalItemKey) {
        this.modalItemKey = modalItemKey;
    }
}
