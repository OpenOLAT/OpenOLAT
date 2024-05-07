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
package org.olat.core.commons.services.vfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Initial date: 26 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSMetadataContainer extends VFSMetadataItem implements VFSContainer {
	
	private static final VFSItemFilter TRUE_FILTER = item -> true;
	
	private final boolean cached;
	private List<VFSItem> children;
	private VFSItemFilter defaultFilter;
	
	public VFSMetadataContainer(VFSRepositoryService vfsRepositoryService, boolean cached, VFSContainer wrappedContainer) {
		this(vfsRepositoryService, cached, wrappedContainer.getMetaInfo(), wrappedContainer.getParentContainer(),
				wrappedContainer.getLocalSecurityCallback(), wrappedContainer.getDefaultItemFilter());
		if (wrappedContainer instanceof NamedContainerImpl namedContainer) {
			this.vfsItem = wrappedContainer;
		}
	}

	public VFSMetadataContainer(VFSRepositoryService vfsRepositoryService, boolean cached, VFSMetadata vfsMetadata,
			VFSContainer parentContainer, VFSSecurityCallback secCallback, VFSItemFilter defaultFilter) {
		super(vfsRepositoryService, vfsMetadata, parentContainer, secCallback);
		this.cached = cached;
		this.defaultFilter = defaultFilter;
	}
	
	boolean isCached() {
		return cached;
	}

	List<VFSItem> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	@Override
	protected void onItemCreated(VFSItem createdItem) {
		if (createdItem instanceof VFSContainer vfsContainer) {
			vfsContainer.setDefaultItemFilter(defaultFilter);
		}
	}
	
	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return defaultFilter;
	}
	
	@Override
	public String getName() {
		if (vfsItem instanceof NamedContainerImpl namedContainer) {
			return vfsItem.getName();
		}
		return super.getName();
	}

	@Override
	public boolean isInPath(String path) {
		if (getMetaInfo() == null || getMetaInfo().getRelativePath() == null) {
			return false;
		}
		return getMetaInfo().getRelativePath().startsWith(path);
	}
	
	@Override
	public List<VFSItem> getItems() {
		return getItems(null);
	}
	
	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if (getMetaInfo() == null || getItem() == null) {
			return List.of();
		}
		if (cached) {
			if (children == null) {
				// Fill the cache with descendants as well.
				// The FolderController likes to display the number of items of the folder
				fillCache();
			}
			return getChildren().stream().filter(vfsItem -> accept(filter, vfsItem)).toList();
		}
		
		List<VFSMetadata> children = vfsRepositoryService.getChildren(getMetaInfo());
		return toItems(children, filter);
	}

	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}

	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		if (getMetaInfo() == null || getItem() == null) {
			return List.of();
		}
		if (cached) {
			if (children == null) {
				fillCache();
			}
			return getCachedItemsFlat(filter);
		}
		List<VFSMetadata> descendants = vfsRepositoryService.getDescendants(getMetaInfo(), Boolean.FALSE);
		return toItems(descendants, filter);
	}

	private void fillCache() {
		List<VFSMetadata> metadatas = vfsRepositoryService.getDescendants(getMetaInfo(), Boolean.FALSE);
		toItems(metadatas, TRUE_FILTER);
	}
	
	public List<VFSItem> getCachedItemsFlat(VFSItemFilter filter) {
		List<VFSItem> allItems = new ArrayList<>();
		loadCachedItemsAndChildren(allItems, this, filter);
		return allItems;
	}
	
	private void loadCachedItemsAndChildren(List<VFSItem> allItems, VFSContainer vfsContainer, VFSItemFilter vfsFilter) {
		List<VFSItem> items = vfsContainer.getItems(vfsFilter);
		allItems.addAll(items);
		
		items.forEach(item -> {
			if (item instanceof VFSContainer childContainer) {
				loadCachedItemsAndChildren(allItems, childContainer, vfsFilter);
			}
		});
	}

	private List<VFSItem> toItems(List<VFSMetadata> metadatas, VFSItemFilter filter) {
		metadatas.sort((m1, m2) -> m1.getRelativePath().compareTo(m2.getRelativePath()));
		
		List<VFSItem> items = new ArrayList<>();
		Map<String, VFSContainer> pathToConatiner = new HashMap<>();
		if (getItem() instanceof VFSContainer vfsContainer) {
			String relativePath = getMetaInfo().getRelativePath() + "/" + getMetaInfo().getFilename();
			pathToConatiner.put(relativePath, this);
		}
		
		for (VFSMetadata descendant : metadatas) {
			if (descendant.isDeleted()) {
				continue;
			}
			VFSContainer descendantParentContainer = pathToConatiner.get(descendant.getRelativePath());
			// If parent container not available the container was (probably) removed by the filter.
			if (descendantParentContainer != null) {
				if (descendant.isDirectory()) {
					String relativePath = descendant.getRelativePath() + "/" + descendant.getFilename();
					VFSMetadataContainer descendantContainer = new VFSMetadataContainer(vfsRepositoryService,
							cached, descendant, descendantParentContainer, getLocalSecurityCallback(), defaultFilter);
					addToCache(descendantParentContainer, descendantContainer);
					if (accept(filter, descendantContainer)) {
						pathToConatiner.put(relativePath, descendantContainer);
						items.add(descendantContainer);
					}
				} else {
					VFSMetadataLeaf descendantLeaf = new VFSMetadataLeaf(vfsRepositoryService, descendant,
							descendantParentContainer, getLocalSecurityCallback());
					addToCache(descendantParentContainer, descendantLeaf);
					if (accept(filter, descendantLeaf)) {
						items.add(descendantLeaf);
					}
				}
			}
		}
		
		return items;
	}

	private void addToCache(VFSContainer parentContainer, VFSItem item) {
		if (parentContainer instanceof VFSMetadataContainer cacheContainer) {
			if (cacheContainer.isCached()) {
				cacheContainer.getChildren().add(item);
			}
		}
	}
	
	protected boolean accept(VFSItemFilter filter, VFSItem vfsItem) {
		return (defaultFilter == null || defaultFilter.accept(vfsItem))
				&& (filter == null || filter.accept(vfsItem));
	}
	
	@Override
	public VFSSuccess copyFrom(VFSItem source, Identity savedBy) {
		if (getItem() instanceof VFSContainer vfsContainer) {
			VFSSuccess vfsStatus = vfsContainer.copyFrom(source, savedBy);
			reset();
			return vfsStatus;
		}
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess copyContentOf(VFSContainer container, Identity savedBy) {
		if (getItem() instanceof VFSContainer vfsContainer) {
			VFSSuccess vfsStatus = vfsContainer.copyContentOf(container, savedBy);
			reset();
			return vfsStatus;
		}
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSContainer createChildContainer(String name) {
		if (getItem() instanceof VFSContainer vfsContainer) {
			return vfsContainer.createChildContainer(name);
		}
		return null;
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
		if (getItem() instanceof VFSContainer vfsContainer) {
			return vfsContainer.createChildLeaf(name);
		}
		return null;
	}

}
