package me.xx2bab.koncat.gradle

import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

@Throws(IOException::class)
fun extractFileByExtensionFromZip(
    zipFile: File,
    fileNameExtension: String,
    destDir: File
) {
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                if (entry.name.endsWith(fileNameExtension)) {
                    val dest = File(destDir, entry.name)
                    val parent = dest.parentFile
                    if (parent.exists()) {
                        parent.mkdirs()
                    }
                    dest.createNewFile()
                    dest.outputStream().use { input.copyTo(it) }
                }
            }
        }
    }
}