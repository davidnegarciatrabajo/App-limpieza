package com.dngarcia.tareasdiarias.domain.usecase

class CategoryDeletionBlockedException(
    val taskCount: Int,
) : IllegalStateException("La categoria no puede eliminarse porque tiene tareas asociadas.")
