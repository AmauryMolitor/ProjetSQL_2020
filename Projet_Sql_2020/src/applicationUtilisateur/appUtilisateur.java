package applicationUtilisateur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class appUtilisateur {

	public static Scanner scanner = new Scanner(System.in);
	private Connection conn;
	private HashMap<String, PreparedStatement> mapStatement = new HashMap<String, PreparedStatement>();
	private int idUser;

	public appUtilisateur() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}

		String url = "jdbc:postgresql://localhost/projetSQL_2020";
		this.conn = null;

		try {
			conn = DriverManager.getConnection(url, "postgres", "kimilapatate");
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le server !");
			System.exit(1);
		}
	}
	
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		appUtilisateur main = new appUtilisateur();
		
		System.out.println("1 : S'inscrire");
		System.out.println("2 : Se connecter");
		
		int choix = Integer.parseInt(scanner.nextLine());
		switch(choix) {
		case 1:
			main.inscription();
			break;
		case 2:
			main.connection();
		}
	}


	private void inscription() {
		System.out.println("\nInscription");
		System.out.println("Email : ");
		String email = scanner.nextLine();
		System.out.println("Nom d'utilisateur : ");
		String nom = scanner.nextLine();
		System.out.println("Mot de passe : ");
		String mdp = scanner.nextLine();
		System.out.println("Bloc : ");
		String bloc = scanner.nextLine();
		String sel = BCrypt.gensalt();
		String mdpCrypte = BCrypt.hashpw(mdp, sel);
		
		try {	 			
			PreparedStatement ps = mapStatement.get("inscription");
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT " + " projet.insertEtudiants(?, ?, ?, ?);");
				mapStatement.put("inscription", ps);
			}
			
			ps.setString(1, email);
			ps.setString(2, nom);
			ps.setString(3, mdpCrypte);
			ps.setString(4, bloc);
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					idUser = rs.getInt(1);
					System.out.println("Votre inscription a bien �t� r�alis�e, votre id est : " + idUser);
			} catch (SQLException se) {
				se.printStackTrace();
				System.exit(1);
			}

		} catch (SQLException se) {
			System.out.println("Erreur lors de l'inscription !");
			se.printStackTrace();
			System.exit(1);

		}
		menu();	
	}


	private void connection() {
		String mdpCrypte;
		System.out.println("\nConnexion");
		System.out.println("Entrez votre nom d'utilisateur : ");
		String nom = scanner.nextLine();
		System.out.println("Entrez votre mot de passe : ");
		String mdp = scanner.nextLine();
		
		try {	 			
			PreparedStatement ps = mapStatement.get("getEtudiant");
			if(ps == null) {
				ps = conn.prepareStatement(" SELECT e.* FROM projet.etudiants e WHERE e.nom LIKE (?) ;");
				mapStatement.put("getEtudiant", ps);
			}
			
			ps.setString(1, nom);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					mdpCrypte = rs.getString(4);
					if(!BCrypt.checkpw(mdp, mdpCrypte)) {
						System.out.println("Mot de passe incorrect!");
						connection();	
					}
					idUser = rs.getInt(1);
					System.out.println("Connexion R�ussie");
				}				
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
	
	private void menu() {
		
	}
	
}
