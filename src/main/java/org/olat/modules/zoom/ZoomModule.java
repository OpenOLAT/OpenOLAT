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
package org.olat.modules.zoom;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
@Service
public class ZoomModule extends AbstractSpringModule implements ConfigOnOff {

    private static final String ZOOM_ENABLED = "zoom.enabled";
    private static final String ZOOM_ENABLED_FOR_COURSE_ELEMENT = "zoom.enabledForCourseElement";
    private static final String ZOOM_ENABLED_FOR_COURSE_TOOL = "zoom.enabledForCourseTool";
    private static final String ZOOM_ENABLED_FOR_GROUP_TOOL = "zoom.enabledForGroupTool";
    private static final String ZOOM_CALENDAR_ENTRIES_ENABLED = "zoom.calendarEntriesEnabled";

    @Value("${zoom.enabled:false}")
    private boolean enabled;
    
    @Value("${zoom.enabledForCourseElement:true}")
    private String enabledForCourseElement;

    @Value("${zoom.enabledForCourseTool:true}")
    private String enabledForCourseTool;

    @Value("${zoom.enabledForGroupTool:true}")
    private String enabledForGroupTool;

    @Value("${zoom.calendarEntriesEnabled:true}")
    private String calendarEntriesEnabled;

    @Autowired
    public ZoomModule(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override
    public void init() {
        String enabledObj = getStringPropertyValue(ZOOM_ENABLED, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            enabled = "true".equals(enabledObj);
        }

        String enabledForCourseElementObj = getStringPropertyValue(ZOOM_ENABLED_FOR_COURSE_ELEMENT, true);
        if (StringHelper.containsNonWhitespace(enabledForCourseElementObj)) {
            enabledForCourseElement = enabledForCourseElementObj;
        }

        String enabledForCourseToolObj = getStringPropertyValue(ZOOM_ENABLED_FOR_COURSE_TOOL, true);
        if (StringHelper.containsNonWhitespace(enabledForCourseToolObj)) {
            enabledForCourseTool = enabledForCourseToolObj;
        }

        String enabledForGroupToolObj = getStringPropertyValue(ZOOM_ENABLED_FOR_GROUP_TOOL, true);
        if (StringHelper.containsNonWhitespace(enabledForGroupToolObj)) {
            enabledForGroupTool = enabledForGroupToolObj;
        }

        String calendarEntriesEnabledObj = getStringPropertyValue(ZOOM_CALENDAR_ENTRIES_ENABLED, true);
        if (StringHelper.containsNonWhitespace(calendarEntriesEnabledObj)) {
            calendarEntriesEnabled = calendarEntriesEnabledObj;
        }
    }

    @Override
    protected void initFromChangedProperties() {
        init();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setStringProperty(ZOOM_ENABLED, Boolean.toString(enabled), true);
    }

    public boolean isEnabledForCourseElement() {
    	return "true".equals(enabledForCourseElement);
    }
    
    public void setEnabledForCourseElement(boolean enabled) {
    	enabledForCourseElement = enabled ? "true" : "false";
    	setStringProperty(ZOOM_ENABLED_FOR_COURSE_ELEMENT, enabledForCourseElement, true);
    }

    public boolean isEnabledForCourseTool() {
        return "true".equals(enabledForCourseTool);
    }

    public void setEnabledForCourseTool(boolean enabled) {
        enabledForCourseTool = enabled ? "true" : "false";
        setStringProperty(ZOOM_ENABLED_FOR_COURSE_TOOL, enabledForCourseTool, true);
    }

    public boolean isEnabledForGroupTool() {
    	return "true".equals(enabledForGroupTool);
    }
    
    public void setEnabledForGroupTool(boolean enabled) {
    	enabledForGroupTool = enabled ? "true" : "false";
    	setStringProperty(ZOOM_ENABLED_FOR_GROUP_TOOL, enabledForGroupTool, true);
    }

    public boolean isCalendarEntriesEnabled() {
        return "true".equals(calendarEntriesEnabled);
    }

    public void setCalendarEntriesEnabled(boolean enabled) {
        calendarEntriesEnabled = enabled ? "true" : "false";
        setStringProperty(ZOOM_CALENDAR_ENTRIES_ENABLED, calendarEntriesEnabled, true);
    }
}
