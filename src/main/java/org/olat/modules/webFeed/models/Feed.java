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
package org.olat.modules.webFeed.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.webFeed.FeedSecurityCallback;

/**
 * This is an OLAT feed (or web/news feed) model. It stores all necessary
 * information of a feed including items. Implements Serializable for caching.
 * 
 * <P>
 * Initial Date: Feb 16, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class Feed implements OLATResourceable, Serializable {
	// Identification
	private Long id;
	private String type;
	// Properties
	private String title;
	private String description;
	private String author;
	private String imageName;
	private String externalFeedURL;
	private String externalImageURL;
	private Date lastModified;
	// String copyright;
	// String language;
	
	// Data model versioning
	public static final int CURRENT_MODEL_VERSION = 2;
	// Default is that model version is set to the initial value 1. This is 
	// necessary to detect models previous to the introduction of this model
	// version flag which was with version 2.
	private int modelVersion = 0; 

	// A feed can either be internal, external or unspecified.
	// Internal means that items are created within OLAT. External implies
	// that the feed is read from an external URL.
	private Boolean isExternal;

	// The items (saved separately from feed)
	// @XStreamOmitField : didn't work
	transient private List<Item> items = new ArrayList<Item>();

	// This list enables us to save items separately and refer to them by id.
	private List<String> itemIds = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 * @param resource
	 */
	public Feed(OLATResourceable resource) {
		this.id = resource.getResourceableId();
		this.type = resource.getResourceableTypeName();
		// new model constructor, set to current version
		this.modelVersion = CURRENT_MODEL_VERSION;
	}
	
	public OLATResourceable getResource() {
		return OresHelper.createOLATResourceableInstanceWithoutCheck(type, id);
	}

	/**
	 * Setter for title
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Setter for description
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Setter for name
	 * 
	 * @param name
	 */
	public void setImageName(String name) {
		this.imageName = name;
	}

	/**
	 * Getter for title
	 * 
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Getter for description
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Getter for imageName
	 * 
	 * @return imageName
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return The id
	 */
	public Long getId() {
		return getResourceableId();
	}

	/**
	 * @return <tt>true</tt> if the current object is an external feed.
	 */
	public boolean isExternal() {
		return isExternal != null && isExternal.booleanValue();
	}

	/**
	 * @return <tt>true</tt> if the current object is an internal feed.
	 */
	public boolean isInternal() {
		return isExternal != null && !this.isExternal.booleanValue();
	}

	/**
	 * @return <tt>true</tt> if the current object is an external feed.
	 */
	public boolean isUndefined() {
		return isExternal == null;
	}

	/**
	 * @param isExternal The isExternal to set. (Valid argument values are true,
	 *          false (their corresponding boolean object) or null)
	 */
	public void setExternal(Boolean isExternal) {
		// Clear all items
		items.clear();
		// Initializes item id's store
		if (isExternal == null) {
			// undefined state
			this.itemIds = null;
		} else if (isExternal.booleanValue()){
			// external feed
			this.itemIds = null;
		} else {
			this.itemIds = new ArrayList<String>();				
		}
		// Set new state
		this.isExternal = isExternal;
	}

	/**
	 * @return All items
	 */
	public List<Item> getItems() {
		return items;
	}
	
	/**
	 * Return a copy of the list of items, but the items
	 * are not copied. Use this method to mitigate concurrent
	 * modifications issues.
	 * 
	 * @return
	 */
	public List<Item> getCopiedListOfItems() {
		return items == null ? null : new ArrayList<>(items);
	}

	/**
	 * @param identity
	 * @return The filtered Items
	 */
	public List<Item> getFilteredItems(FeedSecurityCallback callback, Identity identity) {
		final Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
		if (roles != null) {
			boolean admin = roles.isOLATAdmin();
			if (admin || isExternal()) {
				// An admin can see all items and everybody can see all items of
				// external feeds
				return items;
			}
		}
		List<Item> filteredItems = new ArrayList<Item>();
		for (Item item : items) {
			if (item.isPublished()) {
				// everybody can see published items
				filteredItems.add(item);
			} else if (item.isScheduled() && callback.mayEditItems()) {
				// scheduled items can be seen by everybody who can edit items
				// (moderators)
				filteredItems.add(item);
			} else if (identity.getKey() == item.getAuthorKey()) {
				// scheduled items and drafts of oneself are shown
				filteredItems.add(item);
			} else if (item.isDraft()) {
				if(callback.mayViewAllDrafts()) {
					filteredItems.add(item);
				} else if (identity.getKey() == item.getModifierKey()) {
					filteredItems.add(item);
				}
			}
		}
		return filteredItems;
	}

	/**
	 * @return A list of all published items
	 */
	public List<Item> getPublishedItems() {
		List<Item> publishedItems = new ArrayList<Item>();
		for (Item item : items) {
			if (item.isPublished()) {
				publishedItems.add(item);
			}
		}
		return publishedItems;
	}

	/**
	 * Sorts the items by publish date in reverse chronological order
	 */
	public void sortItems() {
		Collections.sort(items, new ItemPublishDateComparator());
		// reset the itemIds
		setItems(items);
	}

	/**
	 * @return All items in an array
	 */
	public Item[] getItemsArray() {
		Item[] itemsArray = null;
		if (items != null) {
			int size = items.size();
			itemsArray = new Item[size];
			for (int i = 0; i < size; i++) {
				itemsArray[i] = items.get(i);
			}
		}
		return itemsArray;
	}

	/**
	 * @return All item ids
	 */
	public List<String> getItemIds() {
		return itemIds;
	}

	/**
	 * Add an item to the feed.<br>
	 * 
	 * @param item
	 */
	public void add(Item item) {
		items.add(0, item);
		if (isInternal()) {
			itemIds.add(0, item.getGuid());
		}
	}

	/**
	 * Remove an item from the feed
	 * 
	 * @param item
	 * @return <tt>true</tt> if this list contained the specified element.
	 */
	public boolean remove(Item item) {
		itemIds.remove(item.getGuid());
		// Remove below works also when object identity is not the same as
		// item.equals has been overwritten to match on the GUID
		return items.remove(item);
	}

	/**
	 * @param items
	 */
	public void setItems(List<Item> items) {
		this.items = items;
		this.itemIds = new ArrayList<String>(items.size());
		for (Item item : items) {
			itemIds.add(item.getGuid());
		}
	}

	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param feedUrl The external feed URL to set.
	 */
	public void setExternalFeedUrl(String feedUrl) {
		this.externalFeedURL = feedUrl;
	}

	/**
	 * @return Returns the externalFeedURL.
	 */
	public String getExternalFeedUrl() {
		return externalFeedURL;
	}

	/**
	 * @param externalImageURL The externalImageURL to set.
	 */
	public void setExternalImageURL(String externalImageURL) {
		this.externalImageURL = externalImageURL;
	}

	/**
	 * @return Returns the externalImageURL.
	 */
	public String getExternalImageURL() {
		return externalImageURL;
	}

	/**
	 * @return True if there are items
	 */
	public boolean hasItems() {
		boolean hasItems = false;
		if (items != null && items.size() > 0) {
			hasItems = true;
		}
		return hasItems;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableId()
	 */
	public Long getResourceableId() {
		return id;
	}

	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return type;
	}

	/**
	 * Set the resourcable type name. (Sometimes needed on reload, just to make
	 * sure.)
	 * 
	 * @param type The resourcable type name
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the model version to the specific value;
	 * @param modelVersion
	 */
	public void setModelVersion(int modelVersion) {
		this.modelVersion = modelVersion;
	}

	/**
	 * Get the version of the datamodel
	 * 
	 * @return
	 */
	public int getModelVersion() {
		return modelVersion;
	}
	
}
