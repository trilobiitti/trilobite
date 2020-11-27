package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.di.bindDependency
import com.github.trilobiitti.trilobite.dna.hacks.KotlinPropertyMeta

object DocumentAdapters {
    val fieldAccessor = bindDependency<FieldAccessor<*>, KotlinPropertyMeta, Document>(
        "document adapter field accessor"
    )

    val fieldReader = bindDependency<FieldReader<*>, KotlinPropertyMeta, Document>(
        "document adapter field reader"
    ) { prop, doc -> fieldAccessor(prop, doc) }

    val fieldWriter = bindDependency<FieldReader<*>, KotlinPropertyMeta, Document>(
        "document adapter field writer"
    ) { prop, doc -> fieldAccessor(prop, doc) }
}
