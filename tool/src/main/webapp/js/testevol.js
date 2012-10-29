var NUMBER_OF_CATEGORIES = 8;
var versionsSummary = {};

function getCategoryLabel(index) {
	switch(index) {
		case 0:
			return 'TESTREP';
		case 1:
			return 'TESTMODNOTREP';
		case 2:
			return 'TESTDEL (AE|RE)';
		case 3:
			return 'TESTDEL (CE)';
		case 4:
			return 'TESTDEL (P)';
		case 5:
			return 'TESTADD (AE|RE)';
		case 6:
			return 'TESTADD (CE)';
		case 7:
			return 'TESTADD (P)';
	}
}

function isRepairCategory(category) {
	return category == 'TESTREP' || category == 'TESTMODNOTREP';
}

function isCoverageAwareCategory(category) {
	return category == 'TESTDEL (P)' || category == 'TESTADD (P)';

}

function createSummaryTable(summaryTable, versions, summary_data) {
	//add header
	var firstLine = document.createElement("tr");

	var line = document.createElement("tr");
	var leftMostColumn = document.createElement("td");
	leftMostColumn.innerHTML = "All Versions";
	leftMostColumn.className = "allValues";
	line.appendChild(leftMostColumn);
	appendValuesToLine(line, 0, summary_data, "total", "numbers allValues");
	summaryTable.append(line);

	var tbody = document.createElement("tbody");
	var previousVersion = versions[0];
	for(var i = 1; i < versions.length; i++) {
		var version = versions[i];
		line = document.createElement("tr");
		var versionsColumn = document.createElement("td");
		var link = document.createElement("a");
		link.href = version + "/report.html";
		link.innerText = previousVersion + ' - ' + version;
		versionsColumn.appendChild(link);
		line.appendChild(versionsColumn);
		appendValuesToLine(line, i, summary_data, version, "numbers");
		tbody.appendChild(line);
		previousVersion = version;
	}
	summaryTable.append(tbody);
}

function appendValuesToLine(line, rowNum, summary_data, version, css_classes) {
	var sum = summary_data[version][NUMBER_OF_CATEGORIES];
	for(var j = 0; j < NUMBER_OF_CATEGORIES; j++) {
		var categoryColumn = document.createElement("td");
		var value = summary_data[version][j];
		categoryColumn.innerHTML = value + " (" + getPercent(value, sum) + ")";
		categoryColumn.className = css_classes + getColumnAdditionalStyleClass(j, rowNum);
		line.appendChild(categoryColumn);
	}
}

function getColumnAdditionalStyleClass(columnIndex, rowNum) {
	switch (columnIndex) {
		case 2:
			return " testDelLeft";
		case 4:
			return " testDelRight";
	}
	return "";
}

function getPercent(val, total) {
	if(total == 0) {
		return "0.00%";
	}
	return ((val / total) * 100).toFixed(2) + "%";
}

function populateTotals(summary_data) {
	var keys = Object.keys(summary_data);
	for(var j = 0; j < keys.length; j++) {
		var key = keys[j];
		var values = summary_data[key];
		var sum = 0;
		for(var i = 0; i < values.length; i++) {
			sum += values[i];
		}
		values.push(sum);
	}
}

function writeHeader() {
	document.write("<header>TestEvol <span>{Enabling a better understanding of test-suite evolution}</span></header>");
}

function toggleLeftBar(op) {

	if(op == 'show') {
		$("#left_hidden").toggle();
		$("#accordion").css("width", "80%");
	}

	$("#left").toggle("slide", {
		direction : "left"
	}, 1000, function() {
		if(op == 'hide') {
			$("#left_hidden").toggle("slide", {
				direction : "left"
			}, 700);
			$("#accordion").css("width", "98%");
		}
	});
}

