package org.olat.portfolio;

import org.junit.Test;
import org.olat.core.logging.activity.ActionObject;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.CrudAction;

import static org.junit.Assert.*;

public class EPLoggingActionTest {

    @Test
    public void testLog() {
        EPLoggingAction logAction = new EPLoggingAction(
                ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.portfolioartefact);
        assertNotNull(logAction);
    }

}