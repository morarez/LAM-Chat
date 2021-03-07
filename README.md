# LAM-Chat
LAM-Chat is a chatroom application with a fault-tolerant server. This application was developed using Java and Erlang. The dependencies in the java project were managed through maven and the communication between Java and Erlang was handled through the Jinterface.
# Usage
## Running the server
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
## Running the client
The client can be started through the main method of the LAM class but it will work only if the server mailbox in LAM matches the name of the server shell. Then there is a set of commands which the user can use to interact with the application:
* x: leave the application if not in any chatroom or leave the chatroom
* send: send a message ---> r: to a room, p: private message
* show: show the users in the room
