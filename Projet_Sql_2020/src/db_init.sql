DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet; 

CREATE TABLE projet.formations(
	form_id SERIAL PRIMARY KEY,
	nom VARCHAR(100) CHECK (nom<>'') NOT NULL,
	ecole VARCHAR(100) CHECK (ecole <>'') NOT NULL
);

--Blocs
CREATE TABLE projet.blocs(
	bloc_id SERIAL PRIMARY KEY,
	code VARCHAR(50) UNIQUE NOT NULL CHECK(code <>''),
	formation INT NOT NULL,
	nbr_exam_non_complet INT NOT NULL DEFAULT 0,
	FOREIGN KEY(formation) REFERENCES projet.formations(form_id)
);

--Examens
CREATE TABLE projet.examens(
	examen_id SERIAL PRIMARY KEY,
 	code VARCHAR(6) UNIQUE NOT NULL CHECK(code SIMILAR TO 'IPL[0-9]{3}'),
 	nom VARCHAR(50) NOT NULL,
 	bloc INT NOT NULL,
 	sur_machine BOOLEAN NOT NULL,
 	heure_debut TIMESTAMP NULL,
 	duree INTERVAL NOT NULL,
 	completement_reserve BOOLEAN NOT NULL DEFAULT FALSE,
 	FOREIGN KEY(bloc) REFERENCES projet.blocs(bloc_id)
);

CREATE TABLE projet.etudiants(
	etudiant_id SERIAL PRIMARY KEY,
	nom VARCHAR(100) CHECK (nom<>'') NOT NULL,
	email VARCHAR(100) CHECK (email SIMILAR TO '%_@_%._%') NOT NULL UNIQUE,
	mdp VARCHAR(100) CHECK (mdp<>'') NOT NULL,
	bloc INTEGER REFERENCES projet.blocs(bloc_id) NOT NULL
);

CREATE TABLE projet.inscriptions(
	examen_id INTEGER REFERENCES projet.examens(examen_id) NOT NULL,
	etudiant_id INTEGER REFERENCES projet.etudiants(etudiant_id) NOT NULL,
	PRIMARY KEY (examen_id, etudiant_id)
);

CREATE TABLE projet.locaux(
	local_id SERIAL PRIMARY KEY,
	nom VARCHAR(100) CHECK (nom<>'') NOT NULL UNIQUE,
	nb_places INTEGER NOT NULL,
	machines BOOLEAN
);

CREATE TABLE projet.reservations(
	local_id INTEGER REFERENCES projet.locaux(local_id) NOT NULL,
	examen_id INTEGER REFERENCES projet.examens(examen_id) NOT NULL,
	PRIMARY KEY (local_id, examen_id)
);




--INSERT-------------------------------------------------------------

CREATE OR REPLACE FUNCTION projet.insertLocal(_nom VARCHAR(100), _nb_places INTEGER, _machines BOOLEAN) RETURNS INTEGER AS $$
DECLARE
	l_id INTEGER;
BEGIN
	INSERT INTO projet.locaux
	VALUES (DEFAULT, _nom, _nb_places, _machines) RETURNING local_id into l_id;
	RETURN l_id;
END;
$$ LANGUAGE plpgsql;

SELECT projet.insertLocal('B021', 100, false);



CREATE OR REPLACE FUNCTION projet.insertExam(_code VARCHAR(6), _nom VARCHAR(100), _bloc VARCHAR, _sur_machine BOOLEAN, _heure_debut TIMESTAMP, 
						_duree INTERVAL, _completement_reserve BOOLEAN) RETURNS INTEGER AS $$
DECLARE 
	e_id INTEGER;
	b_id INTEGER;
BEGIN

	SELECT bloc_id 
	FROM projet.blocs b
	WHERE b.code LIKE _bloc into b_id;

	INSERT INTO projet.examens
	VALUES (DEFAULT, _code, _nom, b_id, _sur_machine, _heure_debut, _duree, _completement_reserve) RETURNING examen_id into e_id;
	RETURN e_id;
END;
$$ LANGUAGE plpgsql;

