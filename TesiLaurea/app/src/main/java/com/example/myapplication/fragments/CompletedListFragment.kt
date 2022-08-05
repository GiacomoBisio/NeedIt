package com.example.myapplication.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.Request
import com.example.myapplication.models.getRequestsList
import kotlinx.coroutines.*


class CompletedListFragment : Fragment() {

    private var groupId: Long? = null
    private var uid : String? = null
    private var groupName : String? = null
    private lateinit var recv: RecyclerView
    private lateinit var requestsList:ArrayList<Request>
    private lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId")
            uid = it.getString("uid")
            groupName = it.getString("groupName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_completed_list, container, false)
        val context : Context = this.requireContext()
        recv = view.findViewById(R.id.mRecycler)
        listAdapter = ListAdapter(context, ArrayList(), groupName!!, false)
        recv.layoutManager = LinearLayoutManager(context)
        recv.adapter = listAdapter

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                withContext(Dispatchers.Main) {
                    requestsList = ArrayList(requestList)
                    var groupCompletedList : ArrayList<Request> = ArrayList()

                    listAdapter = ListAdapter(requireContext(),groupCompletedList, groupName!!, false)

                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter


                    for (request in requestList){
                        if(request.isCompleted){
                            //groupActiveList.add(request.nameRequest)
                            groupCompletedList.add(request)
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        val mySwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            val intent : Intent = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            requireContext().startActivity(intent)
        }
        return view

    }

    companion object {

        @JvmStatic
        fun newInstance(groupId: Long, uid: String, groupName: String) =
            CompletedListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                    putString("uid", uid)
                    putString("groupName", groupName)
                }
            }
    }
}