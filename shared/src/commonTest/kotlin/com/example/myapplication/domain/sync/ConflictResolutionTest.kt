package com.example.myapplication.domain.sync

import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.ConflictVersion
import com.example.myapplication.domain.model.SyncMetadata
import kotlin.test.Test
import kotlin.test.assertEquals

class ConflictResolutionTest {

    @Test
    fun `resolveConflictChoice with Local returns local version`() {
        val local = ConflictVersion("This phone", 500_00L, SyncMetadata("1", 1L, 1000L, "android"))
        val remote = ConflictVersion("iPhone", 600_00L, SyncMetadata("2", 2L, 2000L, "iphone"))

        val result = resolveConflictChoice(local, remote, ConflictChoice.Local)

        assertEquals(local, result)
    }

    @Test
    fun `resolveConflictChoice with Remote returns remote version`() {
        val local = ConflictVersion("This phone", 500_00L, SyncMetadata("1", 1L, 1000L, "android"))
        val remote = ConflictVersion("iPhone", 600_00L, SyncMetadata("2", 2L, 2000L, "iphone"))

        val result = resolveConflictChoice(local, remote, ConflictChoice.Remote)

        assertEquals(remote, result)
    }

    @Test
    fun `resolveConflictChoice with Latest picks higher updatedAt`() {
        val local = ConflictVersion("This phone", 500_00L, SyncMetadata("1", 1L, 1000L, "android"))
        val remote = ConflictVersion("iPhone", 600_00L, SyncMetadata("2", 2L, 2000L, "iphone"))

        val result = resolveConflictChoice(local, remote, ConflictChoice.Latest)

        assertEquals(remote, result)
    }

    @Test
    fun `resolveConflictChoice with Latest picks higher version on updatedAt tie`() {
        val local = ConflictVersion("This phone", 500_00L, SyncMetadata("1", 2L, 1000L, "android"))
        val remote = ConflictVersion("iPhone", 600_00L, SyncMetadata("2", 1L, 1000L, "iphone"))

        val result = resolveConflictChoice(local, remote, ConflictChoice.Latest)

        assertEquals(local, result)
    }

    @Test
    fun `resolveConflictChoice with Latest picks deviceId alphabetically on version tie`() {
        val local = ConflictVersion("This phone", 500_00L, SyncMetadata("1", 1L, 1000L, "android"))
        val remote = ConflictVersion("iPhone", 600_00L, SyncMetadata("2", 1L, 1000L, "iphone"))

        val result = resolveConflictChoice(local, remote, ConflictChoice.Latest)

        assertEquals(local, result)
    }
}
