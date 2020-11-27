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
	private HashMap<String, PreparedStatement> mapStatement = new HashMap<String, PreparedStatement>();

	public appCentrale() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}

		String urlAmaury =  "jdbc:postgresql://localhost/projetSQL_2020";
		String urlAxel = "jdbc:postgresql://localhost/Projet2020";		
		this.conn = null;

		try {
			//conn = DriverManager.getConnection(urlAmaury, "postgres", "kimilapatate");
			conn = DriverManager.getConnection(urlAxel, "postgres", "axel123");
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
		int choix;
		do {
			System.out.println(" ");
			System.out.println("1 : Ajouter un local");
			System.out.println("2 : Ajouter un examen");
			System.out.println("3 : Encoder l'heure d'un examen");
			System.out.println("4 : Réserver un local pour un examen");
			System.out.println("5 : Visualiser l'horaire d'un bloc");
			System.out.println("6 : Visualiser les réservations d'un local");
			System.out.println("7 : Visualiser les examens pas complètements réservés");
			System.out.println("8 : Visualiser le nombre d'examens pas complètement réservés pour chaque bloc");
			
			choix = Integer.parseInt(scanner.nextLine());
			switch (choix) {
			case 1:
				main.ajouterLocal();
				break;
			case 2:
				main.ajouterExamen();
				break;
			case 3: 
				main.encoderHeureExamen();
				break;
			case 4:
				main.reserverLocal();
				break;
			case 5:
				main.voirHoraireBloc();
				break;
			case 6:
				main.voirReservationsLocal();
				break;
			case 7:
				main.voirExamensPasComplets();
				break;
			case 8:
				main.voirNombreExamenPasCompletsParBloc();
				break;
			}
		} while (choix > 0 && choix <9);

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
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT " + " projet.insertLocal(?, ?, ?);");
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
	
	private void encoderHeureExamen() {
		// TODO Auto-generated method stub
		
	}

	private void reserverLocal() {
		// TODO Auto-generated method stub
		
	}
	
	private void voirHoraireBloc() {
		System.out.println("\nVisualiser l'horaire d'examen d'un bloc");
		System.out.println("Code du bloc : ");
		String bloc = scanner.nextLine();
		
		System.out.println("Heure de début | Code | Nom | Nombre de locaux");
		try {	 
			PreparedStatement ps = mapStatement.get("listExamsBloc");
			
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT " + " heure_debut, code_examen, nom_examen, nbr_locaux from projet.displayExamens WHERE code_bloc LIKE (?);");
				mapStatement.put("listExamsBloc", ps);
			}
			
			ps.setString(1, bloc);
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					System.out.println(" " + rs.getTimestamp(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getInt(4) );
			} catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
			}

		} catch (SQLException se) {
			System.out.println("Erreur lors de l'affichage !");
			se.printStackTrace();
			System.exit(1);

		}
		
	}
	
	private void voirReservationsLocal() {
		System.out.println("\nVisualiser l'horaire d'examen d'un local");
		System.out.println("Code du local : ");
		String local = scanner.nextLine();
		
		System.out.println("Heure de début | Code | Nom");
		try {	 
			PreparedStatement ps = mapStatement.get("listExamsLocal");
			
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT " + " heure_debut, code_examen, nom_examen from projet.displayExamensParLocal  WHERE nom_local  LIKE (?);");
				mapStatement.put("listExamsLocal", ps);
			}
			
			ps.setString(1, local);
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					System.out.println(" " + rs.getTimestamp(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
			} catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
			}

		} catch (SQLException se) {
			System.out.println("Erreur lors de l'affichage !");
			se.printStackTrace();
			System.exit(1);

		}
		
	}
	
	private void voirExamensPasComplets() {
		System.out.println("\nVisualiser les examens pas complets");		
		System.out.println("Id Examen | Code | Nom | Id Bloc | Sur machine | Heure de début | Durée");
		try {	 
			PreparedStatement ps = mapStatement.get("listExamsPasComplets");
			
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT * from projet.displayExamensNonComplets;");
				mapStatement.put("listExamsPasComplets", ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					System.out.println(" " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getInt(4) + " | " + rs.getBoolean(5) + " | " + rs.getTimestamp(6) + " | " + rs.getString(7));
			} catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
			}

		} catch (SQLException se) {
			System.out.println("Erreur lors de l'affichage !");
			se.printStackTrace();
			System.exit(1);

		}
		
	}
	
	private void voirNombreExamenPasCompletsParBloc() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
