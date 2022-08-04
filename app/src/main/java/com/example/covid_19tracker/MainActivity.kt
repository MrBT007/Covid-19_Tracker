package com.example.covid_19tracker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import com.example.covid_19tracker.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class MainActivity : AppCompatActivity() {
    private var chart: PieChart? = null
    private lateinit var binding: ActivityMainBinding
    private val dummySalary = listOf(100,200,300)
    private val dummyMonth = listOf("Death","New Case","Recovered")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        chart = binding.pieChart
        configChartView()
    }
    private fun configChartView()
    {
        val dataPieChart:ArrayList<PieEntry> = ArrayList()


        for (i in dummySalary.indices)
        {
            dataPieChart.add(PieEntry(dummySalary.elementAt(i).toFloat(),dummyMonth.elementAt(i)))
        }

        val pieDataSet:PieDataSet = PieDataSet(dataPieChart,"Cases")

        var colors:ArrayList<Int> = ArrayList()
        for(color in ColorTemplate.MATERIAL_COLORS)
        {
            colors.add(color)
        }
        for(color in ColorTemplate.VORDIPLOM_COLORS)
        {
            colors.add(color)
        }
        pieDataSet.setColors(colors)
        pieDataSet.setValueTextColor(Color.BLACK)
        pieDataSet.valueTextSize = 16f

        val pieData = PieData(pieDataSet)

        chart?.data = pieData
        chart?.invalidate()
        chart?.description?.isEnabled = false
        chart?.centerText = "Covid-19 Cases"
        chart?.setCenterTextSize(24f)
        chart?.isDrawHoleEnabled = true
        chart?.animate()
    }
}