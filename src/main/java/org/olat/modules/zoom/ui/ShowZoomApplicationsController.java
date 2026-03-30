/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.zoom.ui;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.lti13.LTI13Context;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.manager.ZoomProfileDAO;
import org.olat.modules.zoom.ui.ZoomApplicationsTableModel.ZoomApplicationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Detail view controller showing the applications associated with a Zoom LTI Pro
 * profile. Displays a FlexiTable with LTI context ID, application type, and a
 * clickable application name that navigates to the course element, course tool,
 * or group tool. The application display name is built lazily during rendering to
 * avoid loading all course structures at model-load time.
 *
 * Initial date: 2022-08-11<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ShowZoomApplicationsController extends FormBasicController {

    private static final Logger log = Tracing.createLoggerFor(ShowZoomApplicationsController.class);

    private FlexiTableElement applicationsTableEl;
    private ZoomApplicationsTableModel applicationsTableModel;

    private final ZoomProfile zoomProfile;

    @Autowired
    private ZoomManager zoomManager;

    public ShowZoomApplicationsController(UserRequest ureq, WindowControl wControl, ZoomProfile zoomProfile) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        this.zoomProfile = zoomProfile;
        initForm(ureq);
        loadModel();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomApplicationCols.ltiContextId));

        DefaultFlexiColumnModel typeCol = new DefaultFlexiColumnModel(ZoomApplicationCols.applicationType);
        typeCol.setCellRenderer(new ZoomApplicationTypeRenderer());
        columnsModel.addFlexiColumnModel(typeCol);

        FlexiCellRenderer applicationDisplayRenderer = new StaticFlexiCellRenderer("navigate", new ZoomApplicationDisplayRenderer());
        DefaultFlexiColumnModel applicationCol = new DefaultFlexiColumnModel(
                ZoomApplicationCols.application.i18nHeaderKey(),
                ZoomApplicationCols.application.ordinal(),
                "navigate",
                true,
                ZoomApplicationCols.application.name(),
                applicationDisplayRenderer
        );
        applicationCol.setSortKey(ZoomApplicationCols.application.sortKey());
        applicationCol.setSortable(true);
        columnsModel.addFlexiColumnModel(applicationCol);

        applicationsTableModel = new ZoomApplicationsTableModel(columnsModel, getLocale());
        applicationsTableEl = uifactory.addTableElement(getWindowControl(), "applications", applicationsTableModel, 10, false, getTranslator(), formLayout);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == applicationsTableEl && event instanceof SelectionEvent se && "navigate".equals(se.getCommand())) {
            ZoomApplicationRow row = applicationsTableModel.getObject(se.getIndex());
            if (StringHelper.containsNonWhitespace(row.getBusinessPath())) {
                fireEvent(ureq, new OpenBusinessPathEvent(row.getBusinessPath()));
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    private void loadModel() {
        List<ZoomApplicationRow> rows = zoomManager.getProfileApplications(zoomProfile.getKey()).stream()
                .filter(app -> app.getApplicationType() != null)
                .filter(this::isValid)
                .map(this::toRow)
                .collect(toList());
        applicationsTableModel.setObjects(rows);
        applicationsTableEl.reset(true, true, true);
    }

    private boolean isValid(ZoomProfileDAO.ZoomProfileApplication application) {
        LTI13Context ltiContext = application.getLti13Context();
        try {
            switch (application.getApplicationType()) {
                case courseElement:
                    ICourse course = CourseFactory.loadCourse(ltiContext.getEntry());
                    return course.getRunStructure().getNode(ltiContext.getSubIdent()) != null;
                case courseTool:
                    CourseFactory.loadCourse(ltiContext.getEntry());
                    return true;
                case groupTool:
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            log.warn("Zoom application is no longer valid: {}", e.getMessage());
            return false;
        }
    }

    private ZoomApplicationRow toRow(ZoomProfileDAO.ZoomProfileApplication application) {
        return new ZoomApplicationRow(application, buildBusinessPath(application));
    }

    private String buildBusinessPath(ZoomProfileDAO.ZoomProfileApplication application) {
        LTI13Context ltiContext = application.getLti13Context();
        ZoomManager.ApplicationType type = application.getApplicationType();
        switch (type) {
            case courseElement:
                return "[RepositoryEntry:" + ltiContext.getEntry().getKey() +
                       "][CourseNode:" + ltiContext.getSubIdent() + "]";
            case courseTool:
                return "[RepositoryEntry:" + ltiContext.getEntry().getKey() + "][zoom:0]";
            case groupTool:
                return "[BusinessGroup:" + ltiContext.getBusinessGroup().getKey() + "][toolzoom:0]";
            default:
                return null;
        }
    }

    private static String buildDisplayText(ZoomProfileDAO.ZoomProfileApplication application, Translator translator) {
        LTI13Context ltiContext = application.getLti13Context();
        ZoomManager.ApplicationType type = application.getApplicationType();
        if (type == null) {
            return null;
        }
        try {
            switch (type) {
                case courseElement:
                    ICourse course = CourseFactory.loadCourse(ltiContext.getEntry());
                    CourseNode courseNode = course.getRunStructure().getNode(ltiContext.getSubIdent());
                    if (courseNode == null) {
                        return null;
                    }
                    return translator.translate("zoom.profile.application.courseElement",
                            StringHelper.xssScan(courseNode.getShortName()),
                            StringHelper.xssScan(course.getCourseTitle()));
                case courseTool:
                    ICourse c = CourseFactory.loadCourse(ltiContext.getEntry());
                    return translator.translate("zoom.profile.application.courseTool",
                            StringHelper.xssScan(c.getCourseTitle()));
                case groupTool:
                    return translator.translate("zoom.profile.application.groupTool",
                            StringHelper.xssScan(ltiContext.getBusinessGroup().getName()));
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Failed to build display text for Zoom application: {}", e.getMessage());
            return null;
        }
    }

    public static class OpenBusinessPathEvent extends Event {
        private final String businessPath;

        public OpenBusinessPathEvent(String businessPath) {
            super("openBusinessPath");
            this.businessPath = businessPath;
        }

        public String getBusinessPath() {
            return businessPath;
        }
    }

    private static class ZoomApplicationTypeRenderer implements FlexiCellRenderer {
        @Override
        public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
                FlexiTableComponent source, URLBuilder ubu, Translator translator) {
            if (cellValue instanceof ZoomManager.ApplicationType type) {
                target.append(StringHelper.escapeHtml(translator.translate("zoom.application.type." + type.name())));
            }
        }
    }

    private static class ZoomApplicationDisplayRenderer implements FlexiCellRenderer {
        @Override
        public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
                FlexiTableComponent source, URLBuilder ubu, Translator translator) {
            if (cellValue instanceof ZoomApplicationRow applicationRow) {
                String displayText = buildDisplayText(applicationRow.getApplication(), translator);
                if (StringHelper.containsNonWhitespace(displayText)) {
                    target.append(StringHelper.escapeHtml(displayText));
                }
            }
        }
    }
}