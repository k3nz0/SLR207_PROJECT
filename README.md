# SLR207_PROJECT
Decentralization for the masses


# Chronométrage des phases :
Sur l'input exemple ne contenant que 9 mots : 
## Map 
`
[~] Starting map phase !
`

`
[OK] MAP phase terminated !
`

`
[+] TIME SPENT IN MAP : *4718ms* !
`
## Shuffle
`
[~] Preparing slaves shuffle !
`

`
[~] Starting slaves shuffle !
`

`
[OK] Shuffle phase terminated !
`
`
[+] TIME SPENT IN SHUFFLE : *6669ms* !
`
## Reduce

`
[~] Running reduce !
`
`
[+] TIME SPENT IN REDUCE : *4417ms* !
`
## Total 
On obtient au total *15804ms*, ce qui correspond à environ 15 secondes.
En version séquentielle nous avons obtenu *100ms* pour le même input.
Comparé à la version séquentielle produite au début du TP, la version répartie est très lente sur un petit input.

# Réponses aux questions

### Question 1:
1​ - Premier comptage en séquentiel pur

HashMap est la mieux appropriée. 
Clé : Le string
Valeur : Le nombre d'occurence
On utilise HashMap pour pouvoir compter le nombre d'occurence.

### Question 4:
forestier_mayotte.txt
de=12, biens=8, ou=8, forestier=6, des=5


### Question 5:
deontologie_police_nationale.txt

Quels sont les 5 premiers mots:  

de=86, la=40, police=29, et=27, à=25, des=24

### Question 6:
domaine_public_fluvial.txt
de=621, le=373, du=347, la=330, et=266, à=209

### Question 7:
sante_publique.txt

de: 189699
la: 74433
des: 66705
à: 65462
et: 60940
les: 48772
du: 48400
le: 44264
ou: 39464
par: 30224
en: 28922
au: 25026



### Question 8:

Elapsed time after reading input and counting number of occurence: 1375
Elapsed time after sorting by value: 1464
Elapsed time after sorting by key: 1549


### Question 9:

Reading file : CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet
Elapsed time after reading input and counting number of occurence: 32944
Elapsed time after sorting by value: 34429
Elapsed time after sorting by key: 44894


### Question 10:

Le nom COURT de l'ordinateur : 
c45-10

Le nom LONG de l'ordinateur : 
c45-10.enst.fr

Comment les connaître en ligne de commande : 
`cat /etc/hosts `

` hostname `

### Question 11:

` /sbin/ifconfig `
` curl ifconfig.me `

### Question 12:
Comment, à partir d’une adresse IP, obtenir les noms associés en ligne de commande ?

` host c45-10 `

c45-10.enst.fr has address 137.194.34.201

### Question 13:

` host 137.194.34.201 `

201.34.194.137.in-addr.arpa domain name pointer c45-10.enst.fr.

### Question 14:

On teste la communication avec la machine c45-12.
ping c45-12

ping c45-12.enst.fr

ping 137.194.34.203

Les 3 méthodes fonctionnent.

### Question 15:

On utilise un vpn pour pouvoir pinguer depuis l'extérieur.

### Question 16:

Calcul en ligne de commande 
echo $((2+3))

### Question 17:
` ssh c45-13 "echo $((2+3))" `

### Question 18:

Afin d'éviter d'entrer le mot de passe à chaque fois, on peut utiliser une clé rsa.


19 -
$ cd && pwd 
/cal/homes/mezzeddine

21 -
realpath fperso.txt
/cal/homes/mezzeddine/fperso.txt

22 -
Fichier dans /tmp/mezzeddine est stocké physiquement sur l'ordinateur

23 - 
On crée un fichier text.txt dans ~/text.txt

24 - 
Le fichier est bien présent depuis B et C.

25 - 

27 - 
Transférer fichier local.txt (A) vers /tmp/mezzeddine (B)
scp local.txt c45-13:/tmp/mezzeddine/

28 - 
Transférer depuis (A) le fichier de (B) vers (C)
scp c45-13:/tmp/mezzeddine/local.txt c45-14:/tmp/mezzeddine


## Etape 4 :

31 - 
scp SLAVE.jar c128-16:/tmp/mezzeddine/

32 - 
ssh c128-16 "/tmp/mezzeddine/SLAVE.jar" 

## Etape 5 : 
Lancer des commandes avec ProcessBuilder

## Etape 6 : 
36 - 



------------
## Etape 7: déployer automatiquement le programme SLAVE sur un ensemble de machines.

38- Votre programme DEPLOY lance-t-il les connections de manière séquentielle (les unes après les autres) ou de manière parallèle?
DEPLOY lance des connexions de manière séquentielle à cause de "waitFor"


39- Votre programme DEPLOY lance-t-il les copies de manière séquentielle (les unes après les autres) ou de manière parallèle?
De manière séquentielle

  - Comment faites-vous pour attendre que le mkdir se termine correctement?
Utiliser process.waitFor()


## Etape 8: lancer le programme SLAVE sur un ordinateur à distance.


## Etape 9: MapReduce - SPLIT et MAP
41 - 

Arrivé à l'étape 41, check ~/x.sh (add to it some stuff to generate s0, s1, ... ?)

41 - Comment attendez-vous que la création des dossiers soit bien effectuée avant de copier véritablement les fichiers?

Lancer un process pour copier les fichiers, puis attendre la fin de son execution avec process.waitFor();

(Rq : Serait-il plus judicieux de faire waitFor avec timeout ?)

Votre programme MASTER lance-t-il les copies de manière séquentielle (les unes après les autres) ou de manière parallèle?
De manière séquentielle

42-

On retrouve deux lignes "Car 1" et non pas "Car 2" car le comptage d'occurence ne se fait pas en phase de map

43 -

Le MASTER lance les slaves d'une manière parallèle (vu qu'on n'attend pas avec waitFor)

44 -
 
 
49 - 
(done) Préparer la phase de shuffle 


Afin d'éviter de copier UMx plusieurs fois vers la même machine, on utilise pour chaque machine un HashSet dans lequel on stocke les UMx déjà
transférés.

Le programme prépare la phase de Shuffle de manière parallèle. 

Le programme fonctionne d'une manière répartie puisque le contenu des fichiers copiés ne passe pas par le MASTER.
(Le MASTER se contente seulement d'orchestrer le transfert)

50 - 

On attend la fin de la phase shuffle en appliquant un waitFor à chaque processus.

Le MASTER lance les SLAVE de manière parallèle.







