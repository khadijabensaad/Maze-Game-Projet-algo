package com.example.test_labyrinthe;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameView extends AppCompatActivity {
    private GameViews gameView;
    private String diff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_views);

        diff = getIntent().getStringExtra("DIFFICULTY"); //Obtention du contenu de la variable DIFFICULTY
        gameView = findViewById(R.id.gameView);
        gameView.setDimensions(diff); // Ajuster les dimensions de la grille selon le niveau de difficulté choisi

        Button button = findViewById(R.id.path_button);
        button.setOnClickListener(v -> gameView.calculatePath()); // Calculer et afficher le plus court chemin lors du click sur le bouton Show Path

    }
}
