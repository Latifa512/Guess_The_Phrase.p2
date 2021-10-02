package com.example.guess_the_phrasep2

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var cl: ConstraintLayout
    private lateinit var guField: EditText
    private lateinit var guButton: Button
    private lateinit var messages: ArrayList<String>
    private lateinit var tvPhrase: TextView
    private lateinit var tvLett: TextView
    private lateinit var myHighScore: TextView

    private val answer = "this is the secret phrase"
    private val myAnswerDictionary = mutableMapOf<Int, Char>()
    private var myAnswer = ""
    private var guessedLetters = ""
    private var count = 0
    private var guessPhrase = true

    private lateinit var sharedPreferences: SharedPreferences

    private var score = 0
    private var highScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        highScore = sharedPreferences.getInt("HighScore", 0)

        myHighScore = findViewById(R.id.tvhs)
        myHighScore.text = "High Score: $highScore"


        for(i in answer.indices){
            if(answer[i] == ' '){
                myAnswerDictionary[i] = ' '
                myAnswer += ' '
            }else{
                myAnswerDictionary[i] = '*'
                myAnswer += '*'
            }
        }

        cl = findViewById(R.id.clayout)
        messages = ArrayList()

        rvMessages.adapter = MessageAdapter(this, messages)
        rvMessages.layoutManager = LinearLayoutManager(this)

        guField = findViewById(R.id.etGuessField)
        guButton = findViewById(R.id.btGuessButton)
        guButton.setOnClickListener { addMessage() }

        tvPhrase = findViewById(R.id.tvphrase)
        tvLett = findViewById(R.id.tvLett)

        updateText()
    }

    private fun addMessage(){
        val msg = guField.text.toString()

        if(guessPhrase){
            if(msg == answer){
                disableEntry()
                updateScore()
                showAlertDialog("You win!\n\nPlay again?")
            }else{
                messages.add("Wrong guess: $msg")
                guessPhrase = false
                updateText()
            }
        }else{
            if(msg.isNotEmpty() && msg.length==1){
                myAnswer = ""
                guessPhrase = true
                checkLetters(msg[0])
            }else{
                Snackbar.make(cl, "Please enter one letter only", Snackbar.LENGTH_LONG).show()
            }
        }

        guField.text.clear()
        guField.clearFocus()
        rvMessages.adapter?.notifyDataSetChanged()
    }

    private fun disableEntry(){
        guButton.isEnabled = false
        guButton.isClickable = false
        guField.isEnabled = false
        guField.isClickable = false
    }

    private fun updateText(){
        tvPhrase.text = "Phrase:  " + myAnswer.toUpperCase()
        tvLett.text = "Guessed Letters:  " + guessedLetters
        if(guessPhrase){
            guField.hint = "Guess the full phrase"
        }else{
            guField.hint = "Guess a letter"
        }
    }

    private fun checkLetters(guessedLetter: Char){
        var found = 0
        for(i in answer.indices){
            if(answer[i] == guessedLetter){
                myAnswerDictionary[i] = guessedLetter
                found++
            }
        }
        for(i in myAnswerDictionary){myAnswer += myAnswerDictionary[i.key]}
        if(myAnswer==answer){
            disableEntry()
            updateScore()
            showAlertDialog("You win!\n\nPlay again?")
        }
        if(guessedLetters.isEmpty()){guessedLetters+=guessedLetter}else{guessedLetters+=", "+guessedLetter}
        if(found>0){
            messages.add("Found $found ${guessedLetter.toUpperCase()}(s)")
        }else{
            messages.add("No ${guessedLetter.toUpperCase()}s found")
        }
        count++
        val guessesLeft = 10 - count
        if(count<10){messages.add("$guessesLeft guesses remaining")}
        updateText()
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun updateScore(){
        score = 10 - count
        if(score >= highScore){
            highScore = score
            with(sharedPreferences.edit()) {
                putInt("HighScore", highScore)
                apply()
            }
            Snackbar.make(cl, "NEW HIGH SCORE!", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showAlertDialog(title: String) {

        val dialogBuilder = AlertDialog.Builder(this)


        dialogBuilder.setMessage(title)

            .setCancelable(false)

            .setPositiveButton("Yes", DialogInterface.OnClickListener {
                    dialog, id -> this.recreate()
            })

            .setNegativeButton("No", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })


        val alert = dialogBuilder.create()

        alert.setTitle("Game Over")

        alert.show()
    }
}