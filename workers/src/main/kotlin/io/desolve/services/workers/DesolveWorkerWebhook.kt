package io.desolve.services.workers

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.receive.ReadonlyMessage
import club.minnced.discord.webhook.send.WebhookEmbed
import io.desolve.services.core.annotations.Configure
import org.koin.core.component.KoinComponent
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 6/12/2022
 */
object DesolveWorkerWebhook : KoinComponent
{
    var client: WebhookClient? = null

    @Configure
    fun configure()
    {
        val webhook = System
            .getenv("DESOLVE_WEBHOOK")
            ?: return

        this.client = WebhookClient
            .withUrl(webhook)
    }

    fun post(embed: WebhookEmbed): CompletableFuture<ReadonlyMessage?>
    {
        if (this.client == null)
            return CompletableFuture
                .completedFuture(null)

        return this.client!!.send(embed)
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
    }
}
