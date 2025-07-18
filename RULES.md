# Tarot – Règles du jeu

Sont détaillées ci-dessous les règles du jeu suivies par notre groupe.

## Calcul des points

En fin de partie, les points obtenus dans les plis de l'attaque sont comparés au score requis par
le nombre de bouts qu'elle a récoltés.

| **Bouts** | **Score requis** |
|-----------|------------------|
| 0         | 56               |
| 1         | 51               |
| 2         | 41               |
| 3         | 36               |

Chaque contrat est associé à un multiplicateur de score différent.

| **Contrat**  | **Multiplicateur**  |
|--------------|---------------------|
| Petite       | 1                   |
| Garde        | 2                   |
| Garde sans   | 4                   |
| Garde contre | 6                   |

Nous noterons dans ce qui suit :
- $a$ le score obtenu par l'attaque,
- $b$ le score requis en fonction des bouts,
- $m$ le multiplicateur du contrat.

Les points finalement attribués à l'attaque valent $f = (25 + |a - b|) \times m$ lorsque $a \geq b$
(contrat _réussi_), et $-f$ lorsque $a < b$ (contrat _chuté_). Il est alors possible de retrouver le
score obtenu par l'attaque à partir du score final $f$ qui lui a été attribué :
- $f / m + b - 25$ si le contrat a été réussi,
- $b + 25 - f / m$ si le contrat a été chuté.

## Attribution des points

À 3 ou 4 joueurs :
- chaque défenseur obtient $-f$ points,
- le preneur gagne $3f$ points.

À 5 joueurs :
- chaque défenseur obtient $-f$ points,
- le preneur en gagne $2f$,
- le joueur appelé en gagne $f$.

_NB : si le preneur s'est appelé tout seul à 5 joueurs, il obtient alors 4f points._

## Misères

Lorsqu'un joueur n'a pas d'atout ou pas de tête, il peut déclarer une _misère_ (_simple_ si
l'une des conditions seulement est vérifiée, _double_ si les deux sont remplies). En fin
de partie, tous les autres joueurs lui offrent un certain nombre de points, indépendamment
de leur camp. Cette prime n'est pas affectée par le multiplicateur du contrat.

| **Misère** | **Prime** |
|------------|-----------|
| Simple     | 10 points |
| Double     | 20 points |

_NB : la misère doit nécessairement être annoncée lorsque le joueur pose sa première carte._

## Poignées

Lorsqu'un joueur possède un certain nombre d'atouts, il peut déclarer une _poignée_
(simple, double, ou triple).

| **Poignée** | **Atouts (5j)** | **Atouts (4j)** | **Atouts (3j)** | **Prime** |
|-------------|-----------------|-----------------|-----------------|-----------|
| Simple      | 8               | 10              | 13              | 20 points |
| Double      | 10              | 13              | 15              | 30 points |
| Triple      | 13              | 15              | 18              | 40 points |

Les points reviennent en positif à l'attaque si et seulement si le contrat a été réussi.
Cette prime n'est pas affectée par le multiplicateur du contrat.

_NB : la poignée doit nécessairement être annoncée lorsque le joueur pose sa première carte._

## Petit au bout

Lorsque le dernier pli contient le Petit, une prime de 10 points est accordée au camp du joueur
qui remporte le pli. Cette prime est affectée par le multiplicateur du contrat, et **ne dépend
pas du camp du joueur** (si l'attaque chute mais remporte le dernier pli grâce au Petit, alors
la prime lui reviendra en positif).

## Chelem

Il s'agit du pari de réaliser tous les plis. Le chelem est annoncé en plus du contrat, et
l'annonce peut être effectuée après l'écart. L'annonceur du chelem entame toujours le tour.
Un certain nombre de points est attribué à son camp en fonction du résultat de la partie,
sans être affecté par le multiplicateur du contrat.

| **Résultat**               | **Score**   |
|----------------------------|-------------|
| Chelem annoncé, réussi     | 400 points  |
| Chelem non annoncé, réussi | 200 points  |
| Chelem annoncé, chuté      | -200 points |

_NB : si l'annonceur du chelem détient l'Excuse, il devra nécessairement la jouer en dernier
pour remporter le pli, et le Petit sera alors considéré au bout s'il est joué à l'avant-dernier tour._
