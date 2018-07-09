package com.zelther.emojit;


import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

public class MainActivity extends Activity {



    LinearLayout keyboardLayout; //The keyboard layout where there are all the button, in the bottom.
    LinearLayout answerLayout; //The answer layout where there is the space for the word to guess.
    int [] answerLettersNum; //an array which holds the length of each word in the answer
    TextView answer []; //an array of the chosen keys
    ArrayList<View> blankSpaces; //an array of the blank arrays which replaces the keys from the keyboard
    LinearLayout.LayoutParams keyboardRowParams; //LayoutParams for the keys in the keyboard's row
    LevelHelper levelHelper; //The level helper
    TextView emojiTV,levelTV,debugTV; //The text view where the emojis are and the text view where the level is
    Button endButton; //the button in the end screen
    Dialog endDialog; //the dialog displayed in the end
    SharedPreferences sp; //for saving the last level

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugTV = findViewById(R.id.debugTV);
        debugTV.setText("Version:" + BuildConfig.VERSION_NAME +" pre-alpha. itaizelther@gmail.com");

        sp = getSharedPreferences("data",0);

        //gets reference to the emoji text view and level text view
        emojiTV = findViewById(R.id.EmojiTV);
        levelTV = findViewById(R.id.levelTV);
        //gets reference to the keyboard layout
        keyboardLayout = findViewById(R.id.keyboardLayout);

        //Getting the data to start building the layout
        levelHelper = new LevelHelper(sp.getInt("level",1),getResources().openRawResource(R.raw.levels));

