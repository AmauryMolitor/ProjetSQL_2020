package applicationUtilisateur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class appUtilisateur {

	public static Scanner scanner = new Scanner(System.in);
	private Connection conn;
	private HashMap<String, PreparedStatement> mapStatement;

	public appUtilisateur() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}

		String url = "jdbc:postgresql://localhost/projetSQL_2020";
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(url, "postgres", "kimilapatate");
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le server !");
			System.exit(1);
		}
	}
	
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		appUtilisateur main = new appUtilisateur();
	}
	
}
