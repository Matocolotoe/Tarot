# Tarot – Compteur de points

![plot](./src/main/resources/logo.png)

Cette application permet de comptabiliser des parties de tarot à 3, 4, et 5 joueurs, et de créer
automatiquement les classements associés à partir des données fournies.

## Règles du jeu

Cliquez [ici](RULES.md) pour consulter les règles détaillées du jeu suivies par notre groupe.

## Ajout d'une partie

L'interface suivante permet de comptabiliser une partie en fonction des joueurs existants via la rubrique `Données` ou par le raccourci `CTRL+P`. Chaque partie peut ensuite être modifiée ou supprimée dans le menu principal, et les détails du calcul des scores peuvent être affichés.

![plot](./src/main/resources/new_game.png)

_La possibilité de sélectionner une personne appelée apparaît lorsque 5 joueurs sont sélectionnés._

## Menu des joueurs

Un menu accessible via la rubrique `Données` ou par le raccourci `CTRL+J`, permet de visualiser les statistiques par mois et par nombre de joueurs de chaque personne, mais également de modifier leurs surnoms. Ceux-ci apparaissent uniquement dans la [grille des scores](#grille-des-scores). Les surnoms mensuels ont la priorité sur les surnoms génériques (annuels).

![plot](./src/main/resources/player_menu.png)

## Statistiques et graphiques

Pour $N = 3$, $4$, $5$, une rubrique `Tarot à N` propose des statistiques individuelles, des statistiques globales sur un mois ou entre deux parties précises de ce mois, ainsi que des graphiques représentant l'évolution des scores entre deux parties. Nous utilisons pour ce faire la bibliothèque [XChart](https://github.com/knowm/XChart).

![plot](./src/main/resources/score_graphs.png)

_Par défaut, seules les courbes des personnes ayant joué plus de 10 parties sur la période sélectionnée sont affichées._

## Export des données

### Grille des scores

Lorsque vous cliquez sur `Exporter les données` dans la rubrique `Données` ou entrez `CTRL+E`, l'application crée un tableur pour chaque année, chacun contenant une grille par mois contenant au moins une partie. Des statistiques individuelles, des classements et des statistiques globales sont affichées pour chaque nombre de joueurs. Nous utilisons pour ce faire la bibliothèque [fastexcel](https://github.com/dhatim/fastexcel).

![plot](./src/main/resources/leaderboard.png)

_Seuls les taux de réussite des personnes ayant pris plus de 3 fois apparaissent dans la grille._

### Sauvegardes

Lorsque vous cliquez sur `Créer une sauvegarde` dans la rubrique `Données` ou entrez `CTRL+S`, l'application crée une copie des fichiers des parties et des joueurs dans une archive au format ZIP.
