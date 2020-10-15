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
package org.olat.modules.contacttracing.ui;

import java.util.*;
import java.util.stream.*;

import org.apache.logging.log4j.*;
import org.olat.core.commons.persistence.*;
import org.olat.core.gui.components.form.flexible.elements.*;
import org.olat.core.gui.components.form.flexible.impl.elements.*;
import org.olat.core.gui.components.form.flexible.impl.elements.table.*;
import org.olat.core.gui.components.link.*;
import org.olat.core.logging.*;
import org.olat.core.util.*;
import org.olat.modules.contacttracing.*;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationTableModel extends DefaultFlexiTableDataModel<ContactTracingLocation>
implements SortableFlexiTableDataModel<ContactTracingLocation>, FilterableFlexiTableModel {

    public static final String ACTIONS_CMD = "actions_cmd";

    private static final Logger log = Tracing.createLoggerFor(ContactTracingLocationTableModel.class);

    private List<ContactTracingLocation> backup;
    private Map<ContactTracingLocation, FormLink> toolLinks;

    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationTableModel(FlexiTableColumnModel columnModel, List<ContactTracingLocation> locations, ContactTracingManager contactTracingManager) {
        super(columnModel);

        // Set contact tracing manager, @Autowired is not working here
        this.contactTracingManager = contactTracingManager;

        // Set objects
        setObjects(locations);
    }

    @Override
    public void sort(SortKey sortKey) {

    }

    @Override
    public void filter(String searchString, List<FlexiTableFilter> filters) {
        if (StringHelper.containsNonWhitespace(searchString)) {
            search(searchString);
        } else {
            super.setObjects(backup);
        }
    }

    private void search(String searchString) {
        try {
            List<ContactTracingLocation> filteredList;
            String search = searchString.toLowerCase();

            filteredList = backup.stream().filter(location -> {
                if (location.getReference().toLowerCase().contains(search) ||
                        location.getTitle().toLowerCase().contains(search) ||
                        location.getRoom().toLowerCase().contains(search) ||
                        location.getBuilding().toLowerCase().contains(search) ||
                        location.getQrId().toLowerCase().contains(search)) {
                    return true;
                }

                return false;
            }).collect(Collectors.toList());

            super.setObjects(filteredList);
        } catch (Exception e) {
            log.error("", e);
            super.setObjects(backup);
        }
    }

    @Override
    public void setObjects(List<ContactTracingLocation> objects) {
        // Set objects
        super.setObjects(objects);
        // Create tool links
        toolLinks = new HashMap<>();
        for (ContactTracingLocation location : objects) {
            FormLink toolsLink = new FormLinkImpl("tools-" + objects.indexOf(location), ACTIONS_CMD, "", Link.NONTRANSLATED);
            toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
            toolsLink.setUserObject(location);

            toolLinks.put(location, toolsLink);
        }
        // Back up objects (used to filter)
        backup = objects;
    }

    @Override
    public Object getValueAt(ContactTracingLocation row, int col) {
        switch (ContactTracingLocationCols.values()[col]) {
            case reference:
                return row.getReference();
            case title:
                return row.getTitle();
            case room:
                return row.getRoom();
            case building:
                return row.getBuilding();
            case qrId:
                return row.getQrId();
            case guest:
                return row.isAccessibleByGuests();
            case registrations:
                return contactTracingManager.getRegistrations(row);
            case settings:
                return toolLinks.get(row);
            default:
                return "ERROR";
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        ContactTracingLocation location = getObject(row);
        return getValueAt(location, col);
    }

    @Override
    public DefaultFlexiTableDataModel<ContactTracingLocation> createCopyWithEmptyList() {
        return null;
    }

    public enum ContactTracingLocationCols implements FlexiSortableColumnDef {
        reference("contact.tracing.cols.reference"),
        title("contact.tracing.cols.title"),
        room("contact.tracing.cols.room"),
        building("contact.tracing.cols.building"),
        qrId("contact.tracing.cols.qr.id"),
        guest("contact.tracing.cols.guest"),
        registrations("contact.tracing.cols.registrations"),
        settings("contact.tracing.cols.settings", "o_icon o_icon-lg o_icon_actions");

        private final String i18nHeaderKey;
        private final String iconHeader;

        ContactTracingLocationCols(String i18nHeaderKey) {
            this(i18nHeaderKey, null);
        }

        ContactTracingLocationCols(String i18nHeaderKey, String iconHeader) {
            this.i18nHeaderKey = i18nHeaderKey;
            this.iconHeader = iconHeader;
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

        @Override
        public String iconHeader() {
            return iconHeader;
        }
    }
}
