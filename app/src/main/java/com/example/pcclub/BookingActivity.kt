package com.example.pcclub

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class BookingActivity : AppCompatActivity() {

    private lateinit var clientName: String
    private lateinit var tvClientName: TextView
    private lateinit var tvFreeSpaces: TextView
    private lateinit var tvTimeRemaining: TextView
    private lateinit var btnBook: Button
    private lateinit var btnCancelBooking: Button
    private lateinit var numberPickerTime: NumberPicker
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gridPlaces: List<Button>

    private var selectedPlace: String? = null
    private var bookingTimeInMillis: Long = 0L
    private var countDownTimer: CountDownTimer? = null

    private val places = arrayOf("Место 1", "Место 2", "Место 3", "Место 4", "Место 5")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        clientName = intent.getStringExtra("CLIENT") ?: "Client1"

        tvClientName = findViewById(R.id.tvClientName)
        tvFreeSpaces = findViewById(R.id.tvFreeSpaces)
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining)
        btnBook = findViewById(R.id.btnBook)
        btnCancelBooking = findViewById(R.id.btnCancelBooking)
        numberPickerTime = findViewById(R.id.numberPickerTime)

        gridPlaces = listOf(
            findViewById(R.id.place1),
            findViewById(R.id.place2),
            findViewById(R.id.place3),
            findViewById(R.id.place4),
            findViewById(R.id.place5)
        )

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        tvClientName.text = "Привет, $clientName!"

        loadBooking()

        val availableTimes = ArrayList<String>()
        for (i in 1..4) { // 1: 30 минут, 2: 60 минут, 3: 90 минут, 4: 120 минут
            availableTimes.add("${i * 30} мин")
        }

        numberPickerTime.minValue = 0
        numberPickerTime.maxValue = availableTimes.size - 1
        numberPickerTime.displayedValues = availableTimes.toTypedArray()
        numberPickerTime.value = 0 // Устанавливаем значение по умолчанию (30 мин)

        for (placeButton in gridPlaces) {
            placeButton.setOnClickListener {
                selectPlace(placeButton)
            }
        }

        btnBook.setOnClickListener {
            if (selectedPlace != null) {
                bookPlace()
            }
        }

        btnCancelBooking.setOnClickListener {
            cancelBooking()
        }
    }

    private fun loadBooking() {
        val bookedPlaces = sharedPreferences.getStringSet("booked_places", mutableSetOf()) ?: mutableSetOf()

        val availablePlaces = places.filter { !bookedPlaces.contains(it) }
        tvFreeSpaces.text = "Свободные места: ${availablePlaces.joinToString(", ")}"

        val bookedPlace = sharedPreferences.getString(clientName, null)
        if (bookedPlace != null) {
            tvFreeSpaces.text = "Ваше место: $bookedPlace"
            val bookingTime = sharedPreferences.getLong("${clientName}_time", 0L)
            if (bookingTime > 0L) {
                startTimer(bookingTime)
            } else {
                tvTimeRemaining.text = "Время до окончания бронирования: 00:00"
            }
        }
    }

    private fun selectPlace(placeButton: Button) {
        selectedPlace = placeButton.text.toString()

        val bookedPlaces = sharedPreferences.getStringSet("booked_places", mutableSetOf()) ?: mutableSetOf()
        if (bookedPlaces.contains(selectedPlace)) {
            tvFreeSpaces.text = "Это место уже занято!"
        } else {
            tvFreeSpaces.text = "Вы выбрали: $selectedPlace"
        }
    }

    private fun bookPlace() {
        if (selectedPlace != null) {
            val bookedPlaces = sharedPreferences.getStringSet("booked_places", mutableSetOf()) ?: mutableSetOf()
            if (bookedPlaces.contains(selectedPlace)) {
                tvFreeSpaces.text = "Это место уже занято другим клиентом!"
                return
            }

            bookedPlaces.add(selectedPlace!!)
            sharedPreferences.edit().putStringSet("booked_places", bookedPlaces).apply()

            sharedPreferences.edit().putString(clientName, selectedPlace).apply()

            val selectedTimeInMinutes = (numberPickerTime.value + 1) * 30  // value = 0 означает 30 мин, 1 = 60 мин, и т.д.
            bookingTimeInMillis = (selectedTimeInMinutes * 60 * 1000).toLong() // Переводим в миллисекунды
            sharedPreferences.edit().putLong("${clientName}_time", bookingTimeInMillis).apply()

            startTimer(bookingTimeInMillis)
        }
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimeRemaining.text = "Время до окончания бронирования: $minutes:$seconds"
            }

            override fun onFinish() {
                tvTimeRemaining.text = "Время до окончания бронирования: 00:00"
                cancelBooking()
            }
        }
        countDownTimer?.start()
    }

    private fun cancelBooking() {
        val bookedPlace = sharedPreferences.getString(clientName, null)

        if (bookedPlace != null && bookedPlace == selectedPlace) {
            sharedPreferences.edit().remove(clientName).apply()
            sharedPreferences.edit().remove("${clientName}_time").apply()

            val bookedPlaces = sharedPreferences.getStringSet("booked_places", mutableSetOf()) ?: mutableSetOf()

            bookedPlaces.remove(selectedPlace)
            sharedPreferences.edit().putStringSet("booked_places", bookedPlaces).apply()

            countDownTimer?.cancel()
            tvTimeRemaining.text = "Время до окончания бронирования: 00:00"

            loadBooking()
        } else {
            tvFreeSpaces.text = "Вы не можете отменить бронирование другого клиента!"
        }
    }
}
