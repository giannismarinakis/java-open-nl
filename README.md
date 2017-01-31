# Open Network Library for Java Developers - Version 0.5

This is an Open-Source Library for creating network applications in Java easily using UDP Network Protocol. <br>
This library "fits" more on Multiplayer Game Development. <br><br>
<b>Read the LICENSE file before using the plugin!</b>
<br><br>
<b>Menu</b>
<ol>
<li><a href="#how-to-create-a-server">How to create a Server</a>
<ul>
<li>ds</li>
</ul>
</li>
</ol>
<hr> 
# How to use OpenNL
<hr>
First you need to import the OpenNL source code to your existing project, or you can export the OpenNL as a JAR file and import to your project. <br>
<h4>How to import OpenNL JAR file to Eclipse project</h4>
<ol>
<li>Right click on your project's folder > New > Folder</li>
<li>Create a folder inside your project's folder where you will import the JAR file, I will name this folder: lib</li>
<li>Drag and drop the OpenNL JAR file to the folder you just created (mine is lib)<br>A "File Operation" window will popup. Select "Copy files" and click OK.</li>
<li>Right click on your project's folder > Build Path > Configure Build Path...</li>
<li>Select Libraries tab > Add JARs > Select the OpenNL JAR file that you've just dropped > Click OK.</li>
<li>Finally click Apply and then OK.</li>
</ol>
Now you should have successfully imported OpenNL to your project.
<hr>
<h1>How to create a Server</h1>
First you have to import the Server script<br>
<code>import open_nl.server.Server;</code> <br><br>
The command for initializing the server is<br>
<code>Server.initialize(Object caller, int port);</code><br>
<br>
<b>Object caller:</b> You need to fill this parameter with a class object that has library's built-in methods (See more below).<br>
<b>int port:</b> The port that the server will listening to.

<blockquote>
<h4>What I need to fill the parameter "Object caller" with?</h4>
This parameter is used to call specific built-in library's methods such as: 
<ul>
<li>onServerInitialized()</li>
<li>onClientConnect(SClient client)</li>
<li>onClientDisconnect(SClient client)</li>
</ul>
These methods will be called <b><u>automatically</u></b> when they have to! (<u>if they exists</u>)<br>
For example, when a client connects to our server, the method "onClientConnect" will be invoked automatically. If the method does not exist, this <b>will not</b> lead to any errors or exceptions.<br>
So OpenNL needs to know which class object has these methods registered, so it can find them. <br>
Passing a null parameter will lead to NullPointerException.
</blockquote>
For example, lets say that the class object where you are calling the method <i>Server.initialize</i> has a built-in method like <i>onServerInitialized()</i>. The command for initializing the Server will look like this: <br><br>
<code>Server.initialize(this, 7777);</code>
<br><br>
Or, if you want these built-in method(s) to be on a different class object, lets say that its named <i>object1</i>, the command for initializing the Server will look like this:
<br><br>
<code>Server.initialize(object1, 7777);</code>
<br>
Great! Now you have learned how to create a Server with OpenNL, very easily! <br>
<br><br>
Here are the public fields and methods of the Server class, that are pretty useful!<br><br>
<blockquote>
<h3>Methods</h3>
<hr>
<code><b>shutdown(String message) : void</b></code> Shuts down the Server and sends a disconnection message to all the connected client. Leave empty String if you don't want any message.
<br><br>
<code><b>setReceiveBufferSize(int size) : void</b></code> Sets the size of the receive data buffer in bytes. Default size in 1024 bytes.
<br><br>
<code><b>getSocket() : DatagramSocket</b></code> Returns the datagram socket that the Server is using.
<br><br>
<code><b>sendToAll(String data, SClient except) : void</b></code> Sends data to all connected clients, except the client that you pass in the second parameter. Leave the second parameter null, if you want to send the message to all the connected clients with no exception. (The SClient class will be explained above)
<br><br>
<code><b>csend(int clientID, String data) : String</b></code> Sends data to a specific client by the given ID. It returns the result of the send proccess (Empty string "" if success, error as String if failure).
<h3>Fields</h3>
<hr>
<code><b>hosting : boolean</b></code> True if the Server is hosting, false if the Server is not hosting.
<br><br>
<code><b>syncClients : boolean</b></code> Set to <code>true</code> if you want each time a client connects or disconnects, to inform all the connected clients about the new connection/disconnection, else <code>false</code>.
<br><br>
<code><b>port : int</b></code> The port that the Server is listening to.
<br><br>
<code><b>receive_buffer : int</b></code> The size of the receive data buffer in bytes.
</blockquote>
<hr>
