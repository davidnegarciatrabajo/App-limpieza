package com.dngarcia.tareasdiarias.domain.model

data class TaskAdvancedFilters(
    val status: TaskStatus? = null,
    val datePreset: TaskDateFilterPreset = TaskDateFilterPreset.ALL,
    val categoryId: Long? = null,
) {
    fun hasActiveFilters(): Boolean {
        return status != null || datePreset != TaskDateFilterPreset.ALL || categoryId != null
    }
}
