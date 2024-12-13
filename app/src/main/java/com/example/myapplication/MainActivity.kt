package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var reportList: MutableList<Report>
    private lateinit var adapter: ReportAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        // Initialize Firebase App
        FirebaseApp.initializeApp(this)?.let {
            Log.d("FirebaseInit", "Firebase initialized successfully.")
        } ?: Log.e("FirebaseInit", "Failed to initialize Firebase.")

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("Reports")
        database.get().addOnSuccessListener { snapshot ->
            Log.d("FirebaseTest", "Snapshot: ${snapshot.value}")
        }.addOnFailureListener { e ->
            Log.e("FirebaseTest", "Error: ${e.message}")
        }
        Log.d("RecyclerViewDebug", "Report list size: ${reportList.size}")
        Log.d("RecyclerViewDebug", "Adapter item count: ${adapter.itemCount}")

        // Initialize report list and adapter
        reportList = mutableListOf()
        adapter = ReportAdapter(reportList)

        // Setup RecyclerView
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

        // Fetch reports from Firebase
        fetchReports()

        // Set up FAB to navigate to report submission screen
        binding.fabAddReport.setOnClickListener {
            Log.d("FABClick", "Navigating to ReportSubmissionActivity")
            startActivity(Intent(this, ReportSubmissionActivity::class.java))
        }
    }

    private fun fetchReports() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                if (snapshot.exists()) {
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue<Report>()
                        if (report != null) {
                            reportList.add(report)
                        }
                    }
                    Log.d("FetchReports", "Reports loaded: ${reportList.size}")
                } else {
                    Log.d("FetchReports", "No reports found.")
                }
                binding.tvEmptyMessage.visibility = if (reportList.isEmpty()) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchReports", "Failed to fetch reports: ${error.message}")
                Toast.makeText(this@MainActivity, "Failed to fetch reports", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
