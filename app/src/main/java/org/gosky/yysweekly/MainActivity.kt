package org.gosky.yysweekly

//import java.net.URL
//import java.io.IOException
//import java.io.InputStream
//import java.net.MalformedURLException
//import org.jsoup.*
import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
//import org.json.JSONArray
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.collections.HashMap


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

        // 将文件转换为游戏内id
        val list = find?.listFiles()?.map { it.name }?.toMutableList()
        list?.add(0,find?.name?.split("_")!![1])

        list?.forEach {
            tv_content.text = tv_content.text.toString() + "\nroleId = $it"
        }
        if (list != null && list.isNotEmpty()) {
            alert {
                title = "提示"

                // get year and calc the week
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val theFirstDay = Date(year - 1900, 0, 1)
                val today = Date()
                val diffDay = ((today.time - theFirstDay.time) / (60 * 60 * 24 * 1000) - 7)
                val week = diffDay / 7

                // store role_name
                val toMutableList = mutableListOf<String>()
                // name to url
                val dict = HashMap<String, String>()
                // url to roleID
                val urlToRoleID = HashMap<String, String>()

                for (roleID in list) {
                    // sample: https://bdapi.gameyw.netease.com/g37_weekly/week?roleid=57ffa68a6aa0b67275423d0b
                    val url = "https://bdapi.gameyw.netease.com/g" + week.toString() + "_weekly/week?roleid=" + roleID
                    urlToRoleID[url] = roleID
                    // in thread get Json
                    val thread = Thread(Runnable {
                        try {
                            // read json from network and find role_name
                            val br = BufferedReader(InputStreamReader(URL(url).openStream(), "UTF-8"))
                            val msg = org.apache.commons.io.IOUtils.toString(br)
                            val jsObject = JSONObject(msg)
                            val res = jsObject.get("result")
                            if (res is JSONObject) {
                                val name = res.get("role_name").toString()
                                Log.e("YYH", name)
                                dict[name] = url
                                toMutableList.add(name)
                            }

                            // find no role_name
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("YYH", e.toString() + " " + url)
                            toMutableList.add("阴兵")
                        }
                    })
                    thread.start()

                }
                while (toMutableList.count() != list.count()) {
                    Thread.sleep(1000)
                }
                toMutableList.add(0, "找到${list.size}个role_id(可能是好友的,一般自己的会是第一个?),选择自己想看的吧~")
                items(toMutableList, { dialog: DialogInterface, item: String, index: Int ->
                    run {
                        if (index > 0)
                            showOpenDialog(item, dict, urlToRoleID)
                    }
                })

                negativeButton("取消", {

                })
            }.show()
        }
    }

    private fun showOpenDialog(name: String, dict: HashMap<String, String>, urlDict: HashMap<String, String>) {
        alert {
            title = "提示"
            message = "是否前往查看痒痒鼠数据周报?"
//            Log.e("YYH", dict[s])
            negativeButton("取消", {

            })
            positiveButton("ok", {
                val url = dict[name]
                val roleID = urlDict[url]
//                val uri = Uri.parse(dict.get(name))
                val uri = Uri.parse("https://yxzs.163.com/yys/weekly/index.html?roleInfo=id__$roleID")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            })
        }.show()
    }
}
