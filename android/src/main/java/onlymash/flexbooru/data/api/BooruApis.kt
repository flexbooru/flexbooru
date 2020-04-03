package onlymash.flexbooru.data.api

class BooruApis {
    val danApi by lazy { DanbooruApi() }
    val dan1Api by lazy { Danbooru1Api() }
    val moeApi by lazy { MoebooruApi() }
    val gelApi by lazy { GelbooruApi() }
    val sankakuApi by lazy { SankakuApi() }
    val shimmieApi by lazy { ShimmieApi() }
}