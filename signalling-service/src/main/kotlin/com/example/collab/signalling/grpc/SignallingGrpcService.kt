package com.example.collab.signalling.grpc

import com.example.collab.proto.signalling.EventType
import com.example.collab.proto.signalling.SignalEvent
import com.example.collab.proto.signalling.SignalRequest
import com.example.collab.proto.signalling.SignalResponse
import com.example.collab.proto.signalling.SignallingServiceGrpcKt
import com.example.collab.proto.signalling.SubscribeRequest
import com.example.collab.signalling.service.SignalRoutingService
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory

/**
 * gRPC service implementation for WebRTC signalling handling signal exchange and subscriptions.
 *
 * @property signalRoutingService Service responsible for room-based signal routing and storage
 */
@GrpcService
class SignallingGrpcService(
    private val signalRoutingService: SignalRoutingService
) : SignallingServiceGrpcKt.SignallingServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(SignallingGrpcService::class.java)

    /**
     * Handles incoming WebRTC signal requests and routes them to the targeted room.
     *
     * @param request The incoming signal request containing room ID, user ID, SDP, and ICE candidates
     * @return Response indicating whether the signal was successfully accepted
     */
    override suspend fun signal(request: SignalRequest): SignalResponse {
        log.info("Received signal from user '{}' for room '{}'", request.userId, request.roomId)

        val signalEvent = SignalEvent.newBuilder()
            .setFromUserId(request.userId)
            .setSdp(request.sdp)
            .addAllIceCandidates(request.iceCandidatesList)
            .setType(EventType.OFFER)
            .build()

        signalRoutingService.addSignal(request.roomId, signalEvent)

        return SignalResponse.newBuilder()
            .setAccepted(true)
            .setMessage("Signal accepted successfully")
            .build()
    }

    /**
     * Establishes a server-side streaming flow emitting WebRTC signal events for a requested room.
     *
     * @param request The subscription request specifying room ID and user ID
     * @return Flow emitting real-time SignalEvents for the specified room
     */
    override fun subscribeSignals(request: SubscribeRequest): Flow<SignalEvent> = flow {
        log.info("User '{}' subscribed to signals for room '{}'", request.userId, request.roomId)
        var lastIndex = 0

        while (currentCoroutineContext().isActive) {
            val signals = signalRoutingService.getSignalsForRoom(request.roomId)
            if (signals.size > lastIndex) {
                for (i in lastIndex until signals.size) {
                    val event = signals[i]
                    // Do not echo back user's own signals if necessary, or emit all room events
                    log.debug("Emitting signal from '{}' to subscriber '{}'", event.fromUserId, request.userId)
                    emit(event)
                }
                lastIndex = signals.size
            }
            delay(500)
        }
    }
}
