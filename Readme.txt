How to compile:
- The program will be running on medusa cluster.
- Copy files to the location that can be accessed by medusa nodes.
- Use javac *.java to compile all files.
How to run:
- cd to the path containing all the files and classes.
- Bootstrap:
	+ Run "rmiregistry &" to get a port for RMI
	+ On one of the medusa node, run "java -cp . Bootstrap" to run the Bootstrap
	+ The bootstrap IP & name will be displayed.
- Nodes/Clients:
	+ Run rmiregistry & to get a port for RMI. Important: Each client has to run "rmiregistry &" on its medusa node.
	+ Run "java -cp . Node 'IP address of the bootstrap'" to run Node.
	  Example: run "java -cp . Node medusa-node1.vsnet.gmu.edu" if Bootstrap is located on medusa node 1.
	+ A list of commands is shown:
		1. insert: ask for a peer identifier, a keyword, and a file. After a successful insertion, display information about destination node and routing path.
		2. search: ask for a peer identifier and a keyword. After a successful search, display information about destination node and routing path.
		3. view: ask for a peer identifier and display information about that Node if exists. If no peer is given, display information of all active members.
		4. join: ask for a peer identifier for the current node, then do the join.
		5. leave: leave CAN and shutdown the Node program if the node has joined CAN.