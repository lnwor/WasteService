package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext.Companion.getActor
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.AssertionErrors.assertEquals
import org.springframework.test.util.AssertionErrors.fail
import unibo.actor22comm.coap.CoapConnection
import unibo.actor22comm.utils.ColorsOut
import unibo.actor22comm.utils.CommUtils


class TrolleyTest() {
    private var actor_trolley = "trolley"
    private var actor_storage = "storagemanager"
    private var actor_wasteservice = "wasteservice"

    @Before
    fun up() {
        Thread().start()
    }

    @After
    fun down() {
        ColorsOut.outappl(this.javaClass.name + " TEST END", ColorsOut.GREEN)
    }

    @Test
    fun testTrolleyMove() {
        trolleyRequest("trolleyMove", "INDOOR")
        val trolleyContent = PrologUtils.getFuncLine(coapRequest(actor_trolley), "pos")
        val tContentParams = SimplePayloadExtractor("pos").extractPayload(trolleyContent)
        assertEquals("Testing Expected Position", "INDOOR", tContentParams[0])
    }

    @Test
    fun testTrolleyCollect() {
        trolleyRequest("trolleyCollect", "glass, 10")
        val trolleyContent = PrologUtils.getFuncLine(coapRequest(actor_trolley), "content")
        val tContentParams = SimplePayloadExtractor("content").extractPayload(trolleyContent)
        assertEquals("Testing Correct Material", "glass", tContentParams[0])
        assertEquals("Testing Correct Quantity", 10.0f, tContentParams[1].toFloat())
    }

    @Test
    fun testTrolleyDeposit() {
        trolleyRequest("trolleyDeposit", "")
    }

    private fun trolleyRequest(id: String, params: String) {
        val request: String = MsgUtil.buildRequest(
            "test", id, "$id($params)", actor_trolley
        ).toString()
        var reply: String? = null
        try {
            val connTcp = ConnTcp("localhost", CTX_PORT)
            ColorsOut.outappl("Asking trolley: $id($params)", ColorsOut.CYAN)
            reply = connTcp.request(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (reply != null && reply.contains("false")) {
            fail("Trolley request <$request> failed!")
        }
    }

    private fun waitForTrolley() {
        ColorsOut.outappl(this.javaClass.name + " waits for trolley ... ", ColorsOut.GREEN)
        var trolley = getActor(actor_trolley)
        while (trolley == null) {
            CommUtils.delay(200)
            trolley = getActor(actor_trolley)
        }
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN)
    }

    private fun coapRequest(actor: String): String {
        val reqConn = CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor)
        val answer = reqConn.request("")
        ColorsOut.outappl("coapRequest answer=$answer", ColorsOut.CYAN)
        return answer
    }

    companion object {
        const val CTX_HOST = "localhost"
        const val CTX_PORT = 8021
        const val CTX_TEST = "ctx_trolley"
    }
}