        //Set the keyboard's keys's layout parameters
        keyboardRowParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT,10f);
        keyboardRowParams.gravity = Gravity.CENTER;


        //gets the answer Layout pointer
       answerLayout = findViewById(R.id.answerLayout);
       answerLayout.setGravity(Gravity.CENTER);

       blankSpaces = new ArrayList<>();

       endDialog = new Dialog(this);
       endDialog.setContentView(R.layout.ending_screen);
       endDialog.setCancelable(false);
       endButton = endDialog.findViewById(R.id.backEndDialog);
       endButton.setOnClickListener(new endButtonListener());

       resetALevel();
    }
    /**
     * This method rearrange the keyboard field each time called using the current answer letters.
     */
    public void rearrangeKeyboard(ArrayList<Character> needLetters) {

        char [] letters = new char[20];
        Random random = new Random();
        while (!needLetters.isEmpty()) {
            int randomInt = random.nextInt(letters.length);
            if(letters[randomInt]=='\u0000')
                letters[randomInt] = needLetters.remove(0);
        }
        for(int i=0; i<20; i++) {
            if(letters[i]=='\u0000') letters[i] = (char)Character.toUpperCase(random.nextInt(26) + 'a');
        }
        keyboardLayout.removeAllViews();

        //Adds all the buttons for the keyboard. loop for rows and inside loop for button in each row.
        for(int i=0; i<2 ; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,10f)); //2 rows with each 50% (10/20)
            row.setWeightSum(100f); //10 buttons with each 10% out of 100%
            for(int j=0;j<10;j++) {
                TextView key = new TextView(this);
                key.setText(String.valueOf(letters[i*10+j])); //set the random letter
                key.setLayoutParams(keyboardRowParams);
                key.setOnClickListener(new keyboardListener());
                key.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);
                key.setGravity(Gravity.CENTER);
                key.setBackground(getDrawable(R.drawable.keyboad_key)); //custom background xml
                key.setTag("keyboard");
                row.addView(key);
            }
            keyboardLayout.addView(row);
        }
    }

    /**
     * This method rearrange the answer field each time called using the current answer word length and letters, and which buttons is currently chosen.
     */
    public void rearrangeAnswerRows() {
        answerLayout.removeAllViews(); //clear the field
        boolean twoRows = answerLettersNum.length > 1 && levelHelper.getLetters().length > 5; //check if we need 2 rows or one row
        LinearLayout letterRows[] = new LinearLayout[twoRows ? 2:1]; //set 2 rows if we need, 1 otherwise
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.setWeightSum(twoRows ? 20f : 10f); // 20% total for 2 rows, 10% for one
        for(int i=0; i < letterRows.length;i++) { // will repeat once for each row (1 or 2)
            letterRows[i] = new LinearLayout(this);
            //adds the row with 10%. if there are two rows will set size to 50% (10/20), otherwise will set to 100% (10/10)
            letterRows[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,0,10f));
            letterRows[i].setOrientation(LinearLayout.HORIZONTAL);
            answerLayout.addView(letterRows[i]);
        }

        //see middleNumericArray documentation
        int middleArray = ProjectUtiles.middleNumericArray(answerLettersNum);

        //set the current row which is the first. when we have to skip to the second row, it will change to the second row.
        LinearLayout currentRow = letterRows[0];

        //TODO replace from absolute number to percents in case there will be a word too long
        LinearLayout.LayoutParams letterParams = new LinearLayout.LayoutParams(80, 110);
        letterParams.setMargins(5,0,5,0);
        letterParams.gravity = Gravity.CENTER;

        //this is used for the button adding. each loop which a letter (blank or button) added, this value added to keep track of the letter number
        int letterNum = 0;

        //removes all buttons parent, so there will no be "The specified child already has a parent" error
        for(TextView b:answer) {
            if (b!=null && b.getParent()!=null)
                ((ViewGroup)b.getParent()).removeView(b);
        }

        /* this loop runs the number of words in the answer. if the word needs new line (see ProjectUtiles.middleNumericArray()), the loop cast the current row to the second row.
        otherwise, if this is not the first word, the program adds normal space.
        afterwards, there is nested loop which runs the number of letters in the current word. the program checks if the user has already chosen a letter for this spot by
        checking for value in the answer array, which has the length of the letters in the answer string. it uses the letterNum to know what number to choose,
        and then adds the button. if there is no value, it adds a custom view for empty letter.
         */
        for(int j=0; j<answerLettersNum.length; j++) {
            if(j==middleArray && twoRows) {
                currentRow = letterRows[1];
            } else if(j!=0) {
                View spaceView = new View(this);
                spaceView.setBackgroundColor(Color.TRANSPARENT);
                spaceView.setLayoutParams(letterParams);
                currentRow.addView(spaceView);
            }
            for(int i=0; i<answerLettersNum[j];i++) {
                if(answer[letterNum]!=null) {
                    answer[letterNum].setLayoutParams(letterParams);
                    currentRow.addView(answer[letterNum]);
                } else {
                    View v = new View(this);
                    v.setLayoutParams(letterParams);
                    v.setBackground(getDrawable(R.drawable.answer_letter_background));
                    currentRow.addView(v);
                }
                letterNum++;
            }
        }
    }


    class keyboardListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            TextView btn = (TextView) v;

            if(btn.getTag()=="keyboard") { //if the button was clicked from the keyboard field
                int answerIndex = ProjectUtiles.getFirstNullIndex(answer);
                if(answerIndex!=-1) { //if there is clear space in the answer array
                    //remove the button and adds empty view instead, adds to the answer array and rearrange the answer field
                    btn.setTag("answer");
                    LinearLayout btnRow = (LinearLayout) btn.getParent();
                    int rowIndex = btnRow.indexOfChild(btn);
                    View spaceView = new View(MainActivity.this);
                    spaceView.setLayoutParams(new LinearLayout.LayoutParams(0,0,10f));
                    btnRow.addView(spaceView,rowIndex);
                    btnRow.removeView(btn);
                    answer[answerIndex] = btn;
                    blankSpaces.add(spaceView);
                    rearrangeAnswerRows();

                    if(ProjectUtiles.isListFull(answer)) { //check if the user submited whole answer and then checkes it.
                        String attemptedWord = "";
                        for(TextView b:answer) {
                            attemptedWord += b.getText().toString();
                        }
                        if(attemptedWord.equals(levelHelper.getWord().replaceAll(" ",""))) {
                            if(levelHelper.nextLevel())
                                resetALevel();
                            else { //ending screen
                                endDialog.show();
                            }
                        }
                    }
                }
            } else if(v.getTag()=="answer") { //if the button was clicked from the answer
                //remove the view and adds the clicked button instead, remove it from the answer field and rearrange the answer field
                btn.setTag("keyboard");
                answer = ProjectUtiles.removeToNull(answer,btn);
                View availableBlankSpace = blankSpaces.remove(0);
                LinearLayout btnRow = (LinearLayout) availableBlankSpace.getParent();
                int rowIndex = btnRow.indexOfChild(availableBlankSpace);
                ((ViewGroup)btn.getParent()).removeView(btn);
                btnRow.removeView(availableBlankSpace);
                btn.setLayoutParams(keyboardRowParams);
                btnRow.addView(btn,rowIndex);
                rearrangeAnswerRows();
            }
        }
    }

    /**
     * Reset all the layout which represents the level data
     */
    public void resetALevel() {
        // defines the length of the word - using levelHelper
        answerLettersNum = new int[levelHelper.getWordsNum()];
        answerLettersNum = levelHelper.getWordsLengths();
        answer = new TextView[levelHelper.getLetters().length];
        emojiTV.setText(levelHelper.getEmoji());
        blankSpaces.clear();
        levelTV.setText("Level "+levelHelper.getLevel());
        rearrangeAnswerRows();
        rearrangeKeyboard(ProjectUtiles.fromCharArrayToArrayList(levelHelper.getLetters()));

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("level",levelHelper.getLevel());
        editor.commit();
    }


    public class endButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            levelHelper = new LevelHelper(1,getResources().openRawResource(R.raw.levels));
            resetALevel();
            endDialog.cancel();
        }
    }
}
