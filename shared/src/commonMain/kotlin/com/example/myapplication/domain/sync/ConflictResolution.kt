package com.example.myapplication.domain.sync

import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.ConflictVersion

/**
 * Pure function to resolve conflicts based on the following rules:
 * 1. Latest picks the version with the higher updatedAt
 * 2. On a tie, picks the higher version number
 * 3. On a tie, picks the deviceId alphabetically
 */
fun resolveConflictChoice(
    local: ConflictVersion,
    remote: ConflictVersion,
    choice: ConflictChoice
): ConflictVersion {
    return when (choice) {
        ConflictChoice.Local -> local
        ConflictChoice.Remote -> remote
        ConflictChoice.Latest -> {
            val localMeta = local.metadata
            val remoteMeta = remote.metadata

            // Compare by updatedAt first
            if (localMeta.updatedAt != remoteMeta.updatedAt) {
                if (localMeta.updatedAt > remoteMeta.updatedAt) local else remote
            } else {
                // Tie on updatedAt, compare by version
                if (localMeta.version != remoteMeta.version) {
                    if (localMeta.version > remoteMeta.version) local else remote
                } else {
                    // Tie on version, compare by deviceId alphabetically
                    if (localMeta.deviceId < remoteMeta.deviceId) local else remote
                }
            }
        }
    }
}
