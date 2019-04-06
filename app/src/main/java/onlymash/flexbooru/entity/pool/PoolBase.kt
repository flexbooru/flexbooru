package onlymash.flexbooru.entity.pool

abstract class PoolBase {
    var scheme: String = ""
    var host: String = ""
    var keyword: String = ""
    var type: Int = 0

    abstract fun getPoolId(): Int
    abstract fun getPoolName(): String
    abstract fun getPostCount(): Int
    abstract fun getPoolDate(): CharSequence
    abstract fun getPoolDescription(): String
    abstract fun getCreatorId(): Int
    abstract fun getCreatorName(): String?
}