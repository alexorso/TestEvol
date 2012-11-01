var tooltips = {
		"toolTipCatTestRep":"Repaired Tests",
		"toolTipCatTestModNotRep":"Modified Tests",
		"toolTipCatTestDel":"All deleted tests",
		"toolTipCatTestDelAeRe":"Tests deleted because throw an AssertionError or RuntimeException",
		"toolTipCatTestDelCe":"Tests deleted because they the methods the API they test has become obsolete. Throw a Compilation Error when executed in the new version",
		"toolTipCatTestP":"Tests deleted that still pass in the new version. Probably redundant tests.",
		"toolTipTestAdd":"All added tests",
		"toolTipCatTestAddAeRe":"Tests added to test bug Fixes. Throw an AssertionError or RuntimeException when executed in the old version" ,
		"toolTipCatTestAddCe":"Tests added to test modifications on the API of the program.  Throw a Compilation Error when executed in the old version",
		"toolTipCatTestAddP":"Tests added that pass in the old version. Probably to improve the test suite coverage."
};

function setTooltips(){
	for (var key in tooltips) {
		$("#"+key).attr('data-original-title', tooltips[key]);
	}
}