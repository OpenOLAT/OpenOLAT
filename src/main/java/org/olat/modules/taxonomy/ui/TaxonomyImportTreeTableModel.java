/**
 * <a href="https://www.openolat.org">
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
package org.olat.modules.taxonomy.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import java.util.Locale;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class TaxonomyImportTreeTableModel extends DefaultFlexiTableDataModel<TaxonomyLevelRow> {

    private final Translator translator;

    public TaxonomyImportTreeTableModel(FlexiTableColumnModel columnModel, Locale locale) {
        super(columnModel);

        translator = Util.createPackageTranslator(this.getClass(), locale);
    }

    @Override
    public Object getValueAt(int row, int col) {
        TaxonomyLevelRow level = getObject(row);
        switch (TaxonomyImportLevelCols.values()[col]) {
            case key: return level.getKey();
            case displayName: return level.getDisplayName();
            case identifier: return level.hasMultipleLangs() ? null : level.getIdentifier();
            case typeIdentifier: return level.hasMultipleLangs() ? null : level.getTypeIdentifier();
            case updateWarning: return level.isUpdated();
            case description: return level.getDescription();
            case order: return level.hasMultipleLangs() ? null : level.getOrder();
            case path: return level.hasMultipleLangs() ? null : level.getTaxonomyLevel().getMaterializedPathIdentifiersWithoutSlash();
            case language: return level.getLanguage();
            case background: return level.hasMultipleLangs() ? null : (level.hasBackgroundImage() ? translator.translate("yes") : translator.translate("no"));
            case teaser: return level.hasMultipleLangs() ? null : (level.hasTeaserImage() ? translator.translate("yes") : translator.translate("no"));
            default: return "ERROR";
        }
    }

    public enum TaxonomyImportLevelCols implements FlexiSortableColumnDef {
        key("table.header.key"),
        identifier("table.header.taxonomy.level.identifier"),
        typeIdentifier("table.header.taxonomy.level.type.identifier"),
        order("table.header.taxonomy.level.order"),
        language("table.header.taxonomy.level.language"),
        displayName("table.header.taxonomy.level.displayName"),
        description("table.header.taxonomy.level.description"),
        updateWarning("table.header.taxonomy.update.warning", "o_icon o_icon_fw o_icon_warn"),
        path("table.header.taxonomy.level.path"),
        background("table.header.taxonomy.level.background"),
        teaser("table.header.taxonomy.level.background");

        private final String i18nKey;
        private final String iconHeader;


        private TaxonomyImportLevelCols(String i18nKey) {
            this(i18nKey, null);
        }

        private TaxonomyImportLevelCols(String i18nKey, String iconHeader) {
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
}