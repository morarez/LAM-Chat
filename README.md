# LAM-Chat
LAM-Chat is a chatroom application with a fault-tolerant server. This application was developed using Java and Erlang. The dependencies in the java project were managed through maven and the communication between Java and Erlang was handled through the Jinterface.
## Usage
### Running the server
The server should be run through the erlang shell. First of all, the shell needs to be renamed and the name of the server mailbox should be modified in the LAM class of the client. This is done through running this command on a linux terminal
```
erl -sname server
```
After that, the backup module and the server_sup module need to be compiled through the following commands
```
c(backup).
```
and
```
c(server_sup).
```
Then, the backup server should be started through the command
```
backup:start_link().
```
Finally, the supervisor should be started by running
```
server_sup:start_link().
```
### Running the client
The client can be started through the main method of the LAM class but it will work only if the server mailbox in LAM matches the name of the server shell. Then there is a set of commands which the user can use to interact with the application:
* x: leave the application if not in any chatroom or leave the chatroom
* send: send a message ---> r: to a room, p: private message
* show: show the users in the room
## System Architecture
Our system was designed following the client-server architecture because we allow the communication between the different clients only through the server. The server accepts different messages from the client side and performs an action accordingly. As for the client, it contains the necessary data structures that would enable it to store messages locally and it serves as a container for an abstract user within a chatroom. The following diagram is a general overview of our system architecture.

<img width="846" alt="Screen Shot 2021-03-07 at 22 35 19" src="https://user-images.githubusercontent.com/41535744/110251388-708bd900-7f95-11eb-921a-543bb9b0404a.png">

### Server
As mentioned above, the server’s job is to receive different messages from the different clients and it should perform an action based on the received message. The server was written purely in erlang using the gen_server behavior because it provides fast response and it is well suited for our message passing model. It is also thread safe and so there are no race conditions when more than one process tries to modify something. Our server consists mainly of a loop and a simple list that keeps track of the current room and users. The loop waits until a message is received by the server.
### Client
The client is a way to encapsulate the behavior of a user within a chat room as mentioned above. It contains the user object as well as the chat room object to keep track locally of all the messages sent on the chat room and to also keep track of the currently online users in the room. It also contains the needed structures to construct a client node that is able to communicate with the server. Any given client node has three main processes; the advertiser, the sender and the receiver. The advertiser’s job is to inform the server of the presence or the absence of the user that is hosted on that client node. It also constructs the chat room locally upon receiving from the server that the client is added to the room. It does this by creating the list of online users and by setting up the room name locally. The sender’s job, however, is to send a message to the server containing something to be sent either to the room or to a certain user in the room. As for the receiver, all it does is that it tells the server where to send messages to the user hosted on the client node and then it starts receiving those messages upon their arrival. The advertiser process starts with the creation of the client node and it terminates when the user is successfully removed from the room when he/she leaves the room. The receiver process starts immediately after the client is accepted by the server and it is terminated once the user leaves the chat room. The sender process, however, starts once the user decides to send a message and it terminates upon receiving a confirmation message from the server.
## Fault Tolerance
Since we are working in a distributed environment, faults are inevitable and that is why it was necessary to handle them in a proper way. We assume that we have only one server that provides the service to all the clients and so, when this server is down, we need to make sure that the information stored on that server is not lost. In other words, our goal is to make the server pick up from where it has left the work. This is achieved through the supervisor node which restarts the server upon its failure so that the server can restart itself by restoring the list of tuples from the backup node. First, the server will be sending any modification to the backup node. Then, upon failure, the supervisor will restart the server which then sends a recovery message to the backup node which, in turn, replies back to the server with the list of clients. In that way, the application will continue to work despite the failure of the server.
