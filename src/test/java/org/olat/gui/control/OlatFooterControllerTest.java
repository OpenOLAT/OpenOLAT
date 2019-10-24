package org.olat.gui.control;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.WindowManagerImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.test.OlatTestCase;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OlatFooterControllerTest extends OlatTestCase {

    private UserRequest ureq;
    private WindowControl wControl;
    private ModuleConfiguration moduleConfiguration;
    
    @Before
    public void setUp() throws Exception {
        /* mock user request */
        ureq = mock(UserRequest.class);
        when(ureq.getLocale()).thenReturn(new Locale("de_DE"));
        /* mock window manager */
        wControl = mock(WindowControl.class);
        WindowManager windowManager = new WindowManagerImpl();
        when(wControl.getWindowBackOffice()).thenReturn(windowManager.createWindowBackOffice("windowName", null, new WindowSettings()));
        /* mock module configuration
        moduleConfiguration = mock(ModuleConfiguration.class);
        // mock setter
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String key = (String) invocation.getArguments()[0];
                String value = (String) invocation.getArguments()[1];
                moduleConfigurationMap.put(key, value);
                return null;
            }
        }).when(moduleConfiguration).setStringValue(anyString(), anyString());
        // mock getter
        when(moduleConfiguration.getStringValue(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String key = (String) invocation.getArguments()[0];
                return moduleConfigurationMap.get(key);
            }
        }); */
    }


    @Test
    public void doOpenImpressum() {
        OlatFooterController footer = new OlatFooterController(ureq, wControl);
        footer.doOpenImpressum(ureq);

    }
}
