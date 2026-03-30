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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * FlexiTable data model for the Zoom applications detail view.
 *
 * Initial date: 2026-03-27<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ZoomApplicationsTableModel extends DefaultFlexiTableDataModel<ZoomApplicationRow>
        implements SortableFlexiTableDataModel<ZoomApplicationRow> {

    private final Locale locale;

    public ZoomApplicationsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
        super(columnModel);
        this.locale = locale;
    }

    @Override
    public void sort(SortKey sortKey) {
        if (sortKey == null || getObjects() == null) return;

        Comparator<ZoomApplicationRow> comparator = switch (sortKey.getKey()) {
            case "ltiContextId" -> Comparator.comparingLong(r -> r.getApplication().getLti13Context().getKey());
            case "applicationType" -> Comparator.comparing(
                    r -> r.getApplication().getApplicationType() == null
                            ? "" : r.getApplication().getApplicationType().name(),
                    String.CASE_INSENSITIVE_ORDER);
            case "application" -> Comparator.comparing(
                    r -> r.getApplication().getDescription() == null
                            ? "" : r.getApplication().getDescription(),
                    String.CASE_INSENSITIVE_ORDER);
            default -> null;
        };

        if (comparator == null) return;
        if (!sortKey.isAsc()) comparator = comparator.reversed();

        List<ZoomApplicationRow> sorted = new ArrayList<>(getObjects());
        sorted.sort(comparator);
        setObjects(sorted);
    }

    @Override
    public Object getValueAt(int row, int col) {
        return getValueAt(getObject(row), col);
    }

    @Override
    public Object getValueAt(ZoomApplicationRow r, int col) {
        return switch (ZoomApplicationCols.values()[col]) {
            case ltiContextId -> r.getApplication().getLti13Context().getKey();
            case applicationType -> r.getApplication().getApplicationType();
            case application -> r;
        };
    }

    public enum ZoomApplicationCols implements FlexiSortableColumnDef {
        ltiContextId("table.header.application.ltiContextId"),
        applicationType("table.header.application.type"),
        application("table.header.application.name");

        private final String i18nKey;

        ZoomApplicationCols(String i18nKey) {
            this.i18nKey = i18nKey;
        }

        @Override
        public String i18nHeaderKey() {
            return i18nKey;
        }

        @Override
        public boolean sortable() {
            return true;
        }

        @Override
        public String sortKey() {
            return name();
        }
    }
}