package applicationCentrale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class appCentrale {

	public static Scanner scanner = new Scanner(System.in);
	private Connection conn;
	private HashMap<String, PreparedStatement> mapStatement;

	public appCentrale() {
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
		appCentrale main = new appCentrale();

		System.out.println("Bienvenue dans le Projet de Gestion de Base de Données 2020");
		System.out.println();
		System.out.println("Faites votre choix 😡 ");
		do {
			System.out.println("1 : Ajouter un local");
			System.out.println("2 : Ajouter un examen");

			int choix;
			choix = Integer.parseInt(scanner.nextLine());
			switch (choix) {
			case 1:
				main.ajouterLocal();
				break;
			case 2:
				main.ajouterExamen();
				break;
			}

		} while (true);

	}

	private void ajouterLocal() {
		System.out.println("\n Ajouter un local");
		System.out.println("Nom du local : ");
		String nom = scanner.nextLine();
		System.out.println("Nombre de place : ");
		int nbrPlace = Integer.parseInt(scanner.nextLine());
		System.out.println("Le local possède-t-il des machines : (true|false)");
		Boolean machines = Boolean.parseBoolean(scanner.nextLine());

		try {	 
			PreparedStatement ps = mapStatement.get("insertLocal");
			if (ps == null) {
				ps = conn.prepareStatement(" SELECT projet.insertLocal(?, ?, ?);");
				mapStatement.put("insertLocal", ps);
			}

			ps.setString(1, nom);
			ps.setInt(2, nbrPlace);
			ps.setBoolean(3, machines);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					System.out.println("Votre local a bien été ajouté, son id est : " + rs.getInt(1));
			} catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
			}

		} catch (SQLException se) {
			System.out.println("Erreur lors de l'insertion !");
			se.printStackTrace();
			System.exit(1);

		}
	}
	
	private void ajouterExamen() {
		try {
			PreparedStatement ps = mapStatement.get("insertExamen");
			if (ps == null) {
				ps = conn.prepareStatement(" SELECT projet.insertExamen(?, ?, ?, ?, ?, ?);");
				mapStatement.put("insertLocal", ps);
			}
		} catch (SQLException se) {
			System.out.println("Erreur lors de l'insertion !");
			se.printStackTrace();
			System.exit(1);

		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
