# KtoRC
###### An IRC Client-Server Project

### Class Information
> **PSU Spring 2021**  
> This is the final project for CS 594 - Internetworking Protocols  

### How to use
This project was made with the intention being that users are just people testing out
the IRC client to see that it works. As a result, not a lot of work has been put into
build processes.  

__Assumption: users are using Intellij to run the project (only confirmed working env)__

1. Clone the repo and open it in Intellij IDEA
2. Press the green play button next to main in src/main/kotlin/com/ktorc/server/Application.kt
3. Press the green play button next to main in src/main/kotlin/com/ktorc/client/ChatClient.kt 
   1. To emulate multiple users, click on "Edit Configurations" in the dropdown at the top-right
        of the page. Then, enable parallel runs for the ChatClient by clicking the check box in the top
        right of the popup.
      
4. Mess around.

### Commands

#### Note on Commands

The server supports several 'commands'. These are well-known SCREAMING_SNAKE_CASE phrases which can
appear at any point in a message. Frames with these are assumed to be "Command Frames" and will not be
broadcast to the rooms a client may be in.

#### List of Supported Commands
Prefix all commands you wish to use with "cm&"; for example, "cm&JOIN_ROOM".
- _CREATE_ROOM  [new room name]_ - Create new chat room (doesn't auto-join)
- _JOIN_ROOM [room name]_ - Join a chat room (Does nothing if the provided room doesn't exist)
- _LEAVE_ROOM [room name]_ - Leave a chat room (Same as previous command)
- _LIST_ROOMS_ - List all available rooms in the server
- _HERE_ - List all users in the rooms you currently occupy

