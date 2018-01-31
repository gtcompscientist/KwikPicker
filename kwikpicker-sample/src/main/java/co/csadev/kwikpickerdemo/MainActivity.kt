package co.csadev.kwikpickerdemo

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.*
import android.widget.*
import co.csadev.kwikpicker.KwikPicker
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var mGlideRequestManager: RequestManager
    lateinit var image: ImageView
    private lateinit var mSelectedImagesContainer: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mGlideRequestManager = Glide.with(this)

        image = findViewById(R.id.iv_image)
        mSelectedImagesContainer = findViewById(R.id.selected_photos_container)

        setSingleShowButton()
        setMultiShowButton()
    }

    private fun setSingleShowButton() {
        val singleButton = findViewById<Button>(R.id.btn_single_show)
        singleButton.setOnClickListener {
            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    val bottomSheetDialogFragment = KwikPicker.Builder(this@MainActivity,
                        imageProvider = { imageView, uri ->
                            Glide.with(this@MainActivity)
                                .load(uri)
                                .into(imageView)
                        },
                        onImageSelectedListener = { uri: Uri ->
                            image.visibility = View.VISIBLE
                            mSelectedImagesContainer.visibility = View.GONE
                            image.post {
                                mGlideRequestManager
                                    .load(uri)
                                    .into(image)
                            }
                        },
                        peekHeight = 1200)
                        .create(this@MainActivity)
                    bottomSheetDialogFragment.show(supportFragmentManager)
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission Denied\n" + deniedPermissions.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            TedPermission.with(this@MainActivity)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }
    }

    private fun setMultiShowButton() {

        val multiShowButton = findViewById<Button>(R.id.btn_multi_show)
        multiShowButton.setOnClickListener {
            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    val bottomSheetDialogFragment = KwikPicker.Builder(this@MainActivity,
                        imageProvider = { imageView, uri ->
                            Glide.with(this@MainActivity)
                                .load(uri)
                                .into(imageView)
                        },
                        onMultiImageSelectedListener = { list: ArrayList<Uri> ->
                            showUriList(list)
                        },
                        peekHeight = 1600,
                        showTitle = false,
                        completeButtonText = "Done",
                        emptySelectionText = "No Selection")
                    .create(this@MainActivity)
                    bottomSheetDialogFragment.show(supportFragmentManager)
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission Denied\n" + deniedPermissions.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            TedPermission.with(this@MainActivity)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }
    }

    private fun showUriList(uriList: ArrayList<Uri>) {
        // Remove all views before
        // adding the new ones.
        mSelectedImagesContainer.removeAllViews()

        image.visibility = View.GONE
        mSelectedImagesContainer.visibility = View.VISIBLE

        val wdpx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
                .toInt()
        val htpx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
                .toInt()


        for (uri in uriList) {

            val imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, mSelectedImagesContainer, false)
            val thumbnail = imageHolder.findViewById(R.id.media_image) as ImageView

            Glide.with(this)
                .load(uri.toString())
                .into(thumbnail)

            mSelectedImagesContainer.addView(imageHolder)

            thumbnail.layoutParams = FrameLayout.LayoutParams(wdpx, htpx)
        }
    }
}
