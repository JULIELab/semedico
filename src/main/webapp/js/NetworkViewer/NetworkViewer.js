var draw_options = {
		// enables pan and mouse zoom
		network: "placeholder",
		panZoomControlVisible : true,
		panEnabled : true,
		// appearance
		edgesMerged : false,
		edgeTooltipsEnabled : true,
		nodeTooltipsEnabled : true
	};
var vis;
viewNetwork = function(nodes){
	// id of Cytoscape Web container div
	var div_id = "cytoscapeweb";

	/** ******************************************************************* */
	/** *********************** AJAX ************************************* */
	var loadGraph = function(node) {
		loadGraphFromName(node.data.fullName);
	};
	
	var loadGraphFromName = function(name){
		$
		.ajax({
			type : "GET",
			url : "/semedico-frontend-1.7-SNAPSHOT/networkviewer:LoadNetwork/"
					+ name,
			attType : "text",
			error : function() {
				clear();
				print_txt("Error loading network data.");
			},
			success : function(loaded) {
				resetRefs();
				draw_options["network"] = loaded;
				vis.draw(draw_options);
			}
		});
	};
	
	/** ******************************************************************* */
	/** ************ functions used for interactivity ********************* */
	/**
	 * Replaces content of "refs" div.
	 */
	function clear() {
		document.getElementById("refs").innerHTML = "References:";
	}

	/**
	 * Replaces content of "refs" div.
	 */
	function resetRefs() {
		document.getElementById("refs").innerHTML = "Select nodes or edges for links.";
	}

	/**
	 * Appends to content of "refs" div. Produces clickable links
	 * (if possible) corresponding to the selected graph elements.
	 */
	function print(att) {
		if (att.sentence == null) { // node
			if (att.fullName == null)
				print_txt("<p>UniProt: <a href=" + att.link
						+ " target=\"_blank\">" + att.label
						+ "</a></p>");
			else
				print_txt("<p>UniProt: <a href=" + att.link
						+ " target=\"_blank\">" + att.fullName
						+ "</a></p>");
		} else
			// edge
			print_txt("<p>" + att.interaction + ": <a href="
					+ att.link + " target=\"_blank\">"
					+ att.sentence + "</a></p>");
	}

	/**
	 * Appends text to content of "refs" div, no further formatting.
	 */
	function print_txt(txt) {
		if (txt != null) {
			document.getElementById("refs").innerHTML += txt
					.replace(/\\'/g, "'").replace(/\\"'/g, '"');
			// replace necessary as escaping in xml-string doesn't
			// work properly
		}
	}
	
	
	/** ******************************************************************* */
	/** **************** Creating a Visualization ************************* */
	// initialization options, paths are relativ to website
	var options = {
		// where you have the Cytoscape Web SWF
		swfPath : "/semedico-frontend-1.7-SNAPSHOT/assets/1.2-SNAPSHOT/ctx/flash/swf/CytoscapeWeb",
		// where you have the Flash installer SWF
		flashInstallerPath : "/semedico-frontend-1.7-SNAPSHOT/assets/1.2-SNAPSHOT/ctx/flash/swf/playerProductInstall"
	};

	vis = new org.cytoscapeweb.Visualization(div_id, options);

	// adding customMappers
	vis["sizeByCenter"] = function(att) {
		if (att.center)
			return 60;
		return 40;
	};
	vis["widthByConfidence"] = function(att) {
		if (att.confidence < 0.1)
			return 1;
		if (att.confidence < 0.2)
			return 2;
		if (att.confidence < 0.3)
			return 3;
		if (att.confidence < 0.4)
			return 4;
		if (att.confidence < 0.5 || att.confidence == undefined)
			return 5;
		if (att.confidence < 0.6)
			return 6;
		if (att.confidence < 0.7)
			return 7;
		if (att.confidence < 0.8)
			return 8;
		if (att.confidence < 0.9)
			return 9;
		return 10;
		// using 10*Math.round(att.confidence) didn't work :/
	};
	vis["edgeLabelPretty"] = function(atts) {
		return atts.interaction;
	};

	/** ******************************************************************* */
	/** ************ Adding style and interactivity ********************** */

	vis
			.ready(function() { // called after Cytoscape Web has finished
				// drawing
				/** * style ** */
				vis.visualStyle({
					global : {
						tooltipDelay : 600
					// 800 is default
					},
					nodes : {
						size : {
							customMapper : {
								functionName : "sizeByCenter"
							}
						}, // see above
						labelFontSize : 12,
						color : "#e3e3e3",
						selectionColor : "#F4C400",
						tooltipText : {
							passthroughMapper : {
								attrName : "fullName"
							}
						},
					},
					edges : {
						width : {
							customMapper : {
								functionName : "widthByConfidence"
							}
						}, // see above
						color : "#2b4b60",
						tooltipText : {
							customMapper : {
								functionName : "edgeLabelPretty"
							}
						}, // see above{ passthroughMapper: { attrName:
						// "label"} },
						tooltipFontSize : 12,
						selectionColor : "#B70F1D",
						mergeWidth : {
							customMapper : {
								functionName : "widthByConfidence"
							}
						}, // see above
						mergeColor : "#2b4b60"
					}
				});

				// layout, looks different if set via draw options
				vis.layout("CompoundSpringEmbedder");

				/** * interactivity ** */
				// layout switching
				document.getElementById("resetLayout").onclick = function() {
					vis.layout("CompoundSpringEmbedder");
				};
				document.getElementById("circleLayout").onclick = function() {
					vis.layout("Circle");
				};
				document.getElementById("radialLayout").onclick = function() {
					vis.layout("Radial");
				};
				
				// links to merge/unmerge
				document.getElementById("merge").onclick = function() {
					switchMerged(true);
				};
				document.getElementById("unmerge").onclick = function() {
					switchMerged(false);
				};
				function switchMerged(merged) {
					if (vis.selected("edges").length > 0)
						resetRefs();
					var selected = vis.selected("nodes");
					if (selected.length > 0) {
						clear();
						for (i = 0, j = selected.length; i < j; ++i)
							print(selected[i].att);
					}
					vis.deselect("edges");
					vis.edgesMerged(merged);
				};
				
				// context menu for nodes
				vis.addContextMenuItem("Load new interactions from here",
						"nodes", function(evt) {
							// Get the right-clicked node and load new network
							// att:
							loadGraph(evt.target);
						});

				// listeners
				vis.addListener("dblclick", "nodes", function(event) {
					loadGraph(event.target);
				});
				vis.addListener("click", "none", function(event) { // background
					vis.deselect(); // all
					resetRefs();
				});
				vis.addListener("select", "none", function(event) { // all
					clear();
					var selected = event.target;
					for (i = 0, j = selected.length; i < j; ++i)
						print(selected[i].data);
				});
			});

	/** ******************************************************************* */
	/** ************************ drawing ********************************* */
	loadGraphFromName(nodes); //executed at startup
};