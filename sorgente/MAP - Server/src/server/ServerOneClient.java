package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

import data.Data;
import data.OutOfRangeSampleSize;
import database.DatabaseConnectionException;
import database.EmptySetException;
import database.EmptyTypeException;
import database.NoValueException;
import mining.KMeansMiner;

/**
 * La classe ServerOneCliente gestisce il thread per la connessione al singolo
 * client.
 */
class ServerOneClient extends Thread {
	/**
	 * La socket per la connessione al client.
	 */
	private Socket socket;
	/**
	 * Lo stream di input dei dati client/server.
	 */
	private ObjectInputStream in;
	/**
	 * Lo stream di output dei dati client/server.
	 */
	private ObjectOutputStream out;
	/**
	 * Il gestore del clustering.
	 */
	private KMeansMiner kmeans;

	/**
	 * Il costruttore della classe. Inizializza la connessione e gli stream e fa
	 * partire il thread.
	 * 
	 * @param s
	 *            Socket per la connessione al client.
	 * @throws IOException
	 */
	private ServerOneClient(Socket s) throws IOException {
		this.socket = s;
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		start();
	}

	/**
	 * Implementa l'interfacciamento con i comandi ricevuti dal client.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		Integer command;
		Data data = null;
		while (true) {
			try {
				command = (Integer) in.readObject();
				switch (command) {
				case 0:
					try {
						data = new Data((String) in.readObject());
						out.writeObject("OK");
					} catch (ClassNotFoundException | IOException | NoValueException | DatabaseConnectionException
							| SQLException | EmptySetException | NullPointerException | ClassCastException e) {
						try {
							out.writeObject("Errore nell'acquisizione della tabella!");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					} catch (EmptyTypeException e) {
						out.writeObject("La tabella contiene tipi non gestiti!");
						e.printStackTrace();
					}
					break;
				case 1:
					try {
						kmeans = new KMeansMiner((Integer) in.readObject());
						int iterations = kmeans.kmeans(data);
						out.writeObject("OK");
						out.writeObject(iterations);
						out.writeObject(kmeans.getC().toString(data));
					} catch (ClassNotFoundException | IOException | OutOfRangeSampleSize e) {
						try {
							out.writeObject(
									"Operazione non riuscita!\nControllare che sia stato inserito un numero di cluster valido");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
					break;
				case 2:
					try {
						kmeans.salva((String) in.readObject());
						out.writeObject("OK");
					} catch (IOException e) {
						try {
							out.writeObject("Operazione di salvataggio non riuscita!");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
					break;
				case 3:
					try {
						String[] fileName = (String[]) in.readObject();
						kmeans = new KMeansMiner(fileName[0] + fileName[1] + ".dat");
						data = new Data(fileName[0]);
						out.writeObject("OK");
						out.writeObject(kmeans.getC().toString(data));
					} catch (ClassNotFoundException | IOException | NoValueException | DatabaseConnectionException
							| SQLException | EmptySetException | EmptyTypeException e) {
						try {
							out.writeObject("Operazione non riuscita!");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
					break;
				}
			} catch (ClassNotFoundException | IOException e2) {
				e2.printStackTrace();
				break;
			}
		}
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
