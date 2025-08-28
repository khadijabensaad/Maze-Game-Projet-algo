package com.example.test_labyrinthe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Jouer le son de l'application
        mediaPlayer  = MediaPlayer.create(MainActivity.this, R.raw.bakcground_music);
        mediaPlayer.start();

        //Initialisation des boutons en les accèdant a travers leurs id pour les manipuler ulterieurement
        Button playButton = findViewById(R.id.play_button);
        Button list_of_words_button = findViewById(R.id.list_of_words_button);
        Button gameRulesButton = findViewById(R.id.game_rules_button);

        // Si le bouton Play est cliqué
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Difficulty.class);
                startActivity(intent); // Navigation vers l'inferface Difficulty
            }
        });

        // Si le bouton List Of Words est cliqué
        list_of_words_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListOfWords.class);
                startActivity(intent); // Navigation vers l'inferface ListOfWords
            }
        });

        // Si le bouton Game Rules est cliqué
        gameRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameRules.class);
                startActivity(intent); // Navigation vers l'inferface GameRules
            }
        });
    }
}