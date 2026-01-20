package com.ximenez.joel.cazarpatos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ximenez.joel.cazarpatos.database.RankingPlayerDBHelper

class RankingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val jugadores = listOf(
            Player("Joel.Ximenez", 10),
            Player("Jugador2", 6),
            Player("Jugador3", 3),
            Player("Jugador4", 9)
        ).sortedByDescending { it.huntedDucks }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRanking)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RankingAdapter(jugadores)
        recyclerView.setHasFixedSize(true)

        OperacionesSqLite()
    }

    private fun OperacionesSqLite() {
        val db = RankingPlayerDBHelper(this)
        db.deleteAllRanking()
        db.insertRanking(Player("Jugador9", 7))
    }
}
