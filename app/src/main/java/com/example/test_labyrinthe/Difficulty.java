package com.example.test_labyrinthe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Difficulty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        //Initialisation des boutons en les accèdant a travers leurs id pour les manipuler ulterieurement
        Button easyButton = findViewById(R.id.easy_button);
        Button mediumButton = findViewById(R.id.medium_button);
        Button hardButton = findViewById(R.id.hard_button);

        // Si le bouton Easy est cliqué
        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Difficulty.this, GameView.class);
                intent.putExtra("DIFFICULTY","easy" ); //Envoi d'une variable DIFFICULTY de type string ayant comme valeur easy vers l'interface GameView
                startActivity(intent); // Navigation vers l'inferface GameView
            }
        });

        // Si le bouton Medium est cliqué
        mediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Difficulty.this, GameView.class);
                intent.putExtra("DIFFICULTY","medium" );//Envoi d'une variable DIFFICULTY de type string ayant comme valeur medium vers l'interface GameView
                startActivity(intent); // Navigation vers l'inferface GameView
            }
        });

        // Si le bouton Hard est cliqué
        hardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Difficulty.this, GameView.class);
                intent.putExtra("DIFFICULTY","hard" ); //Envoi d'une variable DIFFICULTY de type string ayant comme valeur hard vers l'interface GameView
                startActivity(intent); // Navigation vers l'inferface GameView
            }
        });
    }
}