package org.olat.core.commons.modules.singlepage;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 22.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SinglePageMediaMapper implements Mapper {

	private VFSContainer rootContainer;
	
	public SinglePageMediaMapper() {
		//for serialize/deserialize
	}
	
	public SinglePageMediaMapper(VFSContainer rootContainer) {
		this.rootContainer = rootContainer;
	}
	
	public MediaResource handle(String relPath,HttpServletRequest request) {
		VFSItem currentItem = rootContainer.resolve(relPath);
		if (currentItem == null || (currentItem instanceof VFSContainer)) {
			return new NotFoundMediaResource(relPath);
		}
		return new VFSMediaResource((VFSLeaf)currentItem);
	}
}
