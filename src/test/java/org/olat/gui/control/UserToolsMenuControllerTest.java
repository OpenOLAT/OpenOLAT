package org.olat.gui.control;

import ch.uzh.lms.listener.LogoutListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.test.OlatTestCase;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.mockito.Mockito.mock;


public class UserToolsMenuControllerTest extends OlatTestCase {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void logout_listener() {
        UserRequest req = new SyntheticUserRequest(null, Locale.ENGLISH);
        WindowControl wControl = mock(WindowControl.class);

        UserToolsMenuController utmc = new UserToolsMenuController(req, wControl);

        Event ev = new Event("logout");
        SimpleStackedPanel ssp = (SimpleStackedPanel) utmc.getInitialComponent();

        // we set the request to null here on purpose. In this case the AuthHelper.doLogout
        // returns immediately and we can test the logout listener
        utmc.dispatchEvent(null,  ssp.getContent(), ev);
        Assert.assertEquals("hello listener", outContent.toString());
    }
}
