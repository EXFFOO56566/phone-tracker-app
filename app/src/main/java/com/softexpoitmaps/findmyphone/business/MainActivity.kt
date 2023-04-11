package com.softexpoitmaps.findmyphone.business


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceNavigationView
import com.luseen.spacenavigation.SpaceOnClickListener
import com.paypal.android.sdk.payments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.address.*
import kotlinx.android.synthetic.main.address.view.*
import kotlinx.android.synthetic.main.anotherphoneinfo.view.*
import kotlinx.android.synthetic.main.emailentry.view.*
import kotlinx.android.synthetic.main.my_address.*
import kotlinx.android.synthetic.main.my_address.view.*
import kotlinx.android.synthetic.main.subscriptions.view.*
import kotlinx.android.synthetic.main.thisdeviceinfo.*
import kotlinx.android.synthetic.main.thisdeviceinfo.view.*
import mumayank.com.airlocationlibrary.AirLocation
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
open class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {
    private var lat2: Double? = 0.toDouble()
    private var lng2:Double=0.toDouble()
    private lateinit var list: ArrayList<JSONObject>
    private var lat1: Double = 0.toDouble()
    private var lng1: Double = 0.toDouble()
private var trackmarker1:Marker?=null
    private lateinit var ll: LatLng
    private var myaddressnow = ""

private val TAG:String="Payment"
    val PAYPAL_KEY:String="AW_jFAGak20fNyAPRCPABawxkK37niyv3yEyPivJWF3vO-JwAyVMUY9ZY6d7KM4vtc0F0fZBHcCx3BPR"
    private val REQUEST_CODE_PAYMENT:Int=1345
    private val CONFIG_ENVIRONMENT:String= PayPalConfiguration.ENVIRONMENT_PRODUCTION
    private lateinit var config:PayPalConfiguration
    private lateinit var thingsToBuy:PayPalPayment


    private var home_marker: Marker? = null
private var subname=""
    private var userlist1: List<emailinfo>? = null
    private var gmap: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var airloc: AirLocation
   private lateinit var alert:AlertDialog
    private var mcity: String = ""
    private var mstate: String = ""
    private var mcountry: String = ""
    private var mpostalCode: String = ""
private lateinit var   recyclerView1:RecyclerView


    private var firebaseUser: FirebaseUser?=null
private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        toolbar=findViewById(R.id.appbar)
        setSupportActionBar(toolbar)
        mapFragment = supportFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fab.setOnClickListener(this)
        navigation.setOnClickListener(this)
        faball.setOnClickListener(this)
        askforlocations()
        list = ArrayList<JSONObject>()
        val spaceNavigationView:SpaceNavigationView = findViewById(R.id.bottom_navigation_view)
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState)
        spaceNavigationView.addSpaceItem(SpaceItem("This Device", R.drawable.phone))
        spaceNavigationView.addSpaceItem(SpaceItem("Tracking Device", R.drawable.anotherphone))
        spaceNavigationView.isSelected=false


        firebaseUser= FirebaseAuth.getInstance().currentUser

        configPaypal()
        val intent:Intent=Intent(this,PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config)
        startService(intent)
