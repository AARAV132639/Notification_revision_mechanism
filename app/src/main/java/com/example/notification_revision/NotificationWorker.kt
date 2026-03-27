package com.example.notification_revision

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

//adding necessary imports
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

class NotificationWorker (context: Context, workerParams: WorkerParameters):Worker(context, workerParams)
{
    override fun doWork(): Result{

        val context= applicationContext
        val channelId= "revision_channel"

        //1. get all json files from assets
        val files= context.assets.list("")
            ?.filter{it.endsWith("json")}
            ?:emptyList()

        if(files.isEmpty()) {return Result.failure()}

        //2. pick random file
        val randomFile= files.random()

        //3. load question from json
        val questions= loadQuestions(context, randomFile)

        if(questions.isEmpty()){return Result.failure()}

        //4. pick random question
        val randomQuestion= questions.random()

        //5. Saving log of last 15 notifications
        val prefs= context.getSharedPreferences("logs", Context.MODE_PRIVATE)

        val existingLogs= prefs.getString("data","")?:""

        val newEntry= "$randomQuestion|${System.currentTimeMillis()}\n"

        //Keep only last 15
        val updatedLogs= (existingLogs+newEntry)
            //.lines()
            .split("/n")
            .filter{it.isNotBlank()}
            .takeLast(15)
            .joinToString("\n")

        prefs.edit().putString("data", updatedLogs).apply()

        //6. build notification
        val builder= NotificationCompat.Builder(applicationContext,channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(randomQuestion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true) //don't disappear on tap
            //removed .setOngoing so that user can swipe it away

        //7. Send Notification
        val manager= NotificationManagerCompat.from(applicationContext)
        manager.notify((0..1000).random(),builder.build())


        return Result.success()
    }


    //writing function to load json file
    fun loadQuestions(context:Context, fileName:String):List<String>
    {
        val json= context.assets.open(fileName).bufferedReader().use{it.readText()}
        val jsonArray= org.json.JSONArray(json)

        val questions= mutableListOf<String>()

        for(i in 0 until jsonArray.length()){
            val obj= jsonArray.getJSONObject(i)
            questions.add(obj.getString("question"))
        }

        return questions
    }

}