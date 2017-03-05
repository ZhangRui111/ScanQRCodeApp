package com.example.john.qrcodeapp;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener {

    private Button permissionBtn, scanQRcodeBtn, scanQRcodeFromGalleyBtn;

    //permission:android.permission.CAMERA权限码
    private static final int REQUECT_CODE_CAMERA = 101;
    //REQUEST_CODE
    private static final int REQUEST_CODE = 200;
    //选择系统图片Request Code
    public static final int REQUEST_IMAGE = 112;

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
    }


    private void initAction() {
        //permissionBtn.setOnClickListener(this);
        scanQRcodeBtn.setOnClickListener(this);
        scanQRcodeFromGalleyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.button_permission:
                getCameraPermission();
                break;*/
            case R.id.button_scan_QR:
                funcScanQRcode();
                break;
            case R.id.button_scan_QR_from_galley:
                funcScanQRcodeFromGalley();
                break;
            default:
                break;
        }
    }

    /**
     * 从图库中选取二维码扫描
     */
    private void funcScanQRcodeFromGalley() {
        //获取拍照权限
        getCameraPermission();

        MPermissions.requestPermissions(ActivityMain.this, REQUECT_CODE_CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    /**
     * 扫描二维码获得信息
     */
    private void funcScanQRcode() {
        Intent intent = new Intent(ActivityMain.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
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
            default:
                break;
        }
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