SELECT projet.insertExam('IPL126', 'examen de sql', 'BIN3', true, '2020-12-12 10:00:00', INTERVAL '3' HOUR, false);



CREATE OR REPLACE FUNCTION projet.insertEtudiants(_email VARCHAR, _nom VARCHAR, _mdp VARCHAR, _bloc VARCHAR) RETURNS INTEGER AS $$
DECLARE
	e_id INTEGER;
	b_id INTEGER;
BEGIN
	SELECT bloc_id 
	FROM projet.blocs b
	WHERE b.code LIKE _bloc into b_id;
	
	INSERT INTO projet.etudiants
	VALUES (default, _nom, _email, _mdp, b_id) RETURNING etudiant_id into e_id;
	RETURN e_id;
END;
$$ LANGUAGE plpgsql;

SELECT projet.insertEtudiants('a@gmail.com', 'etudiant1', 'mdp', 'BIN3')


--EN DOUBLE----------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION projet.insertInscription(_etudiant_id INTEGER, _code_examen VARCHAR) RETURNS INTEGER AS $$
DECLARE
	i_id INTEGER;
	e_id INTEGER;
BEGIN
	SELECT e.examen_id
	FROM projet.examens e
	WHERE e.code LIKE _code_examen into e_id;
	
	INSERT INTO projet.inscriptions
	VALUES (e_id, _etudiant_id) RETURNING examen_id into i_id;
	RETURN i_id;

END;
$$ LANGUAGE plpgsql;

SELECT projet.insertInscription(1,'IPL148')



CREATE OR REPLACE FUNCTION projet.inscriptionUnExamen( _etudiant_id INTEGER, _code_examen VARCHAR) RETURNS VOID AS $$
DECLARE
	i_id INTEGER;
	ex_id INTEGER;
BEGIN
	SELECT e.examen_id
	FROM projet.examens e
	WHERE e.code SIMILAR TO _code_examen into ex_id;

	if (SELECT e.heure_debut
	   	FROM projet.examens e
	   	WHERE e.examen_id = ex_id) IS NOT NULL	
	then 	
		RAISE 'errer : heure déjà encodée';
	else	
		INSERT INTO projet.inscriptions VALUES (ex_id, _etudiant_id);
	end if;
END;
$$ LANGUAGE plpgsql;

SELECT projet.inscriptionUnExamen(3,'IPL148')
		

--InscriptionBloc(id_etudiant INTEGER, id_bloc)
CREATE OR REPLACE FUNCTION projet.inscriptionExamensBloc(_id_etudiant INTEGER, _bloc VARCHAR) RETURNS VOID AS $$ 
DECLARE 
	b_id INTEGER;
	exam RECORD; 
BEGIN 
	SELECT bloc_id 
	FROM projet.blocs b
	WHERE b.code LIKE _bloc into b_id;	

	FOR exam IN SELECT e.examen_id FROM projet.examens e WHERE e.bloc = b_id  LOOP 
		INSERT INTO projet.inscriptions VALUES (exam.examen_id,_id_etudiant); 
	END LOOP; 
END; 
$$ LANGUAGE plpgsql; 

SELECT projet.inscriptionBloc(4, 'BIN3')


--DISPLAY-------------------------------------------------------------

--5. Visualiser l'horaire d'examen pour un bloc particulier

CREATE OR REPLACE FUNCTION projet.displayExamensParBloc(_code_bloc VARCHAR) RETURNS SETOF RECORD AS $$
DECLARE
	examen RECORD;
	sortie RECORD;
	nbr_salles INTEGER =0;
	
BEGIN
	FOR examen in 	
		SELECT e.* FROM projet.examens e, projet.blocs b	
			WHERE e.bloc = b.bloc_id AND b.code LIKE  _code_bloc
			ORDER BY e.heure_debut LOOP 
					
			SELECT count(r.local_id)
			FROM projet.reservations r
			WHERE examen.examen_id = r.examen_id into nbr_salles;
					
		SELECT examen.heure_debut, examen.code, examen.nom, nbr_salles INTO sortie;
		RETURN NEXT sortie;
	END LOOP;
	RETURN;
