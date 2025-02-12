package com.zjw.chehang168

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangcle.comapiprotect.CheckCodeUtil


class CheckCodeUtilActivity : AppCompatActivity() {

    private val checkCodeUtil: CheckCodeUtil = CheckCodeUtil.getInstance(this)

    private lateinit var etInput: EditText
    private lateinit var tvOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_code_util)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etInput = findViewById(R.id.et_input)
        tvOutput = findViewById(R.id.tv_output)

        checkCodeUtil.updateServiceTime(System.currentTimeMillis())
    }

    /**
     * 解密
     */
    fun decrypt(v: View) {
        val toString = etInput.text.toString()
        if (TextUtils.isEmpty(toString)) {
            return
        }
        val decheckcode = checkCodeUtil.decheckcode(toString)
        tvOutput.text = decheckcode
    }

    /**
     * 加密
     */
    fun encrypt(v: View) {
        val toString = etInput.text.toString()
        if (TextUtils.isEmpty(toString)) {
            return
        }
        val checkcode = checkCodeUtil.checkcode(toString, CheckCodeUtil.DATA_TYPE_JSON)
        tvOutput.text = checkcode
    }

    fun paste(v: View) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = clipboardManager.primaryClip
        if (primaryClip != null && primaryClip.itemCount > 0) {
            val text = primaryClip.getItemAt(0).text
            etInput.setText(text)
            tvOutput.text = null
        }
    }

    fun copy(v: View) {
        val toString = tvOutput.text.toString()
        if (TextUtils.isEmpty(toString)) {
            return
        }
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", toString)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
    }
}