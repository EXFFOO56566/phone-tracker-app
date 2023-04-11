package com.softexpoitmaps.findmyphone.business

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.HashMap


class EmailsAdapter(var mcontext: Context, var usersList: List<emailinfo>):

    RecyclerView.Adapter<EmailsAdapter.ViewHolder?>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailsAdapter.ViewHolder {
        val v: View =
            LayoutInflater.from(mcontext).inflate(R.layout.trackemailshow, parent, false)


        return EmailsAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: EmailsAdapter.ViewHolder, position: Int) {
        val user: emailinfo = usersList[position]
        holder.nicknameshow.text =user.getnickname()
        holder.emailshow.text =user.getlocateemail()
        val locatepin=user.getlocatepin()
        val nickmanes=user.getnickname()
       val ischecked=user.getchecked()
        if(ischecked.equals("No")){
            holder.locateonoff.isChecked=false
        }else{
            holder.locateonoff.isChecked=true
        }
        val uidd=user.getlocateuid()
   holder.locateonoff.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
var checking=""
       var locem=""
       if(isChecked){

           for(i in usersList){
val checkuserchecked=i.getchecked().toString()
               val emaillo=i.getnickname()
               if(checkuserchecked.equals("Yes")){
                   checking="Yes"
                   if (emaillo != null) {
                       locem=emaillo
                   }
                   break
               }else{
                   checking="No"
               }
           }

           if(checking.equals("Yes")){
               Toast.makeText(mcontext,"Please OFF the Location Tracking For $locem  User first",Toast.LENGTH_SHORT).show()
               holder.locateonoff.isChecked=false
           }else if(checking.equals("No")){



               FirebaseDatabase.getInstance().reference.child("savedemails").child(FirebaseAuth.getInstance()!!.uid!!).child(uidd!!).child("checked").setValue("Yes")
               val ref =
                   FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance()!!.uid!!)

               val userHashmap = HashMap<String, Any>()


               userHashmap["locatemail"]= user.getlocateemail()!!
               userHashmap["locatepin"]= user.getlocatepin()!!

               ref.updateChildren(userHashmap).addOnCompleteListener { tasks ->
                   if (tasks.isSuccessful) {
                       holder.locateonoff.isChecked=true

                   } else {

                   }
               }











           }













       }else{
           FirebaseDatabase.getInstance().reference.child("savedemails").child(FirebaseAuth.getInstance()!!.uid!!).child(uidd!!).child("checked").setValue("No")
           val ref =
               FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance()!!.uid!!)

           val userHashmap = HashMap<String, Any>()
           holder.locateonoff.isChecked=false

           userHashmap["locatemail"]="NONE"
           userHashmap["locatepin"]="NONE"

           ref.updateChildren(userHashmap).addOnCompleteListener { tasks ->
               if (tasks.isSuccessful) {


               } else {

               }
           }



       }




   })

        holder.deleteuser.setOnClickListener {
            if (ischecked == "No") {
                FirebaseDatabase.getInstance().reference.child("savedemails")
                    .child(FirebaseAuth.getInstance()!!.uid!!).child(uidd!!).removeValue()
                FirebaseDatabase.getInstance().reference.child("users")
                    .child(FirebaseAuth.getInstance()!!.uid!!).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val savedemails = snapshot.child("savedemails").value.toString().toInt()
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(FirebaseAuth.getInstance()!!.uid!!).child("savedemails")
                                .setValue("${savedemails - 1}")
                            Toast.makeText(mcontext, "Successfully Deleted", Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })


            }else{
                Toast.makeText(mcontext,"Please OFF the 'Track this user' For $nickmanes",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

    }

    override fun getItemCount(): Int {
        return usersList.size
    }


    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var nicknameshow: TextView = v.findViewById(R.id.nicknameshow)
        var emailshow: TextView = v.findViewById(R.id.emailshow)
        var deleteuser:TextView=v.findViewById(R.id.deleteuser)
        var locateonoff:SwitchCompat=v.findViewById(R.id.locateonoff)

    }















}