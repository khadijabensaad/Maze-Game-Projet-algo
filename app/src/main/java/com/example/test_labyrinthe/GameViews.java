package com.example.test_labyrinthe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import android.os.Handler;

public class GameViews extends View{
    private  enum Direction {UP,DOWN,LEFT,RIGHT} //enum est un type de données de constantes auto-définies
    private Cell[][] cells; // Les cases du labyrinthe
    private  Cell player ,exit; // Case du joueur et de la sortie de type Cell
    private int COLS =100, ROWS =100; // lignes et colonnes
    private static final float WALL_THICKNESS = 8; // épaisseur de mur
    private  float cellSize , hMargin , vMargin; // utilisés pour connaitre les dimensions a l'affichage sur l'éran
    private Paint wallPaint, playerPaint, exitPaint, textPaint, pathPaint, backgroundPaint, scorePaint, shortestPathMovesPaint, movementNumberPaint, timerPaint, roundPaint; // pour dessiner l'interface
    private Random random;
    private char[][] letters; // Tableau pour stocker les lettres
    private List<Cell> path = new ArrayList<>(); //liste chainée pour stocker le chemin
    private List<Character> collectedLetters = new ArrayList<>(); //liste chainée pour stocker la séquence des caracteres pour former un mot
    private Set<String> dictionary = new HashSet<>(); //hashSet pour stocker les mots extraits du fichier dictionary, type hashSet pour éviter la redondance des mots
    private Set<String> usedWords = new HashSet<>(); //hashSet pour stocker les mots collectés
    private boolean[][] collectedCells; //les cases courantes utilisées pour former un mot
    private long lastClickTime = 0; // variable utilisée pour détecter le double-clic
    private int totalScore = 0; // Score total du joueur
    private int currentRound = 1; //la manche actuelle
    private boolean showPath = false; // Indique si le chemin doit être affiché ou non
    private ScoreUpdateListener scoreUpdateListener; // Listener pour mettre à jour le score
    private String diff; // Niveau de difficulté
    private int movesAllowed; // Nombre de mouvements autorisés
    private int initialMovesAllowed; // Stocker la valeur initiale pour la réinitialisation
    private int prevPlayerCol; // la colonne du joueur précedente
    private int prevPlayerRow; // la ligne du joueur précedente
    private int wordsCollectedInRound = 0; // Mots collectés dans la manche actuelle
    private int timeLeft = 60; // Temps restant en secondes
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private boolean isTimerActive = false;
    // Ajout des obstacles mobiles
    private List<Cell> movingObstacles = new ArrayList<>();
    private Handler obstacleHandler = new Handler();
    private Runnable obstacleRunnable;
    MediaPlayer mediaPlayer; // variable pour le son dans l'application

