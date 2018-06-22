package client;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * La classe KMeans gestisce l'interfaccia grafica, le richieste da parte
 * dell'utente da inviare al server e visualizza i risultati.
 *
 */
public class KMeans extends JFrame {
	/**
	 * Lo stream di output dei dati client/server.
	 */
	private ObjectOutputStream out;
	/**
	 * Lo stream di input dei dati client/server.
	 */
	private ObjectInputStream in;

	/**
	 * Inizializza la componente grafica e avvia la richiesta di connessione al
	 * server.
	 * 
	 * @param ip
	 *            L'indirizzo ip del server.
	 * @param port
	 *            La porta del server.
	 * @throws IOException
	 * 
	 */
	public void init(String ip, int port) throws IOException {
		Container cp = getContentPane();
		TabbedPane tabbed = new TabbedPane();
		cp.setLayout(new GridLayout(1, 1));
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("DB", tabbed.panelDB);
		tab.addTab("File", tabbed.panelFile);
		cp.add(tab);
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println(socket);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					System.exit(0);
				}
			}
		});

		setSize(750, 500);
		setVisible(true);
	}

	/**
	 * La inner class TabbedPane gestisce i due pannelli principali
	 * dell'interfaccia.
	 *
	 */
	private class TabbedPane extends JPanel {
		/**
		 * Il pannello per le operazioni da database.
		 */
		private JPanelCluster panelDB;
		/**
		 * Il pannello per le operazioni da file.
		 */
		private JPanelCluster panelFile;

		/**
		 * Il costruttore della classe.
		 */
		private TabbedPane() {
			panelDB = new JPanelCluster("MINE", e -> {
				try {
					learningFromDBAction();
				} catch (ClassNotFoundException | IOException e1) {
					JOptionPane.showMessageDialog(this, "Operazione non riuscita!");
				} catch (ServerException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage());
				}
			});

			panelFile = new JPanelCluster("STORE FROM FILE", e -> {
				try {
					learningFromFileAction();
				} catch (ClassNotFoundException | IOException | ServerException e1) {
					JOptionPane.showMessageDialog(this, "Operazione non riuscita!");
				}
			});

			this.add(panelFile);
			this.add(panelDB);
		}

		/**
		 * Invia al server i comandi necessari per acquisire i dati da file. Mostra
		 * sull'interfaccia l'esito positivo o negativo del comando.
		 * 
		 * @throws SocketException
		 * @throws IOException
		 * @throws ClassNotFoundException
		 * @throws ServerException
		 */
		private void learningFromFileAction()
				throws SocketException, IOException, ClassNotFoundException, ServerException {
			out.writeObject(3);
			String tabName = this.panelFile.tableText.getText();
			String k = this.panelFile.kText.getText();
			String[] fileName = {tabName, k};
			out.writeObject(fileName);
			//out.writeObject(tabName);
			String result = (String) in.readObject();
			if (result.equals("OK")) {
				JOptionPane.showMessageDialog(this, "Operazione riuscita!");
				this.panelFile.clusterOutput.setText((String) in.readObject());
			} else {
				throw new ServerException(result);
			}
		}

		/**
		 * Invia al server i comandi necessari per acquisire i dati da database. Mostra
		 * sull'interfaccia l'esito positivo o negativo del comando.
		 * 
		 * @throws SocketException
		 * @throws IOException
		 * @throws ClassNotFoundException
		 * @throws ServerException
		 */
		private void learningFromDBAction()
				throws SocketException, IOException, ClassNotFoundException, ServerException {
			out.writeObject(0);
			String tabName = panelDB.tableText.getText();
			out.writeObject(tabName);
			String result = (String) in.readObject();
			if (!result.equals("OK")) {
				throw new ServerException(result);
			}
			int k;
			try {
				k = new Integer(panelDB.kText.getText()).intValue();
				out.writeObject(1);
				out.writeObject(k);
			} catch (NumberFormatException e) {
				throw new ServerException("Numero cluster non valido!");
			}
			result = (String) in.readObject();
			if (!result.equals("OK")) {
				throw new ServerException(result);
			}
			panelDB.clusterOutput.setText(
					"Numero iterazioni: " + ((Integer) in.readObject()).toString() + "\n" + (String) in.readObject());
			out.writeObject(2);
			out.writeObject(tabName + k + ".dat");
			result = (String) in.readObject();
			if (!result.equals("OK")) {
				throw new ServerException(result);
			} else {
				JOptionPane.showMessageDialog(this, "Operazione di salvataggio riuscita!");
			}
		}

		/**
		 * La inner class JPanelCluster gestisce un singolo pannello dell'interfaccia.
		 */
		private class JPanelCluster extends JPanel {
			/**
			 * Area di testo in cui scrivere il nome della tabella.
			 */
			private JTextField tableText = new JTextField(20);
			/**
			 * Area di testo in cui scrivere il numero di cluster da calcolare.
			 */
			private JTextField kText = new JTextField(10);
			/**
			 * Area di testo in cui mostrare il risultato.
			 */
			private JTextArea clusterOutput = new JTextArea();
			/**
			 * Bottone per l'esecuzione del comando.
			 */
			private JButton executeButton;

			/**
			 * Il costruttore della classe.
			 * 
			 * @param buttonName
			 *            L'etichetta del bottone.
			 * @param a
			 *            Ascoltatore del bottone.
			 */
			private JPanelCluster(String buttonName, ActionListener a) {
				super(new GridLayout(3, 1));
				JPanel up = new JPanel(new FlowLayout(FlowLayout.CENTER));
				up.add(new JLabel("Tabella:"));
				up.add(tableText);
				up.add(new JLabel("Numero cluster:"));
				up.add(kText);
				this.add(up);
				clusterOutput.setEditable(false);
				JScrollPane mid = new JScrollPane(clusterOutput);
				this.add(mid);
				JPanel down = new JPanel(new FlowLayout(FlowLayout.CENTER));
				executeButton = new JButton(buttonName);
				down.add(executeButton);
				executeButton.addActionListener(a);
				this.add(down);
				this.setVisible(true);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		KMeans test = new KMeans();
		test.init(args[0],new Integer(args[1]));
	}
}
