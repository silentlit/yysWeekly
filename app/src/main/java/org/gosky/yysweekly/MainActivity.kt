package org.gosky.yysweekly

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class MainActivity : AppCompatActivity() {

    private val channelList = arrayOf("靠谱助手" to "com.netease.onmyoji.kaopu",
            "哔哩哔哩" to "com.netease.onmyoji.bili",
            "华为" to "com.netease.onmyoji.huawei",
            "应用宝" to "com.tencent.tmgp.yys.zqb",
            "魅族" to "com.netease.onmyoji.mz",
            "vivo" to "com.netease.onmyoji.vivo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EasyPermissions.requestPermissions(this, "需要sdard权限来获取role_id ^_^"
                , 100, Manifest.permission.READ_EXTERNAL_STORAGE)
        btn_start.setOnClickListener {
            choicePhotoWrapper()
        }

    }

    @AfterPermissionGranted(100)
    private fun choicePhotoWrapper() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            try {
                val file = File(Environment.getExternalStorageDirectory().path + "/Android/data/")
                val listFiles = file.listFiles()
                var pair: Pair<String, String>? = null
                loop@ for (listFile in listFiles) {
                    val find = channelList.find {
                        return@find if (listFile.name.contains(it.second)) {
                            tv_content.text = "已找到${it.first}渠道~"
                            true
                        } else false
                    }
                    if (find != null) {
                        pair = find
                        break@loop
                    }
                }
                val cacheFile = File(file, "${pair?.second}/files/netease/onmyoji/")
                val s = cacheFile.listFiles().find { it.name.contains("chat_") }?.name?.split("_")!![1]
                tv_content.text = tv_content.text.toString() + "\nroleId = $s"
                alert {
                    title = "提示"
                    message = "是否前往查看痒痒鼠数据周报?"
                    negativeButton("取消",{

                    })
                    positiveButton("ok",{
                        val uri = Uri.parse("https://yxzs.163.com/yys/weekly/index.html?roleInfo=60__$s")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    })
                }.show()

            } catch (e: Exception) {
                e.printStackTrace()
                toast(e.message.orEmpty())
            }

        } else {
            toast("获取权限失败/(ㄒoㄒ)/~~")
        }
    }
}
