package com.nongmung.catify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.feed_item.view.*
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private lateinit var mStorageRef: StorageReference;
    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val listOfMessage = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDatabase.child("cat_adoption").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfMessage.clear()

                for (child in dataSnapshot.children) {
                    listOfMessage.add(
                        "${child.child("age").value}@splitHeRe#${child.child("contact").value}@splitHeRe#${child.child(
                            "description"
                        ).value}@splitHeRe#${child.child("location").value}@splitHeRe#${child.child(
                            "poster"
                        ).value}@splitHeRe#${child.child("type").value}@splitHeRe#${child.child("url_image").value}"
                    )
                }
                Collections.reverse(listOfMessage)
                setMessageToView(listOfMessage)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setMessageToView(data: ArrayList<String>) {

        val stringFromSearchBar = "persia" // put result of search bar here
        var dataAfterSearch = ArrayList<String>()
        dataAfterSearch.addAll(data)
        if (!stringFromSearchBar.equals("")) {
            dataAfterSearch = searchData(data, stringFromSearchBar)
        }
        //

        val mLinearLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = mLinearLayoutManager
        recyclerView.adapter = MessageListAdapter(dataAfterSearch)
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            //image_view.setImageURI(data?.data)

            // add adopt cat
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("cat_adoption").push()
            val key = myRef.key
            myRef.child("poster").setValue("poster_name")
            myRef.child("location").setValue("location")
            myRef.child("age").setValue("age")
            myRef.child("type").setValue("type")
            myRef.child("contact").setValue("contact")
            myRef.child("description").setValue("description")

            mStorageRef = FirebaseStorage.getInstance().reference;

            val file = Uri.fromFile(File(getRealPathFromURI(data?.data!!)))
            mStorageRef.child(key!!).putFile(file)
                .addOnSuccessListener {
                    val result = it.metadata!!.reference!!.downloadUrl;
                    result.addOnSuccessListener {
                        val imageLink = it.toString()
                        Toast.makeText(
                            this@MainActivity,
                            imageLink,
                            Toast.LENGTH_SHORT
                        ).show()
                        myRef.child("url_image").setValue(imageLink)
                    }
                }
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val imageURL = mStorageRef.downloadUrl.result.toString()
//                        val result = it.metadata!!.reference!!.downloadUrl;
//                        Toast.makeText(
//                            this@MainActivity,
//                            imageURL,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        myRef.child("image_url").setValue(imageURL)
//                    } else {
//                        // Handle failures
//                        // ...
//                    }
//                }
        }
    }

    fun getRealPathFromURI(contentUri: Uri): String {

        // can post image
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(
            contentUri,
            proj, // WHERE clause selection arguments (none)
            null, null, null
        )// Which columns to return
        // WHERE clause; which rows to return (all rows)
        // Order-by clause (ascending by name)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(column_index)
    }

    inner class MessageListAdapter(
        private val messageList: ArrayList<String>
    ) : RecyclerView.Adapter<MessageListViewHolder>() {

        override fun getItemCount(): Int {
            return messageList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
            val messageItem = LayoutInflater.from(applicationContext)
                .inflate(R.layout.feed_item, parent, false)
            return MessageListViewHolder(messageItem)
        }

        override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
            val dataSplit = messageList[position].split("@splitHeRe#")

            holder.mTextPoster.text = dataSplit[4]
            holder.mTextLocation.text = dataSplit[3]
            Picasso.get().load(dataSplit[6]).into(holder.mImageView);
        }
    }

    inner class MessageListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mImageView = view.photo!!
        val mTextPoster = view.poster!!
        val mTextLocation = view.location!!
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
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    //string alignment
    fun getMinimumPenalty(
        x: String, y: String,
        pxy: Int, pgap: Int
    ): Int {
        var i: Int
        var j: Int // intialising variables

        val m = x.length // length of gene1
        val n = y.length // length of gene2

        val dp = Array(n + m + 1) { IntArray(n + m + 1) }

        for (x1 in dp)
            Arrays.fill(x1, 0)

        // intialising the table
        i = 0
        while (i <= n + m) {
            dp[i][0] = i * pgap
            dp[0][i] = i * pgap
            i++
        }

        // calcuting the
        //        // minimum penalty
        i = 1
        while (i <= m) {
            j = 1
            while (j <= n) {
                if (x[i - 1] == y[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = Math.min(
                        Math.min(
                            dp[i - 1][j - 1] + pxy,
                            dp[i - 1][j] + pgap
                        ),
                        dp[i][j - 1] + pgap
                    )
                }
                j++
            }
            i++
        }

        val l = n + m // maximum possible length

        i = m
        j = n

        var xpos = l
        var ypos = l

        val xans = IntArray(l + 1)
        val yans = IntArray(l + 1)

        while (!(i == 0 || j == 0)) {
            if (x[i - 1] == y[j - 1]) {
                xans[xpos--] = x[i - 1].toInt()
                yans[ypos--] = y[j - 1].toInt()
                i--
                j--
            } else if (dp[i - 1][j - 1] + pxy == dp[i][j]) {
                xans[xpos--] = x[i - 1].toInt()
                yans[ypos--] = y[j - 1].toInt()
                i--
                j--
            } else if (dp[i - 1][j] + pgap == dp[i][j]) {
                xans[xpos--] = x[i - 1].toInt()
                yans[ypos--] = '_'.toInt()
                i--
            } else if (dp[i][j - 1] + pgap == dp[i][j]) {
                xans[xpos--] = '_'.toInt()
                yans[ypos--] = y[j - 1].toInt()
                j--
            }
        }

//        print("Minimum aligning the genes = ")
//        print(dp[m][n].toString() + "\n")
        //        System.out.println("The aligned genes are :");
        //        for (i = id; i <= l; i++)
        //        {
        //            System.out.print((char)xans[i]);
        //        }
        //        System.out.print("\n");
        //        for (i = id; i <= l; i++)
        //        {
        //            System.out.print((char)yans[i]);
        //        }
        return dp[m][n]
    }

    private fun searchData(data: ArrayList<String>, fromSearch: String): ArrayList<String> {
//        fromSearch = "o2"//
        val dataAfter = ArrayList<String>()
        for (x in data) {

            val splitx = x.split("@splitHeRe#")
            if (splitx.get(5).toString().equals(fromSearch)) {
                dataAfter.add(x)
            }
        }
        return dataAfter
    }
}