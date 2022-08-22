package com.example.myapplication.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ActiveListFragment : Fragment() {
    private var groupId: Long? = null
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var requestsList:ArrayList<Request>
    private lateinit var listAdapter:ListAdapter
    private var uid : String? = null
    private var groupName : String? = null
    private var photoList : ArrayList<String>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId")
            uid = it.getString("uid")
            groupName = it.getString("groupName")
            photoList = it.getStringArrayList("photoList")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_active_list, container, false)
        val context : Context = this.requireContext()
        recv = view.findViewById(R.id.mRecycler)
        addsBtn = view.findViewById(R.id.addingBtn)
        listAdapter = ListAdapter(context, ArrayList(), ArrayList(), groupName!!, true)
        recv.layoutManager = LinearLayoutManager(context)
        recv.adapter = listAdapter
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                requestsList = ArrayList(requestList)
                val groupActiveList : ArrayList<Request> = ArrayList()
                for (request in requestList){
                    if(!request.isCompleted){
                        groupActiveList.add(request)
                    }
                }
                groupActiveList.add(Request())
                withContext(Dispatchers.Main) {
                    listAdapter = ListAdapter(requireContext(), groupActiveList, photoList!!, groupName!!, true)
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    addsBtn.setOnClickListener { addInfo() }
                    progressDialog.dismiss()
                }
            }
        }

        val mySwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            val intent = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            requireContext().startActivity(intent)
        }
        return view
    }
    private fun addInfo() {
        val inflter = LayoutInflater.from(requireContext())
        val v = inflter.inflate(R.layout.add_request,null)
        val nameRequest = v.findViewById<EditText>(R.id.nameRequest)
        val comment = v.findViewById<EditText>(R.id.commentRequest)
        val toDo = v.findViewById<RadioButton>(R.id.toDo)
        val addDialog = AlertDialog.Builder(requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val namerequest = nameRequest.text.toString().trim()
            val comment1 = comment.text.toString().trim()
            if(namerequest.isEmpty()){
                Toast.makeText(requireContext(),"Empty Request",Toast.LENGTH_SHORT).show()
            }
            else {
                var request: Request
                GlobalScope.launch {
                    val requestId : Long = getRequestId(requireContext())
                    val currentDate : Date =  Calendar.getInstance().time
                    val user : User = getUser(requireContext())
                    val type : Request.Type
                    if(toDo.isChecked)
                        type = Request.Type.ToDo
                    else
                        type = Request.Type.ToBuy
                    request = Request(requestId, groupId!!, user, namerequest, false, comment1, null, currentDate, null, type)
                    Firebase.database.getReference("requests").child(request.id.toString()).setValue(request)
                    val group : Group? = getGroupById(requireContext(), groupId!!)
                    for(userId in group!!.users!!){
                        if(userId != uid){
                            val notificationId : Long = getNotificationId(requireContext(), userId)
                            val notification = Notification(userId, request, user.nickname, null, groupName!!, notificationId, request.date, request.groupId, Notification.Type.NewRequest)
                            Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                            var unreadMessages = getUnread(requireContext(), groupId!!, userId)
                            unreadMessages++
                            Firebase.database.getReference("unread").child(userId).child(groupId.toString()).setValue(unreadMessages)

                        }
                    }
                    val intent = Intent(requireContext(), GroupActivity::class.java)
                    intent.putExtra("groupId", groupId)
                    intent.putExtra("groupName", groupName)
                    requireContext().startActivity(intent)
                }
            }
        dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(requireContext(),"Cancel",Toast.LENGTH_SHORT).show()
        }
        addDialog.create()
        addDialog.show()
    }
    companion object {
        @JvmStatic
        fun newInstance(groupId: Long, uid: String, groupName: String, photoList : ArrayList<String>) =
            ActiveListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                    putString("uid", uid)
                    putString("groupName", groupName)
                    putStringArrayList("photoList", photoList)
                }
            }
    }
}