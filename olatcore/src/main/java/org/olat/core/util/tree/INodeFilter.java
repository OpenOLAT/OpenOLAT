/**
 * 
 */
package org.olat.core.util.tree;

import org.olat.core.util.nodes.INode;

/**
 * @author patrickb
 *
 */
public interface INodeFilter {
	
	public boolean accept(INode node);

}
