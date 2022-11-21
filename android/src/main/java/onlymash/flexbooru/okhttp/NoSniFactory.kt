package onlymash.flexbooru.okhttp

import android.os.Build
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.KeyStore
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object NoSniFactory : SSLSocketFactory() {
    private val defaultFactory = getDefault() as SSLSocketFactory
    val defaultTrustManager = platformTrustManager()

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        val socket = defaultFactory.createSocket(s, host, port, autoClose)
        setParams(socket)
        return socket
    }

    override fun createSocket(host: String?, port: Int): Socket {
        val socket = defaultFactory.createSocket(host, port)
        setParams(socket)
        return socket
    }

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {
        val socket = defaultFactory.createSocket(host, port, localHost, localPort)
        setParams(socket)
        return socket
    }

    override fun createSocket(host: InetAddress?, port: Int): Socket {
        val socket = defaultFactory.createSocket(host, port)
        setParams(socket)
        return socket
    }

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket {
        val socket = defaultFactory.createSocket(address, port, localAddress, localPort)
        setParams(socket)
        return socket
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return defaultFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return defaultFactory.supportedCipherSuites
    }

    private fun setParams(s: Socket) {
        val socket = s as SSLSocket
        val params = socket.sslParameters
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            params.serverNames = null
        }
        socket.sslParameters = params
    }

    private fun platformTrustManager(): X509TrustManager {
        val algorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(algorithm)
        tmf.init(null as KeyStore?)
        val tms = tmf.trustManagers
        for (tm in tms) {
            if (tm is X509TrustManager) {
                return tm
            }
        }
        throw KeyManagementException()
    }
}
