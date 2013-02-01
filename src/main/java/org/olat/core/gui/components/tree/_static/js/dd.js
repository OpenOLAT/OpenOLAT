function onTreeStartDrag(e, targetId) {
	console.log('onTreeStartDrag', e, targetId);
}

function onTreeDragOver(e, targetId) {
	console.log('onTreeDragOver', e, targetId);
}

function onTreeDragOut(e, targetId) {
	console.log('onTreeDragOut', e, targetId);
}
	
function onTreeEndDrag(e, targetId) {
	console.log('onTreeEndDrag', e, targetId);
}

function onTreeAccept(e, targetId) {
	console.log('onTreeAccept', e, targetId);
}

function onTreeDrop(el, targetId) {
	console.log('onTreeDrop', el, targetId);
}

function treeAcceptDrop(el, targetId) {
	console.log('treeAcceptDrop', el);
	return false;
}

/*Ext.namespace('Ext.fxMenuTree');
Ext.fxMenuTree.DDProxy = function(id, group, dropUrl, overUrl) {
	var config = {dragData:{end:dropUrl, over:overUrl}, scope:this };
	Ext.fxMenuTree.DDProxy.superclass.constructor.call(this, id, group, config);
};
  
Ext.extend(Ext.fxMenuTree.DDProxy, Ext.dd.DDProxy, {
    startDrag: function(x, y) {
        var dragEl = Ext.get(this.getDragEl());
        var el = Ext.get(this.getEl());
        dragEl.applyStyles({border:'','z-index':2000});
        dragEl.update(el.dom.innerHTML);
        dragEl.addClass(el.dom.className + ' b_dd_proxy');
    },
    
	onDragOver: function(e, targetId) {
		if(targetId && (targetId.indexOf('dd') == 0 || targetId.indexOf('ds') == 0 || targetId.indexOf('dt') == 0 || targetId.indexOf('da') == 0)) {
			var target = Ext.get(targetId);
    		this.lastTarget = target;
    		if(this.config.dragData.over && this.config.dragData.over.length > 0) {
    			var url = this.config.dragData.over + "/";
    			var dropId = this.id.substring(2,this.id.length);
    			var targetId = this.lastTarget.id.substring(2,this.lastTarget.id.length);
    			var sibling = "";
    			if(this.lastTarget.id.indexOf('ds') == 0) {
    				sibling = "yes";
    			} else if(this.lastTarget.id.indexOf('dt') == 0) {
    				sibling = "end";
    			}
    			//use prototype for the Ajax call
    			var stat = new Ajax.Request(url, { 
    				method: 'get',
    				asynchronous : false,
            		parameters : { nidle:dropId, tnidle:targetId, sne:sibling },
            		onSuccess: function(transport) {
            			//use prototype to parse JSON response
            			var response = transport.responseText.evalJSON();
            			if(response.dropAllowed) {
            				target.addClass('b_dd_over');
            			} else {
            				target.removeClass('b_dd_over');
            			}
					}
          		});
    		} else {
    			target.addClass('b_dd_over');
    		}
    	}
	},
		
	onDragOut: function(e, targetId) {
    	if(targetId && (targetId.indexOf('dd') == 0 || targetId.indexOf('ds') == 0 || targetId.indexOf('dt') == 0 || targetId.indexOf('da') == 0)) {
    		var target = Ext.get(targetId);
    		this.lastTarget = null;
    		target.removeClass('b_dd_over');
    	}
	},
		
	endDrag: function() {
    	var dragEl = Ext.get(this.getDragEl());
    	var el = Ext.get(this.getEl());
    	if(this.lastTarget) {
    		Ext.get(this.lastTarget).appendChild(el);
    		el.applyStyles({position:'', width:''});
    		var url =  this.config.dragData.end;
    		if(url.lastIndexOf('/') == (url.length - 1)) {
    			url = url.substring(0,url.length-1);
    		}
    		var targetId = this.lastTarget.id.substring(2,url.length);
    		url += '%3Atnidle%3A' + targetId;
    		if(this.lastTarget.id.indexOf('ds') == 0) {
    			url += '%3Asne%3Ayes';
    		} else if(this.lastTarget.id.indexOf('dt') == 0) {
    			url += '%3Asne%3Aend';
    		}
    		frames['oaa0'].location.href = url + '/';
    	}
	}
});*/