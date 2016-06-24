package org.olat.modules.webFeed.portfolio;

import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BlogEntryMedia {
	
	private final Feed feed;
	private final Item item;
	
	public BlogEntryMedia(Feed feed, Item item) {
		this.feed = feed;
		this.item = item;
	}

	public Feed getFeed() {
		return feed;
	}

	public Item getItem() {
		return item;
	}
}
