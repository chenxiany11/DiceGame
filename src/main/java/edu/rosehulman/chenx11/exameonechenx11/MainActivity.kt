package edu.rosehulman.chenx11.exameonechenx11

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var game = DiceGame(this)
    private val bt = arrayOfNulls<Button>(5)
    private var score = StringBuilder()
    private var isClicked = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<Button>(R.id.roll_button).setOnClickListener { view ->
            //shows random values on the dice
            game.rollAndTakeTurn()
            updateView()
            for (num in 0 until 5) {
                val id = resources.getIdentifier("button$num", "id", packageName)
                bt[num] = findViewById(id)
                bt[num]?.text = game.getStringFor(num)
            }
        }
        for (num in 0 until 5) {
            val id = resources.getIdentifier("button$num", "id", packageName)
            bt[num] = findViewById(id)
            bt[num]?.setOnClickListener {
                game.toggleFrozenFor(num)
                updateView()
            }
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if (isClicked == true) {
                game.state = DiceGame.State.INITIAL
                updateView()
                isClicked = false
            }else {
                game.freezeAll()
                game.state = DiceGame.State.WAITING_FOR_RESET
                updateView()
                score.append(game.getBestScore())
                score.appendln()
                scores.text = score
                isClicked = true

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun showAddDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.edit_dialog_title))
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add, null, false)
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) {_, _ ->
           game.resetRoll()
            for (num in 0 until 5) {
                val id = resources.getIdentifier("button$num", "id", packageName)
                bt[num] = findViewById(id)
                bt[num]?.setOnClickListener {
                    game.toggleFrozenFor(num)
                    updateView()
                }
            }
            score.clear()
            scores.text = ""
            isClicked = false
            updateView()
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.ic_menu_delete -> {
                showAddDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateView() {
        game_state.text = game.getInstructionsLine1()
        instruction.text = game.getInstructionsLine2()
        for (num in 0 until 5) {
            if (game.state == DiceGame.State.INITIAL) {
                game.resetRoll()
                bt[num]?.isEnabled = true
                bt[num]?.background?.setColorFilter(null)
                game_state.text = game.getInstructionsLine1()
                instruction.text = game.getInstructionsLine2()
            }
            if (game.getFrozenFor(num)==false) {
                bt[num]?.background?.setColorFilter(null)
            }
            if (game.state == DiceGame.State.WAITING_FOR_RESET || game.getFrozenFor(num)==true) {
                bt[num]?.isEnabled = false
                bt[num]?.background?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
            }
            bt[num]?.text = game.getStringFor(num)
        }
    }
}