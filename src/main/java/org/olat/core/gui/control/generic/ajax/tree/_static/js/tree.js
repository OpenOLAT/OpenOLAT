var BTree = {
	
	// Method to select a path in the tree
	selectPath : function (path, treePanelName){
		var tree = o_info.objectMap.get(treePanelName);
		try {
			if (!tree) return;
			tree.selectPath(path, '', function(s,n){
				if (!s) BTree.reloadPath(path, treePanelName);
				var _tree = o_info.objectMap.get(treePanelName);
				_tree.selectPath(path);
			});
		} catch(e) {
			B_AjaxLogger.logDebug("Problem selecting the node with path::" + path + " ; exception::" + e , "org.olat.core.gui.control.generic.ajax.tree._content.tree.html");	
		}
		tree = null;
	},
	
	// Method to reload a path in the tree (e.g. it has a new child)
	reloadPath : function (path,treePanelName) {
		var tree = o_info.objectMap.get(treePanelName);
		try {
			if (!tree) return;
			var slashPos = path.lastIndexOf('/');
			if (slashPos == -1) return;
			var parentPath = path.substring(0, slashPos);
			var slashPos = parentPath.lastIndexOf('/');
			if (slashPos == -1) return;
			var parent = tree.getNodeById(parentPath.substring(slashPos + 1));
			if (parent) parent.reload();
		} catch(e) {
			B_AjaxLogger.logDebug("Problem reloading the node with path::" + path + " ; exception::" + e , "org.olat.core.gui.control.generic.ajax.tree._content.tree.html");	
		}
	},
	
	// Method to remove a node by the given path in the tree (e.g. because a node was removed on the server side)
	removePath : function (path,treePanelName) {
		var tree = o_info.objectMap.get(treePanelName);
		try {
			if (!tree) return;
			var slashPos = path.lastIndexOf('/');
			if (slashPos == -1) return;
			var id = path.substring(slashPos + 1);
			var node = tree.getNodeById(id);		
			if (!node) return;
			var parentPath = path.substring(0, slashPos);
			var slashPos = parentPath.lastIndexOf('/');
			if (slashPos == -1) return;
			var parent = tree.getNodeById(parentPath.substring(slashPos + 1));
			if (parent)	parent.removeChild(node);
		} catch(e) {
			B_AjaxLogger.logDebug("Problem deleting the node with path::" + path + " ; exception::" + e , "org.olat.core.gui.control.generic.ajax.tree._content.tree.html");	
		}
	}

}