package io.desolve.services.profiles

import io.desolve.services.store.DesolveDataStore
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.*

/**
 * @author GrowlyX
 * @since 5/24/2022
 */
class DesolveUserProfileService : DesolveDataStore<DesolveUserProfile, UUID>()
{
    suspend fun findByAccessToken(uniqueId: UUID) =
        this.desolveModelCollection
            .findOne(DesolveUserProfile::refreshToken / DesolveUserProfileToken::token eq uniqueId)

    suspend fun findByUsername(username: String) =
        this.desolveModelCollection
            .findOne(DesolveUserProfile::username eq username)

    suspend fun findByEmail(email: String) =
        this.desolveModelCollection
            .findOne(DesolveUserProfile::email eq email)

    suspend fun updateRefreshToken(profile: DesolveUserProfile, refreshToken: DesolveUserProfileToken?) =
        this.desolveModelCollection
            .updateOneById(profile._id, setValue(profile::refreshToken, refreshToken))

    override fun getType() = DesolveUserProfile::class
}
