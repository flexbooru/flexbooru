

object Libs {

    val annotation = "androidx.annotation:annotation:1.6.0"
    val coreKtx = "androidx.core:core-ktx:1.9.0"
    val appcompat = "androidx.appcompat:appcompat:1.6.1"
    val material = "com.google.android.material:material:1.9.0-beta01"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    val activityKtx = "androidx.activity:activity-ktx:1.6.1"
    val recyclerview = "androidx.recyclerview:recyclerview:1.3.0"
    val recyclerviewSelection = "androidx.recyclerview:recyclerview-selection:1.1.0"

    private const val preferenceVersion = "1.2.0"
    val preference = "androidx.preference:preference:$preferenceVersion"
    val preferenceKtx = "androidx.preference:preference-ktx:$preferenceVersion"
    val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"

    private const val fragmentVersion = "1.5.5"
    val fragment =  "androidx.fragment:fragment:$fragmentVersion"
    val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentVersion"

    val viewPager2 = "androidx.viewpager2:viewpager2:1.1.0-beta01"
    val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"
    val documentFile = "androidx.documentfile:documentfile:1.1.0-alpha01"
    val multidex = "androidx.multidex:multidex:2.0.1"
    val browser = "androidx.browser:browser:1.5.0"
    val drawerLayout = "androidx.drawerlayout:drawerlayout:1.2.0"
    val pagingRuntimeKtx = "androidx.paging:paging-runtime-ktx:3.1.1"

    private const val kodeinVersion = "7.19.1"
    val kodein = "org.kodein.di:kodein-di:$kodeinVersion"
    val kodeinJvm = "org.kodein.di:kodein-di-jvm:$kodeinVersion"
    val kodeinCore = "org.kodein.di:kodein-di-framework-android-core:$kodeinVersion"
    val kodeinAndroidX = "org.kodein.di:kodein-di-framework-android-x:$kodeinVersion"

    private val daggerVersion = "2.45"
    val daggerHilt = "com.google.dagger:hilt-android:$daggerVersion"
    val daggerHiltCompiler = "com.google.dagger:hilt-android-compiler:$daggerVersion"
    val hiltCompose = "androidx.hilt:hilt-navigation-compose:1.1.0-alpha01"
    val hiltCompiler = "androidx.hilt:hilt-compiler:1.0.0"

    private const val roomVersion = "2.5.0"
    val roomRuntime = "androidx.room:room-runtime:$roomVersion"
    val roomKtx = "androidx.room:room-ktx:$roomVersion"
    val roomCompiler = "androidx.room:room-compiler:$roomVersion"
    val roomPaging = "androidx.room:room-paging:$roomVersion"

    val workRuntimeKtx = "androidx.work:work-runtime-ktx:2.8.1"

    private const val navigationVersion = "2.5.3"
    val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
    val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
    val navigationRuntimeKtx = "androidx.navigation:navigation-runtime-ktx:$navigationVersion"
    val navigationDynamicFeaturesFragment = "androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion"
    val navigationCompose = "androidx.navigation:navigation-compose:$navigationVersion"

    val pagingCompose = "androidx.paging:paging-compose:1.0.0-alpha18"

    val composeBom = "androidx.compose:compose-bom:2023.01.00"
    val composeUi = "androidx.compose.ui:ui"
    val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    val composeMaterial = "androidx.compose.material:material"
    val composeMaterial3 = "androidx.compose.material3:material3"
    val composeUiTooling = "androidx.compose.ui:ui-tooling"
    val composeUiTestMannifest = "androidx.compose.ui:ui-test-manifest"
    val composeMaterialIcons = "androidx.compose.material:material-icons-extended"
    val composeUiGraphics = "androidx.compose.ui:ui-graphics"
    val composeFoundation = "androidx.compose.foundation:foundation"

    val activityCompose = "androidx.activity:activity-compose:1.6.1"

    private const val accompanistVersion = "0.29.2-rc"
    val accompanistSystemUiController = "com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion"
    val accompanistPermissions = "com.google.accompanist:accompanist-permissions:$accompanistVersion"
    val composeCollapsingToolbar = "me.onebone:toolbar-compose:2.3.5"

    private const val lifecycleVersion = "2.6.1"
    val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion"
    val lifecycleCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion"
    val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
    val lifecycleLivedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion"
    val lifecycleViewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion"

