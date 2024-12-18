package com.example.pcclub

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLoginClient1: Button
    private lateinit var btnLoginClient2: Button
    private lateinit var btnCancelAllBookings: Button
    private lateinit var tvBookingInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Инициализация элементов UI
        btnLoginClient1 = findViewById(R.id.btnLoginClient1)
        btnLoginClient2 = findViewById(R.id.btnLoginClient2)
        btnCancelAllBookings = findViewById(R.id.btnCancelAllBookings)
        tvBookingInfo = findViewById(R.id.tvBookingInfo)

        // Загружаем информацию о текущих бронированиях
        loadBookingInfo()

        // Обработка кнопки входа для Клиента 1
        btnLoginClient1.setOnClickListener {
            startBookingActivity("Client1")
        }

        // Обработка кнопки входа для Клиента 2
        btnLoginClient2.setOnClickListener {
            startBookingActivity("Client2")
        }

        // Обработка кнопки для отмены всех бронирований
        btnCancelAllBookings.setOnClickListener {
            cancelAllBookings()
        }
    }

    private fun startBookingActivity(clientName: String) {
        // Передаем имя клиента в BookingActivity
        val intent = Intent(this, BookingActivity::class.java)
        intent.putExtra("CLIENT", clientName)
        startActivity(intent)
    }

    private fun loadBookingInfo() {
        // Загружаем информацию о занятиях мест
        val bookedPlaces = sharedPreferences.getStringSet("booked_places", mutableSetOf()) ?: mutableSetOf()

        // Если есть забронированные места, выводим информацию
        if (bookedPlaces.isNotEmpty()) {
            val placesText = bookedPlaces.joinToString(", ")
            tvBookingInfo.text = "Забронированные места: $placesText"
        } else {
            tvBookingInfo.text = "Все места свободны"
        }
    }

    private fun cancelAllBookings() {
        val editor = sharedPreferences.edit()

        val allBookings = sharedPreferences.all
        for (entry in allBookings.entries) {
            if (entry.key.startsWith("client_")) {
                editor.remove(entry.key)
            }
        }

        editor.remove("booked_places")

        editor.apply()

        loadBookingInfo()

        Toast.makeText(this, "Все бронирования отменены!", Toast.LENGTH_SHORT).show()
    }
}
