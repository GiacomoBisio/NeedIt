package com.example.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.Request
import com.example.myapplication.models.getNicknameById
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ListAdapter(val c:Context,val requestList:ArrayList<Request>, val groupName: String, val active : Boolean):RecyclerView.Adapter<ListAdapter.UserViewHolder>()
{



    inner class UserViewHolder(val v:View):RecyclerView.ViewHolder(v){
        var nameRequest:TextView
        var userName:TextView
        var commentRequest:TextView
        var optionsMenu:ImageView

        init {
            nameRequest = v.findViewById<TextView>(R.id.nameRequest)
            userName = v.findViewById<TextView>(R.id.userName)
            commentRequest = v.findViewById<TextView>(R.id.commentRequest)
            optionsMenu = v.findViewById(R.id.optionsMenu)
            optionsMenu.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(v:View) {
            val position = requestList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            if(active)
                popupMenus.inflate(R.menu.options_menu)
            else
                popupMenus.inflate(R.menu.options_complete_menu)

            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val v = LayoutInflater.from(c).inflate(R.layout.add_request,null)
                        val title = v.findViewById<TextView>(R.id.Title)
                        val name = v.findViewById<EditText>(R.id.nameRequest)
                        val comment = v.findViewById<EditText>(R.id.commentRequest)
                        title.setText("Edit Request")
                        name.setText(position.nameRequest)
                        comment.setText(position.comment)
                        AlertDialog.Builder(c)
                            .setView(v)
                            .setPositiveButton("Ok"){
                                    dialog,_->
                                if(name.text.toString().trim().isEmpty()){
                                    Toast.makeText(c,"Empty Request",Toast.LENGTH_SHORT).show()
                                }
                                else if (name.text.toString().trim().equals(position.nameRequest) && comment.text.toString().trim().equals(position.comment)){
                                    Toast.makeText(c,"You changed nothing",Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    position.nameRequest = name.text.toString()
                                    position.comment = comment.text.toString()
                                    Firebase.database.getReference("requests").child(position.Id.toString()).setValue(position)
                                    val intent : Intent = Intent(c, GroupActivity::class.java)
                                    intent.putExtra("groupId", position.groupId)
                                    //Log.d(ContentValues.TAG,"www: "+groupName)
                                    intent.putExtra("groupName", groupName)
                                    c.startActivity(intent)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                dialog.dismiss()

                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.delete->{
                        /**set delete*/
                        AlertDialog.Builder(c)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("Are you sure delete this Request?")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                Firebase.database.getReference("requests").child(position.Id.toString()).removeValue()
                                val intent : Intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
                                //Log.d(ContentValues.TAG,"www: "+groupName)
                                intent.putExtra("groupName", groupName)
                                c.startActivity(intent)

                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.complete->{
                        /**set delete*/
                        AlertDialog.Builder(c)
                            .setTitle("Complete")
                            .setIcon(R.drawable.ic_baseline_check_circle_24)
                            .setMessage("Do you want to complete this request?")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                position.isCompleted = true
                                Firebase.database.getReference("requests").child(position.Id.toString()).setValue(position)
                                val intent : Intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
                                //Log.d(ContentValues.TAG,"www: "+groupName)
                                intent.putExtra("groupName", groupName)
                                c.startActivity(intent)

                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    else-> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_requests,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val newList = requestList[position]
        holder.commentRequest.text = newList.comment
        holder.nameRequest.text = newList.nameRequest
        GlobalScope.launch {
            val userName : String = getNicknameById(c,newList.userId )
            holder.userName.text = userName
        }

    }

    override fun getItemCount(): Int {
        return  requestList.size
    }
}