try {
    startForegroundService(Intent(this@MainActivity,
        locationservice::class.java))
}catch(e:Exception){

}
        spaceNavigationView.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                if (gmap != null) {

                    refreshgps()
                    airloc =
                        AirLocation(this@MainActivity, true, true, object : AirLocation.Callbacks {
                            @SuppressLint("SetTextI18n")
                            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                                Toast.makeText(this@MainActivity,
                                    "Failed to get the current position",
                                    Toast.LENGTH_SHORT).show()

                            }

                            @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
                            override fun onSuccess(location: Location) {
                                try {


                                    if (home_marker != null) {
                                        home_marker!!.remove()
                                    }
                                    ll = LatLng(lat1, lng1)
                                    val circleDrawable: Drawable =
                                        getResources().getDrawable(R.drawable.smartphone);
                                    val markerIcon: BitmapDescriptor? = getMarkerIconFromDrawable(
                                        circleDrawable)
                                    myaddress()
                                    home_marker = gmap!!.addMarker(MarkerOptions().position(ll)
                                        .title("This Device : $myaddressnow").icon(markerIcon)
                                    )
                                    home_marker!!.showInfoWindow()
                                    gmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(ll,
                                        14.0f))

                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity,
                                        "Failed to get the device's position",
                                        Toast.LENGTH_SHORT).show()

                                }
                                spaceNavigationView.isSelected = false

                            }


                        })

                }


            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(itemIndex: Int, itemName: String) {
                when (itemIndex) {
                    0 -> {
                        try {
                            val bottoms = BottomSheetDialog(this@MainActivity)

                            val inflater = LayoutInflater.from(this@MainActivity)
                            var view2: View = inflater.inflate(R.layout.thisdeviceinfo, null)

                            view2.model.setText(android.os.Build.MODEL)
                            FirebaseDatabase.getInstance().reference.child("users").child(
                                firebaseUser!!.uid)
                                .addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val check = snapshot.child("share").value

                                            if (check!!.equals("OFF")) {
                                                view2.switchtrack.setChecked(false)

                                            } else {
                                                view2.switchtrack.isChecked = true
                                            }

                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })


                            view2.switchtrack.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                                if (isChecked) {
                                    Toast.makeText(this@MainActivity,
                                        "Live Sharing ON",
                                        Toast.LENGTH_LONG)
                                        .show()
                                    FirebaseDatabase.getInstance().reference.child("users").child(
                                        firebaseUser!!.uid).child("share").setValue("ON")



                                    val f = FirebaseAuth.getInstance().currentUser!!
                                   try {
                                       val ref =
                                           FirebaseDatabase.getInstance().reference.child("users")
                                               .child(f.uid)

                                       val userHashmap = HashMap<String, Any>()



                                       userHashmap["latitudes"] = "$lat1"
                                       userHashmap["longitudes"] = "$lng1"

                                       ref.updateChildren(userHashmap)
                                           .addOnCompleteListener { tasks ->
                                               if (tasks.isSuccessful) {


                                               } else {

                                               }
                                           }
                                   }catch( e:Exception){

                                    }


                                } else {
                                    Toast.makeText(this@MainActivity,
                                        "Live Sharing Stopped",
                                        Toast.LENGTH_LONG)
                                        .show()
                                    FirebaseDatabase.getInstance().reference.child("users").child(
                                        firebaseUser!!.uid).child("share").setValue("OFF")




                                }
                            })


                            bottoms.setContentView(view2)

                            bottoms.show()
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this@MainActivity,
                                "Can't Show Device Info",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
                    1 -> {

                        val bottomsheet: BottomSheetDialog = BottomSheetDialog(this@MainActivity)
                        val view: View = layoutInflater.inflate(R.layout.anotherphoneinfo, null)
                        view.cardaddemails.setOnClickListener {
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(firebaseUser!!.uid).addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val limit=snapshot.child("limit").value.toString().toInt()
                                        val savedemails=snapshot.child("savedemails").value.toString().toInt()
                                        var x = 0
                                        if(limit==savedemails){
                                            Toast.makeText(this@MainActivity,"Limit Exceeded",Toast.LENGTH_LONG).show()
                                            return
                                        }else{
                                            val alertdialog=AlertDialog.Builder(this@MainActivity)
                                            val viewe:View=layoutInflater.inflate(R.layout.emailentry,null)
                                            viewe.savebutton.setOnClickListener {
                                                val nickname = viewe.nickname.text.toString().trim()
                                                val locateemail =
                                                    viewe.locateemail.text.toString().trim()
                                                val locatepin =
                                                    viewe.locatepin.text.toString().trim()
                                                FirebaseDatabase.getInstance().reference.child("users")
                                                    .addListenerForSingleValueEvent(
                                                        object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                for (i in snapshot.children) {
                                                                    val searchemail =
                                                                        i.child("email").value.toString()
                                                                    if (searchemail.equals(
                                                                            locateemail)
                                                                    ) {
                                                                        val searchpin =
                                                                            i.child("pin").value.toString()
                                                                        if (searchpin.equals(
                                                                                locatepin)
                                                                        ) {
                                                                            val userHashmap =
                                                                                HashMap<String, Any>()
                                                                            val uidd =
                                                                                i.child("uid").value.toString()

                                                                            userHashmap["locateuid"] =
                                                                                i.child("uid").value.toString()
                                                                            userHashmap["locateemail"] =
                                                                                locateemail
                                                                            userHashmap["locatepin"] =
                                                                                locatepin
                                                                            userHashmap["nickname"] =
                                                                                nickname
                                                                            userHashmap["checked"] =
                                                                                "No"
                                                                            val refUsers =
                                                                                FirebaseDatabase.getInstance().reference.child(
                                                                                    "savedemails")
                                                                                    .child(
                                                                                        firebaseUser!!.uid)
                                                                                    .child(uidd)

                                                                            refUsers.updateChildren(
                                                                                userHashmap)
                                                                                .addOnCompleteListener { tasks ->
                                                                                    if (tasks.isSuccessful) {

                                                                                        Toast.makeText(
                                                                                            this@MainActivity,
                                                                                            "Saved Successfully",
                                                                                            Toast.LENGTH_LONG)
                                                                                            .show()

                                                                                          alert.dismiss()
                                                                                        checkemailsandsubs()

                                                                                    } else {
                                                                                        Toast.makeText(
                                                                                            this@MainActivity,
                                                                                            "Something Wrong! Please try again after some time or contact with us",
                                                                                            Toast.LENGTH_LONG)
                                                                                            .show()
                                                                                        return@addOnCompleteListener
                                                                                    }
                                                                                }


                                                                        } else {
                                                                            Toast.makeText(this@MainActivity,
                                                                                "You set a Wrong Registered pin of  Another phone...",
                                                                                Toast.LENGTH_LONG)
                                                                                .show()
                                                                            return
                                                                        }
                                                                        x = 1
                                                                        break


                                                                    } else {
                                                                        x = 2

                                                                    }

                                                                }
                                                                if (x == 2) {
                                                                    Toast.makeText(this@MainActivity,
                                                                        "There is no registration in the Server with this email address that you want to track...",
                                                                        Toast.LENGTH_LONG).show()

                                                                }


                                                            }


                                                            override fun onCancelled(error: DatabaseError) {

                                                            }

                                                        })
                                            }
                                                alertdialog.setTitle("Set an Email For Live Tracking......")
                                                alertdialog.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                                                    dialogInterface.cancel()

                                                })
                                                alertdialog.setView(viewe)

                                             alert=  alertdialog.create()
                                                alert.show()




                                                                        }
                                            }





                                    override fun onCancelled(error: DatabaseError) {
                                       Toast.makeText(this@MainActivity,"There is a problem in network",Toast.LENGTH_LONG).show()
                                    }
