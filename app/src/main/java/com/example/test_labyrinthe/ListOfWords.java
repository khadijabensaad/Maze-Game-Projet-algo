package com.example.test_labyrinthe;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;

public class ListOfWords extends AppCompatActivity {

    private TextView words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_words);

        words = findViewById(R.id.words);
        readWordsFromFile();
    }

    // Méthode pour charger des mots à partir d'un dictionnaire
    private void readWordsFromFile() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // chargement du contenu du fichier dictionary.txt dans le dossier raw sous le dossier res
            InputStream inputStream = getResources().openRawResource(R.raw.dictionary);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            boolean isFirstWord = true; // Pour éviter d'ajouter un trait d'union avant le premier mot

            while ((line = reader.readLine()) != null) {
                // Ajoutez un trait d'union avant chaque mot (sauf le premier)
                if (!isFirstWord) {
                    stringBuilder.append(" - ");
                } else {
                    isFirstWord = false;
                }

                // Ajouter le mot
                stringBuilder.append(line);
            }

            // Fermer the reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Afficher les mots dans le TextView
        words.setText(stringBuilder.toString());
    }
}