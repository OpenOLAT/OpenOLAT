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
 * <p>
 */
package org.olat.commons.fileutil;

import org.olat.core.commons.modules.bc.FolderConfig;

/**
 * Thrown to indicate that a file/dir size exceeds the configured maximum.
 *
 * Initial Date: 19.10.2015 <br>
 *
 * @author lavinia
 */
public class CustomQuotaDetectedException extends RuntimeException {

    /**
     * @param message
     *
     */
    public CustomQuotaDetectedException(String message) {
        super(message);
    }

}