//
                                })


                        }

                         recyclerView1= view.findViewById(R.id.rv)
                        recyclerView1!!.setHasFixedSize(true)
                        recyclerView1!!.layoutManager = LinearLayoutManager(this@MainActivity)

                        userlist1 = java.util.ArrayList<emailinfo>()

                        collectallusersvalue1()
                        bottomsheet.setContentView(view)
                        bottomsheet.show()

                    }

                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemReselected(itemIndex: Int, itemName: String) {
                when (itemIndex) {
                    0 -> {
                        try {
                            val bottoms = BottomSheetDialog(this@MainActivity)

                            val inflater = LayoutInflater.from(this@MainActivity)
                            var view2: View = inflater.inflate(R.layout.thisdeviceinfo, null)

                            view2.model.setText(android.os.Build.MODEL)
                            FirebaseDatabase.getInstance().reference.child("users").child(
                                firebaseUser!!.uid)
                                .addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val check = snapshot.child("share").value

                                            if (check!!.equals("OFF")) {
                                                view2.switchtrack.setChecked(false)

                                            } else {
                                                view2.switchtrack.isChecked = true
                                            }

                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })


                            view2.switchtrack.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                                if (isChecked) {
                                    Toast.makeText(this@MainActivity,
                                        "Live Sharing ON",
                                        Toast.LENGTH_LONG)
                                        .show()
                                    FirebaseDatabase.getInstance().reference.child("users").child(
                                        firebaseUser!!.uid).child("share").setValue("ON")



                                    val f = FirebaseAuth.getInstance().currentUser!!
                                    try {
                                        val ref =
                                            FirebaseDatabase.getInstance().reference.child("users")
                                                .child(f.uid)

                                        val userHashmap = HashMap<String, Any>()



                                        userHashmap["latitudes"] = "$lat1"
                                        userHashmap["longitudes"] = "$lng1"

                                        ref.updateChildren(userHashmap)
                                            .addOnCompleteListener { tasks ->
                                                if (tasks.isSuccessful) {


                                                } else {

                                                }
                                            }
                                    }catch( e:Exception){

                                    }



                                } else {
                                    Toast.makeText(this@MainActivity,
                                        "Live Sharing Stopped",
                                        Toast.LENGTH_LONG)
                                        .show()
                                    FirebaseDatabase.getInstance().reference.child("users").child(
                                        firebaseUser!!.uid).child("share").setValue("OFF")



                                }
                            })


                            bottoms.setContentView(view2)

                            bottoms.show()
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(this@MainActivity,
                                "Can't Show Device Info",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
                    1 -> {

                        val bottomsheet: BottomSheetDialog = BottomSheetDialog(this@MainActivity)
                        val view: View = layoutInflater.inflate(R.layout.anotherphoneinfo, null)
                        view.cardaddemails.setOnClickListener {
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(firebaseUser!!.uid).addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val limit=snapshot.child("limit").value.toString().toInt()
                                            val savedemails=snapshot.child("savedemails").value.toString().toInt()
                                            var x = 0
                                            if(limit==savedemails){
                                                Toast.makeText(this@MainActivity,"Limit Exceeded",Toast.LENGTH_LONG).show()
                                                return
                                            }else{


                                                val alertdialog=AlertDialog.Builder(this@MainActivity)
                                                val viewe:View=layoutInflater.inflate(R.layout.emailentry,null)
                                                viewe.savebutton.setOnClickListener {
                                                    val nickname=viewe.nickname.text.toString().trim()
                                                    val locateemail=viewe.locateemail.text.toString().trim()
                                                    val locatepin=viewe.locatepin.text.toString().trim()

                                                    FirebaseDatabase.getInstance().reference.child("users")
                                                        .addListenerForSingleValueEvent(
                                                            object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    for (i in snapshot.children) {
                                                                        val searchemail =
                                                                            i.child("email").value.toString()
                                                                        if (searchemail.equals(locateemail)) {
                                                                            val searchpin =
                                                                                i.child("pin").value.toString()
                                                                            if (searchpin.equals(locatepin)) {
                                                                                val userHashmap = HashMap<String, Any>()
                                                                                val uidd=i.child("uid").value.toString()

                                                                                userHashmap["locateuid"] =  i.child("uid").value.toString()
                                                                                userHashmap["locateemail"] = locateemail
                                                                                userHashmap["locatepin"] = locatepin
                                                                                userHashmap["nickname"]=nickname
                                                                                userHashmap["checked"]="No"
                                                                                val refUsers = FirebaseDatabase.getInstance().reference.child("savedemails")
                                                                                    .child(firebaseUser!!.uid).child(uidd)

                                                                                refUsers.updateChildren(userHashmap).addOnCompleteListener { tasks ->
                                                                                    if (tasks.isSuccessful) {

                                                                                        Toast.makeText(this@MainActivity,
                                                                                            "Saved Successfully",
                                                                                            Toast.LENGTH_LONG).show()




                                                                                        alert.dismiss()
                                                                                        checkemailsandsubs()

                                                                                    } else {
                                                                                        Toast.makeText(this@MainActivity,
                                                                                            "Something Wrong! Please try again after some time or contact with us",
                                                                                            Toast.LENGTH_LONG).show()
                                                                                        return@addOnCompleteListener
                                                                                    }
                                                                                }


                                                                            } else {
                                                                                Toast.makeText(this@MainActivity,
                                                                                    "You set a Wrong Registered pin of  Another phone...",
                                                                                    Toast.LENGTH_LONG).show()
                                                                                return
                                                                            }
                                                                            x = 1
                                                                            break


                                                                        } else {
                                                                            x = 2

                                                                        }

                                                                    }
                                                                    if (x == 2) {
                                                                        Toast.makeText(this@MainActivity,
                                                                            "There is no registration in the Server with this email address that you want to track...",
                                                                            Toast.LENGTH_LONG).show()

                                                                    }


                                                                }


                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                            })




                                                }
                                                alertdialog.setTitle("Set an Email For Live Tracking")
                                                alertdialog.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                                                    dialogInterface.cancel()

                                                })
                                                alertdialog.setView(viewe)

                                              alert=  alertdialog.create()
                                                alert.show()
                                            }
                                        }





                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@MainActivity,"There is a problem in network",Toast.LENGTH_LONG).show()
                                        }
