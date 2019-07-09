package com.nongmung.catify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.feed_item.view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val listOfMessage = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val mSharedPreferencesUser =
            applicationContext.getSharedPreferences("ACCOUNT", MODE_PRIVATE)
        val username = mSharedPreferencesUser!!.getString("USERNAME", null)

        if (username == null) {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }

        mDatabase.child("cat_adoption").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfMessage.clear()

                for (child in dataSnapshot.children) {
                    listOfMessage.add(
                        "${child.child("age").value}@splitHeRe#${child.child("contact").value}@splitHeRe#${child.child(
                            "description"
                        ).value}@splitHeRe#${child.child("header").value}@splitHeRe#${child.child("location").value}@splitHeRe#${child.child(
                            "poster"
                        ).value}@splitHeRe#${child.child("type").value}@splitHeRe#${child.child("url_image").value}@splitHeRe#${child.child(
                            "stamp_time"
                        ).value}@splitHeRe#${child.key}"
                    )
                }
                listOfMessage.reverse()
                setMessageToView(listOfMessage, "")
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        search.editText!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                setMessageToView(listOfMessage, search.editText!!.text.toString())

                true
            } else {
                false
            }
        }

        back_button.setOnClickListener {
            setMessageToView(listOfMessage, "")
            search.editText!!.text = null
        }
    }

    private fun setMessageToView(data: ArrayList<String>, keyword: String) {
        var dataAfterSearch = ArrayList<String>()
        dataAfterSearch.addAll(data)
        if (keyword != "") {
            dataAfterSearch = searchData(data, keyword)
        }

        val mLinearLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = mLinearLayoutManager
        recyclerView.adapter = MessageListAdapter(dataAfterSearch)
    }

    inner class MessageListAdapter(private val messageList: ArrayList<String>) :
        RecyclerView.Adapter<MessageListViewHolder>() {

        override fun getItemCount(): Int {
            return messageList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
            val messageItem = LayoutInflater.from(applicationContext)
                .inflate(R.layout.feed_item, parent, false)
            val holder = MessageListViewHolder(messageItem)

            holder.mFeedContainer.setOnClickListener {
                val dataSplit = messageList[holder.adapterPosition].split("@splitHeRe#")

                val mSharedPreferences =
                    getSharedPreferences("FULL_FEED", MODE_PRIVATE)
                val editor = mSharedPreferences!!.edit()

                editor.putString("AGE", dataSplit[0])
                editor.putString("CONTACT", dataSplit[1])
                editor.putString("DESCRIPTION", dataSplit[2])
                editor.putString("HEADER", dataSplit[3])
                editor.putString("LOCATION", dataSplit[4])
                editor.putString("POSTER_NAME", dataSplit[5])
                editor.putString("TYPE", dataSplit[6])
                editor.putString("URL_IMAGE", dataSplit[7])
                editor.putString("TIME", dataSplit[8])
                editor.putString("KEY", dataSplit[9])
                editor.apply()

                startActivity(Intent(applicationContext, FullFeedActivity::class.java))
            }

            return holder
        }

        override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
            val dataSplit = messageList[position].split("@splitHeRe#")

            holder.mTextHeader.text = dataSplit[3]
            holder.mTextLocation.text = dataSplit[4]
            holder.mTextContact.text = dataSplit[1]
            Picasso.get().load(dataSplit[7]).into(holder.mImageView)
        }
    }

    inner class MessageListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mImageView = view.photo!!
        val mTextLocation = view.location!!
        val mTextContact = view.contact!!
        val mTextHeader = view.header!!
        val mFeedContainer = view.feed_container!!
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            true
        }
        R.id.action_add_feed -> {
            startActivity(Intent(applicationContext, CreatePostActivity::class.java))
            true
        }
        R.id.action_search -> {
            if (search_bar.visibility == View.GONE) {
                search_bar.visibility = View.VISIBLE
            } else {
                search_bar.visibility = View.GONE
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun searchData(data: ArrayList<String>, fromSearch: String): ArrayList<String> {
        val dataAfter = ArrayList<String>()
        for (x in data) {
            val splitx = x.split("@splitHeRe#")
            for (i in 0..7) {
                if (splitx[i].contains(fromSearch)) {
                    dataAfter.add(x)
                    break
                }
            }
        }

        return dataAfter
    }
}