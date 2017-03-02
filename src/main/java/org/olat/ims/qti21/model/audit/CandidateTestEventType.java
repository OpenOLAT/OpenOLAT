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

import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * Encapsulates the test-specific events that can arise when delivering
 * a test within a {@link CandidateSession}
 *
 * @author David McKain
 */
public enum CandidateTestEventType {

    /* NB: Observe maximum length for mapped column set in CandidateEvent */
  //1234567890123456789012345678901234567890

    /**
     * Test has been entered.
     * <p>
     * (In test having one {@link TestPart}, we also attempt
     * to enter this automatically. Otherwise, the first {@link TestPart} has to be
     * entered explicitly by the candidate via {@link #ADVANCE_TEST_PART}.)
     */
    ENTER_TEST,

    /**
     * Presentation of navigation menu for the current {@link TestPart}, when in nonlinear
     * mode and while the {@link TestPart} is still interacting.
     */
    SELECT_MENU,

    /**
     * Ends the current {@link TestPart} and moves it into review state
     */
    END_TEST_PART,

    /**
     * Exits the current {@link TestPart} (if currently inside one), then advances to the next
     * available {@link TestPart}, or exits the test if there are no more available.
     */
    ADVANCE_TEST_PART,

    /**
     * Exits the test, either after the combined {@link TestPart} and test feedback page
     * (for single-part tests), or the standalone test feedback page for multi-part tests.
     */
    EXIT_TEST,
    
    /**
     * Exits the test because time limit 
     */
    EXIT_DUE_TIME_LIMIT,

    /** Selection of a particular item for interaction (in {@link NavigationMode#NONLINEAR}) */
    SELECT_ITEM,
    
    /** Selection of the next item for interaction */
    NEXT_ITEM,

    /**
     * Finish interaction of item (in {@link NavigationMode#LINEAR}), moving to the next
     * enterable item in the testPart.
     */
    FINISH_ITEM,

    /**
     * Finish interaction of item (in {@link NavigationMode#LINEAR}), with no further
     * enterable items in the testPart, thus causing the current testPart to end.
     */
    FINISH_FINAL_ITEM,

    /** Item Event within the currently-selected item */
    ITEM_EVENT,

    /** Return to Test Part review (while already in review state, i.e. after {@link #REVIEW_ITEM} */
    REVIEW_TEST_PART,

    /** Review of a particular item */
    REVIEW_ITEM,

    /** Solution of a particular item (in review state) */
    SOLUTION_ITEM,
    
    
    SUSPEND,

    ;

}
