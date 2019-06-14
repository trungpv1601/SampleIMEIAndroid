package com.lexor.sampleimei;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lexor.sampleimei.utils.Crypto;
import com.lexor.sampleimei.utils.QRCodeHelper;

public class MainActivity extends AppCompatActivity {

    private TextView txtUUID;
    private TextView txtIMEI;
    private Button btnGetUUID;
    private ImageView qrCodeImageView;
    private String imeiNumberHolder;
    private TelephonyManager telephonyManager;

    private static final int REQUEST_PERMISSIONS = 100;
    private static final String PERMISSIONS_REQUIRED[] = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        txtUUID = (TextView) findViewById(R.id.txtUUID);
        txtIMEI = (TextView) findViewById(R.id.txtIMEI);
        qrCodeImageView = (ImageView) findViewById(R.id.qrCodeImageView);
        btnGetUUID = (Button) findViewById(R.id.btnGenerateCode);

        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        btnGetUUID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {
                    imeiNumberHolder = telephonyManager.getDeviceId();
                    txtIMEI.setText(imeiNumberHolder);
                    txtUUID.setText(Crypto.encryptAES(imeiNumberHolder));

                    Bitmap bitmap = QRCodeHelper
                            .newInstance(MainActivity.this)
                            .setContent(Crypto.encryptAES(imeiNumberHolder))
                            .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                            .setMargin(2)
                            .getQRCOde();
                    qrCodeImageView.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(getApplicationContext(), "Please ask permission from user.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean hasGrantedPermissions = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    hasGrantedPermissions = false;
                    break;
                }
            }

            if (!hasGrantedPermissions) {
                finish();
            }

        } else {
            finish();
        }
    }

    private boolean checkPermission(String permissions[]) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissions() {
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if (permissionsGranted) {
            Toast.makeText(this, "You've granted all required permissions!", Toast.LENGTH_SHORT).show();
        } else {
            boolean showRationale = true;
            for (String permission : PERMISSIONS_REQUIRED) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    break;
                }
            }

            String dialogMsg = showRationale ? "We need some permissions to run this APP!" : "You've declined the required permissions, please grant them from your phone settings";

            new AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage(dialogMsg)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_REQUIRED, REQUEST_PERMISSIONS);
                        }
                    }).create().show();
        }
    }

}
