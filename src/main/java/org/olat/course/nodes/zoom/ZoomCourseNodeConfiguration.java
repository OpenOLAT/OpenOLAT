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
package org.olat.course.nodes.zoom;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.*;
import org.olat.modules.zoom.ZoomModule;

import java.util.Locale;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomCourseNodeConfiguration extends AbstractCourseNodeConfiguration {

    private final String alias;

    public ZoomCourseNodeConfiguration() {
        this("zoom");
    }

    public ZoomCourseNodeConfiguration(String alias) {
        this.alias = alias;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getGroup() {
        return CourseNodeGroup.collaboration.name();
    }

    @Override
    public String getIconCSSClass() {
        return "o_vc_icon";
    }

    @Override
    public CourseNode getInstance() {
        return new ZoomCourseNode();
    }

    @Override
    public String getLinkText(Locale locale) {
        Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
        Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("title_vc");
    }

    @Override
    public boolean isEnabled() {
        ZoomModule module = CoreSpringFactory.getImpl(ZoomModule.class);
        return module.isEnabled() && module.isEnabledForCourseElement();
    }
}
