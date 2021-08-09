package com.privo.sdk.internal


import android.app.ProgressDialog
import android.content.Context
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.content.FileProvider
import com.privo.sdk.components.LoadingDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


internal class JsPdfDownload(private val webView: WebView) {

    init {
        webView.setDownloadListener { url, _, _, _, _ ->
            webView.loadUrl(getBase64StringFromBlobUrl(url));
        }
        webView.addJavascriptInterface(this, "Android");
    }

    @Throws(IOException::class)
    @JavascriptInterface
    fun getBase64FromBlobData(base64Data: String) {
        convertBase64StringToPdfAndStoreIt(base64Data)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToPdfAndStoreIt(base64PDf: String) {
        val file = File(webView.context.getExternalFilesDir(null), "PRIVO Print Form.pdf")
        val pdfAsBytes: ByteArray =
            Base64.decode(base64PDf.replaceFirst("^data:application/pdf;base64,".toRegex(), ""), 0)
        val os = FileOutputStream(file, false)
        os.write(pdfAsBytes)
        os.flush()
        if (file.exists()) {
            sharePdf(webView.context, file)
        }
    }
    private fun sharePdf(context: Context, file: File) {
        val photoURI = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName.toString() + ".provider",
            file
        )

        PrivoInternal.shareFile(context, photoURI, "PRIVO Print Form", "application/pdf")
    }

    companion object {
        fun getBase64StringFromBlobUrl(blobUrl: String): String {
            return if (blobUrl.startsWith("blob")) {
                "javascript: var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','application/pdf');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobPdf = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobPdf);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            Android.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            } else "javascript: console.log('It is not a Blob URL');"
        }
    }
}