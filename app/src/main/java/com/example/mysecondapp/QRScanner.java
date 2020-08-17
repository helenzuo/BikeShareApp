package com.example.mysecondapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {
    private ZXingScannerView mScannerView;
    private ToggleButton flashToggle;
    private AppCompatImageButton closeButton;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private boolean flash = false;
    private boolean scanned = false;
    private Button OKbutton;
    private Result rawResult;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition( 0,R.anim.slide_down);
        setContentView(R.layout.activity_q_r_scanner);
        getSupportActionBar().hide();
        // Check if camera allowed on device. If not, ask for permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                CustomZXingScannerView customZXingScannerView = new CustomZXingScannerView(context);
                customZXingScannerView.setSquareViewFinder(true);
                return customZXingScannerView;
            }
        };
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(barcodeFormats);
        RelativeLayout relativeLayout = findViewById(R.id.qr_container);
        relativeLayout.addView(mScannerView);

        closeButton = findViewById(R.id.closeQRScannerButton);
        flashToggle = findViewById(R.id.flashToggle);

        closeButton.setOnClickListener(this);
        flashToggle.setOnClickListener(this);

        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeTop() {
                closeQRScanner();
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void closeQRScanner(){
        setResult(RESULT_CANCELED, null);
        finish();
        overridePendingTransition( R.anim.slide_up,0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void onBackPressed(){
        if (!scanned) {
            super.onBackPressed();
        } else {
            OKbutton.callOnClick();
        }
        overridePendingTransition( R.anim.slide_up,0);
    }

    @Override
    public void handleResult(Result rawResult) {
        this.rawResult = rawResult;
        scanned = true;
        findViewById(R.id.QRScannedLayoutContainer).setVisibility(View.VISIBLE);
        flashToggle.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
        OKbutton = findViewById(R.id.confirmButton);
        OKbutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == closeButton){
            closeQRScanner();
        } else if (v == flashToggle){
            mScannerView.toggleFlash();
            flash = !flash;
        } else if (v == OKbutton){
            Intent intent = new Intent();
            intent.putExtra("QRCode", rawResult.getText());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}