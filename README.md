# TCP Client-Server Guessing Game

A Java client-server application demonstrating TCP socket communication, request-response messaging, input validation, and connection handling.
The server generates a secret number between 1 and 100, while the client sends guesses over a TCP connection and receives feedback until the correct number is found.

# Features

-> TCP client-server communication using Java sockets
-> Server listens on 127.0.0.1:50000
-> Simple request-response messaging protocol
-> Client-side and server-side input validation
-> Random or fixed secret number for testing
-> Graceful client termination
-> Client disconnect handling
-> Server unavailable and port-conflict handling
-> Continuous server operation between client sessions

# Technologies
Java · TCP/IP · Sockets · Client-Server Architecture · Network Programming

# Architecture

GuessClient
     |
     | TCP
     | 127.0.0.1:50000
     |
     v
GuessServer

The project contains three main classes:

src/tcpgame/
├── Protocol.java
├── GuessServer.java
└── GuessClient.java

i) Protocol — Defines the host, port, number range, and messages shared between the client and server.

ii) GuessServer — Listens for connections, generates the secret number, processes guesses, and returns responses.

iii) GuessClient — Connects to the server, accepts user input, sends guesses, and displays server responses.

# Communication
The client and server exchange UTF-8 text messages over TCP.
Example:

Client                  Server

   "50"  ---------------->
          <--------------- "TOO_LOW"

   "75"  ---------------->
          <--------------- "TOO_HIGH"

   "63"  ---------------->
          <--------------- "CORRECT"

Supported protocol messages include:
TOO_LOW · TOO_HIGH · CORRECT · INVALID · QUIT · GOODBYE

# Running the Project
1. Compile
From the project directory:
javac -d out src\tcpgame\Protocol.java src\tcpgame\GuessServer.java src\tcpgame\GuessClient.java

2. Start the server
java -cp out tcpgame.GuessServer

The server listens on:
127.0.0.1:50000

3. Start the client
Open another terminal:
java -cp out tcpgame.GuessClient

Enter guesses between 1 and 100 until the correct number is found.
Enter guess: 50
Too low! Try again.

Enter guess: 75
Too high! Try again.

Enter guess: 63
Correct! You guessed the number in 3 attempts.

Type quit to end the connection early.

# Fixed Number Demo

For predictable testing, the server can be started with a fixed secret number:
java -cp out tcpgame.GuessServer 63

The client is not given the secret number; it is displayed only on the server console.

# Error Handling

The application handles several common networking scenarios:
-> Invalid or out-of-range input
-> Server unavailable
-> Unexpected client disconnection
-> Port 50000 already in use
-> Graceful client termination
-> Repeated guesses

Validation is performed on both the client and server so invalid requests are still rejected if the standard client is bypassed.

# Limitations
The current server processes one client session at a time. Additional connections may wait until the active game finishes.
A future improvement would be to use a thread pool or executor so multiple clients could be served concurrently.

# Purpose
This project was built to practice core TCP/IP and client-server networking concepts, including sockets, ports, connection establishment, request-response communication, input validation, connection lifecycle management, and network error handling.

# Author
Rayyan Azher Miswani
Bachelor of Computer Science - Dalhousie University
