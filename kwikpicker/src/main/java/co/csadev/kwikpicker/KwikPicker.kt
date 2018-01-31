package co.csadev.kwikpicker

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.annotation.ColorRes
import android.support.design.widget.*
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import co.csadev.kwikpicker.adapter.ImageGalleryAdapter
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class KwikPicker : BottomSheetDialogFragment() {
    private val TAG = "Kwik"

    private val REQ_CODE_CAMERA = 1
    private val REQ_CODE_GALLERY = 2
    private val EXTRA_CAMERA_IMAGE_URI = "camera_image_uri"
    private lateinit var builder: Builder

    private lateinit var imageGalleryAdapter: ImageGalleryAdapter
    private lateinit var viewTitleContainer: View
    private lateinit var titleText: TextView
    private lateinit var doneButton: Button

    private lateinit var selectedPhotosContainerFrame: FrameLayout
    private lateinit var selectedPhotosHorizontal: HorizontalScrollView
    private lateinit var selectedPhotosContainer: LinearLayout

    private lateinit var selectedPhotosEmpty: TextView
    private lateinit var contentView: View
    private var selectedUriList: ArrayList<Uri> = ArrayList()

    private var cameraImageUri: Uri? = null
    private var galleryRecycler: RecyclerView? = null
    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) { }
    }

    private val imageFile: File?
        get() {
            var imageFile: File? = null
            try {
                val timeStamp =
                    SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_" + timeStamp + "_"
                val storageDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                if (!storageDir.exists())
                    storageDir.mkdirs()

                imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
                )
                cameraImageUri = Uri.fromFile(imageFile)
            } catch (e: IOException) {
                e.printStackTrace()
                errorMessage("Could not create imageFile for camera")
            }


            return imageFile
        }

    private val isMultiSelect: Boolean
        get() = builder.onMultiImageSelectedListener != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraImageUri = savedInstanceState?.getParcelable(EXTRA_CAMERA_IMAGE_URI)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri)
        super.onSaveInstanceState(outState)
    }

    fun show(fragmentManager: FragmentManager) {
        val ft = fragmentManager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val acd = dialog as AppCompatDialog
        when(style) {
            DialogFragment.STYLE_NO_INPUT -> {
                dialog.window?.addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
            }
            DialogFragment.STYLE_NO_FRAME, DialogFragment.STYLE_NO_TITLE -> {
                acd.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
            }
        }
        contentView = View.inflate(context, R.layout.kwik_picker_content_view, null)
        dialog.setContentView(contentView)
        val layoutParams =
            (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
            if (builder.peekHeight > 0) {
                behavior.peekHeight = builder.peekHeight
            }
        }

        initView(contentView)

        setTitle()
        setRecyclerView()
        setSelectionView()

        setDoneButton()
        checkMultiMode()
    }

    private fun setSelectionView() {
        if (builder.emptySelectionText != null) {
            selectedPhotosEmpty.text = builder.emptySelectionText
        }
    }

    private fun setDoneButton() {
        if (builder.completeButtonText != null) {
            doneButton.text = builder.completeButtonText
        }
        doneButton.setOnClickListener { onMultiSelectComplete() }
    }

    private fun onMultiSelectComplete() {
        if (selectedUriList.size < builder.selectMinCount) {
            val message: String? = builder.selectMinCountErrorText
                    ?: String.format(
                        resources.getString(R.string.select_min_count),
                        builder.selectMinCount)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return
        }


        selectedUriList.let {
            builder.onMultiImageSelectedListener?.invoke(it)
        }
        dismissAllowingStateLoss()
    }

    private fun checkMultiMode() {
        if (!isMultiSelect) {
            doneButton.visibility = View.GONE
            selectedPhotosContainerFrame.visibility = View.GONE
        }
    }

    private fun initView(contentView: View) {
        viewTitleContainer = contentView.findViewById(R.id.view_title_container)
        galleryRecycler = contentView.findViewById(R.id.rc_gallery) as RecyclerView
        titleText = contentView.findViewById(R.id.tv_title) as TextView
        doneButton = contentView.findViewById(R.id.btn_done) as Button

        selectedPhotosContainerFrame =
                contentView.findViewById(R.id.selected_photos_container_frame) as FrameLayout
        selectedPhotosHorizontal =
                contentView.findViewById(R.id.hsv_selected_photos) as HorizontalScrollView
        selectedPhotosContainer =
                contentView.findViewById(R.id.selected_photos_container) as LinearLayout
        selectedPhotosEmpty = contentView.findViewById(R.id.selected_photos_empty) as TextView
    }

    private fun setRecyclerView() {

        val gridLayoutManager = GridLayoutManager(activity, 3)
        galleryRecycler?.layoutManager = gridLayoutManager
        galleryRecycler?.addItemDecoration(
            GridSpacingItemDecoration(
                gridLayoutManager.spanCount,
                builder.spacing,
                false
            )
        )
        updateAdapter()
    }

    private fun updateAdapter() {
        imageGalleryAdapter = ImageGalleryAdapter(context!!, builder)
        galleryRecycler?.adapter = imageGalleryAdapter
        imageGalleryAdapter.setOnItemClickListener(object :
            ImageGalleryAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {

                val pickerTile = imageGalleryAdapter.getItem(position)

                when (pickerTile.tileType) {
                    ImageGalleryAdapter.PickerTile.CAMERA -> startCameraIntent()
                    ImageGalleryAdapter.PickerTile.GALLERY -> startGalleryIntent()
                    ImageGalleryAdapter.PickerTile.IMAGE -> complete(pickerTile.imageUri)

                    else -> errorMessage()
                }
            }
        })
    }

    private fun complete(uri: Uri?) {
        Log.d(TAG, "selected uri: " + uri!!.toString())
        if (isMultiSelect) {
            if (selectedUriList.contains(uri)) {
                removeImage(uri)
            } else {
                addUri(uri)
            }
        } else {
            builder.onImageSelectedListener?.invoke(uri)
            dismissAllowingStateLoss()
        }
    }

    private fun addUri(uri: Uri): Boolean {
        if (selectedUriList.size == builder.selectMaxCount) {
            val message: String?
            if (builder.selectMaxCountErrorText != null) {
                message = builder.selectMaxCountErrorText
            } else {
                message = String.format(
                    resources.getString(R.string.select_max_count),
                    builder.selectMaxCount
                )
            }

            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return false
        }

        selectedUriList.add(uri)

        val rootView =
            LayoutInflater.from(activity).inflate(R.layout.kwik_picker_selected_item, selectedPhotosContainer, false)
        val thumbnail = rootView.findViewById(R.id.selected_photo) as ImageView
        val closeImage = rootView.findViewById(R.id.iv_close) as ImageView
        rootView.tag = uri

        selectedPhotosContainer.addView(rootView, 0)

        val px = resources.getDimension(R.dimen.kwik_picker_selected_image_height).toInt()
        thumbnail.layoutParams = FrameLayout.LayoutParams(px, px)

        builder.imageProvider.invoke(thumbnail, uri)


        builder.deSelectIconDrawable?.let { closeImage.setImageDrawable(it) }

        closeImage.setOnClickListener { removeImage(uri) }


        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri)
        return true
    }

    private fun removeImage(uri: Uri) {
        selectedUriList.remove(uri)

        for (i in 0 until selectedPhotosContainer.childCount) {
            val childView = selectedPhotosContainer.getChildAt(i)


            if (childView.tag == uri) {
                selectedPhotosContainer.removeViewAt(i)
                break
            }
        }

        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri)
    }

    private fun updateSelectedView() {
        if (selectedUriList.size == 0) {
            selectedPhotosEmpty.visibility = View.VISIBLE
            selectedPhotosContainer.visibility = View.GONE
        } else {
            selectedPhotosEmpty.visibility = View.GONE
            selectedPhotosContainer.visibility = View.VISIBLE
        }
    }

    private fun startCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(activity!!.packageManager) == null) {
            errorMessage("This Application do not have Camera Application")
            return
        }

        val imageFile = imageFile
        val photoURI = FileProvider.getUriForFile(
            context!!,
            context!!.applicationContext.packageName + ".provider",
            imageFile!!
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(cameraIntent, REQ_CODE_CAMERA)
    }

    private fun errorMessage(message: String? = null) {
        val errorMessage = message ?: "Something went wrong."

        if (builder.onErrorListener == null) {
            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        } else {
            builder.onErrorListener?.invoke(errorMessage)
        }
    }

    private fun startGalleryIntent() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        if (galleryIntent.resolveActivity(activity?.packageManager) == null) {
            errorMessage("This Application does not have Gallery Application")
            return
        }

        startActivityForResult(galleryIntent, REQ_CODE_GALLERY)
    }

    private fun setTitle() {

        if (!builder.showTitle) {
            titleText.visibility = View.GONE

            if (!isMultiSelect) {
                viewTitleContainer.visibility = View.GONE
            }

            return
        }

        if (!TextUtils.isEmpty(builder.title)) {
            titleText.text = builder.title
        }

        if (builder.titleBackgroundResId > 0) {
            titleText.setBackgroundResource(builder.titleBackgroundResId)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQ_CODE_GALLERY -> onActivityResultGallery(data)
            REQ_CODE_CAMERA -> onActivityResultCamera(cameraImageUri)
            else -> errorMessage()
        }
    }

    private fun onActivityResultCamera(cameraImageUri: Uri?) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(cameraImageUri!!.path),
            arrayOf("image/jpeg"),
            object : MediaScannerConnection.MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                }

                override fun onScanCompleted(s: String, uri: Uri) {
                    activity?.runOnUiThread {
                        updateAdapter()
                        complete(cameraImageUri)
                    }
                }
            })
    }

    private fun onActivityResultGallery(data: Intent?) {
        val temp = data?.data

        if (temp == null) {
            errorMessage()
            return
        }

        val selectedImageUri =
            try {
                Uri.fromFile(File(URI.create(temp.encodedPath)))
            } catch (ex: Exception) {
                temp
            }

        complete(selectedImageUri)
    }

    class Builder(
        context: Context,
        var imageProvider: (imageView: ImageView, imageUri: Uri?) -> Unit,
        var previewMaxCount: Int = 25,
        var cameraTileDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_camera),
        var galleryTileDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_gallery),
        var deSelectIconDrawable: Drawable? = null,
        var selectedForegroundDrawable: Drawable? = null,
        var spacing: Int = context.resources.getDimensionPixelSize(R.dimen.kwik_picker_grid_layout_margin),
        var onImageSelectedListener: ((uri: Uri) -> Unit)? = null,
        var onMultiImageSelectedListener: ((uriList: ArrayList<Uri>) -> Unit)? = null,
        var onErrorListener: ((message: String) -> Unit)? = null,
        var showCamera: Boolean = true,
        var showGallery: Boolean = true,
        var peekHeight: Int = -1,
        @ColorRes
        var cameraTileBackgroundResId: Int = R.color.kwik_picker_camera,
        @ColorRes
        var galleryTileBackgroundResId: Int = R.color.kwik_picker_gallery,
        var title: String? = null,
        var showTitle: Boolean = true,
        var titleBackgroundResId: Int = 0,
        var selectMaxCount: Int = Integer.MAX_VALUE,
        var selectMinCount: Int = 0,
        var completeButtonText: String? = "Done",
        var emptySelectionText: String? = "No Image",
        var selectMaxCountErrorText: String? = null,
        var selectMinCountErrorText: String? = null) {

        fun create(context: Context): KwikPicker {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                throw RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?")
            }

            if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
                throw RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri")
            }

            val customBottomSheetDialogFragment = KwikPicker()
            customBottomSheetDialogFragment.builder = this
            return customBottomSheetDialogFragment
        }
    }
}
