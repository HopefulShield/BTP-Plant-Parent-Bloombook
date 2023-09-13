package com.bloombook.backend

import android.text.format.DateUtils.formatDateTime
import android.util.Log
import com.bloombook.models.Journals
import com.bloombook.models.Reminders
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

const val REMINDERS_COLLECTION_REF = "reminders"
class RemindersRepo {

    val db = FirebaseFirestore.getInstance()
    var snapshotStateListener: ListenerRegistration? = null

    // TODO: Define the reminders collection reference here
    private val remindersRef: CollectionReference = db
        .collection("users/${getUserId()}/${REMINDERS_COLLECTION_REF}")

    private val storage = FirebaseStorage.getInstance().reference

    fun storageRef() = storage
    fun user() = Firebase.auth.currentUser

    fun getUserId(): String = Firebase.auth.currentUser?.uid.orEmpty()

    private fun getRemEntryRef(reminderId: String) =
        remindersRef.document(reminderId)

    private fun getNextDueDate(
        dateTime: LocalDateTime,
        interval: String
    ): Timestamp {

        // if reminder does not repeat, the next due date is just the dateTime itself
        if(interval == "Once") {
            val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
            return Timestamp(instant.epochSecond, instant.nano)
        }

        // Otherwise we calculate the next due date
        return when (interval) {
            "Daily" -> dateTime.plusDays(1)
            "Weekly" -> dateTime.plusWeeks(1)
            "Monthly" -> dateTime.plusMonths(1)
            else -> throw IllegalArgumentException("Invalid interval: $interval")
        }.let {
            val instant = it.atZone(ZoneId.systemDefault()).toInstant()
            return Timestamp(instant.epochSecond, instant.nano)
        }
    }


