package com.example.onebuttonplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.provider.DocumentsContract;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    
    // Activity result launcher for file picker
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // Multiple files selected
                        int count = data.getClipData().getItemCount();
                        results = new Uri[count];
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            // Take persistent permission for the URI
                            getContentResolver().takePersistableUriPermission(uri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            results[i] = uri;
                        }
                    } else if (data.getData() != null) {
                        // Single file selected
                        Uri uri = data.getData();
                        // Take persistent permission for the URI
                        getContentResolver().takePersistableUriPermission(uri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        results = new Uri[]{uri};
                    }
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
    );
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);
        
        // Configure WebView for best practices
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Enable hardware acceleration for better performance
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        
        // Set WebView clients
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Handle external URLs if needed
                return false;
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, 
                                           ValueCallback<Uri[]> filePathCallback, 
                                           FileChooserParams fileChooserParams) {
                MainActivity.this.filePathCallback = filePathCallback;
                
                Intent intent = fileChooserParams.createIntent();
                // Set MIME type filter for audio files
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/mpeg", "audio/mp3", "audio/*"});
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                
                try {
                    filePickerLauncher.launch(intent);
                    return true;
                } catch (Exception e) {
                    filePathCallback.onReceiveValue(null);
                    return false;
                }
            }
        });
        
        // Load local HTML file
        webView.loadUrl("file:///android_asset/index.html");
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}