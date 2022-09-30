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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.translator.Translator;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class PFFolderTemplateRow implements FlexiTreeTableNode {

    private final String folderName;
    private final FormLink createSubFolderLink;
    private final FormLink toolsLink;
    private String path;
    private int numOfChildren = 0;
    private PFFolderTemplateRow parent;

    public PFFolderTemplateRow(String folderName, FormLink toolsLink, FormLink createSubFolderLink, Translator translator) {
        this.folderName = folderName;
        this.createSubFolderLink = createSubFolderLink;
        if (!folderName.equals(translator.translate("return.box"))
                && !folderName.equals(translator.translate("drop.box"))) {
            this.toolsLink = toolsLink;
        } else {
            this.toolsLink = null;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren = numOfChildren;
    }

    public int getNumberOfChildren() {
        return numOfChildren;
    }

    public FormLink getCreateSubFolderLink() {
        return createSubFolderLink;
    }

    public FormLink getToolsLink() {
        return toolsLink;
    }

    @Override
    public FlexiTreeTableNode getParent() {
        return parent;
    }

    public void setParent(PFFolderTemplateRow parent) {
        this.parent = parent;
    }

    @Override
    public String getCrump() {
        return null;
    }
}
