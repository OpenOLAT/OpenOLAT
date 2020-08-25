package org.olat.core.commons.controllers.impressum;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;


public class EmptyImpressumExtensionTest extends OlatTestCase {

    @Autowired
    ImpressumModule impressumModule;

    @Autowired
    EmptyImpressumExtension emptyImpressumExtension;

    Path indexEnHtml;
    boolean currentImpressumEnabled;
    Path customizingImpressum;

    @Before
    public void setUp() throws IOException {
        currentImpressumEnabled = impressumModule.isEnabled();
        impressumModule.setEnabled(true);
        customizingImpressum = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "impressum");
        customizingImpressum.toFile().mkdirs();
        indexEnHtml = customizingImpressum.resolve("index_de.html");
        Path file = Files.createFile(indexEnHtml);
        String content = "<div>Hallo Impressum</div>";
        Files.write( file, content.getBytes());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirsAndFiles(customizingImpressum);
        impressumModule.setEnabled(currentImpressumEnabled);
    }

    @Test
    public void getExtensionFor() {
        SyntheticUserRequest ureq = new SyntheticUserRequest(null, Locale.ENGLISH);
        ExtensionElement extElem = emptyImpressumExtension.getExtensionFor(
                "org.olat.core.commons.controllers.impressum.ImpressumMainController", ureq);
        assertThat(extElem).isNull();
    }

}