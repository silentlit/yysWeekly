package org.gosky.yysweekly

import android.Manifest
import android.content.DialogInterface
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
            "vivo" to "com.netease.onmyoji.vivo",
            "uc" to "com.netease.onmyoji.uc",
            "xiaomi" to "com.netease.onmyoji.mi",
            "360" to "com.netease.onmyoji.qihoo"
    )

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

                val finds = channelList.filter { pair ->
                    val find = listFiles.find { it.name.contains(pair.second) }
                    if (find != null) {
                        tv_content.text = tv_content.text.toString() + "已找到${pair.first}渠道~\n"
                        true
                    } else false
                }

                alert {
                    title = "选择想要查看的渠道"
                    items(finds, { dialog: DialogInterface, item: Pair<String, String>, index: Int ->
                        run {
                            showChannelChoose(file, item)
                        }
                    })
                    negativeButton("取消", {

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

    private fun showChannelChoose(file: File, pair: Pair<String, String>) {
        val cacheFile = File(file, "${pair.second}/files/netease/onmyoji/")
        val find = cacheFile.listFiles().find { it.name.contains("chat_") }
        val list = find?.listFiles()?.map { it.name }?.toMutableList()
        list?.add(0,find?.name?.split("_")!![1])
        list?.forEach {
            tv_content.text = tv_content.text.toString() + "\nroleId = $it"
        }
        if (list != null && list.isNotEmpty()) {
            alert {
                title = "提示"
                val toMutableList = list.toMutableList()
                toMutableList.add(0, "找到${list.size}个role_id(可能是好友的,一般自己的会是第一个?),选择自己想看的吧~")
                items(toMutableList, { dialog: DialogInterface, item: String, index: Int ->
                    run {
                        if (index > 0)
                            showOpenDialog(item)
                    }
                })

                negativeButton("取消", {

                })
            }.show()
        }
    }

    private fun showOpenDialog(s: String) {
        alert {
            title = "提示"
            message = "是否前往查看痒痒鼠数据周报?"
            negativeButton("取消", {

            })
            positiveButton("ok", {
                val uri = Uri.parse("https://yxzs.163.com/yys/weekly/index.html?roleInfo=60__$s")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            })
        }.show()
    }
}
