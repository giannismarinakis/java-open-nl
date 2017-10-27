# Open Network Library for Java Developers
<br>
<h2>Latest version: v0.01.1</h2>
<h2>Version under development: v0.02.1</h2>
 
This is an Open-Source Library for creating network applications in Java easily using UDP Network Protocol. <br>
<b><u>Read the LICENSE file before using this library!</u></b>
<br><br>
<b>Menu</b>
<ol>
<li><a href="#how-to-use-opennl">Installation</a></li>
<li><a href="#how-to-create-a-server">How to create a Server</a>
  <ul>
  <li><a href="#server-methods">Server Methods</a></li>
  <li><a href="#server-fields">Server Fields</a></li>
  </ul>
</li>
<li><a href="#how-to-join-a-server">How to join a Server</a>
  <ul>
  <li><a href="#client-methods">Client Methods</a></li>
  <li><a href="#client-fields">Client Fields</a></li>
  </ul>
</li>
<li><a href="#how-to-use-rpcs">How to use RPCs</a>
 <ul>
 <li><a href="#how-it-works">How it works</a></li>
 <li><a href="#sending-a-rpc">Sending a RPC</a></li>
 </ul>
</li>
</ol>
<hr> 
<h1>How to use OpenNL</h1>
<hr>
Import the OpenNL source code to your existing project, OR download the OpenNL JAR file placed in "jars/OpenNL_<i>[VERSION]</i>.jar" (With this method you will lose the access to the OpenNL source code). <br>
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
First you have to import the Server class<br>
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
So OpenNL needs to know which class object contains these methods, so it can find them. <br>
Passing a null parameter will lead to NullPointerException.
</blockquote>
For example, lets say that the class object where you are calling the method <i>Server.initialize</i> has a built-in method like <i>onServerInitialized()</i>. The command for initializing the Server will look like this: <br><br>
<code>Server.initialize(this, 7777);</code>
<br><br>
Or, if you want these built-in method(s) to be on a different class object, lets say that its named <i>object1</i>, the command for initializing the Server will look like this:
<br><br>
<code>Server.initialize(object1, 7777);</code>
<br>
<br>
<b>NOTE:</b> <i>When you want to use built-in methods, they have to be registered as above! If you add or remove (if exists) any parameters, OpenNL will not find the method(s).</i><br><br>
Great! Now you have learned how to create a Server with OpenNL, very easily! <br>
<br><br>
Here are the public fields and methods of the Server class, that are pretty useful!<br><br>
<blockquote>
<h3>Server Methods</h3>
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
<h3>Server Fields</h3>
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
<h1>How to join a Server</h1>
First, you have to import the Client class<br>

<code>import open_nl.client.Client;</code>
<br><br>
The command for connecting to a Server is<br>
<code>Client.connectTo(Object callbackScript, String ip, int port);</code><br>
<br>
<b>Object callbackScript:</b> You need to fill this parameter with a class object that has library's built-in methods (See more below).<br>
<b>String ip:</b> The IP Address of the Server you want to connect.
<br>
<b>int port:</b> The port of the Server you want to connect.

<blockquote>
<h4>What I need to fill the parameter "Object callbackScript" with?</h4>
This parameter is used to call specific built-in library's methods such as: 
<ul>
<li>onConnectedToServer()</li>
<li>onFailedToConnect()</li> 
<li>onDisconnectedFromServer()</li>
<li>onDisconnectedFromServer(String disconnectionMessage)</li>
<li>onMessageReceived(Sender sender, String data)</li>
</ul>
These methods will be called <b><u>automatically</u></b> when they have to! (<u>if they exists</u>)<br>
For example, when you connect to a Server, the method "onConnectedToServer()" will be invoked automatically. If the method does not exist, this <b>will not</b> lead to any errors or exceptions.<br>
So OpenNL needs to know which class object contains these methods, so it can find them. <br>
Passing a null parameter(for "Object callbackScript") will lead to NullPointerException.
</blockquote>
For example, lets say that the class object where you are calling the method <i>Client.connectTo</i> has a built-in method like <i>onConnectedToServer()</i>. The command for connecting to the Server will look like this: <br><br>
<code>Client.connectTo(this, "localhost", 7777);</code>
<br><br>
Or, if you want these built-in method(s) to be on a different class object, lets say that its named <i>object1</i>, the command for connecting to the Server will look like this:
<br><br>
<code>Client.connectTo(object1, "localhost", 7777)</code>
<br><br>
<b>NOTE:</b> <i>When you want to use built-in methods, they have to be registered as above! If you add or remove (if exists) any parameters, OpenNL will not find the method(s).</i><br><br>
Great! Now you have learned how to connect to a Server with OpenNL!! <br>
<br>