    fun addReminder(
        tagList: List<Journals>,
        scheduledDate: LocalDate,
        scheduledTime: LocalTime,
        message: String,
        interval: String,
        timestamp: Timestamp,
        onError: (Throwable?) -> Unit,
        onSuccess: (Reminders?) -> Unit
    ) {
        val documentId = remindersRef.document().id

        val dateTime = LocalDateTime.of(scheduledDate, scheduledTime)

        // Calculate nextDueDate for the reminder and extract its LocalDate and LocalTime
        val nextDueDateTime = getNextDueDate(dateTime, interval)
        val nextDueDate = LocalDate.from(nextDueDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()))
        val nextDueTime = LocalTime.from(nextDueDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()))


        // Omit the last completed date when creating a new reminder
        val reminder = Reminders(
            documentId,
            tagList,
            timestamp,

            // Store dates and datetimes as timestamps
            scheduledDate = Timestamp(Date.from(scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            scheduledDateTime = Timestamp(Date.from(scheduledTime.atDate(scheduledDate).atZone(ZoneId.systemDefault()).toInstant())),
            nextDueDate = Timestamp(Date.from(nextDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            nextDueDateTime = Timestamp(Date.from(nextDueTime.atDate(nextDueDate).atZone(ZoneId.systemDefault()).toInstant())),

            message,
            interval,
            lastCompletedDate = null,
        )
        remindersRef
            .document(documentId)
            .set(reminder)
            .addOnSuccessListener {
                onSuccess.invoke(reminder)
            }
            .addOnFailureListener{result ->
                onError.invoke(result.cause)
            }
    }


    fun getReminder(
        reminderId: String,
        onError:(Throwable?) -> Unit,
        onSuccess:(Reminders?) -> Unit
    ) {
        remindersRef
            .document(reminderId)
            .get()
            .addOnSuccessListener {
                onSuccess.invoke(it?.toObject(Reminders::class.java))
            }
            .addOnFailureListener{result ->
                onError.invoke(result.cause)
            }
    }

    fun updateReminder(
        reminderId: String,
        tagList: List<Journals>,
        scheduledDate: LocalDate,
        scheduledTime: LocalTime,
        message: String,
        interval: String,
        onResult: (Boolean) -> Unit
    ) {
        val dateTime = LocalDateTime.of(scheduledDate, scheduledTime)

        // Calculate nextDueDate for the reminder and extract its LocalDate and LocalTime
        val nextDueDateTime = getNextDueDate(dateTime, interval)
        val nextDueDate = LocalDate.from(nextDueDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()))
        val nextDueTime = LocalTime.from(nextDueDateTime.toDate().toInstant().atZone(ZoneId.systemDefault()))


        val updateData = hashMapOf<String, Any>(
            "tagList" to tagList,
            "scheduledDate" to Timestamp(Date.from(scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "scheduledDateTime" to Timestamp(Date.from(scheduledTime.atDate(scheduledDate).atZone(ZoneId.systemDefault()).toInstant())),
            "nextDueDate" to Timestamp(Date.from(nextDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "nextDueDateTime" to Timestamp(Date.from(nextDueTime.atDate(nextDueDate).atZone(ZoneId.systemDefault()).toInstant())),
            "message" to message,
            "interval" to interval,
        )

        remindersRef
            .document(reminderId)
            .update(updateData)
            .addOnCompleteListener{
                onResult(it.isSuccessful)
            }
    }

    fun markReminderAsDone(
        reminderId: String,
        onResult: (Boolean) -> Unit
    ) {
        val updateData = hashMapOf<String, Any>(
            "lastCompletedDate" to Timestamp.now(),
        )
        remindersRef
            .document(reminderId)
            .update(updateData)
            .addOnCompleteListener{
                onResult(it.isSuccessful)
            }
    }

    fun deleteReminder(
        reminderId: String,
        onResult: (Boolean) -> Unit
        ) {
        getRemEntryRef(reminderId)
            .delete()
            .addOnCompleteListener{
                onResult.invoke(it.isSuccessful)
            }
            .addOnFailureListener {
                onResult.invoke(false)
            }
    }



    fun getUserReminders(): Flow<Resources<List<Reminders>>> = callbackFlow {
        val startTimestamp = Timestamp(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
        try {
            snapshotStateListener = remindersRef
                .orderBy("scheduledDateTime", Query.Direction.ASCENDING)
                .addSnapshotListener {snapshot, e ->

                    val batch = db.batch()
                    val response = if (snapshot != null) {
                        val reminders = snapshot.toObjects(Reminders::class.java)

                        /// Update any reminder's next due date and time
                        val updatedReminders = reminders.map { reminder ->
                            if(reminder.nextDueDate == startTimestamp) {
                                updateReminderDates(reminder)
                            }
                            else {
                                reminder
                            }
                        }
                        val batch = db.batch()

                        Resources.Success(data = reminders)
                        Resources.Success(data = updatedReminders)
                    } else {
                        Resources.Error(throwable = e?.cause)
                    }
                    trySend(response)
                }
        }
        catch (e: Exception) {
            trySend(Resources.Error(e.cause))
            e.printStackTrace()
        }
        awaitClose {
            snapshotStateListener?.remove()
        }
    }

    fun getRemindersAtDate(date: LocalDate): Flow<Resources<List<Reminders>>> = callbackFlow {

        val startTimestamp = Timestamp(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        val todayStartTimeStamp = Timestamp(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))

        try {
            snapshotStateListener = remindersRef
                .where(Filter.or(
                    Filter.equalTo("scheduledDate", startTimestamp),
                    Filter.equalTo("nextDueDate", startTimestamp) )
                )
                //.orderBy("scheduledDateTime", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->

                Log.d("Reminders snapshot", snapshot.toString())
                val response = if (snapshot != null) {
                    val reminders = snapshot.toObjects(Reminders::class.java)

                    /// Update any reminder's next due date and time
                    val updatedReminders = reminders.map { reminder ->
                        if(reminder.nextDueDate == todayStartTimeStamp ) {
                            updateReminderDates(reminder)
                        }
                        else {
                            reminder
                        }
                    }

                    Log.d("Reminders", reminders.toString())
                    Resources.Success(data = updatedReminders)
                } else {
                    Log.d("Reminders", "null")
                    Resources.Error(throwable = e?.cause)
                }
                trySend(response)
            }
        }
        catch (e: Exception) {
            trySend(Resources.Error(e.cause))
            e.printStackTrace()
        }
        awaitClose {
            snapshotStateListener?.remove()
        }
    }



    private fun updateReminderDates(reminder:Reminders): Reminders {
        // set the current scheduled date to the previous next due dates
        // which basically sets the reminder for today
        reminder.scheduledDate = reminder.nextDueDate
        reminder.scheduledDateTime = reminder.nextDueDateTime


        // calculate the next due dates
        val dateTime = LocalDateTime.parse(formatDateTime(reminder.scheduledDateTime!!), DateTimeFormatter.ofPattern("M d yyyy h mm a"))
        val next = getNextDueDate(dateTime, reminder.interval)
        val nextDueDate = LocalDate.from(next.toDate().toInstant().atZone(ZoneId.systemDefault()))
        val nextDueTime = LocalTime.from(next.toDate().toInstant().atZone(ZoneId.systemDefault()))

        reminder.nextDueDate = Timestamp(Date.from(nextDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        reminder.nextDueDateTime = Timestamp(Date.from(nextDueTime.atDate(nextDueDate).atZone(ZoneId.systemDefault()).toInstant()))

        val updateData = hashMapOf<String, Any>(
            "scheduledDate" to reminder.scheduledDate!!,
            "scheduledDateTime" to reminder.scheduledDateTime!!,
            "nextDueDate" to Timestamp(Date.from(nextDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "nextDueDateTime" to Timestamp(Date.from(nextDueTime.atDate(nextDueDate).atZone(ZoneId.systemDefault()).toInstant())),
        )

        remindersRef
            .document(reminder.documentId)
            .update(updateData)

        return reminder
    }
    private fun formatDateTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("M d yyyy hh mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}