package mining;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data.Data;
import data.OutOfRangeSampleSize;
import database.DatabaseConnectionException;
import database.EmptySetException;
import database.EmptyTypeException;
import database.NoValueException;

/**
 * Classe di test per la classe KMeansMiner.
 *
 */
public class KMeansMinerTest {
	/**
	 * KMeansMiner di test.
	 */
	private static KMeansMiner kmeans;

	/**
	 * Inizializza prima di ogni test kmeans con un numero di cluster pari a 5.
	 * Questa configurazione verrà usata per i test come caso generico.
	 * 
	 */
	@BeforeEach
	void setUpEach() {
		try {
			kmeans = new KMeansMiner(5);
		} catch (OutOfRangeSampleSize e) {
			fail();
		}
	}

	/**
	 * Test su funzione carica.
	 */
	@Test
	void caricaTest() {
		try {
			kmeans.kmeans(new Data("playtennis"));
			kmeans.salva("prova.dat");
			assertAll("Test caricamento", () -> {
				assertThrows(FileNotFoundException.class, () -> new KMeansMiner("false"), "Test caricamento fallito");
				assertEquals(kmeans.getC().toString(), new KMeansMiner("prova.dat").getC().toString(),
						"Test caricamento fallito");
			});
		} catch (IOException | OutOfRangeSampleSize | NoValueException | DatabaseConnectionException | SQLException
				| EmptySetException | EmptyTypeException e) {
			fail();
		}
	}

	/**
	 * Test su funzione kmeans corretto.
	 */
	@Test
	void kmeansTest() {
		try {
			assertTrue(kmeans.kmeans(new Data("playtennis")) > 0, "Test kmeans fallito");
		} catch (OutOfRangeSampleSize | NoValueException | DatabaseConnectionException | SQLException
				| EmptySetException | EmptyTypeException e) {
			fail();
		}
	}

	/**
	 * Test su funzione kmeans con input errato.
	 */
	@Test
	void kmeansTestWrong() {
		assertThrows(OutOfRangeSampleSize.class, () -> new KMeansMiner(16).kmeans(new Data("playtennis")),
				"Test kmeans fallito");
	}
}
