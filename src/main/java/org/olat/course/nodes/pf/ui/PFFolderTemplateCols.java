/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.pf.ui;


import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;


/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public enum PFFolderTemplateCols implements FlexiSortableColumnDef {
    folderName("table.elementFolder"),
    numOfChildren("table.elementSubFolder"),
    createSubFolder("table.elementCreateSubFolder"),
    toolsLink("table.header.actions");

    private final String i18nKey;
    private final String iconHeader;

    private PFFolderTemplateCols(String i18nKey) {
        this(i18nKey, null);
    }

    private PFFolderTemplateCols(String i18nKey, String iconHeader) {
        this.i18nKey = i18nKey;
        this.iconHeader = iconHeader;
    }

    @Override
    public String i18nHeaderKey() {
        return i18nKey;
    }

    @Override
    public String iconHeader() {
        return iconHeader;
    }

    @Override
    public boolean sortable() {
        return false;
    }

    @Override
    public String sortKey() {
        return name();
    }
}
