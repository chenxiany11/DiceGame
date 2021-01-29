package edu.rosehulman.chenx11.exameonechenx11

import android.content.Context
import android.util.Log

class DiceGame(val context: Context) {
    enum class State {
        INITIAL, WAITING_FOR_RESET, PLAYING
    }

    var state = State.INITIAL
    var size = 5
    private var remainingRolls = 3
    private var dice = Array(size) { Die() }

    fun resetRoll() {
        state = State.INITIAL
        remainingRolls = 3
        for (k in 0 until size) {
            dice[k].frozen = false
        }
    }

    fun freezeAll() {
        for (k in 0 until size) {
            dice[k].frozen = true
        }
    }

    fun getStringFor(index: Int) = if (state == State.INITIAL) " " else dice[index].value.toString()

    fun getFrozenFor(index: Int) = dice[index].frozen

    fun toggleFrozenFor(index: Int) {
        if (state == State.PLAYING) {
            dice[index].toggleFrozen()
        }
    }

    fun rollAndTakeTurn() {
        if (state == State.WAITING_FOR_RESET) {
            return
        } // can't roll if waiting for reset.
        state = State.PLAYING
        if (remainingRolls > 0) {
            roll()
            remainingRolls--
        }
    }

    private fun roll() {
        for (die in dice) {
            die.roll()
        }
    }

    fun getInstructionsLine1(): String {
        // TODO: Use a string resource instead. Best is to make the pluralization of "roll" work correctly.
        return context.resources.getQuantityString(R.plurals.the_instruction, remainingRolls, remainingRolls)
    }

    fun getInstructionsLine2(): String {
        // TODO: Make the string resources for each of these.
        return if (state == State.WAITING_FOR_RESET) {
            context.getString(R.string.reset)
        } else {
            context.getString(if (remainingRolls > 0) R.string.open_instruction else R.string.instructions_record)
        }
    }

    fun getBestScore(): String {
        // From https://www.memory-improvement-tips.com/support-files/yahtzee-score-sheets.pdf
        // We find scores for Yahtzee, Lg Straight, Sm Straight, Full House, 4 of a kind, 3 of a kind,
        // then the upper section (1-6's).
        val allScores = HashMap<String, Int>()
        allScores["YAHTZEE"] = yahtzee()
        allScores["CHANCE"] = chance()
        allScores["SMALL STRAIGHT"] = smallStraight()
        allScores["LARGE STRAIGHT"] = largeStraight()
        allScores["FULL HOUSE"] = fullHouse()
        allScores["THREE OF A KIND"] = threeOfAKind()
        allScores["FOUR OF A KIND"] = fourOfAKind()

        // For the upper section (1-6's), we simplify by returning only the value of the dice
        // with that number of pips.
        // CONSIDER: It's trickier than this since the more times that number occurs, the
        // more valuable it is in the game (4 twos is usually better than 2 fives, even though
        // it scores fewer points.
        // And these will always have fewer points than chance. So we don't expect to see
        // any of these appear once chance() is implemented.
        allScores["ONES"] = ones()
        allScores["TWOS"] = twos()
        allScores["THREES"] = threes()
        allScores["FOURS"] = fours()
        allScores["FIVES"] = fives()
        allScores["SIXES"] = sixes()
        Log.d("YATLOG", "Scores: $allScores")

        return bestScoreFromMap(allScores)
    }

    private fun bestScoreFromMap(map: HashMap<String, Int>): String {
        var maxScore = 0
        var maxKey = ""
        for ((key, value) in map) {
            if (value > maxScore) {
                maxKey = key
                maxScore = value
            }
        }
        return "$maxKey scores $maxScore"
    }

    // Scoring from https://en.wikipedia.org/wiki/Yahtzee
    private fun yahtzee() = if (dice.count { it.value == dice[0].value } == size) 50 else 0

    //  Update chance to return the sum of the dice. Doing it in 1 line would be cool, but not required.
    // Note that 3 of a kind and 4 of a kind depend on the value of chance.
    private fun chance(): Int{
        var sum = 0
        for (value in 0 ..5){
            sum = sum + dice[0].value
        }
        return sum;
    }
    private fun largeStraight(): Int {
        return if (runLength() == 5) 40 else 0
    }

    private fun smallStraight(): Int {
        return if (runLength() >= 4) 30 else 0
    }

    private fun runLength(): Int {
        val sortedValues = dice.map { it.value }.sorted()
        var count = 1
        for (k in 0 until size - 1) {
            if (sortedValues[k + 1] == sortedValues[k] + 1) {
                count++
            } else if (sortedValues[k + 1] >= sortedValues[k] + 2) {
                count = 1 // reset
            }
        }
        return count
    }

    private fun fullHouse(): Int {
        // Note: returns true even if it is a yahtzee.
        val sortedValues = dice.map { it.value }.sorted()
        val isFullHouse =
            sortedValues[0] == sortedValues[1] && sortedValues[3] == sortedValues[4]
                    && (sortedValues[2] == sortedValues[1] || sortedValues[2] == sortedValues[3])
        return if (isFullHouse) 25 else 0
    }

    private fun fourOfAKind() = nOfAKind(4)
    private fun threeOfAKind() = nOfAKind(3)

    private fun nOfAKind(n: Int): Int {
        val counts = getCounts()
        counts.max()
        return if (counts.max()!! >= n) {
            chance()
        } else 0
        return 0;
    }

    private fun getCounts(): ArrayList<Int> {
        val counts = arrayListOf(0, 0, 0, 0, 0, 0, 0)
        for (k in 0 until size) {
            counts[dice[k].value]++
        }
        return counts
    }

    private fun ones() = countValueOnlyOf(1)
    private fun twos() = countValueOnlyOf(2)
    private fun threes() = countValueOnlyOf(3)
    private fun fours() = countValueOnlyOf(4)
    private fun fives() = countValueOnlyOf(5)
    private fun sixes() = countValueOnlyOf(6)
    private fun countValueOnlyOf(value: Int) = value * dice.count { it.value == value }
}
