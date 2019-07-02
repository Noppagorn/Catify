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
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import android.provider.MediaStore
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.feed_row.*
import kotlinx.android.synthetic.main.feed_row.view.*


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

                setMessageToView(listOfMessage)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setMessageToView(data: ArrayList<String>) {
        val mLinearLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = mLinearLayoutManager
        recyclerView.adapter = MessageListAdapter(data)
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
                .inflate(R.layout.feed_row, parent, false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val username = "chorn"   // username from UI
        val password = "1111"   // password from UI
        // sing out
        // account
//            val database = FirebaseDatabase.getInstance()
//            val myRef = database.getReference("account")
//            myRef.child(username).setValue(password)

        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED
            ) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                //permission already granted
                pickImageFromGallery();
            }
        } else {
            //system OS is < Marshmallow
            pickImageFromGallery();
        }

        return super.onOptionsItemSelected(item)
    }


}