//
                                    })


                        }

                        recyclerView1= view.findViewById(R.id.rv)
                        recyclerView1!!.setHasFixedSize(true)
                        recyclerView1!!.layoutManager = LinearLayoutManager(this@MainActivity)

                        userlist1 = java.util.ArrayList<emailinfo>()

                        collectallusersvalue1()
                        bottomsheet.setContentView(view)
                        bottomsheet.show()

                    }

                }
            }
        })

        getSupportActionBar()!!.title=""







    }

    private fun checkemailsandsubs() {
        var c=0
try {
    FirebaseDatabase.getInstance().reference.child("savedemails").child(firebaseUser!!.uid)
        .addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        c = c + 1
                    }
                    if(c>0){
                        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("savedemails").setValue("$c")
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
}catch(e:Exception){

}

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            gmap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            gmap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            gmap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            gmap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.logout -> {
FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("share").setValue("OFF")

            FirebaseDatabase.getInstance().goOnline();
            FirebaseAuth.getInstance().signOut()
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData() // note: it has a return value!
            } else {
                // use old hacky way, which can be removed
                // once minSdkVersion goes above 19 in a few years.
            }
            startActivity(Intent(this, LoginActivity::class.java))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finishAffinity()

            exitProcess(-1)

            true


        }
        R.id.changepin -> {
            val bottomshh = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.address, null)
            view.pinsave.setOnClickListener {
                val newpin = view.pintype.text.toString().trim()
                if (newpin.isEmpty()) {
                    Toast.makeText(this, "Please Fill the fields", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                } else if (newpin.length < 4) {
                    Toast.makeText(this,
                        "Atleast 4 numbers needed for setting up the pin",
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                } else {
                    FirebaseDatabase.getInstance().reference.child("users")
                        .child(firebaseUser!!.uid).child(
                            "pin").setValue(newpin)
                    Toast.makeText(this, "Pin Changed Successfully", Toast.LENGTH_LONG).show()

                }
            }
            bottomshh.setContentView(view)
            bottomshh.show()

            true
        }
        R.id.subscription -> {

            val bottomshh = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.subscriptions, null)
            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            view.usersubsid.setText(snapshot.child("subscription").value.toString())
                            val limit=snapshot.child("limit").value.toString()
                            view.limitofuser.setText("Can Locate $limit users  only")
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

            view.bronze.setOnClickListener {
                makepayment("11","Bronze")
                subname="Bronze"
            }
            view.silver.setOnClickListener {
                makepayment("19","Silver")
                subname="Silver"
            }
            view.gold.setOnClickListener {
                makepayment("23","Gold")
                subname="Gold"

            }




            bottomshh.setContentView(view)
            bottomshh.show()


            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun makepayment(s: String, name:String) {
val intent=Intent(this,PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config)
        startService(intent)
        thingsToBuy= PayPalPayment(BigDecimal(s.toString()),"USD","Payment for $name Subscription",PayPalPayment.PAYMENT_INTENT_SALE)
        val payment:Intent=Intent(this,PaymentActivity::class.java)
        payment.putExtra(PaymentActivity.EXTRA_PAYMENT,thingsToBuy)
        payment.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config)
        startActivityForResult(payment,REQUEST_CODE_PAYMENT)
    }

    private fun configPaypal() {
        config=PayPalConfiguration().environment(CONFIG_ENVIRONMENT).clientId(PAYPAL_KEY).merchantName("MY May- Find My Phone")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airloc.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode==REQUEST_CODE_PAYMENT){
            if(resultCode== Activity.RESULT_OK){
                val confirmation:PaymentConfirmation=data!!.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)!!
                if(confirmation!=null){

                        Toast.makeText(this,"Payment Successfull...Congratulations....You are now a $subname user",Toast.LENGTH_LONG).show()

               if(subname=="Gold"){
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("subscription").setValue("Gold")
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("limit").setValue("20")


               }else if(subname=="Silver"){
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("subscription").setValue("Silver")
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("limit").setValue("12")


               }else if(subname=="Bronze"){
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("subscription").setValue("Bronze")
                   FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("limit").setValue("7")



               }











                }else{

                        Toast.makeText(this,"Payment Not Successfull",Toast.LENGTH_LONG).show()

                }
            }else if(resultCode==Activity.RESULT_CANCELED){
                Toast.makeText(this,"Payment cancelled",Toast.LENGTH_LONG).show()
            } else if(requestCode==PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this,"Server Error, Can not Pay",Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        airloc.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onMapReady(map: GoogleMap?) {
        gmap = map




        if (gmap != null) {
            airloc = AirLocation(this, true, true, object : AirLocation.Callbacks {
                @SuppressLint("SetTextI18n")
                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    Toast.makeText(this@MainActivity,
                        "Failed to get the current position",
                        Toast.LENGTH_SHORT).show()
                    checkBattery()
                }

                @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
                override fun onSuccess(location: Location) {
                    try {

                        lat1 = location.latitude
                        lng1 = location.longitude
                        ll = LatLng(lat1, lng1)

                        val circleDrawable: Drawable =
                            getResources().getDrawable(R.drawable.smartphone);
                        val markerIcon: BitmapDescriptor? =
                            getMarkerIconFromDrawable(circleDrawable)
                        myaddress()

                        home_marker?.setIcon(null)
                        home_marker = gmap!!.addMarker(MarkerOptions().position(ll)
                            .title("This Device : $myaddressnow").icon(markerIcon)
                        )
                        home_marker!!.showInfoWindow()

                        gmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 14.0f))
                        checkBattery()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity,
                            "Failed to get the current position",
                            Toast.LENGTH_SHORT).show()
                        checkBattery()

                    }

                }


            })

        }

    }


    private  fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
