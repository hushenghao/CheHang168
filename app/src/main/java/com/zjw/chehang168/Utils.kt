package com.zjw.chehang168

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.Closeable
import java.io.IOException


object Utils {

    fun decodeUnicode(unicode: String): String {
        val stringBuilder = StringBuilder()
        val length = unicode.length
        var i = 0
        while (i < length) {
            if (unicode[i] == '\\' && unicode[i + 1] == 'u') {
                val hex = unicode.substring(i + 2, i + 6)
                val decimal = hex.toInt(16)
                stringBuilder.append(decimal.toChar())
                i += 5
            } else {
                stringBuilder.append(unicode[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun createNestedDirectories(
        context: Context,
        treeUri: Uri,
        vararg nestedFolders: String
    ): DocumentFile? {
        val treeDocumentFile = DocumentFile.fromTreeUri(context, treeUri)
        if (treeDocumentFile != null && treeDocumentFile.isDirectory) {
            var currentDirectory: DocumentFile = treeDocumentFile
            val regex = Regex("[:/\\\\*?\"<>|]")// uri路径不支持的字符
            for (folder in nestedFolders) {
                val sanitizedFolder = folder.replace(regex, "_")
                var nextDirectory = currentDirectory.findFile(sanitizedFolder)
                if (nextDirectory == null || !nextDirectory.isDirectory) {
                    nextDirectory = currentDirectory.createDirectory(sanitizedFolder)
                    if (nextDirectory == null) {
                        println("子文件夹创建失败: $sanitizedFolder")
                        return null
                    }
                }
                currentDirectory = nextDirectory
            }
            return currentDirectory
        } else {
            println("无法访问目标文件夹")
            return null
        }
    }

    fun close(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (_: IOException) {
        }
    }
}