    private val coilVersion = "2.3.0"
    val coilCompose = "io.coil-kt:coil-compose:$coilVersion"
    val coil = "io.coil-kt:coil:$coilVersion"
    val coilGif = "io.coil-kt:coil-gif:$coilVersion"
    val coilSvg = "io.coil-kt:coil-svg:$coilVersion"

    val picasso = "com.squareup.picasso:picasso:2.71828"

    private const val kotlinxCoroutineVersion = "1.6.4"
    val kotlinxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion"
    val kotlinxCoroutinesCoreJvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutineVersion"
    val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutineVersion"

    val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0"
    val kotlinxMetadataJvm = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0"

    private const val ktorVersion = "2.2.4"
    val ktorClientContentNegotiation = "io.ktor:ktor-client-content-negotiation:$ktorVersion"
    val ktorSerializationKotlinxJson = "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion"
    val ktorClientCoreJvm = "io.ktor:ktor-client-core-jvm:$ktorVersion"
    val ktorClientOkhttp = "io.ktor:ktor-client-okhttp:$ktorVersion"
    val ktorClientJsonJvm = "io.ktor:ktor-client-json-jvm:$ktorVersion"
    val ktorClientSerializationJvm = "io.ktor:ktor-client-serialization-jvm:$ktorVersion"

    private const val okhttpVersion = "5.0.0-alpha.11"
    val okhttp = "com.squareup.okhttp3:okhttp:$okhttpVersion"
    val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okhttpVersion"
    val okhttpDoH = "com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttpVersion"
    val okio = "com.squareup.okio:okio:3.3.0"

    private const val retrofitVersion = "2.9.0"
    val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    val retrofitKotlinxSerializationConverter = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0"
    val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    val retrofitCoroutinesAdapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"

    val gmsLocation = "com.google.android.gms:play-services-location:21.0.1"
    val threetenabp = "com.jakewharton.threetenabp:threetenabp:1.4.4"

    val loopingLayout = "com.github.beksomega:loopinglayout:0.4.1"
    val loopScrollAnimation = "com.github.onlymash:RecyclerViewLoopScrollAnimation:a262ee9710"

    val firebaseBom = "com.google.firebase:firebase-bom:31.4.0"
    val firebaseAnalyticsKtx = "com.google.firebase:firebase-analytics-ktx"
    val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics"
    val playServicesAds = "com.google.android.gms:play-services-ads:22.0.0"
    val playServicesVision = "com.google.android.gms:play-services-vision:20.1.3"
    val playServicesOssLicenses = "com.google.android.gms:play-services-oss-licenses:17.0.0"
    val billingKtx = "com.android.billingclient:billing-ktx:5.1.0"

    private const val exoplayerVersion = "2.18.5"
    val exoplayerCore = "com.google.android.exoplayer:exoplayer-core:$exoplayerVersion"
    val exoplayerUi = "com.google.android.exoplayer:exoplayer-ui:$exoplayerVersion"

    private const val xmlutilVersion = "0.85.0"
    val xmlutilAndroidCore = "io.github.pdvrieze.xmlutil:core-android:$xmlutilVersion"
    val xmlutilAndroidSerialization = "io.github.pdvrieze.xmlutil:serialization-android:$xmlutilVersion"

    val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:1.2.0"

    val flexboxLayout = "com.google.android.flexbox:flexbox:3.0.0"
    val photoView = "com.github.chrisbanes:PhotoView:2.3.0"
    val subsamplingScaleImageView = "com.github.onlymash:subsampling-scale-image-view:3.10.3"
    val omfm = "com.github.onlymash:OMFM:1.1.4"
    val materialDrawer = "com.mikepenz:materialdrawer:9.0.1"
    val muzeiApi = "com.google.android.apps.muzei:muzei-api:3.4.1"
    val zxingCore = "com.google.zxing:core:3.5.1"
    val barCodeScanner = "xyz.belvi.mobilevision:barcodescanner:2.0.3"


    val junit = "junit:junit:4.13.2"
    val androidxJunit = "androidx.test.ext:junit:1.1.5"
    val espressoCore = "androidx.test.espresso:espresso-core:3.5.1"
    val robolectric = "org.robolectric:robolectric:4.9.2"
}