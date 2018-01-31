# KwikPicker for Android
A simple Kotlin based image picker using BottomSheet
In Google's Material Design, Google introduced **Bottom sheets**.([Components – Bottom sheets](https://material.google.com/components/bottom-sheets.html))<br/>
**Bottom sheets** slide up from the bottom of the screen to reveal more content.

If you want pick an image from gallery or take a picture, this library can help easily.<br/>
**KwikPicker** provide 3 options: <br/>

1. Take a picture by camera(using `MediaStore.ACTION_IMAGE_CAPTURE` intent)
2. Get image from gallery(using `Intent.ACTION_PICK` intent)
3. Get image from recent image(using `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` cursor)

**KwikPicker** is simple image picker using bottom sheet.

<br/><br/>



## Demo
<br/><br/>
1. Show Bottom Sheet.
2. Pick Image

### Single/Multi Select

![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/screenshot1.jpeg?raw=true)    ![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/demo.gif?raw=true)
![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/screenshot_multi_select.jpeg?raw=true)


## Setup


### Gradle
```javascript

dependencies {
    compile 'co.csadev.kwikpicker:1.0.0'
}

```

If you think this library is useful, *STAR* it!
<br/>
<img src="https://phaser.io/content/news/2015/09/10000-stars.png" width="200">
<br/><br/>



## How to use
### 1. Check Permission
You have to grant `WRITE_EXTERNAL_STORAGE` permission from user.<br/>
If your targetSDK version is 23+, you have to check permission and request permission to user.<br/>
Because after Marshmallow (6.0), you have to not only declare permissions in `AndroidManifest.xml`, but also request permissions at runtime.<br/>
There are so many permission check library in [Android-Arsenal](http://android-arsenal.com/tag/235?sort=rating)<br/>
I recommend [TedPermission](https://github.com/ParkSangGwon/TedPermission)<br/>
**TedPermission** is super simple and smart permission check library.<br/>
<br/>


### 2. Start TedBottomPicker
**TedBottomPicker** class extend `BottomSheetDialogFragment`.<br/>
`TedBottomPicker.Builder` make `new TedBottomPicker()`.<br/>
After then, you can show TedBottomPicker<br/>


```Kotlin
    val kwikPicker = KwikPicker.Builder(this@MainActivity,
        imageProvider = { imageView, uri ->
            Glide.with(activity)//Any image provider here!
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
        .create(activity)
    kwikPicker.show(supportFragmentManager)
```

If you want select multi image, you can use `OnMultiImageSelectedListener`
```Kotlin
    val kwikPicker = KwikPicker.Builder(activity,
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
    .create(activity)
    kwikPicker.show(supportFragmentManager)
```

**Don't forget!!**<br/>
You have to pass both an image provider, and either an 'imageSelectedListener' or `multiImageSelectedListener` in the Builder.<br/>
This listener will pass selected Uri/UriList.<br/>




<br/>

## Customizations

### Function

#### Required
* `imageProvider: (imageView: ImageView, imageUri: Uri?) -> Unit`

#### Common

* `previewMaxCount (default: 25)`
* `peekHeight`
* `showCamera (default: true)`
* `cameraTileDrawable (built in default)`
* `cameraTileBackgroundResId (build in default)`
* `galleryTileDrawable (built in default)`
* `galleryTileBackgroundResId (build in default)`
* `spacing (default: 1dp)`
* `onErrorListener ((message: String) -> Unit)`
* `title`
* `showTitle (default: true)`
* `titleBackgroundResId`

#### Multi Select
* `deSelectIconDrawable`
* `selectedForegroundDrawable`
* `selectMaxCount`
* `selectMinCount`
* `completeButtonText (default: 'Done')`
* `emptySelectionText (default: 'No Image')`
* `selectMaxCountErrorText`
* `selectMinCountErrorText`

<br/><br/>



## Contributing

Yes :) If you found a bug, have an idea how to improve library or have a question, please create new issue or comment existing one. If you would like to contribute code fork the repository and send a pull request.

## License

	HelloCharts
    Copyright 2018 Charles Anderson

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

---
     KwikPicker library is developed from the original TedBottomPicker library available:

       https://github.com/ParkSangGwon/TedBottomPicker

---
     KwikPicker library uses code from Flipboard-bottomsheet available:

       https://github.com/Flipboard/bottomsheet
