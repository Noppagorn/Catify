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
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.feed_item.view.*
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class CreatePostActivity : AppCompatActivity() {

    private lateinit var mStorageRef: StorageReference;
    private lateinit var file: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        picInput.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    pickImageFromGallery();
                }
            } else {
                pickImageFromGallery();
            }
        }

        submitBtn.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("cat_adoption").push()
            val key = myRef.key
            myRef.child("poster").setValue("poster_name")
            myRef.child("location").setValue(locationInput.editText!!.text.toString())
            myRef.child("age").setValue(ageInput.editText!!.text.toString())
            myRef.child("type").setValue(typeInput.editText!!.text.toString())
            myRef.child("contact").setValue(contactInput.editText!!.text.toString())
            myRef.child("description").setValue(descriptionInput.editText!!.text.toString())

            mStorageRef = FirebaseStorage.getInstance().reference;
            mStorageRef.child(key!!).putFile(file)
                .addOnSuccessListener {
                    val result = it.metadata!!.reference!!.downloadUrl;
                    result.addOnSuccessListener {
                        val imageLink = it.toString()
                        Toast.makeText(
                            this@CreatePostActivity,
                            imageLink,
                            Toast.LENGTH_SHORT
                        ).show()
                        myRef.child("url_image").setValue(imageLink)
                    }
                }

            finish()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private val IMAGE_PICK_CODE = 1000;
        private val PERMISSION_CODE = 1001;
    }

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
            picShow.setImageURI(data?.data)
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(
            contentUri,
            proj, null, null, null
        )

        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(column_index)
    }
}
