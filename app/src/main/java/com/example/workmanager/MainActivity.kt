package com.example.workmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.workmanager.WorkManager.ComPressManger
import com.example.workmanager.WorkManager.DownloadManager
import com.example.workmanager.WorkManager.FilterManager
import com.example.workmanager.WorkManager.UploadManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_COUNT_VALUE = "key_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            implementWorkable()
        }
    }

    private fun implementWorkable()  {
        //Definr the workManager
        val workManager = WorkManager.getInstance(applicationContext)

        // define the data and constraints
        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //define the workmanager request
        val uploadRequest = OneTimeWorkRequest.Builder(UploadManager::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        val filteringRequest = OneTimeWorkRequest.Builder(FilterManager::class.java)
            .build()
        val compressingRequest = OneTimeWorkRequest.Builder(ComPressManger::class.java)
            .build()
        val downloadingManager = OneTimeWorkRequest.Builder(DownloadManager::class.java)
            .build()
        //definr the parallel request
        val paralleWorks = mutableListOf<OneTimeWorkRequest>()
        paralleWorks.add(downloadingManager)
        paralleWorks.add(filteringRequest)

        //Enque the request
        workManager
            .beginWith(paralleWorks)
            .then(compressingRequest)
            .then(uploadRequest)
            .enqueue()

        //get status update


        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                //  textView.text = it.state.name
                if (it.state.isFinished) {
                    val data = it.outputData
                    val message = data.getString(UploadManager.KEY_WORKER)
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            })

    }
}
