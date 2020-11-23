package applicationCentrale;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
		System.out.println("\nAjouter un local");
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
		System.out.println("\nAjouter un examen");
		System.out.println("Code de l'examen : ");
		String code = scanner.nextLine();
		System.out.println("Nom de l'examen : ");
		String nom = scanner.nextLine();
		System.out.println("Bloc de l'examen : ");
		String bloc = scanner.nextLine();
		System.out.println("Examen sur machines : (true|false)");
		Boolean machines = Boolean.parseBoolean(scanner.nextLine());
		System.out.println("Heure de début :");
		Timestamp debut = null;
		try {
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		    Date parsedDate = (Date) dateFormat.parse(scanner.nextLine());
		    debut = new java.sql.Timestamp(parsedDate.getTime());
		} catch(Exception e) {
			
		}
		System.out.println("Durée de l'examen : (heures)");
		int heures = Integer.parseInt(scanner.nextLine());
		System.out.println("Durée de l'examen : (minutes)");
		int minutes = Integer.parseInt(scanner.nextLine());
		
		if(heures != 0) 
			minutes += heures*60;
		
		String interval = "INTERVAL " + minutes + " MINUTES";
		
		try {
			PreparedStatement ps = mapStatement.get("insertExamen");
			if (ps == null) {
				ps = conn.prepareStatement(" SELECT projet.insertExamen(?, ?, ?, ?, ?, ?);");
				mapStatement.put("insertLocal", ps);
			}
			ps.setString(1, code);
			ps.setString(2, nom);
			ps.setString(3, bloc);
			ps.setBoolean(4, machines);
			ps.setTimestamp(5, debut);
			ps.setString(6, interval);
			
			try (ResultSet rs = ps.executeQuery()){
				if(rs.next())
					System.out.println("Votre concert a bien été ajoutée, son id est : " +rs.getInt(1) );
				} 
			catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
				}
			
		} catch (SQLException se) {
			System.out.println("Erreur lors de l'insertion !");
			se.printStackTrace();
			System.exit(1);

		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
