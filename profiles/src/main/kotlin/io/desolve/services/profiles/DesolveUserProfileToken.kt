package io.desolve.services.profiles

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class DesolveUserProfileToken(
	@Contextual
	val token: UUID,

	@Contextual
	val expiration: Instant
)