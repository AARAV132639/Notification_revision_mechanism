package com.example.notification_revision

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.notification_revision.ui.theme.Notification_revisionTheme
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy


//adding new imports: Part 1
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy


//adding new imports: Part 2---> For storing logs
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder

//adding test import
import androidx.work.OneTimeWorkRequestBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //adding logs section
        //updating main activity to handle logs. This ui is only for viewing logs
        // Currently logs only open when you reopen the app
        val prefs= getSharedPreferences("logs",Context.MODE_PRIVATE)
        val logs= prefs.getString("data","No logs yet")

        setContent{

            //text formatting of logs
            val formattedLogs= logs
                ?.lines()
                ?.filter{it.isNotBlank()}
                ?.reversed() //latest first
                ?.joinToString("\n\n"){ entry->
                   entry.substringBefore("|").trim()

                    /*val parts= entry.split("\\|")
                    parts[0].trim() //only show questions*/
                }

            Text(
                text= formattedLogs?:"No logs yet",
                modifier= Modifier.padding(16.dp)
            )
        }

        //creating notification channel
        createNotificationChannel()

        //ensuring notification channel exits.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }


        //testing the ui and scheduler
        val workRequest= OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
        /* Experimenting
        //added the notification handler
        val workRequest= PeriodicWorkRequestBuilder<NotificationWorker>(
            15, TimeUnit.MINUTES
        ).build()


        // using periodic work with spacing
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "revision_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
*/

    }

    /* Previous function to call notification

    private fun sendTestNotification()
    {
        val channelId= "revision_channel"
        val builder= NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("What is normalization?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager= NotificationManagerCompat.from(this)
        manager.notify(1,builder.build())
    }*/

    private fun createNotificationChannel() {
        val channelId = "revision_channel"
        val name = "Revision Notifications"
        val descriptionText = "Channel for revision questions"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }
}

