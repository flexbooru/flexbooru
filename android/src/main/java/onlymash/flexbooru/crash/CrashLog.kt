package onlymash.flexbooru.crash

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import onlymash.flexbooru.util.Logger
import java.io.BufferedWriter
import java.io.File
import java.io.FileFilter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Comparator
import java.util.Date
import java.util.LinkedHashMap
import kotlin.system.exitProcess

abstract class CrashLog : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "CrashLog"
    }
    
    private var dateFormat: SimpleDateFormat? = null
    
    private val crashLog = LinkedHashMap<String, String>()

    private var crashLogLimit = 10

    private lateinit var crashLogPath: String

    private lateinit var context: Context

    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun setCrashLogPath(path: String) {
        crashLogPath = path
    }

    fun setCrashLogLimit(limit: Int) {
        crashLogLimit = limit
    }


    /**
     * 按日志大小排序
     */
    private val comparator = Comparator<File> { l, r ->
        if (l.lastModified() > r.lastModified())
            return@Comparator 1
        if (l.lastModified() < r.lastModified()) -1 else 0
    }

    abstract fun initParams(context: Context, crashLog: CrashLog)

    abstract fun sendCrashLog(context: Context, folder: File, file: File)

    fun init(context: Context) {

        try {

            this.context = context

            uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

            initParams(context, this)

            Thread.setDefaultUncaughtExceptionHandler(this)

        } catch (e: Exception) {
            Logger.e(TAG, "init - " + e.message)
        }

    }


    /**
     * 此类是当应用出现异常的时候执行该方法
     * @param thread
     * @param throwable
     */
    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        try {

            if (!handlerException(throwable) && uncaughtExceptionHandler != null) {

                /**
                 * 如果此异常不处理则由系统自己处理
                 */
                this.uncaughtExceptionHandler!!.uncaughtException(thread, throwable)

            } else {

                /**
                 * 延迟一秒退出
                 */
                Thread.sleep(1000)
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(1)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "uncaughtException - " + e.message)
        }

    }

    /**
     * 用户处理异常日志
     * @param throwable
     * @return
     */
    private fun handlerException(throwable: Throwable?): Boolean {

        try {

            if (throwable == null)
                return false

            /**
             * 应用信息
             */
            collectCrashLogInfo(context)
            /**
             * 将日志写入文件
             */
            writerCrashLogToFile(throwable)

            /**
             * 限制日子志文件的数量
             */

            limitLogCount(crashLogLimit)

        } catch (e: Exception) {
            Logger.e(TAG, "handlerException - " + e.message)
        }

        return false
    }

    /**
     * @param crashLogLimit log 数限制
     */
    private fun limitLogCount(crashLogLimit: Int) {

        try {

            val file = File(crashLogPath)

            if (file.exists() && file.isDirectory) {

                val files = file.listFiles(CrashLogFilter())

                if (files != null && files.isNotEmpty()) {

                    Arrays.sort(files, comparator)

                    if (files.size > crashLogLimit) {

                        for (i in 0 until files.size - crashLogLimit) {

                            files[i].delete()
                        }
                    }

                }
            }

        } catch (e: Exception) {
            Logger.e(TAG, "limitLogCount - " + e.message)
        }

    }

    /**
     * 过滤.log的文件
     */
    inner class CrashLogFilter : FileFilter {

        override fun accept(file: File): Boolean {

            return file.name.endsWith(".log")
        }
    }


    /**
     * 写入文件中
     * @param ex
     */
    private fun writerCrashLogToFile(ex: Throwable) {

        try {

            val buffer = StringBuffer()

            if (crashLog.size > 0) {

                for ((key, value) in crashLog) {
                    buffer.append("$key: $value\n")
                }
            }

            val writer = StringWriter()
            val printWriter = PrintWriter(writer)
            ex.printStackTrace(printWriter)
            var cause: Throwable? = ex.cause

            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }

            printWriter.flush()
            printWriter.close()

            val result = writer.toString()

            buffer.append("Exception:+\n")

            buffer.append(result)

            writerToFile(buffer.toString())

        } catch (e: Exception) {
            Logger.e(TAG, "writerCrashLogToFile - " + e.message)
        }

    }

    /**
     * 保存 log 文件
     * @param s log
     */
    @SuppressLint("SimpleDateFormat")
    private fun writerToFile(s: String) {

        try {
            /**
             * 创建日志文件名称
             */
            val curtTimer = "" + System.currentTimeMillis()
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            }
            val timer = dateFormat!!.format(Date())

            val fileName = "crash-$timer-$curtTimer.log"
            /**
             * 创建文件夹
             */
            val folder = File(crashLogPath)

            if (!folder.exists())
                folder.mkdirs()

            /**
             * 创建日志文件
             */
            val file = File(folder.absolutePath + File.separator + fileName)

            if (!file.exists()) file.createNewFile()

            val fileWriter = FileWriter(file)
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write(s)
            bufferedWriter.flush()
            bufferedWriter.close()
            sendCrashLog(context, folder, file)
        } catch (e: Exception) {
            Logger.e(TAG, "writerToFile - " + e.message)
        }
    }

    /**
     * 获取应用信息
     * @param context
     */
    private fun collectCrashLogInfo(context: Context?) {

        try {
            if (context == null)
                return

            val packageManager = context.packageManager

            if (packageManager != null) {

                val packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)

                if (packageInfo != null) {

                    val versionName = packageInfo.versionName
                    val packName = packageInfo.packageName

                    crashLog["versionName"] = versionName
                    crashLog["packName"] = packName
                    if (Build.VERSION.SDK_INT >= 28) {
                        crashLog["versionCode"] = packageInfo.longVersionCode.toString()
                    }
                }
            }

            crashLog["Model:"] = Build.MODEL
            crashLog["CodeName"] = Build.VERSION.CODENAME
            if (Build.VERSION.SDK_INT >= 23) {
                crashLog["baseOS"] = Build.VERSION.BASE_OS
            }
            val fields = Build::class.java.fields
            if (fields.isNotEmpty()) {
                for (field in fields) {
                    if (field != null) {
                        field.isAccessible = true
                        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        crashLog[field.name] = field.get(null).toString()
                    }
                }
            }

        } catch (e: Exception) {
            Logger.e(TAG, "collectDeviceInfo - " + e.message)
        }
    }
}