Here are the public fields and methods of the Client class, that are pretty useful!<br><br>
<blockquote>
<h3>Client Methods</h3>
<hr>
<code><b>disconnect() : void</b></code> Disconnects the Client from the connected Server.
<br><br>
<code><b>getSocket() : DatagramSocket</b></code> Returns the Client's datagram socket.
<br><br>
<code><b>sendDataTo(String ip, int port, byte[] data) : void</b></code> Sends data to the specified address and port, from a byte array.
<br><br>
<code><b>sendDataTo(String ip, int port, DatagramPacket packet) : void</b></code> Sends a datagram packet to the specified address and port.
<br><br>
<h3>Client Fields</h3>


<hr>
<code><b>isConnected : boolean</b></code> True if the Client is connected to a Server, false if not.
<br><br>
<code><b>serverIP : String</b></code> The IP Address of the Server that the Client is connected to.
<br><br>
<code><b>serverPort : int</b></code> The Port of the Server that the Client is connected to.
<br><br>
<code><b>id : int</b></code> The <b>unique</b> ID of the Client. This ID is generated from the Server only.
<br><br>
<code><b>receive_buffer : int</b></code> The size of the receive data buffer in bytes.
<br><br>
<code><b>connectTimeout : int</b></code> The maximum time it takes the Client to connect to a Server, in milliseconds. Default is 8000 milliseconds.
<br><br>
<code><b>connectedClients : ArrayList(type: open_nl.server.SClient)</b></code> A ArrayList of type SClient, that contains all the connected Clients. This ArrayList is filled <b>only</b> if the Server field <code>Server.syncClients</code> is set to <code>true</code>.
</blockquote>
<hr>
<h1>How to use RPCs</h1>
<hr>
<blockquote>
<h3>How it works</h3>
RPC stands for "Remote Procedure Call". 
<br>Its job is to call the same method on one or more remote clients (or Server).
<br>The method's name can be anything, but the parameters of the method are limited to some class types. The first parameter <b>needs</b> to be a class type of "Sender" so the receiver can know who did the RPC. 
<br><br>Here are the class types that this version of OpenNL supports:
<ol>
<li>Object
<li>boolean
<li>char
<li>String
<li>int
<li>short
<li>double
<li>long
<li>float
</ol>
<br>
 
<blockquote>
<b><u>IMPORTANT:</u></b> The RPC class needs to know all the class objects that have atleast one or more RPC methods, so it can access those objects and called the specified method.
<br>In order to do that, you need to call before you connect to a server or before you start a server this static method from the RPC class:<br>
<br><code><b>enableRPCfor(Object... caller) : void</b></code>
<br><br>In the <i>caller</i> parameter you pass the class object(s) that has/have one or more RPC methods.<br>
For example, if I want my client to receive RPC methods in the same class object I can call the <i>enableRPCfor</i>
 method like this: <b>RPC.enableRPCfor(this);</b></blockquote>
</blockquote>

<h3>Sending a RPC</h3>
<blockquote>
The static method for sending a RPC is:<br><br>
<code><b>RPC.send(RPCMode mode, String methodName, Object... arguments) : void</b></code>
<br><br>
<b>What is the <i>RPCMode</i> parameter?</b>
<br>
The RPCMode parameter is a enum that consists from 3 different values: <br>
<blockquote>
<ul>
<li>All
<li>Server
<li>Others
</ul>
</blockquote>
<br>
And its job is to inform the RPC.send(...) method where the RPC is going to be sent.<br>
<br>
For example, if we pass the argument <i><b>RPCMode.All</b></i> in the RPC.send(...) method, the specified method will be called to everyone (clients & server). 
<br>With the same reasoning, the argument <i><b>RPCMode.Server</b></i> informs the RPC.send(...) method that the specified method will be called only on the Server and the argument <i><b>RPCMode.Others</b></i> informs the RPC.send(...) method that the specified method will be called to everyone except the one who is calling the RPC.send(...).
</blockquote>
