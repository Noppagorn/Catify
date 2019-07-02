package com.nongmung.catify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.create_post.*
import kotlinx.android.synthetic.main.login.*

class LoginActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private var state = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        signUpBtn.setOnClickListener {
            val newUsername = username.editText!!.text.toString().trim()
            val newPassword = password.editText!!.text.toString()

            if (!newUsername.isBlank()) {
                if (newPassword.length >= 6) {
                    mDatabase.child("account").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild(newUsername)) {
                                if (!state) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Username unavailable",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                state = true;
                                val mSharedPreferences =
                                    getSharedPreferences("ACCOUNT", MODE_PRIVATE)
                                val editor = mSharedPreferences!!.edit()

                                editor.putBoolean("SIGN_IN_STATE", true)
                                editor.putString("USERNAME", newUsername)
                                editor.apply()

                                mDatabase.child("account").child(newUsername).setValue(newPassword)
                                Toast.makeText(
                                    applicationContext,
                                    "Sign up success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Add ${6 - newPassword.length} more chars",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "Username is empty", Toast.LENGTH_SHORT).show()
            }
        }

        signInBtn.setOnClickListener {
            val newUsername = username.editText!!.text.toString().trim()
            val newPassword = password.editText!!.text.toString()

            if (!newUsername.isBlank()) {
                mDatabase.child("account").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(newUsername)) {
                            if (dataSnapshot.child(newUsername).value == newPassword) {
                                val mSharedPreferences =
                                    getSharedPreferences("ACCOUNT", MODE_PRIVATE)
                                val editor = mSharedPreferences!!.edit()

                                editor.putBoolean("SIGN_IN_STATE", true)
                                editor.putString("USERNAME", newUsername)
                                editor.apply()

                                Toast.makeText(
                                    applicationContext,
                                    "Sign in success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Invalid password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "This account isn't exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            } else {
                Toast.makeText(applicationContext, "Username is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}