    // Constructeur de la classe GameViews
    public GameViews(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Méthode pour arrêter le chronomètre et déplacer les obstacles lors de la sortie du partie (touche retourner dans le téléphone)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable); // Empêche timerRunnable de s'exécuter après le détachement de la vue
        }
        isTimerActive = false; // Deactivate the timer
        stopObstacleMovement(); // Stop obstacle movement

    }

    // Méthode pour arrêter le déplacement des obstacles
    private void stopObstacleMovement() {
        if (obstacleRunnable != null) {
            obstacleHandler.removeCallbacks(obstacleRunnable); // Empêche obstacleRunnable de s'exécuter après le détachement de la vue
        }
    }

    // Méthode pour ajuster les dimensions de la grille selon le niveau de difficulté
    public void setDimensions(String diff) {
        this.diff = diff;
        if ("easy".equals(diff)) {
            COLS = 7;
            ROWS = 5;
            resetTimer();
        } else if ("medium".equals(diff)) {
            COLS = 10;
            ROWS = 7;
        } else if ("hard".equals(diff)) {
            COLS = 15;
            ROWS = 12;
        }
        // Réinitialiser le labyrinthe ou toute autre configuration nécessaire
        createMaze();
    }

    // Méthode pour initialiser les élements de l'interface utilisateur et charger des mots à partir d'un dictionnaire
    private void init() {
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.BLUE);

        exitPaint = new Paint();
        exitPaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);

        pathPaint = new Paint();
        pathPaint.setColor(Color.parseColor("#965D1A"));
        pathPaint.setStrokeWidth(WALL_THICKNESS - 3);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#FFBD59"));

        scorePaint = new Paint();
        scorePaint.setColor(Color.parseColor("#965D1A"));
        scorePaint.setTextSize(60);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setStyle(Paint.Style.FILL);
        scorePaint.setFakeBoldText(true); // Texte en gras
        scorePaint.setShadowLayer(7, 3, 3, Color.BLACK);

        roundPaint = new Paint();
        roundPaint.setColor(Color.BLACK);
        roundPaint.setTextSize(60);
        roundPaint.setTextAlign(Paint.Align.CENTER);
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setFakeBoldText(true); // Texte en gras
        roundPaint.setShadowLayer(7, 3, 3, Color.BLACK);

        movementNumberPaint = new Paint();
        movementNumberPaint.setColor(Color.parseColor("#965D1A"));
        movementNumberPaint.setTextSize(60);
        movementNumberPaint.setTextAlign(Paint.Align.CENTER);
        movementNumberPaint.setStyle(Paint.Style.FILL);
        movementNumberPaint.setFakeBoldText(true); // Texte en gras
        movementNumberPaint.setShadowLayer(7, 3, 3, Color.BLACK);

        shortestPathMovesPaint = new Paint();
        shortestPathMovesPaint.setColor(Color.parseColor("#965D1A"));
        shortestPathMovesPaint.setTextSize(60);
        shortestPathMovesPaint.setTextAlign(Paint.Align.CENTER);
        shortestPathMovesPaint.setStyle(Paint.Style.FILL);
        shortestPathMovesPaint.setFakeBoldText(true); // Texte en gras
        shortestPathMovesPaint.setShadowLayer(7, 3, 3, Color.BLACK);

        timerPaint = new Paint();
        timerPaint.setColor(Color.BLACK);
        timerPaint.setTextSize(60);
        timerPaint.setTextAlign(Paint.Align.CENTER);
        timerPaint.setStyle(Paint.Style.FILL);
        timerPaint.setFakeBoldText(true); // Texte en gras
        timerPaint.setShadowLayer(7, 3, 3, Color.BLACK);

        random = new Random();
        loadDictionary(); // charger des mots à partir d'un dictionnaire
        createMaze(); // initialiser la structure du labyrinthe
    }

    // Ajout des méthodes pour les obstacles mobiles
    // Création des obstacles pour le niveau difficile
    private void createMovingObstacles() {
        movingObstacles.clear(); // Clear existing obstacles//bch tkhli toujour meme nombre d obstacle
        for (int i = 0; i < 3; i++) { // Ajouter 3 obstacles mobiles
            int col = random.nextInt(COLS);
            int row = random.nextInt(ROWS);
            // To avoid collision with player or exit
            while ((player.col == col && player.row == row)||(exit.col == col && exit.row == row)) {
                col = random.nextInt(COLS);
                row = random.nextInt(ROWS);
            }
            movingObstacles.add(cells[col][row]);
        }
    }

    // Méthode pour la logique du mouvement des obstacles
    private void moveObstacles() {
        for (Cell obstacle : movingObstacles) {
            int direction = random.nextInt(4); // 0: haut, 1: bas, 2: gauche, 3: droite
            switch (direction) {
                case 0: if (obstacle.row > 0) obstacle.row--; break;
                case 1: if (obstacle.row < ROWS - 1) obstacle.row++; break;
                case 2: if (obstacle.col > 0) obstacle.col--; break;
                case 3: if (obstacle.col < COLS - 1) obstacle.col++; break;
            }
        }
        checkObstacleCollision(); // Vérifier les collisions avec le joueur
        invalidate(); // Redessiner le labyrinthe
    }

    // Méthode pour le déclenchement du mouvement des obstacles
    private void startObstacleMovement() {
        obstacleRunnable = new Runnable() {
            @Override
            public void run() {
                moveObstacles();
                obstacleHandler.postDelayed(this, 500); // Déplacer les obstacles toutes les 0.5 secondes
            }
        };
        obstacleHandler.post(obstacleRunnable);
    }

    // Méthode pour réinitialiser la position du joueur s'il est en collision avec un obstacle
    private void checkObstacleCollision() {
        for (Cell obstacle : movingObstacles) {
            if (player.col == obstacle.col && player.row == obstacle.row) {
                // Réinitialiser la position du joueur
                int[] startPosition = getRandomPosition();
                // Pour éviter une collision avec la sortie
                while (startPosition[0] == exit.col && startPosition[1] == exit.row) {
                    startPosition = getRandomPosition();
                }
                player = cells[startPosition[0]][startPosition[1]];
                Toast.makeText(getContext(), "Collision with obstacle! Player reset.", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    // Méthode pour charger des mots à partir d'un dictionnaire
    private void loadDictionary() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.dictionary); // chargement du contenu du fichier dictionary.txt dans le dossier raw sous le dossier res
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toUpperCase();
                dictionary.add(word);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour génerer des lettres aléatoires
    private void generateRandomLetters() {
        letters = new char[COLS][ROWS]; // matrice de dimension de la labyrinthe qui va contenir a chaque case une lettre aléatoire
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                // Générer une lettre aléatoire entre 'A' et 'Z'
                letters[x][y] = (char) ('A' + random.nextInt(26));
            }
        }
    }

    // Méthode utilisée dans le recursive backtracking algorithme pour renvoyer une cellule voisine aléatoire non visitée( génération de la grille )
    private Cell getNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();
        // voisins gauches
        if(cell.col>0)
            if(!cells[cell.col-1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        // voisins droits
        if(cell.col< COLS-1)
            if(!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col+1][cell.row]);

        // voisins hauts
        if(cell.row>0)
            if(!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);

        // voisins bas
        if(cell.row< ROWS-1)
            if(!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);

        if(neighbours.size()>0){
            int index = random.nextInt(neighbours.size());
            return  neighbours.get(index); //renvoyer une cellule voisine aléatoire non visitée
        }
        return  null;
    }


    // Méthode utilisée dans le recursive backtracking algorithme pour supprimer les murs et la génération de la grille
    private void removeWall(Cell current, Cell next){
        if(current.col == next.col && current.row == next.row+1){
            current.topWall=false;
            next.bottomWall=false;
        }
        if(current.col == next.col && current.row == next.row-1){
            current.bottomWall=false;
            next.topWall=false;
        }
        if(current.col == next.col+1 && current.row == next.row){
            current.leftWall=false;
            next.rightWall=false;
        }
        if(current.col == next.col-1 && current.row == next.row){
            current.rightWall=false;
            next.leftWall=false;
        }
    }

    // Méthode utilisée pour avoir une position aléatoire
    private int[] getRandomPosition() {
        int col = random.nextInt(COLS);
        int row = random.nextInt(ROWS);
        return new int[]{col, row};
    }

    // Méthode pour génerer la grille
    private void createMaze(){
        // Initialisation d'une pile pour stocker les cellules visitées pendant la génération du labyrinthe
        Stack<Cell> stack =new Stack<>();
        Cell current, next;
        // Création d'une grille de cellules (COLS x ROWS) pour représenter le labyrinthe
        cells = new  Cell[COLS][ROWS];
        for(int x=0; x<COLS; x++){
            for(int y=0; y<ROWS; y++){
                cells[x][y] = new Cell(x,y); // Initialisation de chaque cellule avec ses coordonnées
            }
        }
        // Choisir des positions aléatoires pour le joueur et la sortie
        int[] startPosition = getRandomPosition(); // Position de départ du joueur
        int[] exitPosition = getRandomPosition(); // Position de la sortie

        // S'assurer que le joueur et la sortie ne sont pas au même endroit
        while (startPosition[0] == exitPosition[0] && startPosition[1] == exitPosition[1]) {
            exitPosition = getRandomPosition();
        }
        player = cells[startPosition[0]][startPosition[1]];
        exit = cells[exitPosition[0]][exitPosition[1]];
        //Recursive backtracking algorithme, pour générer le labyrinthe
        current = cells[startPosition[0]][startPosition[1]];
        current.visited =true; // Marquer la cellule actuelle comme visitée

        // Initialiser la position précédente du joueur
        prevPlayerCol = player.col;
        prevPlayerRow = player.row;

        // Boucle principale de génération du labyrinthe
        do{
            next = getNeighbour(current); // Trouver un voisin non visité de la cellule actuelle
            if(next !=null){
                removeWall(current, next); // Supprimer le mur entre la cellule actuelle et le voisin
                stack.push(current); // Empiler la cellule actuelle
                current= next; // Déplacer la cellule actuelle vers le voisin
                current.visited = true; // Marquer le voisin comme visité
            }
            else
                current= stack.pop(); // Si aucun voisin n'est trouvé, revenir à la cellule précédente
        }while(!stack.empty()); // Continuer jusqu'à ce que toutes les cellules soient visitées

        generateRandomLetters(); // Génération de lettres aléatoires dans le labyrinthe
        // Réinitialisation des cellules collectées et de la liste des lettres collectées
        collectedCells = new boolean[COLS][ROWS];
        collectedLetters.clear();
        // Calcul du chemin le plus court entre le joueur et la sortie en utilisant l'algorithme de Dijkstra
        path = dijkstra(player, exit);
        if("easy".equals(diff)) {
            movesAllowed = path.size() - 1 + 30; // Chemin le plus court + 30 mouvements pour le niveau facile
        }else if ("medium".equals(diff)) {
            movesAllowed = path.size() - 1 + 20; // Chemin le plus court + 20 mouvements pour le niveau moyen
        } else if ("hard".equals(diff)) {
            movesAllowed = path.size() - 1 + 20; // Chemin le plus court + 20 mouvements pour le niveau difficile
            createMovingObstacles(); // Ajout d'obstacles mobiles pour le niveau difficile
            startObstacleMovement(); // Démarrage du mouvement des obstacles
        }
        initialMovesAllowed = movesAllowed; // Sauvegarde du nombre initial de mouvements autorisés (pour réinitialisation ultérieure)
        // Démarrage du timer pour les niveaux autres que "easy" (medium et hard)
        if(! ("easy".equals(diff)) ){
            startTimer();
        }
    }


    // Méthode pour dessiner l'interface utilisateur
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(0xFFF4DFB8); // Définir la couleur de fond du canvas
        // Obtenir la largeur et la hauteur de l'écran
        int width = getWidth(); // Largeur de l'écran
        int height = getHeight(); // Hauteur de l'écran

        // Déterminer la taille des cellules en fonction de l'orientation (portrait ou paysage)
        if (width / height < COLS / ROWS) { // Si le labyrinthe est en mode portrait
            cellSize = width / (COLS + 1); // Taille des cellules basée sur la largeur
        } else { // Si le labyrinthe est en mode paysage
            cellSize = height / (ROWS + 1); // Taille des cellules basée sur la hauteur
        }

        // Calculer les marges horizontales et verticales pour centrer le labyrinthe
        hMargin = (width - COLS * cellSize) / 2; // Marge horizontale (gauche et droite)
        vMargin = (height - ROWS * cellSize) / 2; // Marge verticale (haut et bas)
        canvas.translate(hMargin,vMargin);// Déplacer le point d'origine du canvas pour centrer le labyrinthe

        // Dessiner le fond du labyrinthe
        canvas.drawRect(0, 0, COLS * cellSize, ROWS * cellSize, backgroundPaint);

        // Afficher le numéro du tour (round) en haut de l'écran
        String roundText = "Round: " + currentRound;
        canvas.drawText(roundText, COLS * cellSize / 2, (-vMargin / 2)- 60 - scorePaint.getTextSize(), roundPaint);

        // Afficher le score total en haut de l'écran
        String scoreText = "Score: " + totalScore;
        canvas.drawText(scoreText, COLS * cellSize / 2, -vMargin / 2, scorePaint);

        // Afficher le nombre de mouvements pour le chemin le plus court
        int numberOfMovesForShortestPath = path.size() - 1;
        String shortestPathMoves = "Shortest Path: " + numberOfMovesForShortestPath;
        float shortestPathMovesX = COLS * cellSize / 2; // Center horizontally
        float shortestPathMovesY = (-vMargin / 2) + scorePaint.getTextSize() + 20;
        canvas.drawText(shortestPathMoves, shortestPathMovesX, shortestPathMovesY, shortestPathMovesPaint);

        // Afficher le nombre de mouvements autorisés
        String movesAllowedText = "Moves Allowed: " + movesAllowed;
        float MovesX = COLS * cellSize / 2; // Center horizontally
        float MovesY = shortestPathMovesY  + shortestPathMovesPaint.getTextSize() + 20;
        canvas.drawText(movesAllowedText, MovesX, MovesY, movementNumberPaint);

        // Afficher le timer uniquement pour les niveaux moyen et difficile
        if(!("easy".equals(diff))){
            String timerText = "Time: " + timeLeft + "s";
            canvas.drawText(timerText, COLS * cellSize / 2, vMargin+260 , timerPaint);
        }

        // Dessiner les murs du labyrinthe
        for(int x=0; x<COLS; x++){
            for(int y=0; y<ROWS; y++){
                if(cells[x][y].topWall) // Dessiner le mur du haut
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint
                    );
                if(cells[x][y].leftWall) // Dessiner le mur de gauche
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                if(cells[x][y].bottomWall) // Dessiner le mur du bas
                    canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                if(cells[x][y].rightWall) // Dessiner le mur de droite
                    canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );

                // Dessiner la lettre aléatoire dans chaque cellule
                String letter = String.valueOf(letters[x][y]);
                float textX = x * cellSize + cellSize / 2;
                float textY = y * cellSize + cellSize / 2 - (textPaint.descent() + textPaint.ascent()) / 2;
                canvas.drawText(letter, textX, textY, textPaint);
            }
        }

        // Dessiner les obstacles mobiles que dans le mode difficile
        if("hard".equals(diff)){
            Paint obstaclePaint = new Paint();
            obstaclePaint.setColor(Color.GREEN);
            for (Cell obstacle : movingObstacles) {
                RectF obstacleRect = new RectF(obstacle.col * cellSize + cellSize / 4, obstacle.row * cellSize + cellSize / 4,
                        (obstacle.col + 1) * cellSize - cellSize / 4, (obstacle.row + 1) * cellSize - cellSize / 4);
                canvas.drawRoundRect(obstacleRect, 20, 20, obstaclePaint); // Obstacles avec coins arrondis
            }
        }

        // Dessiner le joueur avec des coins arrondis
        float margin= cellSize/10;
        RectF playerRect = new RectF(player.col * cellSize + margin, player.row * cellSize + margin, (player.col + 1) * cellSize - margin, (player.row + 1) * cellSize - margin);
        canvas.drawRoundRect(playerRect, 20, 20, playerPaint); // Joueur avec coins arrondis

        // Dessiner la sortie avec des coins arrondis
        RectF exitRect = new RectF(exit.col * cellSize + margin, exit.row * cellSize + margin, (exit.col + 1) * cellSize - margin, (exit.row + 1) * cellSize - margin);
        canvas.drawRoundRect(exitRect, 20, 20, exitPaint); // Sortie avec coins arrondis

        // Afficher le chemin le plus court si l'option est activée
        if (showPath) {
            int playerIndex = -1;
            // Trouver la position du joueur dans le chemin
            for (int i = 0; i < path.size(); i++) {
                if (path.get(i).col == player.col && path.get(i).row == player.row) {
                    playerIndex = i;
                    break;
                }
            }
            // Dessiner le chemin à partir de la position du joueur
            if (playerIndex != -1) {
                for (int i = playerIndex; i < path.size() - 1; i++) {
                    Cell current = path.get(i);
                    Cell next = path.get(i + 1);

                    float startX = current.col * cellSize + cellSize / 2;
                    float startY = current.row * cellSize + cellSize / 2;
                    float endX = next.col * cellSize + cellSize / 2;
                    float endY = next.row * cellSize + cellSize / 2;

                    canvas.drawLine(startX, startY, endX, endY, pathPaint);// Dessiner une ligne entre les cellules
                }
            }
        }
    }

    // Méthode utilisée pour réinitialiser le timer à 60 secondes, arrêter le timer actuel s'il est en cours d'exécution, et désactiver le timer
    private void resetTimer() {
        timeLeft = 60; // Réinitialiser le minuteur à 60 secondes
        if (timerRunnable != null) { // Vérifier si le timerRunnable existe (s'il est en cours d'exécution)
            timerHandler.removeCallbacks(timerRunnable); // Arrêter le timer actuel en supprimant le timerRunnable du timerHandler
        }
        // Désactiver le timer en mettant isTimerActive à false
        isTimerActive = false; // Deactivate the timer
    }

    private void startTimer() {
        if (!isTimerActive && !"easy".equals(diff)) { // Vérifier si le timer n'est pas déjà actif et que la difficulté n'est pas "easy"
            timeLeft = 60; // Réinitialiser le temps restant à 60 secondes
            isTimerActive = true; // Activer le timer
            // Créer un Runnable pour gérer le décompte du temps
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (timeLeft > 0) { // Vérifier si le temps restant est supérieur à 0
                        timeLeft--; // Décrémenter le temps restant
                        timerHandler.postDelayed(this, 1000); // Planifier l'exécution de ce Runnable à nouveau après 1 seconde (1000 ms)
                        invalidate(); // Redessiner l'interface utilisateur pour mettre à jour l'affichage du temps
                    } else {
                        isTimerActive = false; // Si le temps est écoulé, désactiver le timer
                        endRound(); // Fin du tour si le temps est écoulé
                    }
                }
            };
            timerHandler.postDelayed(timerRunnable, 1000); // Démarrer le timer en planifiant la première exécution du Runnable après 1 seconde
        }
    }

    // Méthode pour réinitialiser le jeu si le temps est écoulé
    private void endRound() {

        // Arrêter le timer s'il est en cours d'exécution
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        // Arrêter le mouvement des obstacles s'il est en cours d'exécution
        if (obstacleRunnable != null) {
            obstacleHandler.removeCallbacks(obstacleRunnable);
        }
        isTimerActive = false; // Désactiver le timer
        // Réduire le score total de 300 points, en s'assurant qu'il ne devienne pas négatif
        totalScore -= 300;
        if (totalScore < 0) totalScore = 0; // Si le score devient negatif, le rendre nul
        // Réinitialiser le round et les statistiques associées
        currentRound = 1;
        wordsCollectedInRound = 0;
        timeLeft = 60;
        // Ajuster la taille du labyrinthe en fonction du niveau de difficulté
        if ("medium".equals(diff)) {
            COLS = 10;
            ROWS = 7;
        } else if ("hard".equals(diff)) {
            COLS = 15;
            ROWS = 12;
        }
        createMaze(); // Générer un nouveau labyrinthe
        Toast.makeText(getContext(), "Time's up! You lost 300 points.", Toast.LENGTH_SHORT).show(); // Afficher un message à l'utilisateur indiquant qu'il a perdu 300 points
        invalidate(); // Redessiner la vue pour refléter les changements
    }

    // Méthode pour le plus court chemin
    private List<Cell> dijkstra(Cell start, Cell end) {
        // Initialisation des structures de données pour Dijkstra
        Map<Cell, Integer> distances = new HashMap<>(); // Stocke la distance minimale depuis le départ
        Map<Cell, Cell> previous = new HashMap<>(); // Stocke le chemin pour reconstruire la trajectoire
        PriorityQueue<Cell> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get)); // File de priorité pour traiter les cellules les plus proches en premier

        // Initialisation des distances et du tableau des précédents
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Cell cell = cells[x][y];
                distances.put(cell, Integer.MAX_VALUE); // Initialiser toutes les distances à l'infini
                previous.put(cell, null); // Aucun chemin connu initialement
            }
        }
        // Définir la distance de la cellule de départ à 0 et l'ajouter à la file de priorité
        distances.put(start, 0);
        queue.add(start);

        // Boucle principale de l'algorithme de Dijkstra
        while (!queue.isEmpty()) {
            Cell current = queue.poll(); // Récupérer la cellule avec la plus petite distance
            // Si la cellule actuelle est la destination, on arrête la recherche
            if (current == end) {
                break;
            }
            // Parcourir tous les voisins accessibles de la cellule actuelle
            for (Cell neighbor : getNeighbors(current)) {
                int newDistance = distances.get(current) + 1; // La distance entre deux cellules adjacentes est de 1
                if (newDistance < distances.get(neighbor)) { // Si un chemin plus court est trouvé
                    distances.put(neighbor, newDistance); // Mettre à jour la distance minimale
                    previous.put(neighbor, current); // Mettre à jour le chemin
                    queue.add(neighbor); // Ajouter le voisin mis à jour dans la file de priorité
                }
            }
        }
        // Reconstruction du chemin le plus court en remontant depuis la destination
        List<Cell> path = new ArrayList<>();
        for (Cell at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path); // Inverser la liste pour obtenir le chemin dans le bon ordre
        return path; // Retourner le chemin le plus court
    }

    // Méthode pour  retourner une liste des cellules voisines accessibles (utilisée dans Dijkstra)
    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>(); // Liste des voisins accessibles
        if (!cell.topWall && cell.row > 0) neighbors.add(cells[cell.col][cell.row - 1]); // Vérifier si la cellule a un voisin en haut (absence de mur supérieur)
        if (!cell.bottomWall && cell.row < ROWS - 1) neighbors.add(cells[cell.col][cell.row + 1]); // Vérifier si la cellule a un voisin en bas (absence de mur inférieur)
        if (!cell.leftWall && cell.col > 0) neighbors.add(cells[cell.col - 1][cell.row]); // Vérifier si la cellule a un voisin à gauche (absence de mur gauche)
        if (!cell.rightWall && cell.col < COLS - 1) neighbors.add(cells[cell.col + 1][cell.row]); // Vérifier si la cellule a un voisin à droite (absence de mur droit)
        return neighbors; // Retourner la liste des voisins accessibles
    }

    //  Méthode pour calculer et afficher le chemin optimal entre la position actuelle du joueur et la sortie en utilisant l'algorithme de Dijkstra
    public void calculatePath() {
        path = dijkstra(player, exit); // Calculer le chemin le plus court entre la position du joueur et la sortie en utilisant Dijkstra
        if (showPath == false) { // Vérifier si l'affichage du chemin est désactivé
            if(totalScore >= 200) { // Vérifier si le joueur a suffisamment de points pour afficher le chemin
                totalScore -= 200; // Déduire 200 points du score total
                showPath = true; // Activer l'affichage du chemin
            }
            else
                // Afficher un message indiquant que le score est insuffisant
                Toast.makeText(getContext(), "Insufficient Score to show path (Minimum Score : 200)", Toast.LENGTH_SHORT).show();
        }

        // Mettre à jour l'affichage du score si un listener est défini
        if (scoreUpdateListener != null)
            scoreUpdateListener.onScoreUpdate(totalScore);
        invalidate(); // Redessiner l'écran pour afficher les changements
    }

    private void movePlayer(Direction direction) {
        // Enregistrez la position actuelle du joueur avant de tenter de se déplacer
        int currentPlayerCol = player.col;
        int currentPlayerRow = player.row;

        // Tenter de déplacer le joueur
        switch (direction) {
            case UP:
                if (!player.topWall)
                    player = cells[player.col][player.row - 1];
                break;
            case DOWN:
                if (!player.bottomWall)
                    player = cells[player.col][player.row + 1];
                break;
            case LEFT:
                if (!player.leftWall)
                    player = cells[player.col - 1][player.row];
                break;
            case RIGHT:
                if (!player.rightWall)
                    player = cells[player.col + 1][player.row];
                break;
        }

        // Vérifiez si le joueur a réellement bougé
        if (player.col != currentPlayerCol || player.row != currentPlayerRow) {
            // Le joueur a été déplacé vers une nouvelle case

            // Vérifiez si le joueur est revenu à la position précédente
            if (player.col == prevPlayerCol && player.row == prevPlayerRow) {
                totalScore -= 5; // Déduire 5 points pour retour en arrière
                if (totalScore < 0) totalScore = 0; // S'assurer que le score ne devienne pas négatif
                Toast.makeText(getContext(), "You went back! -5 points", Toast.LENGTH_SHORT).show();
            }

            // Mettre à jour la position précédente à la position actuelle avant le déplacement
            prevPlayerCol = currentPlayerCol;
            prevPlayerRow = currentPlayerRow;

            // Décrémenter movesAllowed uniquement si le joueur a bougé
            movesAllowed--;
            if (movesAllowed < 0) { // Si le joueur a dépasse le nombre de deplacements alloués, il perds 10 points a chaque mouvement
                totalScore -= 10;
                if (totalScore < 0) totalScore = 0; // S'assurer que le score ne devienne pas négatif
            }
        }

        // Recalculer le chemin après le déplacement du joueur
        path = dijkstra(player, exit);

        checkExit(); // Vérifiez si le joueur a atteint la sortie
        invalidate(); // Redessiner l'écran pour afficher les changements
    }


    // Méthode pour vérifier si le joueur a atteint la sortie
    private void checkExit(){
        // Vérifier si le joueur a atteint la sortie
        if(player== exit){
            if (!"easy".equals(diff)) { // Si la difficulté n'est pas "easy", arrêter le timer
                isTimerActive = false;
                timerHandler.removeCallbacks(timerRunnable);
            }
            totalScore+=300; // Ajouter 300 points au score total pour la victoire
            Toast.makeText(getContext(), "Congratulations! You won! \n(+300 points)", Toast.LENGTH_SHORT).show();
            collectedLetters.clear(); // Vider la liste des lettres collectées
            showPath=false; // Désactiver l'affichage du chemin (si activé)
            // Augmenter la taille du labyrinthe (plus grand à chaque victoire)
            COLS+=1;
            ROWS+=1;
            // Jouer un son de victoire
            mediaPlayer  = MediaPlayer.create(getContext(), R.raw.victory);
            mediaPlayer.start();

            // Arrêter et redémarrer le mouvement des obstacles pour garder une vitesse stable
            stopObstacleMovement();
            // Gestion spécifique en fonction du niveau de difficulté
            if ("easy".equals(diff) ) {
                if (COLS>12){
                    COLS = 12;
                    ROWS = 10;
                }
                // Récompenser le joueur s'il a utilisé exactement le nombre optimal de déplacements
                if(movesAllowed==30){
                    totalScore+=200;
                    Toast.makeText(getContext(), "Perfect! You found the shortest path! \n(+200 points)", Toast.LENGTH_SHORT).show();
                }
            } else if ("medium".equals(diff)) {
                if (COLS>17){
                    COLS = 17;
                    ROWS = 14;
                }
                // Récompenser le joueur s'il a utilisé exactement le nombre optimal de déplacements
                if(movesAllowed==20){
                    totalScore+=200;
                    Toast.makeText(getContext(), "Perfect! You found the shortest path! \n(+200 points)", Toast.LENGTH_SHORT).show();
                }
                // Pénaliser le joueur s'il n'a pas collecté au moins 2 mots avant d'atteindre la sortie
                if(wordsCollectedInRound<2){
                    totalScore -= 100; // Pénalité de 100 points
                    if (totalScore < 0) totalScore = 0; // Assurer que le score ne devienne pas négatif
                    Toast.makeText(getContext(), "Collect 2 words before reaching the exit! (-100 points)", Toast.LENGTH_SHORT).show();
                    currentRound--; // Rejouer le round actuel
                }
            } else if ("hard".equals(diff)) {
                if(COLS>20){
                    COLS = 20;
                    ROWS = 17;
                }
                // Récompenser le joueur s'il a utilisé exactement le nombre optimal de déplacements
                if(movesAllowed>=20){
                    totalScore+=200;
                    Toast.makeText(getContext(), "Perfect! You found the shortest path! \n(+200 points)", Toast.LENGTH_SHORT).show();
                }
                // Pénaliser le joueur s'il n'a pas collecté au moins 2 mots avant d'atteindre la sortie
                if(wordsCollectedInRound<2){
                    totalScore -= 100; // Pénalité de 100 points
                    if (totalScore < 0) totalScore = 0; // Assurer que le score ne devienne pas négatif
                    Toast.makeText(getContext(), "Collect 2 words before reaching the exit! (-100 points)", Toast.LENGTH_SHORT).show();
                    currentRound--; // Rejouer le round actuel
                }
            }
            currentRound++; // Passer au round suivant
            wordsCollectedInRound=0; // Réinitialiser le compteur de mots collectés pour le nouveau round
            createMaze(); // Générer un nouveau labyrinthe
        }
    }


    // Méthode pour gèrer les interactions tactiles
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Ne traiter l'événement ACTION_MOVE que si ACTION_DOWN a été détecté
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < 300) {         // Vérifier si le temps entre deux clics est inférieur à 300ms (détection d'un double-clic)

                float x = event.getX();
                float y = event.getY();

                // Convertir les coordonnées de l'écran en indices de colonnes et de lignes du labyrinthe
                int col = (int) ((x - hMargin) / cellSize);
                int row = (int) ((y - vMargin) / cellSize);

                // Vérifier si les indices sont dans les limites du labyrinthe
                if (col >= 0 && col < COLS && row >= 0 && row < ROWS) {
                    // Vérifier si le joueur est bien sur cette case
                    if (player.col == col && player.row == row) {
                        // Vérifier si la lettre n'a pas encore été collectée
                        if (!collectedCells[col][row]) {
                            collectedLetters.add(letters[col][row]); // Ajouter la lettre collectée
                            collectedCells[col][row] = true; // Marquer la cellule comme collectée
                            checkWord(); // Vérifier si un mot valide est formé
                            invalidate(); // Redessiner l'écran pour afficher les changements
                        }
                    }
                }
            }
            lastClickTime = clickTime; // Mettre à jour le temps du dernier clic
            return true;}
        // Gérer le mouvement du joueur en fonction du glissement du doigt
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();
            // Déterminer la position centrale du joueur dans la grille
            float playerCenterX = hMargin + (player.col+0.5f)*cellSize;
            float playerCenterY = vMargin + (player.row+0.5f)*cellSize;
            // Calculer la différence entre la position du toucher et celle du joueur
            float dx=x - playerCenterX;
            float dy=y - playerCenterY;
            // Calculer les valeurs absolues des distances
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);
            // Vérifier si le mouvement est significatif (supérieur à la taille d'une cellule)
            if(absDx> cellSize|| absDy> cellSize){
                if (absDx> absDy){
                    // Déplacement horizontal (gauche/droite)
                    if(dx > 0)
                        // Déplacer à droite
                        movePlayer(Direction.RIGHT);
                    else {
                        // Déplacer à gauche
                        movePlayer(Direction.LEFT);
                    }
                }
                else {
                    // Déplacement vertical (haut/bas)
                    if(dy > 0)
                        // Déplacer vers le bas
                        movePlayer(Direction.DOWN);
                    else
                        // Déplacer vers le haut
                        movePlayer(Direction.UP);
                }
            }
            return true; // Retourner vrai pour indiquer que l'événement est traité
        }
        return super.onTouchEvent(event);
    }

    private void checkWord() {
        // Construire le mot à partir des lettres collectées
        StringBuilder wordBuilder = new StringBuilder();
        for (Character letter : collectedLetters) {
            wordBuilder.append(letter);

        }
        String word = wordBuilder.toString().toUpperCase(); // Convertir le mot en majuscules
        Toast.makeText(getContext(), "Word Formed: " + word , Toast.LENGTH_SHORT).show(); // Afficher un message avec le mot formé

        // Vérifier si le mot formé existe dans le dictionnaire
        if (dictionary.contains(word)) {
            // Vérifier si le mot n'a pas déjà été utilisé
            if(!usedWords.contains(word)){
                usedWords.add(word); // Ajouter le mot à la liste des mots utilisés
                // Calculer le score en fonction de la longueur du mot
                int wordLength = word.length();
                int score = 0;
                score= score+(wordLength*100);
                totalScore += score;
                wordsCollectedInRound++; // Incrémenter le compteur de mots collectés
                Toast.makeText(getContext(), "Good job!\nWord found: " + word + " ( +"+ score + " points)", Toast.LENGTH_SHORT).show();
                // Jouer un son pour signaler la validation du mot
                mediaPlayer  = MediaPlayer.create(getContext(), R.raw.word_found);
                mediaPlayer.start();
                collectedLetters.clear(); // Réinitialiser la liste des lettres collectées après validation du mot
            }else{
                // Afficher un message si le mot a déjà été utilisé
                Toast.makeText(getContext(), "Word already used: " + word, Toast.LENGTH_SHORT).show();
            }
            // Réinitialiser les cellules collectées pour permettre la collecte d'un nouveau mot
            collectedCells = new boolean[COLS][ROWS];
        }
    }

    // Classe Cell
    private class  Cell{
        boolean topWall = true, leftWall = true, bottomWall = true, rightWall = true,
                visited = false;
        int col , row;

        // Constructeur de Classe Cell
        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    public interface ScoreUpdateListener {
        void onScoreUpdate(int score);
    }
}