function createTable(category) {
	$("#dataTable").empty();
	$("#headerAccordion").html("Tests on category " + category);
	var packages = getCategories()[category];
	if(packages.length == 0) {
		$("#dataTable").html("<tr><td>No tests found in this category.</td></tr>")
		return;
	}
	for(var i = 0; i < packages.length; i++) {
		var info = packages[i];
		var name = info["name"];
		var classes = info["value"];
		for(var j = 0; j < classes.length; j++) {
			var className = classes[j]["name"];

			var line = document.createElement("tr");
			var col1 = document.createElement("td");
			line.appendChild(col1);
			col1.innerHTML = name + "." + className;
			line.className = "packageAndClass";
			$("#dataTable").append(line);

			var tests = classes[j]["value"]
			for(var w = 0; w < tests.length; w++) {
				line = document.createElement("tr");
				col1 = document.createElement("td");
				var testName = tests[w]['name'];
				var completeTestName = name + "." + className + "." + testName;
				if(isRepairCategory(category)) {
					var link = document.createElement("a");
					link.href = 'javascript:showCode("' + (completeTestName) + '")';
					link.innerHTML = testName;
					col1.appendChild(link);
				} else if(category == 'TESTDEL (P)') {
					if(tests[w]['good_coverage']) {
						col1.innerHTML = "<span style='float:left;padding-right:10px;'>" + tests[w]['name'] + "</span><span class='ui-state-error' style='border:none;cursor:pointer;' onclick='showCoverageLost(\"" + completeTestName + "\")'><span class='ui-icon ui-icon-alert' title='Some statements covered by this test are not covered by the new test suite.<br><br>Click for more details.' style='background-color:transparent;'></span></span>";
					} else {
						col1.innerHTML = tests[w]['name'];
					}
				} else if(category == 'TESTADD (P)') {
					if(tests[w]['good_coverage']) {
						col1.innerHTML = "<span style='float:left;padding-right:10px;'>" + tests[w]['name'] + "</span><span class='ui-state-highlight' style='border:none;cursor:pointer;'><span class='ui-icon ui-icon-star' title='This test has helped to increase the coverage of the test suite.' style='background-color:transparent;'></span></span>";
					} else {
						col1.innerHTML = tests[w]['name'];
					}
				} else {
					col1.innerHTML = tests[w]['name'];
				}
				col1.style.paddingLeft = "30px";
				line.appendChild(col1);
				$("#dataTable").append(line);

			}
		}
	}
	$(".ui-icon-alert").qtip();
	$(".ui-icon-star").qtip();
}

function populateSummaryBody() {
	populateTotals(versionsSummary);
	var summaryInfo = versionsSummary[getVersionName()];
	var total = summaryInfo[NUMBER_OF_CATEGORIES];
	for(var i = 0; i < NUMBER_OF_CATEGORIES; i++) {
		//<tr onclick="openAccordion(this)"><td>TESTREP</td><td>2&nbsp;<div id="percent">(13.33%)</div></td></tr>
		var line = document.createElement("tr");
		var col1 = document.createElement("td");
		var categoryLabel = getCategoryLabel(i);
		col1.innerText = categoryLabel;
		col1.onclick = (function(curLabel) {
			return function() {
				createTable(curLabel);
			}
		})(categoryLabel);
		var col2 = document.createElement("td");
		var value = summaryInfo[i];
		col2.innerHTML = summaryInfo[i] + "&nbsp;<div class='percent'>(" + getPercent(value, total) + ")</div>";
		line.appendChild(col1);
		line.appendChild(col2);
		$("#summaryBody").append(line);
	}
}

function showCoverageLost(testName) {
	var coverageLost = getCoverageLost()[testName];

	var info = coverageLost[0].split(',');
	var className = info[0];
	var uncoveredLine = new Number(info[1]);
	var currentClassName = className;
	var currentUncoveredLines = new Array(uncoveredLine);

	var coverageData = '';

	for(var i = 1; i < coverageLost.length; i++) {
		info = coverageLost[i].split(',');
		className = info[0];
		uncoveredLine = new Number(info[1]);
		if(currentClassName != className) {
			currentUncoveredLines.sort();
			coverageData += currentClassName+" - Line(s): "+createRanges(currentUncoveredLines) + "<br/>"
			
			currentUncoveredLines = new Array();
			currentClassName = className;
		}

		currentUncoveredLines.push(uncoveredLine);

	}
	currentUncoveredLines.sort();
	coverageData += currentClassName+" - Line(s): "+createRanges(currentUncoveredLines) + "<br/>"
	
	$("#coverage_data").html(coverageData);
	$("#dialog_coverage").dialog('open');
}

function createRanges(numbers){
	
	var beginning = numbers[0];
	var lastNumber = beginning;
	
	var ranges = new Array();
	for(var i = 1;i<numbers.length;i++){
		if(numbers[i] == lastNumber + 1){
			lastNumber = numbers[i];
			continue;
		}
		ranges.push(createRange(beginning, lastNumber));
		beginning = numbers[i];
		lastNumber = beginning;
	}
	ranges.push(createRange(beginning, lastNumber));
	return ranges;
}

function createRange(begin, last){
	if(begin==last){
		return begin;
	}
	else if(begin + 1 == last){
		return begin + ' and ' +last
	}
	return begin + '..' +last;
}

