package com.example.photoeditorproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import ly.img.android.pesdk.assets.filter.basic.*
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes
import ly.img.android.pesdk.backend.decoder.ImageSource
import ly.img.android.pesdk.backend.filter.FilterAsset
import ly.img.android.pesdk.backend.filter.LutColorFilterAsset
import ly.img.android.pesdk.backend.model.config.CropAspectAsset
import ly.img.android.pesdk.backend.model.constant.Directory
import ly.img.android.pesdk.backend.model.state.AssetConfig
import ly.img.android.pesdk.backend.model.state.EditorLoadSettings
import ly.img.android.pesdk.backend.model.state.EditorSaveSettings
import ly.img.android.pesdk.backend.model.state.manager.SettingsList
import ly.img.android.pesdk.ui.activity.ImgLyIntent
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder
import ly.img.android.pesdk.ui.model.state.*
import ly.img.android.pesdk.ui.panels.item.CropAspectItem
import ly.img.android.pesdk.ui.panels.item.ToolItem
import ly.img.android.pesdk.ui.utils.PermissionRequest
import ly.img.android.serializer._3._0._0.PESDKFileWriter
import java.io.File
import java.io.IOException

class EditorActivity : Activity(), PermissionRequest.Response {

    companion object {
        const val PESDK_RESULT = 1
        const val GALLERY_RESULT = 2
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun permissionGranted() {}

    override fun permissionDenied() {
    }

    private fun createPesdkSettingsList() = SettingsList().apply {

        getSettingsModel(UiConfigFilter::class.java).apply {
            setFilterList(FilterPackBasic.getFilterPack())
        }

        getSettingsModel(UiConfigFrame::class.java).apply {
            setFrameList(FramePackBasic.getFramePack())
        }

        getSettingsModel(UiConfigOverlay::class.java).apply {
            setOverlayList(OverlayPackBasic.getOverlayPack())
        }

        getSettingsModel(UiConfigSticker::class.java).apply {
            setStickerLists(
                StickerPackEmoticons.getStickerCategory(),
                StickerPackShapes.getStickerCategory()
            )
        }

        // Set custom editor image export settings
        getSettingsModel(EditorSaveSettings::class.java).apply {
            setExportDir(Directory.DCIM, "SomeFolderName")
            setExportPrefix("result_")
            savePolicy = EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openSystemGalleryToSelectAnImage()
    }

    fun openSystemGalleryToSelectAnImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, GALLERY_RESULT)
        } else {
            Toast.makeText(
                this,
                "No Gallery APP installed",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    fun openEditor(inputImage: Uri) {
        val settingsList = createPesdkSettingsList().apply {
            getSettingsModel(EditorLoadSettings::class.java).apply {
                imageSource = inputImage
            }
        }

        settingsList.getSettingsModel(UiConfigMainMenu::class.java).toolList.clear()

        PhotoEditorBuilder(this)
            .setSettingsList(settingsList)
            .startActivityForResult(this, PESDK_RESULT)

        settingsList.getSettingsModel(UiConfigMainMenu::class.java).apply {
            setToolList(
                ToolItem("imgly_tool_transform", R.string.pesdk_transform_title_name, ImageSource.create(R.drawable.imgly_icon_tool_transform)),
                ToolItem("imgly_tool_filter", R.string.pesdk_filter_title_name, ImageSource.create(R.drawable.imgly_icon_tool_filters)),
                ToolItem("imgly_tool_adjustment", R.string.pesdk_adjustments_title_name, ImageSource.create(R.drawable.imgly_icon_tool_adjust)),
                ToolItem("imgly_tool_sticker_selection", R.string.pesdk_sticker_title_name, ImageSource.create(R.drawable.imgly_icon_tool_sticker)),
                ToolItem("imgly_tool_overlay", R.string.pesdk_overlay_title_name, ImageSource.create(R.drawable.imgly_icon_tool_overlay))
            )
        }
// Remove default Assets and add your own aspects
        settingsList.getSettingsModel(AssetConfig::class.java).apply {
            getAssetMap(CropAspectAsset::class.java)
                .clear()
                .add(CropAspectAsset("aspect_1_1", 1, 1, true))
        }
// Add your own Asset to UI config and select the Force crop Mode.
        settingsList.getSettingsModel(UiConfigAspect::class.java).apply {
            setAspectList(CropAspectItem("aspect_1_1"))
            forceCropMode = UiConfigAspect.ForceCrop.SHOW_TOOL_ALWAYS
        }

        settingsList.getSettingsModel(AssetConfig::class.java).apply {
            getAssetMap(FilterAsset::class.java).apply {
                add(FilterAsset.NONE_FILER)
                add(LutColorFilterAsset("my_own_lut_id", ImageSource.create(R.drawable.imgly_lut_ad1920_5_5_128), 5, 5, 128))
                add(ColorFilterAssetAD1920())
                add(ColorFilterAssetAncient())
                add(ColorFilterAssetBleached())
                add(ColorFilterAssetBleachedBlue())
                add(ColorFilterAssetBlues())
                add(ColorFilterAssetBlueShadows())
                add(ColorFilterAssetBreeze())
                add(ColorFilterAssetBW())
                add(ColorFilterAssetCelsius())
                add(ColorFilterAssetChest())
                add(ColorFilterAssetClassic())
                add(ColorFilterAssetColorful())
                add(ColorFilterAssetCool())
                add(ColorFilterAssetCottonCandy())
                add(ColorFilterAssetCreamy())
                add(ColorFilterAssetEighties())
                add(ColorFilterAssetElder())
                add(ColorFilterAssetEvening())
                add(ColorFilterAssetFall())
                add(ColorFilterAssetFixie())
                add(ColorFilterAssetFood())
                add(ColorFilterAssetFridge())
                add(ColorFilterAssetFront())
                add(ColorFilterAssetGlam())
                add(ColorFilterAssetGobblin())
                add(ColorFilterAssetHighCarb())
                add(ColorFilterAssetHighContrast())
                add(ColorFilterAssetK1())
                add(ColorFilterAssetK2())
                add(ColorFilterAssetK6())
                add(ColorFilterAssetKDynamic())
                add(ColorFilterAssetKeen())
                add(ColorFilterAssetLenin())
                add(ColorFilterAssetLitho())
                add(ColorFilterAssetLomo())
                add(ColorFilterAssetLomo100())
                add(ColorFilterAssetLucid())
                add(ColorFilterAssetMellow())
                add(ColorFilterAssetNeat())
                add(ColorFilterAssetNoGreen())
                add(ColorFilterAssetOrchid())
                add(ColorFilterAssetPale())
                add(ColorFilterAssetPitched())
                add(ColorFilterAssetPola669())
                add(ColorFilterAssetPolaSx())
                add(ColorFilterAssetPro400())
                add(ColorFilterAssetQuozi())
                add(ColorFilterAssetSepiahigh())
                add(ColorFilterAssetSettled())
                add(ColorFilterAssetSeventies())
                add(ColorFilterAssetSin())
                add(ColorFilterAssetSoft())
                add(ColorFilterAssetSteel())
                add(ColorFilterAssetSummer())
                add(ColorFilterAssetSunset())
                add(ColorFilterAssetTender())
                add(ColorFilterAssetTexas())
                add(ColorFilterAssetTwilight())
                add(ColorFilterAssetWinter())
                add(ColorFilterAssetX400())
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_RESULT) {
            openEditor(data.data)

        } else if (resultCode == Activity.RESULT_OK && requestCode == PESDK_RESULT) {

            val resultURI = data.getParcelableExtra<Uri?>(ImgLyIntent.RESULT_IMAGE_URI)?.also {
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(it))
            }

            val sourceURI = data.getParcelableExtra<Uri?>(ImgLyIntent.SOURCE_IMAGE_URI)?.also {
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(it))
            }

            Log.i("PESDK", "Source image is located here $sourceURI")
            Log.i("PESDK", "Result image is located here $resultURI")

            // TODO: Do something with the result image

            val lastState = data.getParcelableExtra<SettingsList>(ImgLyIntent.SETTINGS_LIST)
            try {
                PESDKFileWriter(lastState).writeJson(
                    File(
                    Environment.getExternalStorageDirectory(),
                    "serialisationReadyToReadWithPESDKFileReader.json"
                )
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == PESDK_RESULT) {
            // Editor was canceled
            val sourceURI = data.getParcelableExtra<Uri?>(ImgLyIntent.SOURCE_IMAGE_URI)
            // TODO: Do something with the source...
        }
    }
}