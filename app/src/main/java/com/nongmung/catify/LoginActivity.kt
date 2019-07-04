package com.nongmung.catify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private var state = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        signupBtn.setOnClickListener {
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
                                        "Username ถูกใช้ไปแล้ว",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                state = true
                                val mSharedPreferences =
                                    getSharedPreferences("ACCOUNT", MODE_PRIVATE)
                                val editor = mSharedPreferences!!.edit()

                                editor.putBoolean("SIGN_IN_STATE", true)
                                editor.putString("USERNAME", newUsername)
                                editor.apply()

                                mDatabase.child("account").child(newUsername).setValue(newPassword)
                                Toast.makeText(
                                    applicationContext,
                                    "เข้าสู่ระบบแล้ว",
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
                        "รหัสผ่านสั้นไปนะ\nเพิ่มอีก ${6 - newPassword.length} ตัว",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "กรอก username ก่อนสิ", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        signinBtn.setOnClickListener {
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
                                    "เข้าสู่ระบบแล้ว",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "รหัสผ่านผิดจ้า",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "บัญชีนี้ไม่มีอยู่จริง",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            } else {
                Toast.makeText(applicationContext, "กรอก username ก่อนสิ", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        val mSharedPreferencesUser =
            applicationContext.getSharedPreferences("ACCOUNT", MODE_PRIVATE)
        val username = mSharedPreferencesUser!!.getString("USERNAME", null)

        if (username == null) {
            Toast.makeText(applicationContext, "ต้องสมัครสมาชิกก่อนนะ", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}