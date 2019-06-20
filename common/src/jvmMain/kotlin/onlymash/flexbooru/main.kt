package onlymash.flexbooru

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import onlymash.flexbooru.saucenao.api.SauceNaoApi
import onlymash.flexbooru.saucenao.di.kodeinSauceNao
import org.kodein.di.erased.instance

@UnstableDefault
fun main() {
    val api: SauceNaoApi by kodeinSauceNao.instance("SauceNaoApi")
    GlobalScope.launch {
        val response = try {
            api.searchByUrl(url = "https://i.pximg.net/img-master/img/2019/04/13/00/00/01/74170729_p0_master1200.jpg", apiKey = "bbfa3e504bacf627a130b07ac3ec2d1b94e3f2c4")
        } catch (ex: Exception) {
            println(ex.message)
            null
        }
        println(response.toString())
    }
    Thread.sleep(10000L) // block main thread for 10 seconds to keep JVM alive
}