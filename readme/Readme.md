# Project 1

### UDP Client-Serve Communication

To set up a connectionless client server communication, please follow the steps in order:

**Step 1**: Start the coordinator with the following code in the terminal, with **IP** & **PORT** as a command line argument:
>`java -jar coordinator.jar <COORDINATOR_IP> <COORDINATOR_PORT>`

**Step 2**: Start the participant with the following code in the terminal, with **IP** & **PORT** as a command line argument:
>`java -jar participant.jar <IP> <PORT> <COORDINATOR_IP> <COORDINATOR_PORT>`

**Step 3**: Start the client with the following code in the terminal and pass **IP** and **PORT** pairs of all the servers you want to interact with as a command line argument
>`java -jar client.jar <IP1> <PORT1> <IP2> <PORT2> ... <IPn> <PORTn>`

**Step 4:** Pass request from the client by following the instructions listed in the terminal.

**Note** Please make sure you have the `map.properties` file in the same folder as the executable jar files.

---

### Interaction Rules with the server

1. The **GET** operation accepts a `string` as an input and returns the value associated with it in the map. If no value is associated, it returns `NULL`.
2. The **PUT** operation accepts 2 arguments, name a key and a value as `string`, and stores them in the map. In case the key already exits, it rewrites the value with the latest passed argument.
3. The **DELETE** operation deletes a key-value pair from the map. If the key does not exist, the map remains unchanged.

---

### Map characteristics:

1. key-value pairs are case sensitive.
2. key-value pairs are stored as`string`
3. keys and values are trimmed of trailing spaces before storing in the map.
4. At the start of the program, following 5 key-value pairs are initially added:

| Key                | Value                                |   
|--------------------|--------------------------------------|
| MS                 | Computer Science                     |
| Firstname Lastname | John Doe                             |
| hello              | world                                |
| CS6650             | Building Scalable Distributed System |
| BTC                | Bitcoin                              |