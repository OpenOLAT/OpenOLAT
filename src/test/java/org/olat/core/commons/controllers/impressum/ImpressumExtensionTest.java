package org.olat.core.commons.controllers.impressum;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
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


public class ImpressumExtensionTest extends OlatTestCase {

    @Autowired
    ImpressumModule impressumModule;

    @Autowired
    ImpressumExtension impressumExtension;

    boolean currentImpressumEnabled;
    Path customizingImpressum;

    @Before
    public void setUp() throws IOException {
        currentImpressumEnabled = impressumModule.isEnabled();
        impressumModule.setEnabled(true);
        customizingImpressum = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "impressum");
        customizingImpressum.toFile().mkdirs();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirsAndFiles(customizingImpressum);
        impressumModule.setEnabled(currentImpressumEnabled);
    }

    @Test
    public void getExtensionFor() throws IOException {
        Path indexHtml = createImpressumFile("en", "<div>Hello Impressum</div>");
        SyntheticUserRequest ureq = new SyntheticUserRequest(null, Locale.ENGLISH);
        ExtensionElement extElem = impressumExtension.getExtensionFor(
                "org.olat.core.commons.controllers.impressum.ImpressumMainController", ureq);
        assertThat(extElem).isInstanceOf(ImpressumExtension.class);
        Files.deleteIfExists(indexHtml);
    }

    @Test
    public void getExtensionFor_NotEven_A_File() throws IOException {
        Path indexEnHtml = customizingImpressum.resolve("index_en.html");
        indexEnHtml.toFile().mkdirs();

        SyntheticUserRequest ureq = new SyntheticUserRequest(null, Locale.ENGLISH);
        ExtensionElement extElem = impressumExtension.getExtensionFor(
                "org.olat.core.commons.controllers.impressum.ImpressumMainController", ureq);
        assertThat(extElem).isNull();
        Files.deleteIfExists(indexEnHtml);
    }

    @Test
    public void getExtensionFor_Malicious() throws IOException {
        Path indexHtml = createImpressumFile("de", "<script>alert('bad!');</script>");
        SyntheticUserRequest ureq = new SyntheticUserRequest(null, Locale.ENGLISH);
        ExtensionElement extElem = impressumExtension.getExtensionFor(
                "org.olat.core.commons.controllers.impressum.ImpressumMainController", ureq);
        assertThat(extElem).isNull();
        Files.deleteIfExists(indexHtml);
    }
    
    private Path createImpressumFile(String langCode, String content) throws IOException {
        String filename = String.format("index_%s.html", langCode);
        Path indexHtml = customizingImpressum.resolve(filename);
        Path file = Files.createFile(indexHtml);
        Files.write(file, content.getBytes());
        return indexHtml;
    }
}