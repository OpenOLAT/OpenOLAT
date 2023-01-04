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
package org.olat.modules.catalog.filter;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.springframework.stereotype.Service;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OERIndexMetadataHandler implements CatalogFilterHandler {

    private static final String TYPE = "oerpub";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(boolean isGuestOnly) {
        return false;
    }

    @Override
    public int getSortOrder() {
        return 0;
    }

    @Override
    public boolean isMultiInstance() {
        return false;
    }

    @Override
    public String getTypeI18nKey() {
        return "details.index.metadata";
    }

    @Override
    public String getAddI18nKey() {
        return null;
    }

    @Override
    public String getEditI18nKey() {
        return null;
    }

    @Override
    public String getDetails(Translator translator, CatalogFilter catalogFilter) {
        return null;
    }

    @Override
    public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogFilter catalogFilter) {
        return new CatalogFilterBasicController(ureq, wControl, this, catalogFilter);
    }

    @Override
    public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogRepositoryEntrySearchParams searchParams, CatalogFilter catalogFilter) {
        return null;
    }

    @Override
    public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {

    }
}
