/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.ghost1;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn = false;
    private Random random = new Random();
    private static TextView ghostText;
    private static TextView gameStatus;
    private static Button challengeButton;
    private static Button restartButton;
    private static String wordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);

        ghostText = (TextView) findViewById(R.id.ghostText);
        gameStatus = (TextView) findViewById(R.id.gameStatus);
        challengeButton = (Button) findViewById(R.id.challengeButton);
        restartButton = (Button) findViewById(R.id.restartButton);

        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
           // dictionary = new FastDictionary(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            createToast("Could not load dictionary",500);
        }

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStart(null);
                createToast("Game restarts...",100);
            }
        });

        challengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                challenge(view);
            }
        });

        onStart(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    public boolean onStart(View view) {
        userTurn = random.nextBoolean();
        TextView text = (TextView) findViewById(R.id.ghostText);
        text.setText("");
        TextView label = (TextView) findViewById(R.id.gameStatus);
        if (userTurn) {
            label.setText(USER_TURN);
        } else {
            label.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void computerTurn() {
        //TextView label = (TextView) findViewById(R.id.gameStatus);
        // Do computer turn stuff then make it the user's turn again

        gameStatus.setText(COMPUTER_TURN);
        String word = ghostText.getText().toString();
        if (dictionary.isWord(word) && word.length() >= 4) {
            gameStatus.setText("Computer won");
        }
        else {
            String longerWord = dictionary.getAnyWordStartingWith(word);
            if (longerWord!=null) {
                char nextChar = longerWord.charAt(word.length());
                word += nextChar;
                ghostText.setText(word);
                gameStatus.setText(USER_TURN);
            } else {
                gameStatus.setText("you can't bluff this , you lost");
            }
        }

        userTurn = true;
        //label.setText(USER_TURN);
    }

    /**
     * Handler for user key presses.
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        char pressedKey = (char) event.getUnicodeChar();
        pressedKey = Character.toLowerCase(pressedKey);

        if(pressedKey >= 'a' && pressedKey <= 'z'){
            wordFragment = String.valueOf(ghostText.getText());
            wordFragment += pressedKey;
            ghostText.setText(wordFragment);
            //createToast("Computer's turn",100);
           /* if (dictionary.isWord(wordFragment)) {
                gameStatus.setText("VALID WORD");
            } else
                gameStatus.setText("INVALID WORD");*/
            gameStatus.setText(COMPUTER_TURN);
            Handler handler =new Handler();
            Runnable r=new Runnable() {
                @Override
                public void run() {
                    computerTurn();
                }
            };
            handler.postDelayed(r,1000);

            return true;
        }
        else
        return super.onKeyUp(keyCode, event);
    }

    public void createToast(String message, int time){
        Toast.makeText(getApplication(),message,time).show();
    }

    public void challenge(View view) {
        String currentWord = ghostText.getText().toString();
        if (currentWord.length() > 3 && dictionary.isWord(currentWord)) {
            gameStatus.setText("you won");
        }
        else {
            String anotherWord = dictionary.getAnyWordStartingWith(currentWord);
            if (anotherWord!=null) {
                gameStatus.setText("computer won");
                ghostText.setText(anotherWord);
            }
            else {
                gameStatus.setText("you won, computer lost this game");
            }
        }

    }
}
