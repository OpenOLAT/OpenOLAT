package org.olat.admin.layout;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;


public class LayoutModuleTest extends OlatTestCase {

    @Autowired
    private LayoutModule layoutModule;

    @Test
    public void getConfigProperty() {
        String expected = "my-logo.svg";
        layoutModule.setLogoFilename(expected);
        String provided = layoutModule.getConfigProperty("logo.filename");
        assertThat(provided).isEqualTo(expected);
    }

    @Test
    public void removeLogo() throws IOException {
        layoutModule.setLogoFilename("new-logo.svg");
        File logo = layoutModule.getLogo();
        File dir = layoutModule.getLogoDirectory();

        layoutModule.removeLogo();
        assertThat(FileUtils.directoryContains(dir, logo)).isFalse();
        assertThat(layoutModule.getLogoFilename()).isNullOrEmpty();
    }

    @Test
    public void getLogoUri() {
        assertThat(layoutModule.getLogoUri()).isEqualTo("/g/logo/oo-logo@1x.png");
    }

    @Test
    public void getLogoAlt() {
        String provided = "unittest alt";
        layoutModule.setLogoAlt(provided);
        assertThat(layoutModule.getLogoAlt()).isEqualTo(provided);
    }

    @Test
    public void getLogoLinkType() {
        String provided = "unittest link type";
        layoutModule.setLogoLinkType(provided);
        assertThat(layoutModule.getLogoLinkType()).isEqualTo(provided);
    }

    @Test
    public void getFooterLinkUri() {
        String provided = "unittest link uri";
        layoutModule.setFooterLinkUri(provided);
        assertThat(layoutModule.getFooterLinkUri()).isEqualTo(provided);
    }

    @Test
    public void getFooterLine() {
        String provided = "unittest footer line";
        layoutModule.setFooterLine(provided);
        assertThat(layoutModule.getFooterLine()).isEqualTo(provided);
    }

    @Test
    public void getLogoLinkUri() {
        String provided = "unittest link uri";
        layoutModule.setLogoLinkUri(provided);
        assertThat(layoutModule.getFooterLinkUri()).isEqualTo(provided);
    }


    @Test
    public void getLogo_empty() {
        layoutModule.setLogoFilename(" ");
        assertThat(layoutModule.getLogo()).isNull();
    }

}
