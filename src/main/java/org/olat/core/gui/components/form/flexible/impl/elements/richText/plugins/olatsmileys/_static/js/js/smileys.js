// Olatsmileys plugin dialog
var SmileysDialog = {
	
	// Initialization
	init : function(ed) {
		tinyMCEPopup.resizeToInnerSize();
	},

	// Content insertion
	insert : function(file, title, type) {
		// Create IMG tag...
		var html = '<img src="' + tinyMCEPopup.getParam("olatsmileys_transparentImage") + '" class="b_emoticons_' + type + '">'
		
		// ...insert into editor content...
		tinyMCEPopup.execCommand('mceInsertContent', false, html);
		
		// ...and close popup dialog.
		tinyMCEPopup.close();
	}
};

// Dialog initialization handler
tinyMCEPopup.onInit.add(SmileysDialog.init, SmileysDialog);

/** 
 * Renders the dialog dynamically. This is necessary to be able to embed
 * the plugin parameter for the transparent image.
 */
function renderDialog() {
	
	// Create a multi-dimensional array which holds the smiley names
	var smileyNames = new Array(3);
	for (i=0; i<4; i++) {
		smileyNames[i] = new Array(6);
	}
	smileyNames[0][0] = "smile";
	smileyNames[0][1] = "sad";
	smileyNames[0][2] = "blushing";
	smileyNames[0][3] = "confused";
	smileyNames[0][4] = "cool";
	smileyNames[0][5] = "cry";
	
	smileyNames[1][0] = "devil";
	smileyNames[1][1] = "grin";
	smileyNames[1][2] = "kiss";
	smileyNames[1][3] = "ohoh";
	smileyNames[1][4] = "angry";
	smileyNames[1][5] = "sick";
	
	smileyNames[2][0] = "angel";
	smileyNames[2][1] = "tongue";
	smileyNames[2][2] = "ugly";
	smileyNames[2][3] = "weird";
	smileyNames[2][4] = "wink";
	smileyNames[2][5] = "worried";
	
	
	
	var transparentImg = tinyMCEPopup.getParam("olatsmileys_transparentImage");
	var table = document.getElementById("smileystable");

	for (var row=0; row<3; row++) {
		var rowElement = document.createElement("tr");
		table.appendChild(rowElement);
		for (var col=0; col<6; col++) {
			var n = smileyNames[row][col];
			// use OLAT translator for OALT image
			var altText = translator.translate('olatsmileys.icon.' + n);
			var cellElement = document.createElement("td");
			cellElement.innerHTML = 
					  "<a href=\"javascript:SmileysDialog.insert('smiley-" + n + ".gif','smileys_dlg." + n + "','" + n + "');\">"
					+"<img class=\"b_emoticons_" + n + "\" src=\"" + tinyMCEPopup.getParam("olatsmileys_transparentImage") +"\" width=\"18\" height=\"18\" border=\"0\" alt=\"" + n 
					+ "\" title=\"" + altText + "\" />"
					+"</a>";
			rowElement.appendChild(cellElement);
		}
	}
}