fun mydetails(){
    try{
        val bottom = BottomSheetDialog(this)
        bottom.setTitle("MY Information")
        val inflater = LayoutInflater.from(this)
        var view1: View = inflater.inflate(R.layout.my_address, null)
        view1.city_value.setText(mcity)
        view1.myaddress.setText("My Location : " + myaddressnow)
        view1.state_value.setText(mstate)
        view1.country_value.setText(mcountry)
        view1.postal_value.setText(mpostalCode)
        bottom.setContentView(view1)

        bottom.show()
        }
    catch (e: Exception){
        Toast.makeText(this, "Can't access...try after some time", Toast.LENGTH_SHORT).show()
    }
}

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {


        when (v!!.id) {


            R.id.fab -> {
                if (gmap != null) {
                    refreshgps()
                    airloc = AirLocation(this, true, true, object : AirLocation.Callbacks {
                        @SuppressLint("SetTextI18n")
                        override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to get the current position",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
                        override fun onSuccess(location: Location) {
                            try {

                                ll = LatLng(lat1, lng1)
                                myaddress()


                                if (home_marker != null) {
                                    home_marker!!.remove()
                                }

                                val circleDrawable: Drawable =
                                    getResources().getDrawable(R.drawable.smartphone);
                                val markerIcon: BitmapDescriptor? = getMarkerIconFromDrawable(
                                    circleDrawable)

                                home_marker = gmap!!.addMarker(MarkerOptions().position(ll)
                                    .title("This Device : $myaddressnow").icon(markerIcon)
                                )
                                home_marker!!.showInfoWindow()
                                gmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 14.5f))
                                mydetails()

                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity,
                                    "Can not get Details",
                                    Toast.LENGTH_LONG).show()
                            }
                        }

                    })


                }


            }
            R.id.navigation -> {
                if (lat2 == 0.0 || lng2 == 0.0) {
                    Toast.makeText(this,
                        "No Device is being Tracked.Routes can not draw",
                        Toast.LENGTH_LONG).show()
                    return
                } else {
                    val uri =
                        "http://maps.google.com/maps?f=d&hl=en&saddr=" + lat1.toString() + "," + lng1.toString() + "&daddr=" + lat2.toString() + "," + lng2
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(Intent.createChooser(intent,
                        "Select an application For Draw Routes"))
                }


            }
            R.id.faball -> {
                if(gmap!=null){
                    gmap!!.clear()
                }
                val circleDrawable: Drawable =
                    getResources().getDrawable(R.drawable.smartphone);
                val markerIcon: BitmapDescriptor? = getMarkerIconFromDrawable(
                    circleDrawable)

                home_marker = gmap!!.addMarker(MarkerOptions().position(ll)
                    .title("This Device : $myaddressnow").icon(markerIcon)
                )
                home_marker!!.showInfoWindow()


                var x = 0

                FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val locateemail = snapshot.child("locatemail").value.toString()
                                val locatepin = snapshot.child("locatepin").value.toString()
                                if (locateemail.equals("NONE") && locatepin.equals("NONE")) {
                                    Toast.makeText(this@MainActivity,
                                        "Please Set  Another Phone's Registered Email Address and Pin On Tracking Device Section and make sure that 'Track this user' is ON",
                                        Toast.LENGTH_LONG).show()
                                    return
                                } else {
                                    FirebaseDatabase.getInstance().reference.child("users")
                                        .addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (i in snapshot.children) {
                                                        val searchemail =
                                                            i.child("email").value.toString()
                                                        if (searchemail.equals(locateemail)) {
                                                            val searchpin =
                                                                i.child("pin").value.toString()
                                                            if (searchpin.equals(locatepin)) {
                                                                val latitudeoftrack =
                                                                    i.child("latitudes").value.toString()
                                                                val longitudesoftrack =
                                                                    i.child("longitudes").value.toString()
                                                                val  share=i.child("share").value.toString()

                                                                if (share!="ON")
                                                                 {
                                                                    Toast.makeText(this@MainActivity,
                                                                        "Please ON the 'Track this device' option in  another phone's 'This Device' Section... Another Phone is Currently not sharing its Live location",
                                                                        Toast.LENGTH_LONG).show()
                                                                    return
                                                                } else {
                                                                    val track =
                                                                        LatLng(latitudeoftrack.toDouble(),
                                                                            longitudesoftrack.toDouble())
                                                                    lat2 =
                                                                        latitudeoftrack.toDouble()
                                                                    lng2 =
                                                                        longitudesoftrack.toDouble()
                                                                    val circleDrawable1: Drawable =
                                                                        getResources().getDrawable(R.drawable.trackofphone);
                                                                    val markerIcon: BitmapDescriptor? =
                                                                        getMarkerIconFromDrawable(
                                                                            circleDrawable1)
                                                                    val address = findaddress(
                                                                        latitudeoftrack.toDouble(),
                                                                        longitudesoftrack.toDouble())
                                                                    if (trackmarker1 != null) {
                                                                        trackmarker1!!.remove()
                                                                    }

                                                                    trackmarker1 =
                                                                        gmap!!.addMarker(
                                                                            MarkerOptions().position(
                                                                                track)
                                                                                .title("$locateemail : $address")
                                                                                .icon(
                                                                                    markerIcon)
                                                                        )
                                                                    trackmarker1!!.showInfoWindow()
                                                                    gmap!!.animateCamera(
                                                                        CameraUpdateFactory.newLatLngZoom(
                                                                            track,
                                                                            14.5f))

                                                                }


                                                            } else {
                                                                Toast.makeText(this@MainActivity,
                                                                    "You set a Wrong Registered pin of  Another phone...Please Change The pin number on Tracking Device Section in your app and Set a Correct pin that is registed on  another phone",
                                                                    Toast.LENGTH_LONG).show()
                                                                return
                                                            }
                                                            x = 1
                                                            break


                                                        } else {
                                                            x = 2

                                                        }

                                                    }
                                                    if (x == 2) {
                                                        Toast.makeText(this@MainActivity,
                                                            "There is no registration in MyMap-Find My Phone App Server with this email address that you want to track...Please Change That email address on Tracking Device Section in your app and and Set a Correct email address that is registed on the Server",
                                                            Toast.LENGTH_LONG).show()

                                                    }


                                                }


                                                override fun onCancelled(error: DatabaseError) {

                                                }

                                            })


                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })


            }




        }

    }













    // My location
   private fun myaddress() {
        val geocoder: Geocoder
        val addresses: List<Address>

            geocoder = Geocoder(this, Locale.getDefault())

            addresses = geocoder.getFromLocation(lat1,
                lng1,
                1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        try {
            val address: String =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            myaddressnow = address
            mcity = addresses[0].getLocality().toString()
            mstate = addresses[0].getAdminArea().toString()
            mcountry = addresses[0].getCountryName().toString()
            mpostalCode = addresses[0].getPostalCode().toString()
        }catch (e: Exception){

        }

    }



//end


 //Address for draw routes
  private  fun findaddress(lat: Double, lng: Double):String{
        val geocoder: Geocoder
        val addresses: List<Address>
       try {
           geocoder = Geocoder(this, Locale.getDefault())

           addresses = geocoder.getFromLocation(lat,
               lng,
               1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


           val address: String =
               addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
           return address
       }catch (e: Exception){
           Toast.makeText(this@MainActivity, "Can not find Details", Toast.LENGTH_LONG).show()
       }

return "None"
    }

    //End





    private fun collectallusersvalue1() {
        val firebaseuserid = FirebaseAuth.getInstance().uid
        val refOfUsers = FirebaseDatabase.getInstance().reference.child("savedemails").child(firebaseUser!!.uid)
        refOfUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                (userlist1 as java.util.ArrayList<emailinfo>).clear()
                for (i in snapshot.children) {
                    val emailinfo: emailinfo? = i.getValue(emailinfo::class.java)

                    (userlist1 as java.util.ArrayList<emailinfo>).add(emailinfo!!)

                }



                val  Emailadapter = EmailsAdapter(this@MainActivity,
                    userlist1 as java.util.ArrayList<emailinfo>)

                recyclerView1!!.adapter = Emailadapter


            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,
                    "No one is found! Please try after some time ",
                    Toast.LENGTH_LONG).show()
            }

        })
    }


    override fun onDestroy() {
        super.onDestroy()


        stopService(Intent(this,PayPalService::class.java))



    }


    override fun onResume() {
        super.onResume()
        askforlocations()
        checkBattery()
        refreshgps()

    }

    override fun onStart() {
        super.onStart()
        askforlocations()
        checkBattery()
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
                                        lng1 = locations!!.longitude
                                        lat1 = locations.latitude
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


    private  fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }


    private fun checkBattery() {
        if (!isIgnoringBatteryOptimizations(applicationContext) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // val name = "OFFICE SERVICES:  Employee App"
            //  Toast.makeText(applicationContext, "Battery optimization -> All apps -> $name -> Don't optimize", Toast.LENGTH_LONG).show()
            val builderr=AlertDialog.Builder(this)
          builderr.setIcon(R.drawable.logo_map1)
            builderr.setTitle("Attention Please! Read Must")
            builderr.setMessage("Compulsory: Please turn OFF Battery Optimization for this app. Its Compulsory For Background Feature.\n\n\nAdditional:(Just read these once)\n1.If You are using Xiaomi Redmi model Phone, then off the MIUI Optimization for this app manually from app settings.\n2.Don't use the app in ultra low power mode or Data Saver Mode")
            builderr.setPositiveButton("Compulsory: Turn Off Battery Optimization from here", DialogInterface.OnClickListener { dialogInterface, i ->

                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
                dialogInterface.dismiss()
                finish()

            })

            val alert= builderr.create()
            alert.show()
            alert.setCanceledOnTouchOutside(false)
            alert.setCancelable(false)

        }
    }







private fun refreshgps(){

}










    }






