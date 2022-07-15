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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.*;

import java.util.List;
import java.util.Locale;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomProfilesTableModel extends DefaultFlexiTableDataModel<ZoomProfileRow> implements SortableFlexiTableDataModel<ZoomProfileRow> {

    private static final ZoomProfileCols[] COLS = ZoomProfileCols.values();
    private final Locale locale;

    public enum ZoomProfileCols implements FlexiSortableColumnDef {
        name("table.header.profile.name"),
        status("table.header.profile.status"),
        mailDomain("table.header.profile.mailDomains"),
        studentsCanHost("table.header.profile.studentsCanHost"),
        clientId("table.header.profile.clientId"),
        applications("table.header.profile.applications"),
        tools("table.header.actions");

        private final String i18nHeaderKey;

        ZoomProfileCols(String i18nHeaderKey) {
            this.i18nHeaderKey = i18nHeaderKey;
        }

        @Override
        public boolean sortable() {
            return true;
        }

        @Override
        public String sortKey() {
            return name();
        }

        @Override
        public String i18nHeaderKey() {
            return i18nHeaderKey;
        }
    }

    public ZoomProfilesTableModel(FlexiTableColumnModel columnModel, Locale locale)
    {
        super(columnModel);
        this.locale = locale;
    }

    @Override
    public Object getValueAt(int row, int col) {
        ZoomProfileRow zoomProfileRow = getObject(row);
        return getValueAt(zoomProfileRow, col);
    }

    @Override
    public Object getValueAt(ZoomProfileRow row, int col) {
        switch (COLS[col]) {
            case name: return row.getName();
            case status: return row.getStatus();
            case mailDomain: return row.getMailDomains();
            case studentsCanHost: return row.isStudentsCanHost();
            case clientId: return row.getClientId();
            case applications: return row.getNumberOfApplications();
            case tools: return row.getToolLink();
            default:
                return "ERROR";
        }
    }

    @Override
    public void sort(SortKey sortKey) {
        if (sortKey != null) {
            List<ZoomProfileRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
            super.setObjects(rows);
        }
    }
}
