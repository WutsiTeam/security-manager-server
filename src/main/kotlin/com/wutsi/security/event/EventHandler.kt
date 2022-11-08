package com.wutsi.security.event

import com.wutsi.membership.manager.event.EventURN
import com.wutsi.platform.core.stream.Event
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class EventHandler(
    private val membership: MembershipEventHandler
) {
    @EventListener
    fun handleEvent(event: Event) {
        when (event.type) {
            EventURN.MEMBER_REGISTRATION_STARTED.urn -> membership.onRegistrationStarted(event)
            EventURN.MEMBER_REGISTERED.urn -> membership.onMemberRegistered(event)
            EventURN.MEMBER_DELETED.urn -> membership.onMemberDeleted(event)
            else -> {}
        }
    }
}
