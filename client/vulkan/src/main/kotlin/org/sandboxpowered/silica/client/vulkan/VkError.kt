package org.sandboxpowered.silica.client.vulkan

import org.lwjgl.vulkan.VK10
import java.lang.RuntimeException
import java.util.function.Predicate

class VkError(message: String) : RuntimeException(message) {
    companion object {
        fun checkError(error: String, value: Int) {
            checkError(error, value, VK10.VK_SUCCESS)
        }

        fun <T> checkError(error: String, value: T, expectedValue: T) {
            if (value != expectedValue) {
                throw VkError(error)
            }
        }
        fun <T> checkError(error: String, value: T?, filter: (T?)->Boolean) {
            if (filter.invoke(value)) {
                throw VkError(error)
            }
        }
    }
}