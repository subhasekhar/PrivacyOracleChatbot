document.getElementById("sendButton").addEventListener("click", function() {
	query();
});

var fs = require("fs");
var records = JSON.parse(fs.readFileSync("src/records.json", "utf8"));
//console.log(obj);

async function query() {
	var node = document.createElement("div");
	node.className = "message";
	current_question = document.getElementById("userQuery").value;
	var textInNode = document.createTextNode(current_question);
	document.getElementById("userQuery").value = "";
	node.appendChild(textInNode);

	//append user message
	var screendiv = document.getElementById("screen");
	screendiv.appendChild(node);

	//append line breaks
	linebreak = document.createElement("br");
	screendiv.appendChild(linebreak);
	screendiv.appendChild(linebreak);

	/*
	query LUIS asking for intent
	check top most intent confidence. if found not enough, ask user
	get domain kbid details from json
	query domain and obtain output
	if most conf. output < 60% confidence, tell user not sure
	show most confident output to user
	*/

	var luisIntentResponse = await getIntent(current_question);
	console.log(luisIntentResponse);
	var luisIntent = luisIntentResponse.prediction.topIntent;
	var luisIntentScore =
		luisIntentResponse.prediction.intents[luisIntent]["score"];
	var kbid;

	for (var i = 0; i < records["records"].length; i++) {
		var curr_rec = records["records"][i];
		if (curr_rec.intent == luisIntent) {
			kbid = curr_rec.kbid;
			break;
		}
	}
	console.log(kbid);

	var serverResponse = await getAnswer(kbid, current_question);
	console.log(serverResponse);

	//append server message
	var servernode = document.createElement("div");
	servernode.className = "servermessage";
	var serverTextInNode = document.createTextNode(
		serverResponse.answers[0].answer
	);
	servernode.appendChild(serverTextInNode);

	//append user message
	var screendiv = document.getElementById("screen");
	screendiv.appendChild(servernode);
}

async function getAnswer(kbid, current_question) {
	var question = { question: current_question, top: 1 };

	try {
		var url =
			"https://privacyoracle.azurewebsites.net/qnamaker/knowledgebases/" +
			kbid +
			"/generateAnswer";
		const data = await fetch(url, {
			method: "POST",
			headers: {
				Authorization: "EndpointKey 0195acbc-2d94-48a5-8776-4322c531f049",
				"Content-Type": "application/json"
			},
			body: JSON.stringify(question)
		});
		return data.json();
	} catch (error) {
		console.error(error);
	}
}

async function getIntent(current_question) {
	try {
		var url =
			"https://westus.api.cognitive.microsoft.com/luis/prediction/v3.0/apps" +
			"/acd7e09e-259e-47de-ac1a-16c763e43f30/slots/production/predict?verbose=true&timezoneOffset=0" +
			"&subscription-key=12d23ac95af1478984b44d5f77ec122f&query=" +
			current_question +
			"&show-all-intents=true";
		//const data = await postData(url, { answer: 42 });
		const data = await fetch(url);
		return data.json();
	} catch (error) {
		console.error(error);
	}
}
