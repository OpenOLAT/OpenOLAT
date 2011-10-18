package org.olat.core.util.vfs.restapi;

import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class VFSStreamingOutput implements StreamingOutput {
	
	private final VFSLeaf leaf;
	
	public VFSStreamingOutput(VFSLeaf leaf) {
		this.leaf = leaf;
	}

	@Override
	public void write(OutputStream output) throws WebApplicationException {
		InputStream in = leaf.getInputStream();
		FileUtils.copy(in, output);
		FileUtils.closeSafely(in);
	}
}