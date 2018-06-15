package client;

/**
 * La classe ServerException modella l'eccezione: "eccezione dal server".
 */
public class ServerException extends Exception {
	ServerException(String error) {
		super(error);
	}
}
