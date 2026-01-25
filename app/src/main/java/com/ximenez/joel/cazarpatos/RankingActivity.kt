package com.ximenez.joel.cazarpatos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.ximenez.joel.cazarpatos.database.RankingPlayerDBHelper

class RankingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)
        //Codigo anterior comentado ...
        consultarPuntajeJugadores()
    }

    fun consultarPuntajeJugadores() {
        val db = Firebase.firestore
        db.collection("ranking")
            .orderBy("huntedDucks", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d(EXTRA_LOGIN, "Success getting documents")
                var jugadores = ArrayList<Player>()
                for (document in result) {
                    val jugador = document.toObject(Player::class.java)
                    //val jugador = document.toObject<Player>()
                    jugadores.add(jugador)
                }
                //Poblar en RecyclerView información usando mi adaptador
                val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking);
                recyclerViewRanking.layoutManager = LinearLayoutManager(this);
                recyclerViewRanking.adapter = RankingAdapter(jugadores);
                recyclerViewRanking.setHasFixedSize(true);
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error getting documents.", exception)
                Toast.makeText(this, "Error al obtener datos de jugadores", Toast.LENGTH_LONG)
                    .show()
            }
    }
}
