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
package org.olat.ims.qti21.model.audit;

/**
 * Enumerates the various errors conditions that can arise as a result of calls into the
 * candidate service layer.
 * <p>
 * These are logged via {@link CandidateAuditLogger}
 *
 * @see CandidateException
 * @see CandidateAuditLogger
 *
 * @author David McKain
 */
public enum CandidateExceptionReason {

    //----------------------------------------------------
    // Session start errors

    /**
     * Candidate's user account is disabled
     */
    USER_ACCOUNT_DISABLED,

    /**
     * Attempted to launch a {@link Delivery} that is not linked to an {@link Assessment}.
     */
    LAUNCH_INCOMPLETE_DELIVERY,

    /**
     * Attempted to launch a {@link Delivery} which is not currently open.
     */
    LAUNCH_CLOSED_DELIVERY,

    /**
     * Attempted to launch a non-sample {@link Assessment} in demo mode
     */
    LAUNCH_ASSESSMENT_AS_SAMPLE,

    /**
     * Attempted to launch an {@link Assessment} that the current user does
     * not have access to.
     */
    LAUNCH_ASSESSMENT_NO_ACCESS,

    //----------------------------------------------------
    // General session errors

    /**
     * Caller attempted API call that requires the {@link CandidateSession} to have been
     * fully entered.
     */
    SESSION_NOT_ENTERED,

    /**
     * Caller attempted API call that requires the {@link CandidateSession} to not have been
     * terminated.
     */
    SESSION_IS_TERMINATED,

    /**
     * Caller made API call for a {@link CandidateSession} started on an item when in fact the session is for a test,
     * or vice versa
     */
    SESSION_WRONG_TYPE,

    /**
     * Caller attempted to rendering authoring information on a {@link CandidateSession} on which
     * this is not allowed
     */
    AUTHOR_INFO_FORBIDDEN,

    /**
     * Caller attempted to access a file in the {@link AssessmentPackage} that hasn't been
     * whitelisted
     */
    ACCESS_BLACKLISTED_ASSESSMENT_FILE,

    //----------------------------------------------------
    // Item-specific errors

    /**
     * Caller attempted to submit responses at an inappropriate time
     */
    RESPONSES_NOT_EXPECTED,

    /**
     * Caller attempted to submit a candidate comment, but the {@link ItemDeliverySettings}
     * do not allow this
     */
    CANDIDATE_COMMENT_FORBIDDEN,

    /**
     * Caller attempted to end the item session while in interacting state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    END_SESSION_WHEN_INTERACTING_FORBIDDEN,

    /**
     * Caller attempted to end the item session after it has already
     * been ended
     */
    END_SESSION_WHEN_ALREADY_ENDED,

    /**
     * Caller attempted a soft reset while in interacting state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    SOFT_RESET_SESSION_WHEN_INTERACTING_FORBIDDEN,

    /**
     * Caller attempted a soft reset while in ended state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    SOFT_RESET_SESSION_WHEN_ENDED_FORBIDDEN,

    /**
     * Caller attempted a hard reset while in interacting state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    HARD_RESET_SESSION_WHEN_INTERACTING_FORBIDDEN,

    /**
     * Caller attempted a hard reset while in ended state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    HARD_RESET_SESSION_WHEN_ENDED_FORBIDDEN,

    /**
     * Caller requested the solution while in interacting state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    SOLUTION_WHEN_INTERACTING_FORBIDDEN,

    /**
     * Caller requested the solution while in ended state,
     * but this is not allowed by the {@link ItemDeliverySettings}
     */
    SOLUTION_WHEN_ENDED_FORBIDDEN,

    //----------------------------------------------------
    // Test-specific errors

    /**
     * Caller has attempted to select the (nonlinear mode) test question
     * menu, but this is not possible.
     */
    CANNOT_SELECT_NONLINEAR_MENU,

    /**
     * Caller has attempted to select a test question (in nonlinear mode),
     * but this is not possible.
     */
    CANNOT_SELECT_NONLINEAR_TEST_ITEM,

    /**
     * Caller has attempted to finish the current (linear mode) question,
     * but this is not currently possible.
     */
    CANNOT_FINISH_LINEAR_TEST_ITEM,

    /**
     * Caller has attempted to end the {@link TestPart}, but this is not
     * currently possible.
     */
    CANNOT_END_TEST_PART,

    /**
     * Caller has attempted to review the current {@link TestPart}, but
     * this is not currently possible.
     */
    CANNOT_REVIEW_TEST_PART,

    /**
     * Caller has attempted to review an item within the current {@link TestPart},
     * but this is not currently possible.
     */
    CANNOT_REVIEW_TEST_ITEM,

    /**
     * Caller has attempted to see the solution of an item within the current {@link TestPart},
     * but this is not currently possible.
     */
    CANNOT_SOLUTION_TEST_ITEM,

    /**
     * Caller has attempted to advance the current {@link TestPart}, but this is not
     * currently possible.
     */
    CANNOT_ADVANCE_TEST_PART,

    /**
     * Caller has attempted to exit the {@link AssessmentTest}, but this is not currently
     * possible.
     */
    CANNOT_EXIT_TEST,

    ;

}
