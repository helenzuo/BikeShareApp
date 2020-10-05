package com.example.mysecondapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mysecondapp.extensions.CustomZXingScannerView;
import com.example.mysecondapp.extensions.OnSwipeTouchListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

// Activity for QR Scanner, started by the MainActivity to scan QR codes at the bike stations
public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {
    private ZXingScannerView mScannerView;
    private ToggleButton flashToggle;
    private AppCompatImageButton closeButton;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private boolean flash = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition( R.anim.slide_up,0);
        setContentView(R.layout.activity_q_r_scanner);
        getSupportActionBar().hide();  // hide action bar
        // Check if camera allowed on device. If not, ask for permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {  // create our custom QR Scanner View
                CustomZXingScannerView customZXingScannerView = new CustomZXingScannerView(context);
                customZXingScannerView.setSquareViewFinder(true);
                return customZXingScannerView;
            }
        };
        // set allowable barcode formats for QR scanner (just QR codes in our case_
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(barcodeFormats);
        RelativeLayout relativeLayout = findViewById(R.id.qr_container);
        relativeLayout.addView(mScannerView);

        closeButton = findViewById(R.id.closeQRScannerButton);
        flashToggle = findViewById(R.id.flashToggle);

        closeButton.setOnClickListener(this);
        flashToggle.setOnClickListener(this);
        // swipe up to return to MainActivity
        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeBottom() {
                closeQRScanner();
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }
    // no result if closing the QR scanner
    private void closeQRScanner(){
        setResult(RESULT_CANCELED, null);
        finish();
        overridePendingTransition( 0, R.anim.slide_down);
    }

    // Response to camera permission made
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
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
        super.onBackPressed();
        overridePendingTransition(0 ,R.anim.slide_down);
    }
    // pass the QR code scanned back to MainActivity and end QRScannerActivity
    @Override
    public void handleResult(final Result rawResult) {
        findViewById(R.id.QRScannedLayoutContainer).setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        intent.putExtra("QRCode", rawResult.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == closeButton){  // close the QR Scanner
            closeQRScanner();
        } else {  // toggle flash
            mScannerView.toggleFlash();
            flash = !flash;
        }
    }
}