END;
$$ LANGUAGE 'plpgsql';

SELECT * FROM projet.displayExamensParBloc('BIN3') t(heure_debut TIMESTAMP, code VARCHAR, nom VARCHAR, nbr_salles INTEGER);



--6. Visualiser toutes les réservations d’examen d’un local particulier

CREATE OR REPLACE FUNCTION projet.displayExamensParLocal(_nom_local VARCHAR) RETURNS SETOF RECORD AS $$
DECLARE
	examen RECORD;
	sortie RECORD;
	
BEGIN
	FOR examen in 	SELECT e.* FROM projet.examens e, projet.locaux l, projet.reservations r
					WHERE e.examen_id = r.examen_id AND r.local_id = l.local_id AND l.nom LIKE  _nom_local
					ORDER BY e.heure_debut LOOP 
										
				SELECT examen.heure_debut, examen.code, examen.nom INTO sortie;
			RETURN NEXT sortie;
	END LOOP;
	RETURN;
END;
$$ LANGUAGE 'plpgsql';

SELECT * FROM projet.displayExamensParLocal('A023') t(heure_debut TIMESTAMP, code VARCHAR, nom VARCHAR);


--7. Visualiser tous les examens qui ne sont pas encore complètement réservés (triés par code).

CREATE OR REPLACE FUNCTION projet.displayExamensNonComplet() RETURNS SETOF RECORD AS $$
DECLARE
	examen RECORD;
	sortie RECORD;
	
BEGIN
	FOR examen in 	SELECT e.* FROM projet.examens e
					WHERE e.completement_reserve = 'false'
					ORDER BY e.code LOOP 
										
				SELECT examen.heure_debut, examen.code, examen.nom INTO sortie;
			RETURN NEXT sortie;
	END LOOP;
	RETURN;
END;
$$ LANGUAGE 'plpgsql';

SELECT * FROM projet.displayExamensNonComplet() t(heure_debut TIMESTAMP, code VARCHAR, nom VARCHAR);


	 

--VIEW----------------------------------------------------------

--Visualier tous les examens (application utilisateur, connecté :1)
CREATE OR REPLACE VIEW projet.displayTousExamens AS
	SELECT e.code AS "Code examen" , e.nom AS "Nom examen", e.duree AS "Durée"
	FROM projet.examens e;


CREATE OR REPLACE VIEW projet.displayExamens AS
	SELECT e.heure_debut AS "Heure debut", e.code AS "Code examen" , e.nom AS "Nom examen", 
		COUNT(r.local_id) AS "Nombre de local"	
	FROM projet.reservations r, projet.examens e
	WHERE e.examen_id = r.examen_id
	GROUP By e.examen_id
	ORDER BY e.heure_debut;





--TRIGGER----------------------------------------------------------------

--NE FONCTIONNE PAS

CREATE OR REPLACE FUNCTION projet.triggerExamenNonComplet() RETURNS TRIGGER AS $$
DECLARE
	record RECORD;
	total INTEGER;
BEGIN
	SELECT b.nbr_exam_non_complet, b.bloc_id
		FROM projet.blocs b, projet.examens e
		WHERE b.bloc_id = NEW.bloc AND e.bloc = b.bloc_id INTO record;
		
	Update projet.blocs 
	set nbr_exam_non_complet = record.nbr_exam_non_complet+1
	WHERE bloc_id = record.bloc_id;
	
	RETURN NEW;

END;
$$ LANGUAGE plpgsql;

--INSERT--------------------------------------------------------------

insert into projet.formations values (default, 'info', 'ipl');
insert into projet.blocs values (default, 'BIN3', 1, default);
insert into projet.examens values ( default, 'IPL123', 'examen de sql', 1, true, '2020-12-12 10:00:00', INTERVAL '3' HOUR, default);

insert into projet.locaux values (default, 'A023', 30, true);

insert into projet.reservations values (1,1);

insert into projet.etudiants values (default, 'jean', 'jean@gmail.com', 'mdp', 1);

insert into projet.inscriptions values (1, 1);












