package com.delivery.sopo

import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        calendar.time = simpleDateFormat.parse("2020-01")

        println("calendar : $calendar")
        println("YEAR : ${calendar.get(Calendar.YEAR)}")
        println("MONTH : ${calendar.get(Calendar.MONTH)}")

        println("DATE : ${calendar.get(Calendar.DATE)}")


    }
}
