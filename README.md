
Récapitulatif du Projet
Nous avons développé une application Java EE complète avec Spring Boot 3.3 et Java 21 pour la gestion des objets perdus et trouvés. Voici les principales fonctionnalités implémentées :
1.	Authentification sécurisée
o	Enregistrement et connexion d'utilisateurs
o	Gestion des rôles (utilisateur et administrateur)
o	Protection des routes sensibles
2.	Gestion des objets perdus
o	Déclaration d'un objet perdu avec détails et photo
o	Validation administrative avant publication
o	Recherche et filtrage des annonces
o	Édition et suppression des annonces
3.	Gestion des objets trouvés
o	Déclaration d'un objet trouvé
o	Validation administrative avant publication
o	Possibilité de réclamation par les propriétaires potentiels
4.	Interface d'administration
o	Validation/rejet des annonces d'objets perdus et trouvés
o	Motif de rejet avec notification à l'utilisateur
5.	Système de notifications
o	Alerte lors de la validation/rejet d'une annonce
o	Notification lors d'une réclamation d'objet trouvé
o	Compteur de notifications non lues
6.	Interface utilisateur soignée
o	Design responsive avec Bootstrap
o	Tableaux de bord personnalisés
o	Filtres de recherche avancée
o	Pagination des résultats

Structure de l'application
L'application suit une architecture MVC classique avec les composants suivants:
1.	Modèles (Model): Entités JPA pour la persistance des données
2.	Vues (View): Templates Thymeleaf avec Bootstrap pour l'interface utilisateur
3.	Contrôleurs (Controller): Contrôleurs Spring MVC pour la gestion des requêtes
4.	Services: Couche métier encapsulant la logique applicative
5.	Repositories: Interface avec la base de données via Spring Data JPA
Détails techniques
•	Base de données: H2 (embedded) pour le développement, facilement migrables vers MySQL ou PostgreSQL
•	Sécurité: Spring Security avec authentification basée sur email/mot de passe
•	Frontend: Thymeleaf, Bootstrap 5, Font Awesome, CSS personnalisé
•	Upload d'images: Gestion des fichiers sur le système de fichiers local
•	Validation: Validation des formulaires côté serveur avec messages d'erreur contextuels
Pour déployer l'application:
1.	Cloner le repo du projet
2.	Configurer les propriétés de la base de données dans application.properties
3.	Exécuter mvn clean install pour construire le projet
4.	Lancer avec java -jar target/objets-perdus-0.0.1-SNAPSHOT.jar
Des utilisateurs de test sont automatiquement créés au démarrage de l'application:
•	Admin: admin@example.com / admin123
•	Utilisateur: user@example.com / user123
Cette application offre une base solide qui peut être facilement étendue avec des fonctionnalités supplémentaires comme la géolocalisation des objets, la notification par email, ou encore l'intégration avec des API externes.

