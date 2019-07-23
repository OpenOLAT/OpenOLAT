/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.groupsandrights;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.group.right.BGRightManager;

/**
 * Initial Date:  Aug 24, 2004
 *
 * @author gnaegi 
 */
public class CourseRights {
    
    // since right are stored as permissions, lenght is limited to 12 chars!!
    
    /** course right for groupmanagement */
    public static final String RIGHT_GROUPMANAGEMENT = BGRightManager.BG_RIGHT_PREFIX + "groupmngt";
    /** course right for groupmanagement */
    public static final String RIGHT_MEMBERMANAGEMENT = BGRightManager.BG_RIGHT_PREFIX + "membermngt";
    /** course right for course editor */
    public static final String RIGHT_COURSEEDITOR = BGRightManager.BG_RIGHT_PREFIX + "editor";
    /** course right for archive tool */
    public static final String RIGHT_ARCHIVING = BGRightManager.BG_RIGHT_PREFIX + "archive";
    /** course right for assessment tool */
    public static final String RIGHT_ASSESSMENT = BGRightManager.BG_RIGHT_PREFIX + "assess";
    /** course right for glossary tool */
    public static final String RIGHT_GLOSSARY = BGRightManager.BG_RIGHT_PREFIX + "glossary";
    /** course right for statistics tool */
    public static final String RIGHT_STATISTICS = BGRightManager.BG_RIGHT_PREFIX + "statistics";
    /** course right for assessment mode tool */
    public static final String RIGHT_ASSESSMENT_MODE = BGRightManager.BG_RIGHT_PREFIX + "assessmode";
    /** course right for custom dbs */
    public static final String RIGHT_DB = BGRightManager.BG_RIGHT_PREFIX + "dbs";
    
    private static List<String> rights;
    private Translator trans;

    static {
        // initialize list of valid course rights
        rights = new ArrayList<>();
        rights.add(RIGHT_GROUPMANAGEMENT);
        rights.add(RIGHT_MEMBERMANAGEMENT);
        rights.add(RIGHT_COURSEEDITOR);
        rights.add(RIGHT_ARCHIVING);
        rights.add(RIGHT_ASSESSMENT);
        rights.add(RIGHT_GLOSSARY);
        rights.add(RIGHT_STATISTICS);
        rights.add(RIGHT_ASSESSMENT_MODE);
        rights.add(RIGHT_DB);
    }
    
   
    /**
     * Constructor for the course rights
     * @param locale
     */
    public CourseRights(Locale locale) {
        this.trans = Util.createPackageTranslator(CourseRights.class,locale);
    }
    
    /**
     * @return A string array that contains the group rights keys used in pulldown menus
     * plus an empty entry for the 'no restriction key' as used in search forms
     */
    public static String[] getCourseRightsSearchKeys() {
        return new String[]{
                "",
                RIGHT_ASSESSMENT,
                RIGHT_GROUPMANAGEMENT,
                RIGHT_MEMBERMANAGEMENT,
                RIGHT_COURSEEDITOR,
                RIGHT_ARCHIVING,
                RIGHT_GLOSSARY,
                RIGHT_STATISTICS,
                RIGHT_ASSESSMENT_MODE
        };
    }

    /**
     * @param trans The translator
     * @return A string array that contains the group rights display values used in pulldown menus
     * plus an empty entry for the 'no restriction key' as used in search forms
     */
    public static String[] getCourseRightsSearchDisplayValues(Translator trans) {
        return new String[]{
                trans.translate("noRestriction"),
                trans.translate(RIGHT_ASSESSMENT),
                trans.translate(RIGHT_GROUPMANAGEMENT),
                trans.translate(RIGHT_MEMBERMANAGEMENT),
                trans.translate(RIGHT_COURSEEDITOR),
                trans.translate(RIGHT_ARCHIVING),
                trans.translate(RIGHT_GLOSSARY),
                trans.translate(RIGHT_STATISTICS),
                trans.translate(RIGHT_ASSESSMENT_MODE)
        };
    }
    
    public static List<String> getAvailableRights() {
    		return new ArrayList<>(rights);
    }

    /**
     * @see org.olat.group.right.BGRights#getRights()
     */
    public List<String> getRights() {
        return rights;
   }

    /**
     * @see org.olat.group.right.BGRights#transateRight(java.lang.String)
     */
    public String transateRight(String right) {
        return trans.translate(right);
    }
}