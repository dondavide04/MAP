package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * La classe MultiServer gestisce le connessioni multiple con i client e
 * contiene il main del programma.
 *
 */
class MultiServer {
	/**
	 * La porta da usare per la connessione.
	 */
	private int PORT = 8080;

	/**
	 * Il costruttore della classe. Assegna la porta al server e fa partire la
	 * gestione delle connessioni da client.
	 * 
	 * @param port
	 *            La porta da usare per la connessione.
	 */
	MultiServer(int port) {
		this.PORT = port;
		run();
	}

	/**
	 * Gestisce le connessioni multiple dai client.
	 */
	void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			try {
				while (true) {
					Socket clientSocket = serverSocket.accept();
					try {
						new ServerOneClient(clientSocket);
					} catch (IOException e) {
						clientSocket.close();
						e.printStackTrace();
					}
				}
			} finally {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		new MultiServer(8080);
	}
}