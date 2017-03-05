package com.example.john.qrcodeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.finalteam.rxgalleryfinal.RxGalleryFinal;
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType;
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultSubscriber;
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class ActivityMain extends BaseActivity implements View.OnClickListener {

    //扫描二维码相关BUtton
    private Button permissionBtn, scanQRcodeBtn, scanQRcodeFromGalleyBtn, scanSelfDefineUIBtn;
    //获取头像相关Button
    private Button getIconTakePhotoBtn, getIconFromGalleyBtn;
    private ImageView iconImageView;

    //用于头像的拍照文件
    private File img;

    //permission:android.permission.CAMERA权限码
    private static final int REQUECT_CODE_CAMERA = 101;
    //permission:android.permission.READ_EXTERNAL_STORAGE权限码
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 102;

    //REQUEST_CODE
    private static final int REQUEST_CODE = 111;
    //选择系统图片Request Code
    public static final int REQUEST_IMAGE = 112;

    //拍照作用户头像 Request Code
    public static final int REQUEST_ICON_TAKE_PHOTO = 122;
    //裁剪图片Request Code
    public static final int REQUEST_RESIZE_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化二维码扫描操作
        ZXingLibrary.initDisplayOpinion(this);

        initView();
        initAction();
    }

    private void initView() {
        //permissionBtn = (Button) findViewById(R.id.button_permission);
        scanQRcodeBtn = (Button) findViewById(R.id.button_scan_QR);
        scanQRcodeFromGalleyBtn = (Button) findViewById(R.id.button_scan_QR_from_galley);
        scanSelfDefineUIBtn = (Button) findViewById(R.id.button_scan_QR_self_define);

        getIconTakePhotoBtn = (Button) findViewById(R.id.button_icon_take_photo);
        getIconFromGalleyBtn = (Button) findViewById(R.id.button_icon_from_galley);
        iconImageView = (ImageView) findViewById(R.id.image_view_icon);
    }


    private void initAction() {
        //permissionBtn.setOnClickListener(this);
        scanQRcodeBtn.setOnClickListener(this);
        scanQRcodeFromGalleyBtn.setOnClickListener(this);
        scanSelfDefineUIBtn.setOnClickListener(this);

        getIconTakePhotoBtn.setOnClickListener(this);
        getIconFromGalleyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //扫描二维码
            /*case R.id.button_permission:
                getCameraPermission();
                break;*/
            case R.id.button_scan_QR:
                funcScanQRcode();
                break;
            case R.id.button_scan_QR_from_galley:
                funcScanQRcodeFromGalley();
                break;
            case R.id.button_scan_QR_self_define:
                funcSelfDefineUI();
                break;
            //获取用户头像
            case R.id.button_icon_take_photo:
                funcGetIconTakePhoto();
                break;
            case R.id.button_icon_from_galley:
                funcGetIconFromGalley();
                break;
            default:
                break;
        }
    }

    /**
     * 扫描二维码获得信息
     */
    private void funcScanQRcode() {
        //获取拍照权限
        getCameraPermission();
        Intent intent = new Intent(ActivityMain.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 从图库中选取二维码扫描
     */
    private void funcScanQRcodeFromGalley() {
        //获取存储、读取权限
        MPermissions.requestPermissions(ActivityMain.this, REQUECT_CODE_CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image*//*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    /**
     * 自定义扫描界面UI
     */
    private void funcSelfDefineUI() {
        Intent intent = new Intent(ActivityMain.this, ActivityselfDefineScanUI.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 从图库中选取用户头像并裁剪
     */
    private void funcGetIconFromGalley() {
        //获取存储、读取权限
        MPermissions.requestPermissions(ActivityMain.this, REQUEST_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image*//*");
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
            startActivityForResult(intent, REQUEST_ICON_GALLEY_KITLAT);
        }else{
            startActivityForResult(intent, REQUEST_ICON_GALLEY);
        }*/

        initImageLoader();
        initFresco();

        RxGalleryFinal
                .with(ActivityMain.this)
                .image()
                .radio()
                .crop()
                .imageLoader(ImageLoaderType.GLIDE)
                .subscribe(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                    @Override
                    protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                        //imageRadioResultEvent.getResult().getCropPath();裁剪后的路径
                        Bitmap bitmap = getLoacalBitmap(imageRadioResultEvent.getResult().getCropPath()); //从本地取图片(在cdcard中获取)  //
                        iconImageView.setImageBitmap(bitmap); //设置Bitmap
                        //imageRadioResultEvent.getResult().getOriginalPath();原始路径
                        //Toast.makeText(getBaseContext(), imageRadioResultEvent.getResult().getOriginalPath(), Toast.LENGTH_SHORT).show();
                    }
                })
                .openGallery();
    }

    private void initImageLoader() {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        ImageLoader.getInstance().init(config.build());
    }

    private void initFresco() {
        Fresco.initialize(this);
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 拍照获取用户头像
     */
    private void funcGetIconTakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_ICON_TAKE_PHOTO);
    }

    /**
     * 保存照相的图片
     * @param bm
     * @return
     */
    private Uri saveBitmap(Bitmap bm) {
        File tmpDir;
        if (hasSD()) {
            tmpDir = new File(Environment.getExternalStorageDirectory() + "/com.upc.avatar");
        } else {
            tmpDir = new File(Environment.getDataDirectory() + "/com.upc.avatar");
        }

        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        img = new File(tmpDir.getAbsolutePath() + "avater.png");
        try {
            FileOutputStream fos = new FileOutputStream(img);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 是否有SD卡
     */
    private boolean hasSD() {
        //如果有SD卡 则下载到SD卡中
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            //如果没有SD卡
            return false;
        }
    }

    /**
     * 获取拍照权限
     */
    private void getCameraPermission() {
        MPermissions.requestPermissions(ActivityMain.this, REQUECT_CODE_CAMERA, Manifest.permission.CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //处理二维码扫描结果
            case REQUEST_CODE:
                //处理扫描结果（在界面上显示）
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();

                        funcOpenURL(result);
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        Toast.makeText(ActivityMain.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            //从图库中选取扫描图片
            case REQUEST_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                            @Override
                            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                                Toast.makeText(ActivityMain.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                                funcOpenURL(result);
                            }

                            @Override
                            public void onAnalyzeFailed() {
                                Toast.makeText(ActivityMain.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            /*//4.4版本从图库选取照片
            case REQUEST_ICON_GALLEY_KITLAT:

                break;
            //4.4以上版本从图库中选择照片
            case REQUEST_ICON_GALLEY:

                break;*/
            case REQUEST_ICON_TAKE_PHOTO:
                if (data == null) {
                    return;
                } else {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bm = extras.getParcelable("data");
                        //iconImageView.setImageBitmap(bm);
                        Uri uri = saveBitmap(bm);
                        //Toast.makeText(this, uri + "", Toast.LENGTH_SHORT).show();
                        resizeImage(uri);

//					startImageZoom(uri);
                    }
                }
                break;
            //裁剪图片
            case REQUEST_RESIZE_CODE:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        iconImageView.setImageBitmap(photo);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 裁剪图片
     * @param uri
     */
    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        //打开裁剪的activity,并且获取到裁剪图片(在第二步的RESIZE_REQUEST_CODE请求码中处理)
        startActivityForResult(intent, REQUEST_RESIZE_CODE);
    }

    /**
     * 打开网页
     */
    private void funcOpenURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent .setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     *以下三个方法均用于申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @PermissionGrant(REQUECT_CODE_CAMERA)
    public void requestSdcardSuccess()
    {
        Toast.makeText(this, "GRANT ACCESS PERMISSION！", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUECT_CODE_CAMERA)
    public void requestSdcardFailed()
    {
        Toast.makeText(this, "DENY ACCESS PERMISSION!", Toast.LENGTH_SHORT).show();
    }
}
