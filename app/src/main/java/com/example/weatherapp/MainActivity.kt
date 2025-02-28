package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.screens.MainCard
import com.example.weatherapp.screens.TabLayOut
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.json.JSONObject

const val API_KEY = "ba219286f3a14564b2042909250802"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogeState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(WeatherModel(
                        "",
                        "",
                        "0.0",
                        "",
                        "",
                        "0.0",
                        "0.0",
                        ""
                    )
                    )
                }
                if (dialogeState.value){
                    DialogSearch(dialogeState, onSubmit = {
                        getData(it, this, daysList, currentDay)
                    })
                }

                getData("Kungur", this, daysList, currentDay)
                Image(
                    painter = painterResource(
                        id = R.drawable.weather_bg
                    ),
                    contentDescription = "im1",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f),
                    contentScale = ContentScale.FillBounds
                )
                Column{
                    MainCard(currentDay, onClickSync = {
                        getData("Kungur", this@MainActivity, daysList, currentDay)
                    }, onClickSearch = {
                        dialogeState.value = true
                    })
                    TabLayOut(daysList, currentDay)
                }

            }
        }
    }
}

private fun getData(city: String, context: Context, daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=7&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            Log.d("MyLog", "Успешный ответ: $response")  // Логируем успешный ответ
            val list = getWeatherByDays(response)
            if (list.isNotEmpty()) {
                currentDay.value = list[0]
                daysList.value = list
            } else {
                Log.d("MyLog", "Список погоды пуст")
            }
        },
        { error ->
            Log.d("MyLog", "Ошибка Volley: ${error.message}")
            if (error.networkResponse != null) {
                Log.d("MyLog", "Код ошибки: ${error.networkResponse.statusCode}")
            }
        }
    )
    queue.add(sRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel>{
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()){
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()

            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
    )
    return list
}