package com.codebutler.funwithnativecode

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.Structure

interface LibRetro : Library {
    fun retro_set_environment(cb: retro_environment)
    fun retro_set_video_refresh(cb: retro_video_refresh)
    fun retro_set_audio_sample_batch(cb: retro_audio_sample_batch)
    fun retro_set_input_poll(cb: retro_input_poll)
    fun retro_set_input_state(cb: retro_input_state)
    fun retro_init()
    fun retro_load_game(game: retro_game_info): Boolean
    fun retro_run()
}

interface retro_environment : Callback {
    fun invoke(cmd: UInt, data: Pointer): Boolean
}

interface retro_video_refresh : Callback {
    fun invoke(data: Pointer, width: UInt, height: UInt, pitch: SizeT)
}

interface retro_audio_sample_batch : Callback {
    fun invoke(data: Pointer, frames: SizeT): SizeT
}

interface retro_input_poll : Callback {
    fun invoke()
}

interface retro_input_state : Callback {
    fun invoke(port: UInt, device: UInt, index: UInt, id: UInt): Short
}

class retro_game_info : Structure() {
    @JvmField var path: String? = null
    @JvmField var data: Pointer? = null
    @JvmField var size: SizeT = SizeT()
    @JvmField var meta: String? = null
    override fun getFieldOrder() = listOf("path", "data", "size", "meta")
}
