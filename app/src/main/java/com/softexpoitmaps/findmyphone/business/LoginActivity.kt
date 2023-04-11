package com.softexpoitmaps.findmyphone.business

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var tab:TabLayout
    private lateinit var viewpager:ViewPager
    private lateinit var fb:FloatingActionButton
    private lateinit var insta:FloatingActionButton
    private lateinit var privacy:FloatingActionButton
    private var firebaseUser: FirebaseUser? = null
     val REQUEST_CODE_PERMISSIONS=101
    private lateinit var mAuth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        tab=findViewById(R.id.tab_layout)
        viewpager=findViewById(R.id.view_pager)
        fb=findViewById(R.id.fab_fb)
        insta=findViewById(R.id.fab_insta)
        privacy=findViewById(R.id.fab_privacy)

        tab.addTab(tab.newTab().setText("Login"))
        tab.addTab(tab.newTab().setText("Signup"))
        tab.tabGravity=TabLayout.GRAVITY_FILL
        val viewpageAdapter=viewPAGERadapter(supportFragmentManager)
        viewpageAdapter.addFragment(Login(), "Login")
        viewpageAdapter.addFragment(signup_fragment(), "Signup")
        viewpager.adapter=viewpageAdapter
        tab.setupWithViewPager(viewpager)
        requestLocationPermission()
        statusCheck()
    }

    private  fun buildAlertMessageNoGps() {
      /*  val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Please Manually 'OFF' The GPS/Location First and Then Again, Manually 'ON' The GPS/Location For Best Tracking Experiences From Here")
            .setCancelable(false)
            .setPositiveButton("GO To GPS/Location Section of your device From Here",
                DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("Close App",
                DialogInterface.OnClickListener { dialog, id ->

                    dialog.cancel()
                    finishAffinity()
                    exitProcess(-1)
                })
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.setCanceledOnTouchOutside(false)

        //Enable GPS
        //Enable GPS
        try {
            var intent = Intent("android.location.GPS_ENABLED_CHANGE")
            intent.putExtra("enabled", true)
            sendBroadcast(intent)
            intent = Intent("android.location.GPS_ENABLED_CHANGE")
            intent.putExtra("enabled", false)
            sendBroadcast(intent)
        }catch(e:Exception){

        }
        */

    }

    private fun statusCheck() {

         //   buildAlertMessageNoGps()

    }


    internal class viewPAGERadapter(fragmentManager: FragmentManager):
        FragmentPagerAdapter(fragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList:ArrayList<Fragment> = ArrayList<Fragment>()
        private val titles:ArrayList<String> = ArrayList<String>()
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
        fun addFragment(fragment: Fragment, title: String){
            fragmentList.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }
    }




    override fun onStart() {
        super.onStart()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            askforlocations()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun requestLocationPermission() {
        val foreground = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (foreground) {
            val foreground = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
            if (foreground) {

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                    REQUEST_CODE_PERMISSIONS)
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE),
                REQUEST_CODE_PERMISSIONS)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var foreground = false

            for (i in permissions.indices) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION,
                        ignoreCase = true)
                ) {
                    //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        foreground = true
                        Toast.makeText(applicationContext,
                            "location permission allowed",
                            Toast.LENGTH_SHORT).show()
                        askforlocations()
                        continue
                    } else {
                        Toast.makeText(applicationContext,
                            "Location Permission denied",
                            Toast.LENGTH_SHORT).show()
                        break
                    }
                }
                if (permissions[i].equals(Manifest.permission.FOREGROUND_SERVICE,
                        ignoreCase = true)
                ) {
                    if (grantResults[i] >= 0) {
                        foreground = true

                        Toast.makeText(applicationContext,
                            "location permission allowed",
                            Toast.LENGTH_SHORT).show()
                        askforlocations()
                    } else {
                        Toast.makeText(applicationContext,
                            "location permission denied",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }











    private fun askforlocations(){
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager


        @SuppressLint("MissingPermission")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            if( lm.isLocationEnabled &&  lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                try {

                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0L,
                        0.0f,
                        object : LocationListener {

                            override fun onLocationChanged(location: Location) {
                                try {
                                    val locations = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    if(locations!=null) {

                                    }


                                } catch (e: Exception) {

                                }


                            }

                        })


                }catch (e:Exception){

                }




            }


        }
    }









}