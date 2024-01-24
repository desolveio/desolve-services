package io.desolve.services.profiles

import io.desolve.services.store.DesolveDataStoreMongo

/**
 * @author GrowlyX
 * @since 5/30/2022
 */
object DesolveUserProfilePlatformTools
{
    private var service: DesolveUserProfileService? = null

    fun update(
        service: DesolveUserProfileService
    )
    {
        if (this.service != null)
        {
            throw IllegalStateException("Profile service is already running on this platform")
        }

        this.service = service
    }

    fun service(): DesolveUserProfileService
    {
        if (this.service == null)
        {
            this.service = DesolveUserProfileService()
            DesolveDataStoreMongo.configureJvmShutdown()

            return this.service!!
        }

        return this.service!!
    }
}
