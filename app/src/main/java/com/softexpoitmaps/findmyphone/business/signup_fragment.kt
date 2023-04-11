package com.softexpoitmaps.findmyphone.business

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_signup_fragment.*
import kotlinx.android.synthetic.main.fragment_signup_fragment.view.*


class signup_fragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var email_re: String
    private lateinit var passwordss: String
    private lateinit var pin_re:String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_signup_fragment, container!!, false)
        mAuth = FirebaseAuth.getInstance()
        email_re = view.email_register.text.toString().trim()
        passwordss = view.password_register.text.toString().trim()
        view.register_btn.setOnClickListener {

            registeruser()

        }





        return view
    }


    private fun registeruser() {
        mAuth = FirebaseAuth.getInstance()
        email_re = email_register.text.toString().trim()
        passwordss = password_register.text.toString().trim()
        pin_re=pin_register.text.toString().trim()

        if (email_re.isEmpty()) {
            email_register.setError("Email cannot be Empty, it Must be filled")

        }
        if (passwordss.isEmpty()) {
            password_register.setError("Password cannot be Empty,it Must be filled")

        }
        if (pin_re.isEmpty()) {
            password_register.setError("Pin cannot be Empty,it Must be filled")

        }

        if (email_re.isEmpty() || passwordss.isEmpty()||pin_re.isEmpty()) {
            Toast.makeText(requireContext(),
                "Can Not Register ,Please fillup all the fields",
                Toast.LENGTH_LONG)
                .show()
            return
        } else {

            mAuth.createUserWithEmailAndPassword(email_re, passwordss)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUserID = mAuth.currentUser!!.uid
                        val refUsers = FirebaseDatabase.getInstance().reference.child("users")
                            .child(firebaseUserID)
                        val userHashmap = HashMap<String, Any>()

                        userHashmap["uid"] = firebaseUserID
                        userHashmap["search"] = email_re
                        userHashmap["email"] = email_re
                        userHashmap["pin"] = pin_re
                        userHashmap["model"] = android.os.Build.MODEL
userHashmap["share"]="OFF"
                        userHashmap["subscription"] = "Free"
                        userHashmap["limit"] = "3"
                        userHashmap["locatemail"]="NONE"
                        userHashmap["locatepin"]="NONE"
                        userHashmap["latitudes"] = "No"
                        userHashmap["savedemails"]="0"
                        userHashmap["longitudes"] = "No"
                        refUsers.updateChildren(userHashmap).addOnCompleteListener { tasks ->
                            if (tasks.isSuccessful) {
                                val i = Intent(requireContext(), MainActivity::class.java)
                                startActivity(i)
                                Toast.makeText(requireContext(),
                                    "Registered Successfully",
                                    Toast.LENGTH_LONG).show()


                            } else {
                                Toast.makeText(requireContext(),
                                    "Something Wrong! Please try again after some time or contact with us",
                                    Toast.LENGTH_LONG).show()
                                return@addOnCompleteListener
                            }
                        }

                    } else {
                        Toast.makeText(requireContext(),
                            "Can Not Registered,Error!!!!" + task.exception!!.message,
                            Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }
                }
        }
    }

}


