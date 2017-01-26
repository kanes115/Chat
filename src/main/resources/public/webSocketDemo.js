//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
webSocket.onmessage = function (msg) { updateChat(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };


//we're asking for name
var userName = "";
do {
    userName = prompt("Enter your username", "");
} while(userName == ""){
    if(userName)
        document.cookie = "username=" + userName;
}


//adding channel prompt
id("addChannel").addEventListener("click", function () {
    var newChannelName = "";
    do {
        newChannelName = prompt("Enter channel name", "");
    } while(newChannelName == ""){
        sendMessage("$Create_channel::" + newChannelName);

    }
});


//--------------------------------

//Send users message if "Send" is clicked
id("send").addEventListener("click", function () {
    sendMessage(id("message").value);
});
//Send message if enter is pressed in the input field
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendMessage(e.target.value); }
});
//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}

//--------------------------------


id("exitChannel").addEventListener("click", function (){
    sendMessage("$Change_forChannel::main chat");
})


//Update the chat-panel, and the list of connected users
function updateChat(msg) {

    var data = JSON.parse(msg.data);


    if(data.messageType == "normalMessage" || data.messageType == "normalMessageAsServer"){
        insert("chat", data.userMessage);
    }else if(data.messageType == "updateInfoMessage"){

        //update mainchat header
        id("mainchat").innerHTML = "";
        id("mainchat").innerHTML = data.currentChannel;

        id("userlist").innerHTML = "";
        data.userlist.forEach(function (user) {
            insert("userlist", "<li>" + user + "</li>");
        });

        id("channellist").innerHTML = "";

        data.channellist.forEach(function (channel) {

            insert("channellist", "<li id=" + channel + ">" + channel + "</li>");

            document.getElementById(String(channel)).addEventListener("click", function () {
                sendMessage("$Change_forChannel::" + channel);
            });
        });
    }
}


//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}