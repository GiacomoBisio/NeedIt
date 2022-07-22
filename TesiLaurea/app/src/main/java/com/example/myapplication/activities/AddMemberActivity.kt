package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddMemberActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)
        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)
        var myNickname: String? = null
        GlobalScope.launch {
            myNickname = getUser(this@AddMemberActivity).nickname
        }
        val nicknameEditText: EditText = findViewById(R.id.memberNickname)
        var userExists = false
        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userExists = false
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        userExists = nicknameIsAlreadyUsed(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (!userExists) {
                                nicknameEditText.error = "user with this nickname does not exist"
                            }
                            else if(nicknameEditText.text.toString().trim()==myNickname)
                                nicknameEditText.error = "this is your nickname"
                        }
                    }
                }


            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        val button : Button = findViewById(R.id.buttonAddNewMember)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(!userExists)
                    nicknameEditText.error = "user with this nickname does not exist"
                else if(nicknameEditText.text.toString().trim() == myNickname)
                    nicknameEditText.error = "this is your nickname"
                else {
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            Log.d(TAG, "AAA 1")
                            val group : Group = getGroupById(this@AddMemberActivity, groupId)
                            Log.d(TAG, "AAA 2")
                            val id : String? = getUserIdByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                            Log.d(TAG, "AAA 3")
                            val user : User = getUserByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                            Log.d(TAG, "AAA 4")
                            //val myId : String = FirebaseAuthWrapper(this@AddMemberActivity).getUid()!!
                            withContext(Dispatchers.Main) {
                                Log.d(TAG, "AAA 5")
                                if(id == null)
                                    nicknameEditText.error = "user with this nickname does not exist"
                                else if (group.users!!.contains(id))
                                    nicknameEditText.error = "user is already a member of the group"
                                else {
                                    group.users!!.add(id)
                                    user.groups!!.add(groupId)
                                    Firebase.database.getReference("users").child(id).setValue(user)
                                    Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)
                                    val intent = Intent(this@AddMemberActivity, GroupActivity::class.java)
                                    intent.putExtra("groupId", groupId)
                                    this@AddMemberActivity.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    /*override fun onBackPressed() {
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }*/


}
