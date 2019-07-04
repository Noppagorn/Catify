package com.nongmung.catify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_full_feed.*
import kotlinx.android.synthetic.main.comment_item.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog


class FullFeedActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val listOfMessage = ArrayList<String>()
    private lateinit var key: String
    private lateinit var username: String
    private var isMe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_feed)

        val mSharedPreferencesUser =
            applicationContext.getSharedPreferences("ACCOUNT", MODE_PRIVATE)
        val username = mSharedPreferencesUser!!.getString("USERNAME", null)

        val mSharedPreferences = applicationContext.getSharedPreferences("FULL_FEED", MODE_PRIVATE)

        Picasso.get().load(mSharedPreferences!!.getString("URL_IMAGE", null)).into(catImage)
        headerText.text = mSharedPreferences.getString("HEADER", null)
        posterText.text = "${mSharedPreferences.getString(
            "POSTER_NAME",
            null
        )} | ${getDateTime(mSharedPreferences.getString("TIME", null)!!)}"
        locationText.text = "อยู่ที่ ${mSharedPreferences!!.getString("LOCATION", null)}"
        ageText.text = "อายุ ${mSharedPreferences!!.getString("AGE", null)}"
        typeText.text = "สายพันธุ์ ${mSharedPreferences!!.getString("TYPE", null)}"
        contactText.text = "ติดต่อ ${mSharedPreferences!!.getString("CONTACT", null)}"
        descriptionText.text = mSharedPreferences.getString("DESCRIPTION", null)
        key = mSharedPreferences.getString("KEY", null)!!

        if (mSharedPreferences.getString("POSTER_NAME", null) == username) {
            image_contact.setImageResource(R.drawable.ic_delete_black_24dp)
            isMe = true
        } else {
            image_contact.setImageResource(R.drawable.ic_contact_phone_white_24dp)
            isMe = false
        }

        Toast.makeText(applicationContext, key, Toast.LENGTH_SHORT).show()

        loadMessage()

        send_button.setOnClickListener {
            sendComment()
        }

        image_contact.setOnClickListener {
            if (isMe) {
                deletePost()
            } else {
                contact(mSharedPreferences.getString("CONTACT", null)!!)
            }
        }
    }

    private fun getDateTime(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm")
            val netDate = Date(s.toLong())
            sdf.format(netDate)
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    private fun contact(phoneNum: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNum")
        startActivity(intent)
    }

    private fun deletePost() {
        val builder = AlertDialog.Builder(this@FullFeedActivity)
        builder.setTitle("เตือนๆ")
        builder.setMessage("อยากลบประกาศจริงๆเหรอ")
        builder.setPositiveButton("จริงสิ") { dialog, which ->
            mDatabase.child("cat_adoption").child(key).removeValue()
            finish()
        }
        builder.setNegativeButton("ไม่อะ") { _, _ -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun sendComment() {
        if (!commentInput.editText!!.text.isEmpty()) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("cat_adoption").child(key).child("comment").push()

            val tsLong = Timestamp(System.currentTimeMillis())

            myRef.child("message_text").setValue(commentInput.editText!!.text.toString())
            myRef.child("message_user").setValue(username)
            myRef.child("message_time").setValue(tsLong.time)

            commentInput.editText!!.text = null
        } else {
            Toast.makeText(applicationContext, "คอมเม้นต์ว่างไปนะ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMessage() {
        mDatabase.child("cat_adoption").child(key).child("comment")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listOfMessage.clear()

                    for (child in dataSnapshot.children) {
                        listOfMessage.add(
                            "${child.child("message_user").value}@splitHeRe#${child.child("message_time").value}@splitHeRe#${child.child(
                                "message_text"
                            ).value}"
                        )
                    }
                    listOfMessage.reverse()
                    setMessageToView(listOfMessage)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun setMessageToView(data: ArrayList<String>) {
        val mLinearLayoutManager = LinearLayoutManager(applicationContext)
        mLinearLayoutManager.stackFromEnd = true

        recycler_comment.layoutManager = mLinearLayoutManager
        recycler_comment.adapter = MessageListAdapter(data)
    }

    inner class MessageListAdapter(private val messageList: ArrayList<String>) :
        RecyclerView.Adapter<MessageListViewHolder>() {

        override fun getItemCount(): Int {
            return messageList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
            val messageItem = LayoutInflater.from(applicationContext)
                .inflate(R.layout.comment_item, parent, false)
            return MessageListViewHolder(messageItem)
        }

        override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
            val dataSplit = messageList[position].split("@splitHeRe#")

            if (position == 0) {
                holder.mLine.visibility = View.GONE
            } else {
                holder.mLine.visibility = View.VISIBLE
            }

            holder.mTextUsername.text = dataSplit[0]
            holder.mTextTime.text = getDateTime(dataSplit[1])
            holder.mTextMessage.text = dataSplit[2]
        }
    }

    inner class MessageListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mTextUsername = view.text_username!!
        val mTextTime = view.text_time!!
        val mTextMessage = view.text_message!!
        val mLine = view.separate_line!!
    }
}