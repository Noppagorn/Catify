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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import android.provider.MediaStore
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_create_post.*
import java.sql.Timestamp

class CreatePostActivity : AppCompatActivity() {

    private lateinit var mStorageRef: StorageReference
    private lateinit var file: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        picInput.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    pickImageFromGallery()
                }
            } else {
                pickImageFromGallery()
            }
        }

        submitBtn.setOnClickListener {
//            val mSharedPreferences =
//                applicationContext.getSharedPreferences("ACCOUNT", MODE_PRIVATE)
//            val username = mSharedPreferences!!.getString("USERNAME", null)
//
//            val database = FirebaseDatabase.getInstance()
//            val myRef = database.getReference("cat_adoption").push()
//            val key = myRef.key
//            myRef.child("poster").setValue(username)
//            myRef.child("stamp_time").setValue(Timestamp(System.currentTimeMillis()).time.toString())
//            myRef.child("header").setValue(headerInput.editText!!.text.toString())
//            myRef.child("location").setValue(locationInput.editText!!.text.toString())
//            myRef.child("age").setValue(ageInput.editText!!.text.toString())
//            myRef.child("type").setValue(typeInput.editText!!.text.toString())
//            myRef.child("contact").setValue(contactInput.editText!!.text.toString())
//            myRef.child("description").setValue(descriptionInput.editText!!.text.toString())
//
//            mStorageRef = FirebaseStorage.getInstance().reference
//            mStorageRef.child(key!!).putFile(file)
//                .addOnSuccessListener {
//                    val result = it.metadata!!.reference!!.downloadUrl
//                    result.addOnSuccessListener {
//                        val imageLink = it.toString()
//                        Toast.makeText(
//                            this@CreatePostActivity,
//                            imageLink,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        myRef.child("url_image").setValue(imageLink)
//                    }
//                }
//
//            finish()

            startActivity(Intent(applicationContext, MapsActivity::class.java))


        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            file = Uri.fromFile(File(getRealPathFromURI(data?.data!!)))
            picShow.setImageURI(data.data)
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(
            contentUri,
            proj, null, null, null
        )

        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(columnIndex)
    }
}