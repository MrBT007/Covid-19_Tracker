package com.example.covid_19tracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.covid_19tracker.Adapter.StateAdapter
import com.example.covid_19tracker.Model.StateModel
import com.example.covid_19tracker.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONException


class MainActivity : BaseActivity() {
    private var chart: PieChart? = null
    private lateinit var binding: ActivityMainBinding
    var case: ArrayList<Int>? = null
    var field: ArrayList<String>? = null

    private lateinit var stateList: ArrayList<StateModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //implementing chart of India Covid-19
        chart = binding.pieChart

        //getting whole data from API
        getStateCovidInfo()
    }

    private fun configChartView() {
        val dataPieChart: ArrayList<PieEntry> = ArrayList()

//        Making a dataList for Pie chart
        for (i in field!!.indices) {
            dataPieChart.add(PieEntry(case?.elementAt(i)!!.toFloat(), field!!.elementAt(i)))
        }

        //set the data and label
        val pieDataSet = PieDataSet(dataPieChart, "Cases")

        //adding different colours for showing the area of cases in Pie Chart
        val colors: ArrayList<Int> = ArrayList()
        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        pieDataSet.colors = colors
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 13f

        // make dataset compatible to PieChart
        val pieData = PieData(pieDataSet)

        //setting up chart
        chart?.data = pieData
        chart?.description?.isEnabled = false
        chart?.centerText = "Covid-19 Cases"
        chart?.setCenterTextSize(20f)
        chart?.isDrawHoleEnabled = true
        chart?.invalidate()
        chart?.animate()
//        Toast.makeText(this, "ChartView", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
    }

    private fun getStateCovidInfo() {

        showProgressDialog("Please Wait...")
        case = ArrayList()
        field = ArrayList()
        //for api fetch
        val queue = Volley.newRequestQueue(applicationContext)

        //API
        val url = "https://api.rootnet.in/covid19-in/stats/latest"

        //checking if network is not there then show an error otherwise move ahead
        if (isNetworkConnected()) {
            val jsonObjectRequest =
                @SuppressLint("SetTextI18n")
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener() {
                    try {
                        //object
                        val dataObject = it.getJSONObject("data")
                        // getting the reference to summary object
                        val summaryObject = dataObject.getJSONObject("summary")

                        val totalCases: Int = summaryObject.getInt("total")
                        val totalRecovered: Int = summaryObject.getInt("discharged")
                        val totalDeaths: Int = summaryObject.getInt("deaths")
                        val indianCase = summaryObject.getInt("confirmedCasesIndian")
                        val foreignCase = summaryObject.getInt("confirmedCasesForeign")

                        //set total case
                        binding.total.text = "Total : ${totalCases.toString()}"

                        //add indian person cases and recovered cases to array list (for Pie chart)
                        case!!.add(indianCase)
                        case!!.add(totalRecovered)

//                        Toast.makeText(this, "${case!!.elementAt(0)} , $indianCase , $foreignCase , $deaths", Toast.LENGTH_SHORT).show()
                        //add indian person cases and recovered as field to array list (for Pie chart)
                        field?.add("Indian")
                        field?.add("Recovered")

                        binding.foreigners.text = "Foreigners : $foreignCase"
                        binding.deaths.text = "Deaths : $totalDeaths"

                        // get the chart view
                        configChartView()
//                        Toast.makeText(this, "${field?.elementAt(0)}, indianCase , foreignCase , deaths", Toast.LENGTH_SHORT).show()

                        // setting up the regional data (Recycler view)
                        val regionalArray = dataObject.getJSONArray("regional")
                        stateList = ArrayList()

                        //get object and add the object model to model list
                        for (i in 0 until regionalArray.length()) {
                            val regionalObject = regionalArray.getJSONObject(i)
                            val stateName = regionalObject.getString("loc")
                            val cases = regionalObject.getInt("totalConfirmed").toString()
                            val deaths = regionalObject.getInt("deaths").toString()
                            val recovered = regionalObject.getInt("discharged").toString()
                            val indian = regionalObject.getInt("confirmedCasesIndian").toString()
                            val foreigner =
                                regionalObject.getInt("confirmedCasesForeign").toString()

                            val stateModel =
                                StateModel(stateName, cases, indian, foreigner, recovered, deaths)

                            stateList.add(stateModel)
                        }

                        // set model list to custom adapter
                        val stateAdapter = StateAdapter(stateList)
                        // set adapter to recycler view
                        binding.rvRegionalCases.adapter = stateAdapter
                        // set recycler view's layout manager
                        binding.rvRegionalCases.layoutManager = LinearLayoutManager(this)

                        //If error
                    } catch (c: JSONException) {
                        Toast.makeText(this, "Some unexpected error occurred!!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        this,
                        "Volley error occurred!!",
                        Toast.LENGTH_SHORT
                    ).show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"

                        return headers
                    }
                }
            // adding object
            queue.add(jsonObjectRequest)

        } else {
            // Error for network Issue
            val dialog = android.app.AlertDialog.Builder(this)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                //Opening setting using implicit intent
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                this.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                //Closing the app
                ActivityCompat.finishAffinity(this)
            }
            dialog.create()
            dialog.show()

        }
    }

    // checking network connectivity
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
}