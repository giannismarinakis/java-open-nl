# Open Network Library for Java Developers - Version 0.5

This is an Open-Source Library for creating network applications in Java easily. <br>
This library "fits" more on Multiplayer Game Development. 
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
# How to create a Server
First you have to import the Server script<br>
<code>import open_nl.server.Server;</code> <br><br>
The command for initializing the server is<br>
<code>Server.initialize(Object caller, int port);</code><br>
<br>
<b>Object caller:</b> You need to fill this parameter with a script object that has library's built-in methods (See more below).<br>
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
So OpenNL needs to know which script object has these methods registered, so it can find them. <br>
Passing a null parameter will lead to NullPointerException.
</blockquote>
For example, lets say that the script where you are calling the method <i>Server.initialize</i> has a built-in method like <i>onServerInitialized()</i>. The command for initializing the Server will look like this: <br><br>
<code>Server.initialize(this, 7777);</code>
<br><br>
Great! Now you have learned how to create a Server with OpenNL, very easily! <br>
