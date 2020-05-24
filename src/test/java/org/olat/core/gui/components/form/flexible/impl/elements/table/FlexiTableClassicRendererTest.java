package org.olat.core.gui.components.form.flexible.impl.elements.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.login.AboutController;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.ui.CoursesTableDataModel;
import org.olat.test.KeyTranslator;
import org.olat.test.OlatTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.olat.core.util.WebappHelper.getInstanceId;

public class FlexiTableClassicRendererTest extends OlatTestCase {

    FlexiTableClassicRenderer ftcr;
    StringOutput sb;
    String csrfToken = "28797b99-b63d-41ea-8750-2607a9fe2e52";
    Translator translator = new KeyTranslator(Locale.ENGLISH);
    Panel panel;
    URLBuilder ubu = new URLBuilder("/", getInstanceId(), "123", csrfToken);
    RenderResult renderResult;
    GlobalSettings gsettings = new DefaultGlobalSettings();
    WindowControl wControl;

    @Before
    public void setup() {
        sb = new StringOutput();
        renderResult = new RenderResult();
        wControl = new WindowControlMocker();
        ftcr = new FlexiTableClassicRenderer();
        panel = new Panel("unittest-panel-classic");
    }

    @Test
    public void basic_render() {
        // these are the table components
        FlexiTableColumnModelImpl ftcmi = new FlexiTableColumnModelImpl();
        CoursesTableDataModel tableModel = new CoursesTableDataModel(ftcmi);
        List<CourseStatEntry> rows = new ArrayList<>();
        rows.add(new CourseStatEntry());

        tableModel.setObjects(rows);
        FlexiTableElementImpl fte = new FlexiTableElementImpl(wControl, "foobar", translator, tableModel);
        FlexiTableComponent source = new FlexiTableComponent(fte);

        // source is disabled here, since the AbstractFlexiTableRenderer kicks in
        // FormJSHelper.appendFlexiFormDirty, which makes the unittest even more complicated
        source.setEnabled(false);

        // finally get the renderer and make sure the rendering of the table works
        Renderer renderer = Renderer.getInstance(panel, translator, ubu, renderResult, gsettings, csrfToken);
        ftcr.render(renderer, sb, source, ubu, translator, renderResult, null);

        assertThat(sb.toString()).contains("<table id=\"o_fi");
    }

    @Test
    public void render_batching() {
        // these are the table components
        FlexiTableColumnModelImpl ftcmi = new FlexiTableColumnModelImpl();
        CoursesTableDataModel tableModel = new CoursesTableDataModel(ftcmi);
        List<CourseStatEntry> rows = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            CourseStatEntry entry = new CourseStatEntry();
            entry.setRepoDisplayName("Course " + i);
            rows.add(entry);
        }
        tableModel.setObjects(rows);
        FormLayoutContainer flc = FormLayoutContainer.createPanelFormLayout("ffo_panel", translator);
        SyntheticUserRequest ureq = new SyntheticUserRequest(null, Locale.ENGLISH);
        // use just a simple controller. it is not really used but necessary to initialize the form.
        Controller controller = new AboutController(ureq, wControl);
        Form mainForm = Form.create("mainFormId", "ffo_main_panel", flc, controller);
        FlexiTableElementImpl fte = new FlexiTableElementImpl(wControl, "foobar", translator, tableModel, 10, true);
        fte.setPageSize(10);
        fte.setRootForm(mainForm);
        fte.setPage(3);
        FlexiTableComponent source = new FlexiTableComponent(fte);

        // source is disabled here, since the AbstractFlexiTableRenderer kicks in
        // FormJSHelper.appendFlexiFormDirty, which make things even more complicated
        // source.setEnabled(false);

        // finally get the renderer and make sure the rendering of the table works
        Renderer renderer = Renderer.getInstance(panel, translator, ubu, renderResult, gsettings, csrfToken);
        ftcr.render(renderer, sb, source, ubu, translator, renderResult, null);

        String result = sb.toString();
        assertThat(result).contains("<div class='col-sm-2 col-xs-4 o_table_row_count'>page.size.c: 31, 32, 32</div>");
        assertThat(result).contains("<li  class='disabled'><a href=\"#\">&raquo;</a></li>");
    }

}
