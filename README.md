# Open Network Library for Java Developers
<br>
<h2>Latest version: v0.01.3</h2> 

This is an Open-Source Library for creating network applications in Java easily using UDP Network Protocol. <br><br>
<b><u>Read the LICENSE file before using this library!</u></b><br><br>
<b>Note</b> that this library uses the UDP protocol. It is recommended to use this library on applications where the loss of packets does not matter (Voice transfer applications, video calls, multiplayer games, etc...)
<br><br>

<h2>With this library you can</h2>
<ul>
  
  <li>Instantiate a server
    <ul>
      <li>Store and manage incoming connections (Clients)
      <li>Send data to a specific client or a group of clients
      <li>Analyzes and sends the incoming RPCs to the proper clients
    </ul>
  </li>
  
   <li>Connect to a server 
    <ul>
      <li>Send data to a specific client, a group of clients or the server
      <li>Executes or sends RPCs
    </ul>
  </li>
    
  <li>Send / receive RPCs
    <ul>
      <li>You can set a Group ID for your RPC class, and you will receive only the RPCs that has the same group ID as yours
      <li>Send RPC to All, Others (everyone except you), Server
      <li>Send RPC to a Sender (When you receive a RPC you can send directly back to the Sender of this RPC) or a Client
      <li>Send RPC to a specific group
    </ul>
  </li> 
    
</ul>
