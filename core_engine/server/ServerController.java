package filter.bayesian.server;

import filter.bayesian.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is responsible for incoming connection request from the server.
 * 
 * @author Kunal Mehrotra
 * 
 */
public class ServerController {

	public static void main(String args[]) {
		ServerSocket serverSoc = null;
		boolean serverTerminated = false;
		int portNum = 3132; // Default Port
		
		try {
			if (args.length != 0) {
				portNum = Integer.parseInt(args[0]);
			}
			serverSoc = new ServerSocket(portNum);
		} catch (IOException e) {
			System.out.println("Cannot listen on port: " + portNum);
			System.exit(-1);
		} catch (NumberFormatException e) {
			System.out.println("ERROR, Illegal arguments: " + e);
			System.exit(-1);
		}

		Socket soc = null;
		while (!serverTerminated) {
			try {
				soc = serverSoc.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new Thread(new ClientHandler(soc)).start();
		}
	}
}