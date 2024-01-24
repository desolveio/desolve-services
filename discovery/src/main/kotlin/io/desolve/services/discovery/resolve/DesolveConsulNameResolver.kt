package io.desolve.services.discovery.resolve

import com.orbitz.consul.model.health.ServiceHealth
import io.desolve.services.discovery.DesolveDiscoveryClient.discovery
import io.grpc.Attributes
import io.grpc.EquivalentAddressGroup
import io.grpc.NameResolver
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.URI
import java.util.*
import kotlin.concurrent.thread

class DesolveConsulNameResolver(
    private val uri: URI,
    private val serviceName: String,
    delay: Int = 1
) : NameResolver()
{
    companion object
    {
        private val LOGGER = LoggerFactory
            .getLogger(
                DesolveConsulNameResolver::class.java
            )
    }

    private var listener: Listener? = null
    private var healthyServices: List<ServiceHealth>? = null

    init
    {
        thread {
            val connectionCheckTimer =
                ConnectionCheckTimer(this, delay)

            connectionCheckTimer.runTimer()
        }
    }

    override fun getServiceAuthority() =
        uri.authority ?: "fakeAuthority"

    override fun start(listener: Listener)
    {
        this.listener = listener
    }

    private fun loadServiceNodes()
    {
        val addressGroups =
            mutableListOf<EquivalentAddressGroup>()

        healthyServices = kotlin
            .runCatching {
                getServiceNodes(serviceName).response
            }
            .onFailure {
                it.printStackTrace()
            }
            .getOrNull()

        if (healthyServices.isNullOrEmpty())
        {
            LOGGER.info("There isn't any information for node: [{}]!", serviceName)
            return
        }

        for (node in healthyServices!!)
        {
            val host = node.service.address
            val port = node.service.port

            val socketAddresses = mutableListOf<SocketAddress>()
            socketAddresses.add(InetSocketAddress(host, port))
            addressGroups.add(EquivalentAddressGroup(socketAddresses))
        }

        listener?.onAddresses(addressGroups, Attributes.EMPTY)
    }

    private fun getServiceNodes(serviceName: String) =
        discovery().healthClient().getHealthyServiceInstances(serviceName)

    override fun shutdown()
    {
        discovery().destroy()
    }

    private class ConnectionCheckTimer(
        private val consulNameResolver: DesolveConsulNameResolver,
        private val pauseInSeconds: Int
    )
    {
        private var timerTask =
            ConnectionCheckTimerTask(consulNameResolver)

        private val timer = Timer()

        fun runTimer()
        {
            val delay = 1000
            timer.scheduleAtFixedRate(timerTask, delay.toLong(), pauseInSeconds * 1000L)
        }

        fun reset()
        {
            timerTask.cancel()
            timer.purge()

            timerTask = ConnectionCheckTimerTask(consulNameResolver)
        }
    }

    private class ConnectionCheckTimerTask(
        private val consulNameResolver: DesolveConsulNameResolver
    ) : TimerTask()
    {
        override fun run()
        {
            this.consulNameResolver.loadServiceNodes()
        }
    }
}
