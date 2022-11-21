package onlymash.flexbooru.okhttp

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

// https://chromium.googlesource.com/chromium/src/+/refs/heads/main/net/dns/public/doh_provider_entry.cc
object DohProviders {
    fun buildGoogle(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://dns.google/dns-query".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("8.8.8.8"),
                    InetAddress.getByName("8.8.4.4"),
                    InetAddress.getByName("2001:4860:4860::8888"),
                    InetAddress.getByName("2001:4860:4860::8844"),
                )
            )
            .build()
    }

    fun buildCloudflare(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("1.1.1.1"),
                    InetAddress.getByName("1.0.0.1"),
                    InetAddress.getByName("2606:4700:4700::1111"),
                    InetAddress.getByName("2606:4700:4700::1001"),
                )
            )
            .build()
    }

    fun buildCleanBrowsing(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://doh.cleanbrowsing.org/doh/security-filter".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("185.228.168.9"),
                    InetAddress.getByName("185.228.169.9"),
                    InetAddress.getByName("2a0d:2a00:1::2"),
                    InetAddress.getByName("2a0d:2a00:2::2"),
                )
            )
            .build()
    }

    fun buildDnsSb(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://doh.sb/dns-query".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("185.222.222.222"),
                    InetAddress.getByName("45.11.45.11"),
                    InetAddress.getByName("2a09::"),
                    InetAddress.getByName("2a11::"),
                )
            )
            .build()
    }

    fun buildOpenDns(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://doh.opendns.com/dns-query".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("208.67.222.222"),
                    InetAddress.getByName("208.67.220.220"),
                    InetAddress.getByName("2620:119:35::35"),
                    InetAddress.getByName("2620:119:53::53"),
                )
            )
            .build()
    }

    fun buildQuad9(bootstrapClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder().client(bootstrapClient)
            .url("https://dns11.quad9.net/dns-query".toHttpUrl())
            .resolvePrivateAddresses(true)
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("9.9.9.11"),
                    InetAddress.getByName("149.112.112.11"),
                    InetAddress.getByName("2620:fe::11"),
                    InetAddress.getByName("2620:fe::fe:11"),
                )
            )
            .build()
    }
}
