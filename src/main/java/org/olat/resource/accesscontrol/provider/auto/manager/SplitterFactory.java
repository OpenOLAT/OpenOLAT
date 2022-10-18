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
package org.olat.resource.accesscontrol.provider.auto.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Initial date: 23.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class SplitterFactory {

	@Autowired
    private List<IdentifierValueSplitter> services;

    private static final Map<String, IdentifierValueSplitter> cache = new HashMap<>();

    @PostConstruct
    void initIdentifierValueSplitterCache() {
        for(IdentifierValueSplitter service : services) {
            cache.put(service.getType(), service);
        }
    }

    public IdentifierValueSplitter getSplitter(String type) {
        IdentifierValueSplitter splitter = cache.get(type);
        if (!StringHelper.containsNonWhitespace(type)) {
        	splitter = cache.get(SemicolonSplitter.TYPE);
        }
        return splitter;
    }
}
