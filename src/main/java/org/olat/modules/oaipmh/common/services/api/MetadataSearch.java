/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.olat.modules.oaipmh.common.services.api;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @param <T> - The type of object that this searcher returns 
 */
public interface MetadataSearch<T> {
    T findOne(String xoaiPath);

    List<T> findAll(String xoaiPath);

    /**
     *
     * @return - the entire metadata map
     */
    Map<String, List<T>> index();
}
