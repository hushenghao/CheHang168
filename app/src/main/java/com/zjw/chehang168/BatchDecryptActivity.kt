package com.zjw.chehang168

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bangcle.comapiprotect.CheckCodeUtil
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader

private const val TAG = "BatchDecryptActivity"

class BatchDecryptActivity : AppCompatActivity() {

    private val checkCodeUtil: CheckCodeUtil = CheckCodeUtil.getInstance(this)

    private lateinit var tvExportPath: TextView
    private lateinit var getContentsLauncher: ActivityResultLauncher<String>
    private lateinit var selectDocumentDirLauncher: ActivityResultLauncher<Uri?>
    private var exportPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_batch_decrypt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvExportPath = findViewById(R.id.tv_export_path)

        checkCodeUtil.updateServiceTime(System.currentTimeMillis())

        getContentsLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
                Log.i(TAG, "解析文件: $it")
                batchDecrypt(it)
            }

        selectDocumentDirLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
                exportPath = it
                tvExportPath.text = it?.toString()
            }
    }

    fun exportPath(v: View) {
        selectDocumentDirLauncher.launch(null)
    }

    fun importJson(view: View) {
        if (exportPath == null) {
            Toast.makeText(this, "选择导出路径", Toast.LENGTH_SHORT).show()
            return
        }
        getContentsLauncher.launch("text/*")
    }

    private fun batchDecrypt(fileUris: List<Uri>) {
        val exportPath = exportPath ?: return

        lifecycleScope.launch {
            val dialog = ProgressDialog.show(this@BatchDecryptActivity, "正在解密", null, false)
            var successFileCount = 0
            var successModelCount = 0
            var totalModelCount = 0
            for (uri in fileUris) {
                dialog.setTitle("正在解密, 第${successFileCount + 1}个文件")
                val inputReader: InputStreamReader? = withContext(Dispatchers.IO) {
                    try {
                        contentResolver.openInputStream(uri)?.reader(Charsets.UTF_8)
                    } catch (_: IOException) {
                        null
                    }
                }
                if (inputReader == null) {
                    dialog.dismiss()
                    Toast.makeText(this@BatchDecryptActivity, "文件打开失败", Toast.LENGTH_SHORT)
                        .show()
                    continue
                }

                val list: List<RR> = try {
                    GsonBuilder().create()
                        .fromJson<List<RR>>(inputReader, object : TypeToken<List<RR>>() {}.type)
                } catch (_: Exception) {
                    dialog.dismiss()
                    Toast.makeText(this@BatchDecryptActivity, "文件解析失败", Toast.LENGTH_SHORT)
                        .show()
                    continue
                } finally {
                    Utils.close(inputReader)
                }

                totalModelCount += list.size

                for (rr in list) {
                    dialog.setMessage("%d/%d".format(successModelCount, totalModelCount))

                    val base64 = rr.res.base64
                    Log.i(TAG, "base64: $base64")
                    val string = String(Base64.decode(base64, Base64.DEFAULT))
                    Log.i(TAG, "base64解密: $string")
                    val decrypted = checkCodeUtil.decheckcode(string)
                    Log.i(TAG, "CheckCode解密: $decrypted")
                    val b = try {
                        GsonBuilder()
                            .disableHtmlEscaping()
                            .create()
                            .fromJson(decrypted, B::class.java)
                    } catch (_: Exception) {
                        null
                    }

                    val modelInfo = b?.l?.modelPriceShow ?: continue

                    withContext(Dispatchers.IO) {
                        val exportDirFile = Utils.createNestedDirectories(
                            this@BatchDecryptActivity,
                            exportPath,
                            modelInfo.info.psname, modelInfo.hq.title
                        )
                        val file =
                            exportDirFile?.createFile("application/json", modelInfo.info.date_day)
                        if (file != null) {
                            contentResolver.openOutputStream(file.uri)?.use {
                                it.write(decrypted.toByteArray())
                                successModelCount++
                                return@use
                            }
                        }
                    }
                }

                successFileCount++
            }

            dialog.dismiss()
            AlertDialog.Builder(this@BatchDecryptActivity)
                .setTitle("解密完成")
                .setMessage(
                    "共解析${fileUris.size}个文件, 成功: ${successFileCount}个, 失败: ${fileUris.size - successFileCount}个\n" +
                            "共解密${totalModelCount}个车型, 成功: ${successModelCount}个, 失败: ${totalModelCount - successModelCount}个"
                )
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show()
        }
    }

}
