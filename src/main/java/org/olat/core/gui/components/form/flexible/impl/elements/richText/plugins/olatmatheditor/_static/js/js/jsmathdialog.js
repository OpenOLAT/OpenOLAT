var JSMathDialog = {
    init : function() {
	
        // Initialize local variables
        var dialogForm = document.forms[0];
        
        tinyMCEPopup.resizeToInnerSize();
        
        // Get the selected contents as text and place it in the latex input field
        tinyMCEPopup.restoreSelection();
        var selectedNode = tinyMCEPopup.editor.selection.getNode();
        if ((selectedNode.nodeName.toLowerCase() == "img") && (selectedNode.className == "mceItemJSMath")) {
            dialogForm.latex.value = unescape(selectedNode.alt);
        }
    },
    
    insert : function() {
        // Initialize local variables
        var latexCode = document.forms[0].latex.value;
        var editor = tinyMCEPopup.editor;
        var contentNode = editor.selection.getNode();

        // Check whether the selection is a jsMath objet by looking at its class attribute
        if ((contentNode != null) && (/mceItemJSMath/.test(editor.dom.getAttrib(contentNode, "class")))) {
            editor.dom.setAttrib(contentNode, "title", latexCode);
            editor.dom.setAttrib(contentNode, "alt", escape(latexCode));
            editor.execCommand("mceRepaint");
        } else {
        	var htmlCode = '<img src="' + tinyMCEPopup.getParam("olatsmileys_transparentImage") + '" class="mceItemJSMath" title="' + latexCode + '" alt="' + escape(latexCode) + '" width="32" height="32"/>';
            editor.execCommand("mceInsertContent", false, htmlCode);
        }
        
        // Restore and close
        tinyMCEPopup.restoreSelection();
        tinyMCEPopup.close();
    }
};



/**
 * This updates the math preview.
 */
function updatePreview() {
    
    // Get the offscreen preview element and the latex code.
    var offscreenPreview = $("mathpreviewOffscreen");
    var latexCode = $("latex").value;
    
    // Copy the latex code into the offscreen preview DIV.
    offscreenPreview.update(latexCode);
    
    // Set the class of the offscreen preview DIV to "math" so that jsMath will recognize it.
    offscreenPreview.addClassName("math");
    
    // Tell jsMath to render the LaTeX into the offscreen preview DIV.
    jsMath.Process();
    
    // After the offscreen preview has been rendered, copy it onscreen
    jsMath.Synchronize(copyPreviewToScreen);
}


/**
 * This copies the offscreen preview to the onscreen preview.
 * Do not use directly. Called asynchronously from updatePreview() via jsMath.Synchronize().
 */
function copyPreviewToScreen() {

    // Get the offscreen and onscreen previews
    var offscreenPreview = $("mathpreviewOffscreen");
    var preview = $("mathpreviewFormula");
    var errorMessage = $("mathpreviewErrorMessage");
    
    // Check wheter we have a rendered formula, an error message, or nothing
    if (offscreenPreview.down() != null) {
        if (offscreenPreview.down().nodeName == "NOBR") {
            // We have a formula
            preview.update(offscreenPreview.innerHTML);
            errorMessage.update("");
        } else if (offscreenPreview.down().nodeName == "SPAN") {
            if (offscreenPreview.down().hasClassName("error")) {
                errorMessage.update(offscreenPreview.innerHTML);
            }
        }
    } else {
        // We have nothing.
        preview.update(offscreenPreview.innerHTML);
    }
}
 
 /**
  * Dynamically loads the jsMath library, depending on the setting of 
  * "jsMathLibBasePath" setting (set either in olatdefaultconfig.xml or OlatMathEditorPlugin.java).
  * 
  */
 
 function loadJSMath() {
	 var jsMathLibBasePath = tinyMCEPopup.getParam("olatmatheditor_jsMathLibBasePath");
	 var jsMathLoader = jsMathLibBasePath + "easy/load.js";
	 document.write("<scr" + "ipt type=\"text/javascript\" src=\"" + jsMathLoader + "\"></scr"+"ipt>");
 }

  
  // Modify jsMath layout.
  jsMath = {
      styles: {
          // fix math button to the top right
  "#jsMath_button": {
      position:   "fixed",
      top:        "1px",
      right:      "2px",
      bottom:     "auto",
      "font-size": "x-small"
  },
  "#jsMath_message": {
      display:    "none"
  },
  '.typeset .error':  {
  'font-size':        '120%'
        }
      }
  };
  
tinyMCEPopup.onInit.add(JSMathDialog.init, JSMathDialog);

//Update preview on load to show the preview correctly if there's already a formula
window.onload = function() {
	updatePreview();
};
