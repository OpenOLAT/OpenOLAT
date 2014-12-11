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

import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * Encapsulates the required data for rendering the current state of a standalone
 * item.
 *
 * @author David McKain
 */
public final class ItemRenderingRequest extends AbstractRenderingRequest<ItemRenderingOptions> {

    /** Required {@link ItemSessionState} to be rendered */

    private ItemSessionState itemSessionState;

    /** Set to enable the modal solution mode */
    private boolean solutionMode;

    private String prompt;
    private boolean endAllowed;
    private boolean softSoftResetAllowed;
    private boolean hardResetAllowed;
    private boolean solutionAllowed;
    private boolean candidateCommentAllowed;

    //----------------------------------------------------

    public ItemSessionState getItemSessionState() {
        return itemSessionState;
    }

    public void setItemSessionState(final ItemSessionState itemSessionState) {
        this.itemSessionState = itemSessionState;
    }


    public boolean isSolutionMode() {
        return solutionMode;
    }

    public void setSolutionMode(final boolean solutionMode) {
        this.solutionMode = solutionMode;
    }


    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }


    public boolean isEndAllowed() {
        return endAllowed;
    }

    public void setEndAllowed(final boolean endAllowed) {
        this.endAllowed = endAllowed;
    }


    public boolean isSoftResetAllowed() {
        return softSoftResetAllowed;
    }

    public void setSoftResetAllowed(final boolean softSoftResetAllowed) {
        this.softSoftResetAllowed = softSoftResetAllowed;
    }


    public boolean isHardResetAllowed() {
        return hardResetAllowed;
    }

    public void setHardResetAllowed(final boolean hardResetAllowed) {
        this.hardResetAllowed = hardResetAllowed;
    }


    public boolean isSolutionAllowed() {
        return solutionAllowed;
    }

    public void setSolutionAllowed(final boolean solutionAllowed) {
        this.solutionAllowed = solutionAllowed;
    }


    public boolean isCandidateCommentAllowed() {
        return candidateCommentAllowed;
    }

    public void setCandidateCommentAllowed(final boolean candidateCommentAllowed) {
        this.candidateCommentAllowed = candidateCommentAllowed;
    }
}
