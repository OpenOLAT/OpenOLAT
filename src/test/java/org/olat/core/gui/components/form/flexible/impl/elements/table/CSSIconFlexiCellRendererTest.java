package org.olat.core.gui.components.form.flexible.impl.elements.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.olat.test.OlatTestCase;

public class CSSIconFlexiCellRendererTest extends OlatTestCase {

    CSSIconFlexiCellRenderer renderer;

    @Before
    public void setUp() {
       renderer = new CSSIconFlexiCellRenderer("foobar");
    }

    @Test
    public void test_cssclass() {
        assertThat(renderer.getCssClass(null)).isEqualTo("foobar");
    }

    @Test
    public void render() {
        //
    }
}