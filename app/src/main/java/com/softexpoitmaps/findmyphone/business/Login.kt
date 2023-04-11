package com.softexpoitmaps.findmyphone.business

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.layout_forgot_pass.view.*


class Login : Fragment() {
private lateinit var mAuth:FirebaseAuth
private lateinit var emails:String
    private lateinit var password:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_login, container!!, false)
        mAuth= FirebaseAuth.getInstance()
        emails=view.email.text.toString().trim()
        password=view.password_login.text.toString().trim()
        view.login_btn.setOnClickListener {

            loginUser()

        }

        view.orr.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setTitle("Password Recovery System")
            val v: View = layoutInflater.inflate(R.layout.layout_forgot_pass, null)


            v.forbtn.setOnClickListener {

                recoverpassword(v)
            }
            bottomSheetDialog.setContentView(v)
            bottomSheetDialog.show()

        }


        return view
    }



    private fun loginUser() {
        emails= email.text.toString().trim()
        password= password_login.text.toString().trim()
        if(emails.isEmpty()){
            email.setError("Email cannot be Empty, it Must be filled")

        }
        if(password.isEmpty()){
            password_login.setError("Password cannot be Empty,it Must be filled")

        }

        if(emails.isEmpty()||password.isEmpty()){
            Toast.makeText(requireContext(),"Can Not Log in,Please fillup all the fields", Toast.LENGTH_LONG).show()
            return
        }else{
            mAuth.signInWithEmailAndPassword(emails,password).addOnCompleteListener {task->
                if(task.isSuccessful){
                    val i= Intent(requireContext(),MainActivity::class.java)
                    startActivity(i)

                }else{
                    Toast.makeText(requireContext(),"Something Wrong! Please Check your Email and Password ! or Try again after few seconds",
                        Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
            }
        }
    }


    private fun recoverpassword(v:View) {

        val getEmailtext: EditText =v.findViewById<EditText>(R.id.recoveryEmail)
        if(getEmailtext.text.toString().isEmpty()){
            Toast.makeText(requireContext(),"Enter your Email",Toast.LENGTH_LONG).show()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(getEmailtext.text.toString()).matches()){
            Toast.makeText(requireContext(),"Email is invalid",Toast.LENGTH_LONG).show()
            return
        }

        mAuth.sendPasswordResetEmail(getEmailtext.text.toString()).addOnCompleteListener{task->
            if(task.isSuccessful) {
                Toast.makeText(requireContext(),"Email has been Sent, Please Check Your Inbox",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(requireContext(),"Email not Send! Please Try Again Later",Toast.LENGTH_LONG).show()
            }
        }
    }


}